package com.hyperring.ringofrings.core.utils.nfc

import android.content.Context
import android.util.Log
import com.hyperring.ringofrings.core.utils.hyperring_nfc.HyperRingUtil
import com.hyperring.sdk.core.nfc.HyperRingTag

class NFCUtil {
    companion object {
        fun startPolling(context: Context, onDiscovered: (tag: HyperRingTag) -> HyperRingTag) {
            HyperRingUtil.initNFCStatus(context).let {
                HyperRingUtil.startPolling(context, onDiscovered = onDiscovered)
            }
        }

        fun stopPolling(context: Context) {
            HyperRingUtil.stopPolling(context)
        }
    }
}