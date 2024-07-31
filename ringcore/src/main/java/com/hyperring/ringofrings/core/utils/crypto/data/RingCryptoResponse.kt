package com.hyperring.ringofrings.core.utils.crypto.data

import android.util.Log

/**
 * Crypto Data for Ring of rings
 */
class RingCryptoResponse {
    //    "word1 word2 word3"
    private var mnemonic: String? = null
    private var address: String? = null
    private var privateKey: String? = null
    private var publicKey: String? = null
    fun setMnemonic(mnemonics: List<String>) {
        var mnemonicStr = ""
        mnemonics.forEach { mnemonicStr = mnemonicStr +" $it" }
        this.mnemonic = mnemonicStr
        Log.d("RingCryptoResponse", "mnemonic: ${this.mnemonic}")
    }

    fun getMnemonic() : String? {
        try {
            return mnemonic
        } catch (_: Exception) { }
        return null
    }

    fun getPublicKey(): String? {
        try {
            return publicKey
        } catch (_: Exception) { }
        return null
    }

    fun getPrivateKey(): String? {
        try {
            return privateKey
        } catch (_: Exception) { }
        return null
    }

    fun getAddress(): String? {
        try {
            return address
        } catch (_: Exception) { }
        return null
    }

    fun setPrivateKey(privateKey: String?) {
        this.privateKey = privateKey
        Log.d("RingCryptoResponse", "privateKey: $privateKey")
    }

    fun setPublicKey(publicKey: String?) {
        this.publicKey = publicKey
        Log.d("RingCryptoResponse", "publicKey: $publicKey")
    }

    fun setAddress(address: String?) {
        this.address = address
        Log.d("RingCryptoResponse", "setAddress: ${address}")
    }
}