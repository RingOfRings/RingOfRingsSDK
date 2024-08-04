package com.ringofrings.sdk.core.nfc
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.util.Log
import com.google.gson.Gson
import com.ringofrings.sdk.core.data.RingOfRingsDataNFCInterface
import org.json.JSONObject
import java.nio.charset.StandardCharsets


/**
 * Default Data class
 * RingOfRingsDataInterface contains default functions and variables
 */
open class RingOfRingsData(tag: Tag?, override var id: Long? = null, override var data: String? = null) : RingOfRingsDataNFCInterface {
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
            val ndef = RingOfRingsTag.getNDEF(tag)
            if (ndef != null) {
                try {
                    ndef.connect()
                    val msg: NdefMessage = ndef.ndefMessage
                    if (msg.records != null) {
                        msg.records?.forEach {
                            val payload = String(it.payload, StandardCharsets.UTF_8)
                            if (it.tnf == NdefRecord.TNF_UNKNOWN) {
                                val jsonObject = JSONObject(payload)
                                RingOfRingsNFC.logD("payload: ${payload}")
                                try {
                                    id = jsonObject.getLong("id")
                                } catch (e: Exception) {
                                    Log.e("RingOfRingsData", "ID not exist.")
                                }
                                try {
                                    data = jsonObject.getString("data")
                                } catch (e: Exception) {
                                    Log.e("RingOfRingsData", "Data not exist.")
                                }
                            }
                        }
                    } else {
                        RingOfRingsNFC.logD("no records")
                    }
                } catch (e: Exception) {
                    Log.e("RingOfRings", "ndef err:${e}")
//                data = emptyJsonString()
//                throw RingOfRingsDataInitFailed()
                } finally {
                    ndef.close()
                }
            } else {
                RingOfRingsNFC.logD("ndef is null")
            }
        } catch (e: Exception) {
            Log.e("RingOfRings", "initData err:${e}")
        }
    }

    fun ndefMessageBody(): NdefMessage {
        Log.d("RingOfRingsData", "ndefMessageBody")
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
        Log.d("RingOfRingsData", "payload(): $id, $data")
        Log.d("RingOfRingsData", "payload(): ${gson.toJson(mapOf("id" to id, "data" to data)).toByteArray(Charsets.UTF_8)}")
        return gson.toJson(mapOf("id" to id, "data" to data)).toByteArray(Charsets.UTF_8)
    }

//    private fun fromJsonString(payload: String): IdData {
//        var id: Long? = null
//        var data: String? = null
//        Log.d("RingOfRingsData", "fromJsonString: payload: ${payload}")
//
//        try {
//            val jsonObject = JSONObject(payload)
//            id = jsonObject.getLong("id")
//            var dataJson = jsonObject.getString("data")
//            Log.d("RingOfRingsData", "data json:  ${dataJson}")
//        } catch (e: Exception) {
//            Log.e("RingOfRingsData", "fromJsonString err: $e")
//        }
//        try {
//            val model: RingOfRingsData = gson.fromJson(payload, RingOfRingsData::class.java)
//            data = model.data
//        } catch (e: Exception) {
//            Log.e("RingOfRingsData", "Not matched data type")
//        }
//        return IdData(id, data)
//    }

    /**
     * Must be used by overriding
     * @param source Any type
     */
    override fun encrypt(source: Any?): ByteArray {
        val encrypted = source.toString().toByteArray()
        Log.d("RingOfRingsData", "encrypted: $encrypted")
        this.data = String(encrypted)
        return encrypted
    }

    /**
     * Must be used by overriding
     * @param data Any type
     */
    override fun decrypt(source: String?): String {
        val decrypted = source.toString()
        Log.d("RingOfRingsData", "decrypted: $decrypted")
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
        fun createData(id: Long, dataMap: Map<String, Any>): RingOfRingsData {
            val RingOfRingsData = RingOfRingsData(null, id, null)
            try {
                val jsonStr = jsonStringFromMap(dataMap)
                RingOfRingsData.encrypt(jsonStr)
            } catch (e: Exception) {
                Log.e("RingOfRingsData", e.toString())
            }
            return RingOfRingsData
        }
    }
}
