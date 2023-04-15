package io.clouditor.graph

import java.util.concurrent.TimeUnit
import kotlin.io.path.*
import kotlinx.benchmark.Scope
import kotlinx.benchmark.readFile
import org.junit.Test
import org.openjdk.jmh.annotations.*
import kotlin.system.measureTimeMillis

open class GDPRExtensionPerformanceTest {

    @Test
    open fun testScalability() {
        // create a list of times
        val times = mutableListOf<Long>()

        // Warmup
        executePPG(
            Path(
                System.getProperty("user.dir") +
                        "/../ppg-testing-library/GDPRComplianceChecks/RightToRectification/Python"
            ),
            listOf(Path("."))
        )

        // measure time needed for execution of PPG (20 times)
        for (i in 1..20) {
            // measure time
            val timeForExecution = measureTimeMillis {
                executePPG(
                    Path(
                        System.getProperty("user.dir") +
                                "/../ppg-testing-library/GDPRComplianceChecks/RightToDataPortability/Python"
                    ),
                    listOf(Path("."))
                )
            }
            println("Iteration $i: time for execution of PPG: $timeForExecution ms")
            times.add(timeForExecution)
        }
        println("--------------------")
        // calculate average time
        var averageTime = 0L
        for (time in times) {
            averageTime += time
        }
        averageTime /= times.size
        // print average time
        println("Average time for execution of PPG: $averageTime ms")

        // calculate standard deviation
        var standardDeviation = 0L
        for (time in times) {
            standardDeviation += (time - averageTime) * (time - averageTime)
        }
        standardDeviation /= times.size
        standardDeviation = Math.sqrt(standardDeviation.toDouble()).toLong()
        println("Standard deviation for execution of PPG: $standardDeviation ms")

        // calculate maximum time
        var maximumTime = 0L
        for (time in times) {
            if (time > maximumTime) {
                maximumTime = time
            }
        }
        println("Maximum time for execution of PPG: $maximumTime ms")

        // calculate minimum time
        var minimumTime = Long.MAX_VALUE
        for (time in times) {
            if (time < minimumTime) {
                minimumTime = time
            }
        }
        println("Minimum time for execution of PPG: $minimumTime ms")
    }

}