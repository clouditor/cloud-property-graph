package io.clouditor.graph.nodes

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Name
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import io.clouditor.graph.*

/**
 * This (temporary) object holds all information coming from the code (such as Translation Units) as
 * well as the program itself and the Cloud resources.
 *
 * We need to rename this later.
 */
class Holder(
    var translationUnits: MutableList<TranslationUnitDeclaration> = mutableListOf(),
    var services: MutableList<NetworkService> = mutableListOf(),
    var images: MutableList<Image> = mutableListOf(),
    var builders: MutableList<Builder> = mutableListOf()
)

fun TranslationResult.location(locationName: String): GeoLocation {
    var location =
        this.additionalNodes.firstOrNull { it is GeoLocation && it.name.localName == locationName } as?
            GeoLocation
    if (location == null) {
        location = GeoLocation(locationName)
        location.name = Name(location.region, null)

        this += location
    }

    return location
}
