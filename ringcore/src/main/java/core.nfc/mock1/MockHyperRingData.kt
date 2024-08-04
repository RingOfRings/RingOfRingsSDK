package com.ringofrings.sdk.core.mock1
import com.ringofrings.sdk.core.nfc.RingOfRingsData

class MockRingOfRingsData(data: String) : RingOfRingsData(null) {
    override fun encrypt(source: Any?): ByteArray {
        // Simulate encryption
        return source.toString().toByteArray()
    }

    override fun decrypt(source: String?): String {
        // Simulate decryption
        return source.toString()
    }
}
