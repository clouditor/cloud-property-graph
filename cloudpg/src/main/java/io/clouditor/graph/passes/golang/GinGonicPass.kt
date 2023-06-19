package io.clouditor.graph.passes.golang

import de.fraunhofer.aisec.cpg.TranslationContext
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Name
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.FunctionDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.*
import de.fraunhofer.aisec.cpg.graph.types.PointerType
import de.fraunhofer.aisec.cpg.passes.TranslationResultPass
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.*

class GinGonicPass(ctx: TranslationContext) : TranslationResultPass(ctx) {
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

    override fun accept(result: TranslationResult) {
        for (tu in result.translationUnits) {
            tu.accept(
                Strategy::AST_FORWARD,
                object : IVisitor<Node>() {
                    override fun visit(t: Node) {
                        when (t) {
                            is VariableDeclaration -> handleVariable(result, tu, t)
                            is MemberCallExpression -> handleGinResponse(t)
                        }
                    }
                }
            )
        }
    }

    private fun handleGinResponse(m: MemberCallExpression) {
        if (m.base?.type?.name?.startsWith("gin.Context") == true &&
                m.arguments.firstOrNull()?.name?.startsWith("http.Status") == true
        ) {
            // replace the status code name with the harmonized naming
            m.arguments.firstOrNull()?.name =
                Name(httpMap[m.arguments.firstOrNull()?.name?.toString()].toString())
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
            if (m.name.localName == "GET" || m.name.localName == "POST" || m.name.localName == "PUT"
            ) {
                val endpoint =
                    HttpEndpoint(
                        NoAuthentication(),
                        funcDeclaration,
                        m.name.localName,
                        getPath(m),
                        null,
                        null
                    )
                endpoint.name = Name(endpoint.path)

                // get the endpoint's handler
                funcDeclaration?.accept(
                    Strategy::AST_FORWARD,
                    object : IVisitor<Node>() {
                        override fun visit(t: Node) {
                            when (t) {
                                // look through its mces
                                is MemberCallExpression -> handleBind(t, endpoint)
                                // look through its mes
                                is MemberExpression -> handleForm(t, endpoint)
                            }
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
            } else if (m.name.localName == "Group") {
                // add a new (sub) client
                val app = result.findApplicationByTU(tu)

                val requestHandler =
                    HttpRequestHandler(
                        app,
                        mutableListOf(),
                        client?.path?.appendPath(getPath(m)) ?: "/"
                    )
                requestHandler.name = Name(requestHandler.path)

                val subClient = m.nextDFG.filterIsInstance<VariableDeclaration>().firstOrNull()
                subClient?.let {
                    clients[it] = requestHandler

                    log.debug("Adding new group client {}", requestHandler.name)

                    result += requestHandler
                }
            }
        }
    }

    private fun handleBind(m: MemberCallExpression, e: HttpEndpoint) {
        if (m.name.localName == "BindJSON" || m.name.localName == "Bind") {
            val obj = (m.arguments.firstOrNull() as UnaryOperator).input
            if (obj is DeclaredReferenceExpression) {
                obj.refersTo?.let { e.addNextDFG(it) }
            } else {
                e.addNextDFG(obj)
            }
        } else if (m.name.localName == "Get") {
            // lets see, whether we have a chain of member calls that go
            // to the base
            var memberCall: MemberExpression? = m.base as? MemberExpression
            val calls = mutableListOf<MemberExpression>()
            while (memberCall != null) {
                // add the call to the list of chained calls
                calls += memberCall

                // check, if its base is already of our gin type
                if (memberCall.base.type is PointerType &&
                        memberCall.base.type.name.localName == "gin.Context*"
                ) {
                    // we can break immediately
                    break
                }

                // otherwise, go to the next base
                memberCall = memberCall.base as? MemberExpression
            }
            e.addNextDFG(m)
        }
    }

    // TODO consolidate duplicated code here and above in handleBind
    private fun handleForm(m: MemberExpression, e: HttpEndpoint) {
        if (m.name.localName == "Form") {
            // lets see, whether we have a chain of member calls that go
            // to the base
            var memberCall: MemberExpression? = m.base as? MemberExpression
            val calls = mutableListOf<MemberExpression>()
            while (memberCall != null) {
                // add the call to the list of chained calls
                calls += memberCall

                // check, if its base is already of our gin type
                if (memberCall.base.type is PointerType &&
                        memberCall.base.type.name.localName == "gin.Context*"
                ) {
                    // we can break immediately
                    break
                }

                // otherwise, go to the next base
                memberCall = memberCall.base as? MemberExpression
            }
            e.addNextDFG(m)
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
        if (r.initializer is CallExpression &&
                (r.initializer as CallExpression).toString() == "gin.Default" ||
                (r.initializer as CallExpression).toString() == "gin.New"
        ) {
            val app = result.findApplicationByTU(tu)

            val requestHandler = HttpRequestHandler(app, mutableListOf(), "/")
            requestHandler.name = Name(requestHandler.path)

            clients[r] = requestHandler

            log.debug("Adding new client {}", r.name)

            result += requestHandler

            // look for calls to that client
            r.accept(
                Strategy::EOG_FORWARD,
                object : IVisitor<Node>() {
                    override fun visit(t: Node) {
                        when (t) {
                            is MemberCallExpression -> handleMemberCall(result, tu, t)
                        }
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
