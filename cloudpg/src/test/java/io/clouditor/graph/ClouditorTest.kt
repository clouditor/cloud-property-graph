package io.clouditor.graph

import java.nio.file.Path
import kotlin.test.Test

class ClouditorTest {
    @Test
    fun testClouditor() {
        executePPG(Path.of("/Users/chr55316/Repositories/clouditor"), listOf(Path.of(".")))
    }
}
