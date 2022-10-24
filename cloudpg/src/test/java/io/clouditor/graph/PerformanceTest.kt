package io.clouditor.graph

import kotlin.io.path.*
import kotlinx.benchmark.Scope
import kotlinx.benchmark.readFile
import org.junit.Test
import org.openjdk.jmh.annotations.*

@State(Scope.Benchmark)
open class PerformanceTest {

    // sets up many files
    @Setup
    open fun setUpMultipleFiles() {
        // set the range
        val range = Math.pow(2.0, 0.0)
        // set the temporary directory and duplicate files into it
        val dir =
            Path(
                System.getProperty("user.dir") +
                    "/../ppg-testing-library/Disclosure/unencrypted-transmission/Python/tmp"
            )
        val clientSrc =
            System.getProperty("user.dir") +
                "/../ppg-testing-library/Disclosure/unencrypted-transmission/Python/client.py"
        val serverSrc =
            System.getProperty("user.dir") +
                "/../ppg-testing-library/Disclosure/unencrypted-transmission/Python/server.py"

        for (i in 1..range.toInt()) {
            val srvTmp = createTempFile(dir, "disclosureServer$i", ".py")
            srvTmp.writeText(serverSrc.readFile())
            val clientTmp = createTempFile(dir, "disclosureClient$i", ".py")
            clientTmp.writeText(serverSrc.readFile())
        }
    }

    // sets up one large file
    // @Setup
    open fun setUpLargeFile() {
        // set the range
        val range = Math.pow(2.0, 10.0)
        // set the temporary directory and duplicate files into it
        val dir =
            Path(
                System.getProperty("user.dir") +
                    "/../ppg-testing-library/Disclosure/unencrypted-transmission/Python/tmp"
            )
        val serverSrc =
            System.getProperty("user.dir") +
                "/../ppg-testing-library/Disclosure/unencrypted-transmission/Python/server.py"

        // create ONE large file
        val tmp = createTempFile(dir, "detectability", ".py")
        tmp.appendText(serverSrc.readFile())
        // append the function i times to the temporary file
        for (i in 1..range.toInt()) {
            val sampleFunction =
                "def encrypt(input_string: str, key: int, alphabet: str | None = None) -> str:\n" +
                    "    alpha = alphabet or ascii_letters\n" +
                    "\n" +
                    "    result = \"\"\n" +
                    "\n" +
                    "    for character in input_string:\n" +
                    "        if character not in alpha:\n" +
                    "            result += character\n" +
                    "        else:\n" +
                    "            new_key = (alpha.index(character) + key) % len(alpha)\n" +
                    "            result += alpha[new_key]\n" +
                    "\n" +
                    "    return result"
            tmp.appendText(sampleFunction)
        }
    }

    // executes the PPG on the generated file(s)
    @Benchmark
    @Test
    open fun testScalability() {
        // println(Runtime.getRuntime().totalMemory())
        executePPG(
            Path(
                System.getProperty("user.dir") +
                    "/../ppg-testing-library/Disclosure/unencrypted-transmission/Python"
            ),
            listOf(Path("."), Path("tmp"))
        )
    }

    // deletes the generated test files
    @OptIn(ExperimentalPathApi::class)
    @TearDown
    open fun tearDown() {
        val dir =
            Path(
                System.getProperty("user.dir") +
                    "/../ppg-testing-library/Disclosure/unencrypted-transmission/Python/tmp"
            )
        for (file in dir.walk(PathWalkOption.BREADTH_FIRST)) {
            file.deleteExisting()
        }
    }
}
