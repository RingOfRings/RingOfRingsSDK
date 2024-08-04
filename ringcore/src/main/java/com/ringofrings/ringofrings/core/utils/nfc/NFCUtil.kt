package com.ringofrings.ringofrings.core.utils.nfc
import android.app.Activity
import android.content.Context
import com.ringofrings.sdk.core.nfc.RingOfRingsNFC
import com.ringofrings.sdk.core.nfc.RingOfRingsTag
import com.ringofrings.sdk.core.nfc.NFCStatus

class NFCUtil {
    companion object {
        /// true = Read mode, false = Write mode
        var isReadMode: Boolean = true
        var isPolling: Boolean = false
        var nfcStatus: NFCStatus = NFCStatus.NFC_UNSUPPORTED
        fun initNFCStatus(context: Context) {
            // init RingOfRingsNFC
            if (nfcStatus == NFCStatus.NFC_UNSUPPORTED) {
                RingOfRingsNFC.initializeRingOfRingsNFC(context).let {
                    nfcStatus = RingOfRingsNFC.getNFCStatus()
                }
            }
        }

        fun startPolling(context: Context, onDiscovered: (tag: RingOfRingsTag) -> RingOfRingsTag) {
            RingOfRingsNFC.startNFCTagPolling(
                context as Activity, onDiscovered = onDiscovered).let {
                isPolling = RingOfRingsNFC.isPolling
            }
        }

        fun stopPolling(context: Context) {
            RingOfRingsNFC.stopNFCTagPolling(context as Activity).let {
                isPolling = RingOfRingsNFC.isPolling
            }
        }
    }
}