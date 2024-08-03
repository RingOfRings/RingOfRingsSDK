package com.hyperring.sdk.core.data
import android.nfc.NdefMessage
import android.nfc.Tag
import android.util.Log
import com.hyperring.sdk.core.nfc.HyperRingTag

/**
 * Default HyperRing Data Interface
 */
interface HyperRingDataNFCInterface : HyperRingDataInterface {
    override var id: Long?
    override var data : String?

//    override fun initData(tag: Tag?)
    override fun encrypt(source: Any?) : ByteArray
    override fun decrypt(source: String?) :Any
//    fun fromJsonString(payload: String): IdData
}
