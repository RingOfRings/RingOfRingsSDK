package com.ringofrings.sdk.core.mock1

import android.nfc.tech.Ndef


class MockRingOfRingsTag {
    private var data: MockRingOfRingsData = MockRingOfRingsData("Some data") // Use mock RingOfRingsData

    val id: Long?
        get() {
            return data.id
        }

    fun isRingOfRingsTag(): Boolean {
        // Simulate NFC tag checking
        return true // For mock purposes, always return true
    }

    fun getNDEF(): Ndef? {
        // Simulate getting NDEF from tag
        return null // For mock purposes, always return null
    }
}
