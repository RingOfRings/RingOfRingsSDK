package com.ringofrings.sdk.core.nfc
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.util.Log

/**
 * RingOfRingsData
 * data structure
 *     NDEFRecord
 *     - Json
 *     {
 *         "id": RingOfRingsTadId
 *         "data": encrypted or original jsonStringData
 *     }
 * @param tag NFC Tag
 * @param RingOfRingsTagId RingOfRings`s Tag ID - if null, not initialized NFC Card
 */
open class RingOfRingsTag(private var tag: Tag) {
    var data: RingOfRingsData = RingOfRingsData(tag)
    val id: Long?
        get() {
            return data.id
        }

    fun isRingOfRingsTag(): Boolean {
            return isNFCA() && isNDEF()
        }

        private fun isNFCA(): Boolean {
            return tag.techList.contains("android.nfc.tech.NfcA")
        }

        private fun isNDEF(): Boolean {
            return isNDEF(tag)
        }

        /***
         * return NDEF from tag
         */
        fun getNDEF(): Ndef? {
            return Companion.getNDEF(tag)
        }

    companion object {
        fun getNDEF(tag: Tag): Ndef? {
            try {
                if(isNDEF(tag)) {
                    return Ndef.get(tag)
                }
            }catch (e: Exception) {
                Log.e("RingOfRingsData", "getNDEF"+e.toString())
            }
            return null
        }

        fun isNDEF(tag: Tag): Boolean {
            return tag.techList.contains("android.nfc.tech.Ndef")
            //todo check it
//        try {
//            Log.d("RingOfRingsData", "isNDEF tag id: ${tag.id}")
//            val formatableTag : NdefFormatable = NdefFormatable.get(tag)
//            formatableTag.connect()
//            formatableTag.format(emptyMessage())
//            formatableTag.close()
//        }catch (e: Exception) {
//            Log.e("RingOfRingsData", "isNDEF: "+e.toString())
//        }
        }

        const val flags = NfcAdapter.FLAG_READER_NFC_A
        /*        private const val flags = NfcAdapter.FLAG_READER_NFC_A or
                        NfcAdapter.FLAG_READER_NFC_B or
                        NfcAdapter.FLAG_READER_NFC_F or
                        NfcAdapter.FLAG_READER_NFC_V or
                        NfcAdapter.FLAG_READER_NFC_BARCODE or
                        NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK or
                        NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS
        */
    }
}