package com.hyperring.sdk.core.mock1

import android.nfc.tech.Ndef


class MockHyperRingTag {
    private var data: MockHyperRingData = MockHyperRingData("Some data") // Use mock HyperRingData

    val id: Long?
        get() {
            return data.id
        }

    fun isHyperRingTag(): Boolean {
        // Simulate NFC tag checking
        return true // For mock purposes, always return true
    }

    fun getNDEF(): Ndef? {
        // Simulate getting NDEF from tag
        return null // For mock purposes, always return null
    }
}
