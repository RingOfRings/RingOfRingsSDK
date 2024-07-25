package com.hyperring.ringofrings.core.utils.hyperring_nfc

import android.app.Activity
import android.content.Context
import android.util.Log
import com.hyperring.sdk.core.nfc.HyperRingNFC
import com.hyperring.sdk.core.nfc.HyperRingTag
import com.hyperring.sdk.core.nfc.NFCStatus
import kotlinx.coroutines.flow.update

class HyperRingUtil {
    companion object {
        /// true = Read mode, false = Write mode
        var isReadMode: Boolean = true
        var isPolling: Boolean = false
        var nfcStatus: NFCStatus = NFCStatus.NFC_UNSUPPORTED
        fun initNFCStatus(context: Context) {
            // init HyperRingNFC
            if(nfcStatus == NFCStatus.NFC_UNSUPPORTED) {
                HyperRingNFC.initializeHyperRingNFC(context).let {
                    nfcStatus = HyperRingNFC.getNFCStatus()
            }
        }
    }

    fun startPolling(context: Context, onDiscovered: (tag: HyperRingTag) -> HyperRingTag) {
        HyperRingNFC.startNFCTagPolling(
            context as Activity, onDiscovered = onDiscovered).let {
            isPolling = HyperRingNFC.isPolling
        }
    }

    fun stopPolling(context: Context) {
        HyperRingNFC.stopNFCTagPolling(context as Activity).let {
            isPolling = HyperRingNFC.isPolling
        }
    }

//    private fun onDiscovered(hyperRingTag: HyperRingTag) : HyperRingTag {
//        if(isReadMode) {
//            if(hyperRingTag.isHyperRingTag()) {
//                Log.d("HyperRingUtil", "hyperRingTag.data: ${hyperRingTag.data}")
//                val readTag: HyperRingTag? = HyperRingNFC.read(null, hyperRingTag)
//                if(readTag != null) {
//                    Log.d("HyperRingUtil", "[read]${hyperRingTag.id}")
//                }
//            }
//        }
//        if(_uiState.value.isWriteMode) {
//            /// Writing Data to Any HyperRing NFC TAG
//            val isWrite = HyperRingNFC.write(uiState.value.targetWriteId, hyperRingTag,
//                // Default HyperRingData
////                HyperRingData.createData(10, mutableMapOf("age" to 25, "name" to "홍길동")))
//                // Demo custom Data
//                if(_uiState.value.dateType == "AES") AESWalletHRData.createData(uiState.value.dataTagId?:10, _uiState.value.nfcTagId)
////                if(_uiState.value.dateType == "AES") AESWalletHRData.createData(uiState.value.dataTagId?:10, " 0x81Ff4cac5Ad0e8E4b7D4D05bc22B4DdcB87599A3")
//                else JWTHRData.createData(10,
//                    "John Doe", MainActivity.jwtKey)
//            )
//
//            if(isWrite && MainActivity.mainActivity != null)
//                showToast(MainActivity.mainActivity!!, "[write] Success [${uiState.value.dataTagId}]")
//        } else {
//            if(hyperRingTag.isHyperRingTag()) {
//                Log.d("MainActivity", "hyperRingTag.data: ${hyperRingTag.data}")
//                val readTag: HyperRingTag? = HyperRingNFC.read(uiState.value.targetReadId, hyperRingTag)
//                if(readTag != null) {
//                    if(MainActivity.mainActivity != null) showToast(MainActivity.mainActivity!!, "[read]${hyperRingTag.id}")
//                    if(_uiState.value.dateType == "AES") {
//                        val demoNFCData = AESHRData(readTag.id, readTag.data.data)
//                        Log.d("MainActivity", "[READ-AES] : ${demoNFCData.data} / ${demoNFCData.decrypt(demoNFCData.data)}")
//                    } else {
//                        val demoNFCData = JWTHRData(readTag.id, readTag.data.data, MainActivity.jwtKey)
//                        Log.d("MainActivity", "[READ-JWT]1 : ${demoNFCData.data} / ${demoNFCData.decrypt(demoNFCData.data)}")
//                    }
//                }
//            }
//        }
//            return hyperRingTag
//        }
    }
}