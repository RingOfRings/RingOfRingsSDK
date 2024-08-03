package com.hyperring.sdk.core.nfc
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.util.Log
import com.google.gson.Gson
import com.hyperring.sdk.core.data.HyperRingDataNFCInterface
import org.json.JSONObject
import java.nio.charset.StandardCharsets


/**
 * Default Data class
 * HyperRingDataInterface contains default functions and variables
 */
open class HyperRingData(tag: Tag?, override var id: Long? = null, override var data: String? = null) : HyperRingDataNFCInterface {
    init {
        this.initData(tag)
    }

    /**
     * If tag data exist. init id, data
     * else id = null, data = null
     */
    private fun initData(tag: Tag?) {
        if (tag == null) {
            return
        }
        try {
            val ndef = HyperRingTag.getNDEF(tag)
            if (ndef != null) {
                try {
                    ndef.connect()
                    val msg: NdefMessage = ndef.ndefMessage
                    if (msg.records != null) {
                        msg.records?.forEach {
                            val payload = String(it.payload, StandardCharsets.UTF_8)
                            if (it.tnf == NdefRecord.TNF_UNKNOWN) {
                                val jsonObject = JSONObject(payload)
                                HyperRingNFC.logD("payload: ${payload}")
                                try {
                                    id = jsonObject.getLong("id")
                                } catch (e: Exception) {
                                    Log.e("HyperRingData", "ID not exist.")
                                }
                                try {
                                    data = jsonObject.getString("data")
                                } catch (e: Exception) {
                                    Log.e("HyperRingData", "Data not exist.")
                                }
                            }
                        }
                    } else {
                        HyperRingNFC.logD("no records")
                    }
                } catch (e: Exception) {
                    Log.e("HyperRing", "ndef err:${e}")
//                data = emptyJsonString()
//                throw HyperRingDataInitFailed()
                } finally {
                    ndef.close()
                }
            } else {
                HyperRingNFC.logD("ndef is null")
            }
        } catch (e: Exception) {
            Log.e("HyperRing", "initData err:${e}")
        }
    }

    fun ndefMessageBody(): NdefMessage {
        Log.d("HyperRingData", "ndefMessageBody")
        return NdefMessage(
            NdefRecord(
                NdefRecord.TNF_UNKNOWN,
                null,
//                null,
                null,
                payload()
            )
        )
    }

    private fun payload(): ByteArray {
        Log.d("HyperRingData", "payload(): $id, $data")
        Log.d("HyperRingData", "payload(): ${gson.toJson(mapOf("id" to id, "data" to data)).toByteArray(Charsets.UTF_8)}")
        return gson.toJson(mapOf("id" to id, "data" to data)).toByteArray(Charsets.UTF_8)
    }

//    private fun fromJsonString(payload: String): IdData {
//        var id: Long? = null
//        var data: String? = null
//        Log.d("HyperRingData", "fromJsonString: payload: ${payload}")
//
//        try {
//            val jsonObject = JSONObject(payload)
//            id = jsonObject.getLong("id")
//            var dataJson = jsonObject.getString("data")
//            Log.d("HyperRingData", "data json:  ${dataJson}")
//        } catch (e: Exception) {
//            Log.e("HyperRingData", "fromJsonString err: $e")
//        }
//        try {
//            val model: HyperRingData = gson.fromJson(payload, HyperRingData::class.java)
//            data = model.data
//        } catch (e: Exception) {
//            Log.e("HyperRingData", "Not matched data type")
//        }
//        return IdData(id, data)
//    }

    /**
     * Must be used by overriding
     * @param source Any type
     */
    override fun encrypt(source: Any?): ByteArray {
        val encrypted = source.toString().toByteArray()
        Log.d("HyperRingData", "encrypted: $encrypted")
        this.data = String(encrypted)
        return encrypted
    }

    /**
     * Must be used by overriding
     * @param data Any type
     */
    override fun decrypt(source: String?): String {
        val decrypted = source.toString()
        Log.d("HyperRingData", "decrypted: $decrypted")
        return decrypted
    }

    companion object {
        private val gson: Gson = Gson()

        private fun jsonStringFromMap(map: Map<String, Any>): String {
            return gson.toJson(map)
        }

        /**
         * dataMap example = {"name": "John doe",  "age": 20}
         * {"id": n?, "data": encryptedString }
         */
        fun createData(id: Long, dataMap: Map<String, Any>): HyperRingData {
            val hyperRingData = HyperRingData(null, id, null)
            try {
                val jsonStr = jsonStringFromMap(dataMap)
                hyperRingData.encrypt(jsonStr)
            } catch (e: Exception) {
                Log.e("HyperRingData", e.toString())
            }
            return hyperRingData
        }
    }
}
