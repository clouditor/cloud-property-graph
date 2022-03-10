package io.clouditor.graph

import kotlin.io.path.Path
import kotlin.test.Test

class PrivacyTest {

    @Test
    fun executePPG() {
        App.let {
            it.rootPath = Path("~/cloud-property-graph)")
            it.paths = listOf(Path("ppg-testing-library/"))
            it.labelsEnabled = true
            it.localMode = true
        }
        App.call()
    }
}
