package io.clouditor.graph

import kotlin.io.path.*
import kotlinx.benchmark.Scope
import kotlinx.benchmark.readFile
import org.junit.Test
import org.openjdk.jmh.annotations.*

@State(Scope.Benchmark)
open class PerformanceTest {

    // sets up many files
    // @Setup
    open fun setUpFilesForLoCTest() {
        // set the range
        val range = 10000
        // set the temporary directory and duplicate files into it
        val dir =
            Path(
                System.getProperty("user.dir") +
                    "/../ppg-testing-library/Disclosure/unencrypted-transmission/Python/tmp"
            )

        // duplicate the function as specified in the range
        for (i in 1..range) {
            val tmp = createTempFile(dir, "detectability", ".py")
            // append the function 10 times so the file has 100 LoC
            for (i in 1..10) {
                val sampleFunction =
                    "def encrypt(input_string: str, key: int, alphabet: str | None = None) -> str:\n" +
                        "    alpha = alphabet or ascii_letters\n" +
                        "    result = \"\"\n" +
                        "    for character in input_string:\n" +
                        "        if character not in alpha:\n" +
                        "            result += character\n" +
                        "        else:\n" +
                        "            new_key = (alpha.index(character) + key) % len(alpha)\n" +
                        "            result += alpha[new_key]\n" +
                        "    return result" +
                        "\n"
                tmp.appendText(sampleFunction)
            }
        }
    }

    // sets up many files
    @Setup
    open fun setUpMultipleClientServerFiles() {
        val range = 1500
        // set the temporary directory and duplicate files into it
        val dir =
            Path(
                System.getProperty("user.dir") +
                    "/../ppg-testing-library/Detectability/D2-detectable-communication/Python-validation/tmp"
            )
        val clientSrc =
            System.getProperty("user.dir") +
                "/../ppg-testing-library/Detectability/D2-detectable-communication/Python-validation/client.py"
        val serverSrc =
            System.getProperty("user.dir") +
                "/../ppg-testing-library/Detectability/D2-detectable-communication/Python-validation/server.py"

        for (i in 1..range) {
            val srvTmp = createTempFile(dir, "server$i", ".py")
            srvTmp.writeText(serverSrc.readFile())
            val clientTmp = createTempFile(dir, "client$i", ".py")
            clientTmp.writeText(clientSrc.readFile())
        }
    }

    // executes the PPG on the generated file(s)
    // @Benchmark
    @Test
    // @Benchmark
    // @Measurement(iterations = 6)
    open fun testScalability() {
        executePPG(
            Path(
                System.getProperty("user.dir") +
                    "/../ppg-testing-library/Disclosure/unencrypted-transmission/Python/tmp"
            ),
            listOf(Path("."), Path("tmp"))
        )
    }

    @Test
    @Benchmark
    @Measurement(iterations = 8)
    open fun testClientServerScalability() {
        executePPG(
            Path(
                System.getProperty("user.dir") +
                    "/../ppg-testing-library/Detectability/D2-detectable-communication/Python-validation/tmp"
            ),
            listOf(Path("."), Path("tmp"))
        )
    }

    // deletes the generated test files
    @OptIn(ExperimentalPathApi::class)
    // @TearDown
    open fun tearDown() {
        var dir =
            Path(
                System.getProperty("user.dir") +
                    "/../ppg-testing-library/Disclosure/unencrypted-transmission/Python/tmp"
            )
        for (file in dir.walk(PathWalkOption.BREADTH_FIRST)) {
            file.deleteExisting()
        }
        dir =
            Path(
                System.getProperty("user.dir") +
                    "/../ppg-testing-library/Detectability/D2-detectable-communication/Python-validation/tmp"
            )
        for (file in dir.walk(PathWalkOption.BREADTH_FIRST)) {
            file.deleteExisting()
        }
    }
}
