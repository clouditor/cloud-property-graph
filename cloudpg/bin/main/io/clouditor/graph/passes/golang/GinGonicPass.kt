package io.clouditor.graph.passes.golang

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.FunctionDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.*
import de.fraunhofer.aisec.cpg.passes.Pass
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.*

class GinGonicPass : Pass() {
    private val clients = mutableMapOf<VariableDeclaration, HttpRequestHandler>()

    private val httpMap: Map<String, String> =
        mapOf(
            // harmonize the http status codes across frameworks; we use the names as used in Spring
            // here
            "http.StatusOK" to "HttpStatus.OK",
            "http.StatusAccepted" to "HttpStatus.ACCEPTED",
            "http.StatusNonAuthoritativeInformation" to "HttpStatus.NON_AUTHORITATIVE_INFORMATION",
            "http.StatusNoContent" to "HttpStatus.NO_CONTENT",
            "http.StatusMultipleChoices" to "HttpStatus.MULTIPLE_CHOICES",
            "http.StatusMultipleMovedPermanently" to "HttpStatus.MOVED_PERMANENTLY",
            "http.StatusFound" to "HttpStatus.FOUND",
            "http.StatusSeeOther" to "HttpStatus.SEE_OTHER",
            "http.StatusNotModified" to "HttpStatus.NOT_MODIFIED",
            "http.StatusUseProxy" to "HttpStatus.USE_PROXY",
            "http.StatusBadRequest" to "HttpStatus.BAD_REQUEST",
            "http.StatusUnauthorized" to "HttpStatus.UNAUTHORIZED",
            "http.StatusPaymentRequired" to "HttpStatus.PAYMENT_REQUIRED",
            "http.StatusForbidden" to "HttpStatus.FORBIDDEN",
            "http.StatusNotFound" to "HttpStatus.NOT_FOUND",
            "http.StatusMethodNotAllowed" to "HttpStatus.METHOD_NOT_ALLOWED",
            "http.StatusNotAcceptable" to "HttpStatus.NOT_ACCEPTABLE",
            "http.StatusProxyAuthenticationRequired" to "HttpStatus.PROXY_AUTHENTICATION_REQUIRED",
            "http.StatusRequestTimeout" to "HttpStatus.REQUEST_TIMEOUT",
            "http.StatusConflict" to "HttpStatus.CONFLICT",
            "http.StatusGone" to "HttpStatus.GONE",
            "http.StatusLengthRequired" to "HttpStatus.LENGTH_REQUIRED",
            "http.StatusPreconditionFailed" to "HttpStatus.PRECONDITION_FAILED",
            "http.StatusPayloadTooLarge" to "HttpStatus.PAYLOAD_TOO_LARGE",
            "http.StatusUriTooLong" to "HttpStatus.URI_TOO_LONG",
            "http.StatusIAmATeapot" to "HttpStatus.I_AM_A_TEAPOT",
            "http.StatusLocked" to "HttpStatus.LOCKED",
            "http.StatusTooManyRequests" to "HttpStatus.TOO_MANY_REQUESTS",
            "http.StatusUnavailableForLegalReasons" to "HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS",
            "http.StatusInternalServerError" to "HttpStatus.INTERNAL_SERVER_ERROR",
            "http.StatusNotImplemented" to "HttpStatus.NOT_IMPLEMENTED",
            "http.StatusBadGateway" to "HttpStatus.BAD_GATEWAY",
            "http.StatusServiceUnavailable" to "HttpStatus.SERVICE_UNAVAILABLE",
            "http.StatusGatewayTimeout" to "HttpStatus.GATEWAY_TIMEOUT",
            "http.StatusHttpVersionNotSupported" to "HttpStatus.HTTP_VERSION_NOT_SUPPORTED",
            "http.StatusBadRequest" to "HttpStatus.BAD_REQUEST",
            "http.StatusBadRequest" to "HttpStatus.BAD_REQUEST"
        )

    override fun cleanup() {}

    override fun accept(result: TranslationResult?) {
        if (result != null) {
            // first, look for clients
            for (tu in result.translationUnits) {
                tu.accept(
                    Strategy::AST_FORWARD,
                    object : IVisitor<Node?>() {
                        fun visit(r: VariableDeclaration) {
                            handleVariable(result, tu, r)
                        }
                    }
                )
                tu.accept(
                    Strategy::AST_FORWARD,
                    object : IVisitor<Node?>() {
                        fun visit(r: MemberCallExpression) {
                            handleGinResponse(r)
                        }
                    }
                )
            }
        }
    }

