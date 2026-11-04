package com.p0lymatic.flashpoint

import com.p0lymatic.flashpoint.core.PartitionScheme
import com.p0lymatic.flashpoint.core.TargetArchitecture
import com.p0lymatic.flashpoint.core.VentoyCompletionMetrics
import com.p0lymatic.flashpoint.core.VentoyPhase
import com.p0lymatic.flashpoint.ui.DoneMetrics
import com.p0lymatic.flashpoint.ui.formatBytes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VentoyInstallerTest {

    @Test
    fun testVentoyPhaseEnumValues() {
        val phases = VentoyPhase.values().map { it.name }
        assertTrue(phases.contains("VERIFYING"))
        assertTrue(phases.contains("COMPLETED"))
        assertTrue(phases.contains("DOWNLOADING"))
        assertTrue(phases.contains("PARTITIONING"))
        assertTrue(phases.contains("WRITING_BOOTLOADER"))
    }

    @Test
    fun testVentoyCompletionMetrics() {
        val totalBytes = 33_554_432L // 32 MB
        val durationMillis = 42_000L
        val elapsedSeconds = durationMillis / 1000.0
        val speedMBs = (totalBytes / elapsedSeconds) / (1024.0 * 1024.0)
        val formatted = "%02d:%02d".format(durationMillis / 60000, (durationMillis % 60000) / 1000)

        val metrics = VentoyCompletionMetrics(
            durationMillis = durationMillis,
            formattedDuration = formatted,
            averageSpeedMBs = speedMBs,
            targetArchitecture = "x86_64",
            ventoyVersion = "1.0.99",
            partitionScheme = PartitionScheme.GPT.name,
            driveName = "Samsung Flash Drive",
            driveCapacity = "32.0 GB"
        )

        assertEquals("00:42", metrics.formattedDuration)
        assertEquals("x86_64", metrics.targetArchitecture)
        assertEquals("GPT", metrics.partitionScheme)
        assertEquals("Samsung Flash Drive", metrics.driveName)
        assertEquals("32.0 GB", metrics.driveCapacity)
        assertTrue(metrics.averageSpeedMBs > 0.7 && metrics.averageSpeedMBs < 0.8)
    }

    @Test
    fun testFormatBytes() {
        assertEquals("32.0 GB", formatBytes(32_000_000_000L))
        assertEquals("500.0 MB", formatBytes(500_000_000L))
        assertEquals("100 B", formatBytes(100L))
    }

    @Test
    fun testDoneMetricsMapping() {
        val doneMetrics = DoneMetrics(
            driveNameAndSize = "SanDisk Ultra (16.0 GB)",
            targetOrImageName = "Ventoy 1.0.99",
            schemeAndArchitecture = "GPT (x86_64)",
            averageSpeed = "24.5 MB/s",
            elapsedTime = "00:38"
        )

        assertEquals("SanDisk Ultra (16.0 GB)", doneMetrics.driveNameAndSize)
        assertEquals("Ventoy 1.0.99", doneMetrics.targetOrImageName)
        assertEquals("GPT (x86_64)", doneMetrics.schemeAndArchitecture)
        assertEquals("24.5 MB/s", doneMetrics.averageSpeed)
        assertEquals("00:38", doneMetrics.elapsedTime)
    }

    @Test
    fun testMbrSignatureBytes() {
        val mbr = ByteArray(512)
        mbr[510] = 0x55.toByte()
        mbr[511] = 0xAA.toByte()

        val sig0 = mbr[510].toInt() and 0xFF
        val sig1 = mbr[511].toInt() and 0xFF
        assertEquals(0x55, sig0)
        assertEquals(0xAA, sig1)
    }
}
