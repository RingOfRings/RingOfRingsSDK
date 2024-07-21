package com.hyperring.ringofrings.core.utils.crypto.data

/**
 * Crypto Data for Ring of rings
 */
class RingCryptoResponse {
    //    "word1 word2 word3"
    private var mnemonic: String? = null
    private var address: String? = null
    private var privateKey: String? = null
    private var publicKey: String? = null
    fun setMnemonic(mnemonic: String?) {
        this.mnemonic = mnemonic
    }

    fun setPrivateKey(privateKey: String?) {
        this.privateKey = privateKey
    }

    fun setPublicKey(publicKey: String?) {
        this.publicKey = publicKey
    }

    fun setAddress(address: String?) {
        this.address = address
    }
}