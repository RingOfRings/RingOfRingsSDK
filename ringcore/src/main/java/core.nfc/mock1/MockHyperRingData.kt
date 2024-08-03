package com.hyperring.sdk.core.mock1
import com.hyperring.sdk.core.nfc.HyperRingData

class MockHyperRingData(data: String) : HyperRingData(null) {
    override fun encrypt(source: Any?): ByteArray {
        // Simulate encryption
        return source.toString().toByteArray()
    }

    override fun decrypt(source: String?): String {
        // Simulate decryption
        return source.toString()
    }
}