    private fun handleGinResponse(m: MemberCallExpression) {
        if (m.base.type.name.startsWith("gin.Context") &&
                m.arguments.firstOrNull()?.name?.startsWith("http.Status") == true
        ) {
            // replace the status code name with the harmonized naming
            m.arguments.firstOrNull()?.name =
                httpMap.get(m.arguments.firstOrNull()?.name).toString()
        }
    }

    private fun handleMemberCall(
        result: TranslationResult,
        tu: TranslationUnitDeclaration,
        m: MemberCallExpression
    ) {
        if (m.base is DeclaredReferenceExpression &&
                clients.containsKey((m.base as DeclaredReferenceExpression).refersTo)
        ) {
            val client = clients[(m.base as DeclaredReferenceExpression).refersTo]
            val app = result.findApplicationByTU(tu)

            val funcDeclaration =
                (m.arguments[1] as? DeclaredReferenceExpression)?.refersTo as? FunctionDeclaration
            if (m.name == "GET" || m.name == "POST") {
                val endpoint =
                    HttpEndpoint(
                        NoAuthentication(),
                        funcDeclaration,
                        m.name,
                        getPath(m),
                        null,
                        null
                    )
                endpoint.name = endpoint.path

                // get the endpoint's handler and look through its mces
                funcDeclaration?.accept(
                    Strategy::AST_FORWARD,
                    object : IVisitor<Node?>() {
                        fun visit(r: MemberCallExpression) {
                            handleBindJson(r, endpoint)
                        }
                    }
                )

                log.debug(
                    "Adding {} to {} - resolved to {}",
                    m.name,
                    client?.name,
                    endpoint.handler?.name
                )

                client?.httpEndpoints?.plusAssign(endpoint)
                app?.functionalities?.plusAssign(endpoint)
                result += endpoint
            } else if (m.name == "Group") {
                // add a new (sub) client
                val app = result.findApplicationByTU(tu)

                val requestHandler =
                    HttpRequestHandler(
                        app,
                        mutableListOf(),
                        client?.path?.appendPath(getPath(m)) ?: "/"
                    )
                requestHandler.name = requestHandler.path

                val subClient = m.nextDFG.filterIsInstance<VariableDeclaration>().firstOrNull()
                subClient?.let {
                    clients[it] = requestHandler

                    log.debug("Adding new group client {}", requestHandler.name)

                    result += requestHandler
                }
            }
        }
    }

    private fun handleBindJson(m: MemberCallExpression, e: HttpEndpoint) {
        // TODO restrict this to the actual parameter that is the input for the function, e.g.
        // c.bindJSON
        if (m.name == "BindJSON") {
            var obj = (m.arguments.firstOrNull() as UnaryOperator).input
            if (obj is DeclaredReferenceExpression) {
                obj.refersTo?.let { e.addNextDFG(it) }
            } else {
                e.addNextDFG(obj)
            }
        }
    }

    private fun getPath(call: MemberCallExpression): String {
        val literal = call.arguments.firstOrNull() as? Literal<*>

        return if (literal?.value.toString() == "") {
            "/"
        } else {
            literal?.value.toString()
        }
    }

    private fun handleVariable(
        result: TranslationResult,
        tu: TranslationUnitDeclaration,
        r: VariableDeclaration
    ) {
        // check initializers for http.NewServeMux()
        // actually check for return types - but that does not work (yet) with the standard library
        if (r.initializer is CallExpression &&
                (r.initializer as CallExpression).fqn == "gin.Default" ||
                (r.initializer as CallExpression).fqn == "gin.New"
        ) {
            val app = result.findApplicationByTU(tu)

            val requestHandler = HttpRequestHandler(app, mutableListOf(), "/")
            requestHandler.name = requestHandler.path

            clients[r] = requestHandler

            log.debug("Adding new client {}", r.name)

            result += requestHandler

            // look for calls to that client
            r.accept(
                Strategy::EOG_FORWARD,
                object : IVisitor<Node?>() {
                    fun visit(m: MemberCallExpression) {
                        handleMemberCall(result, tu, m)
                    }
                }
            )
        }
    }
}

fun String.appendPath(path: String): String {
    return if (this.endsWith("/") && path.startsWith("/")) {
        this + path.substring(1)
    } else if (!path.startsWith("/")) {
        "$this/$path"
    } else {
        this + path
    }
}
