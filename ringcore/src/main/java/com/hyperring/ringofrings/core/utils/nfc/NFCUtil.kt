package com.hyperring.ringofrings.core.utils.nfc
import android.app.Activity
import android.content.Context
import com.hyperring.sdk.core.nfc.HyperRingNFC
import com.hyperring.sdk.core.nfc.HyperRingTag
import com.hyperring.sdk.core.nfc.NFCStatus

class NFCUtil {
    companion object {
        /// true = Read mode, false = Write mode
        var isReadMode: Boolean = true
        var isPolling: Boolean = false
        var nfcStatus: NFCStatus = NFCStatus.NFC_UNSUPPORTED
        fun initNFCStatus(context: Context) {
            // init HyperRingNFC
            if (nfcStatus == NFCStatus.NFC_UNSUPPORTED) {
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
    }
}