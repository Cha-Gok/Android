package com.roro.core.gemma

sealed class DeviceSupportResult {
    object Supported : DeviceSupportResult()
    object UnsupportedCpu : DeviceSupportResult()
    data class InsufficientRam(val actualGb: Double) : DeviceSupportResult()
}