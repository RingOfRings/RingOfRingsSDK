package com.hyperring.ringofrings.data.nfc
import android.nfc.Tag
import android.util.Base64
import android.util.Log
import com.hyperring.sdk.core.nfc.HyperRingData
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * AES Encrypt Demo
 * When implement encode/decode functions yourself
 *
 */
class AESHRData(tag: Tag?) : HyperRingData(tag) {
    override var id: Long? = null
    override var data: String? = ""
    constructor(id: Long?, data: String?) : this(null) {
        this.id = id
        this.data = data
    }

    override fun encrypt(source: Any?): ByteArray {
        if(source is String) {
            val iv = IvParameterSpec((DEMO_KEY).toByteArray())
            val keySpec = SecretKeySpec(DEMO_KEY.toByteArray(), "AES")    /// 키
            val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")     //싸이퍼
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv)       // 암호화/복호화 모드
            val crypted = cipher.doFinal(source.toByteArray())
            val encodedByte = Base64.encode(crypted, Base64.DEFAULT)
            Log.d("DemoData","encrypted: ${crypted}, ${encodedByte}")
            data = String(encodedByte)
            return encodedByte
        }
        return "".toByteArray()
    }

    override fun decrypt(source: String?): String {
        Log.d("DemoData", "decrypt(source: $source) [${source?.length}]")
        if(source == null) {
            throw DecryptFailure()
        }
        var decodedByte: ByteArray = Base64.decode(source, Base64.DEFAULT)
        val iv = IvParameterSpec(DEMO_KEY.toByteArray())
        val keySpec = SecretKeySpec(DEMO_KEY.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(Cipher.DECRYPT_MODE, keySpec, iv)
        val output = cipher.doFinal(decodedByte)
        return String(output)
    }

    companion object {
        private var DEMO_KEY = "DEMODEMODEMODEMO"

        /**
         * Return {"id": id, "data": encryptedString("name": "John doe") }
         */
        fun createData(id: Long, mnemonic: String?, privateKey: String): AESHRData {
            val demoNFCData = AESHRData(id, "")
//            var jsonData = "{\"id\":$id,\"data\":\"{\\\"name\\\":\\\"$name\\\"}\"}"
            demoNFCData.encrypt("{\\\"mnemonic\\\":\\\"$mnemonic\\\", \\\"private\\\":\\\"$privateKey\\\"}")
            return demoNFCData
        }

        fun decrypt(source: String?): String {
            Log.d("DemoData", "decrypt(source: $source) [${source?.length}]")
            if(source == null) {
                throw DecryptFailure()
            }
            var decodedByte: ByteArray = Base64.decode(source, Base64.DEFAULT)
            val iv = IvParameterSpec(DEMO_KEY.toByteArray())
            val keySpec = SecretKeySpec(DEMO_KEY.toByteArray(), "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
            cipher.init(Cipher.DECRYPT_MODE, keySpec, iv)
            val output = cipher.doFinal(decodedByte)
            return String(output)
        }
    }

    class DecryptFailure : Exception("Decrypt Exception")
}

