package com.hyperring.sdk.core.nfc
import android.app.Activity
import android.content.Context
import android.nfc.NfcAdapter
import android.util.Log
import com.hyperring.sdk.core.data.HyperRingDataNFCInterface

class HyperRingNFC {
    companion object {
        private var initialized = false
        private var adapter: NfcAdapter? = null
        var isPolling: Boolean = false // Polling status

        /**
         * Initialized HyperRingNFC
         */
        fun initializeHyperRingNFC(context: Context) {
            initialized = true
            adapter = NfcAdapter.getDefaultAdapter(context)
        }

        /**
         * Get current NFC status
         *
         * @return NFCStatus
         * @exception NeedInitializeException If not initialized HyperRingNFC
         */
        fun getNFCStatus(): NFCStatus {
            var status = NFCStatus.NFC_UNSUPPORTED

            // If not initialized, throw exception
            if(!initialized) {
                isPolling = false
                throw NeedInitializeException()
            }

            when {
                adapter == null -> {
                    logD( "NFC is not available.")
                    status = NFCStatus.NFC_UNSUPPORTED
                }
                adapter!!.isEnabled -> {
                    logD( "Start NFC Polling.")
                    status = NFCStatus.NFC_ENABLED
                }
                !adapter!!.isEnabled -> {
                    logD( "NFC is not polling.")
                    status = NFCStatus.NFC_DISABLED
                }
            }

            (status == NFCStatus.NFC_ENABLED).also { isPolling = it }
            return status
        }

        /**
         * Start NFCTag Polling
         *
         * @param activity NFC adapter need Android Activity
         * @param onDiscovered When NFC tagged. return tag data
         */
        fun startNFCTagPolling(activity: Activity, onDiscovered: (HyperRingTag) -> HyperRingTag ) {
            if(getNFCStatus() == NFCStatus.NFC_ENABLED) {
                logD( "Start NFC Polling.")
                adapter?.enableReaderMode(activity, {
                    val scannedHyperRingTag = HyperRingTag(it)
                    onDiscovered(scannedHyperRingTag)
                }, HyperRingTag.flags, null)
            }
        }

        /**
         * Stop NFC Tag Polling
         *
         * @param activity
         */
        fun stopNFCTagPolling(activity: Activity) {
            isPolling = false
            adapter?.disableReaderMode(activity)
            logD( "Stop NFC Polling.")
        }

        /**
         * Write data to HyperRingTag
         * If hyperRingTagId is null, write data to Regardless of HyperRing iD
         * else hyperRingTagId has ID, write data to HyperRing with only the same HyperRing ID
         *
         * @param hyperRingTagId HyperRing tag ID
         * @param hyperRingTag HyperRing data.
         * @param hyperRingData: HyperRingDataInterface
         */
        fun write(hyperRingTagId: Long?, hyperRingTag: HyperRingTag, hyperRingData: HyperRingData): Boolean {
            if(hyperRingTagId == null) {
                // Write data to Any HyperRing NFC Device
            } else if(hyperRingTag.id != hyperRingTagId) {
                // Not matched HyperRingTagId (Other HyperRingTag Tagged)
                logD("[Write] tag id is not matched.")
                return false
            }

            if(hyperRingTag.isHyperRingTag()) {
                val ndef = hyperRingTag.getNDEF()
                if (ndef != null) {
                    if(!ndef.isWritable) {
                        throw ReadOnlyNFCException()
                    }
                    if(ndef.maxSize <= hyperRingData.ndefMessageBody().toByteArray().size) {
                        throw OverMaxSizeMsgException(ndef.maxSize, hyperRingData.ndefMessageBody().toByteArray().size)
                    }
                    try {
                        ndef.connect()
                        ndef.writeNdefMessage(hyperRingData.ndefMessageBody())
                        logD("[Write] success. [${hyperRingData.ndefMessageBody().records.get(0).tnf}] [${hyperRingData.ndefMessageBody().records.get(0).payload}]")
                    } catch (e: Exception) {
                        logE("[Write] exception: ${e}")
                    } finally {
                        ndef.close()
                    }
                    return true
                } else {
                    logD("ndef is null")
                }
            }
            return false
        }

        /***
         * If HyperRingTagId is same HyperRingData`s inner hyperRingTagId return Data
         *
         * @param hyperRingTagId
         * @param hyperRingTag
         */
        fun read(hyperRingTagId: Long?, hyperRingTag: HyperRingTag): HyperRingTag? {
            if(hyperRingTagId == null) {
                return hyperRingTag
            } else if(hyperRingTag.id == hyperRingTagId) {
                return hyperRingTag
            }
            return null
        }

        /**
         * HyperRingNFC Logger
         */
        fun logD(text: String) {
            Log.d("HyperRingNFC", "log: $text")
        }

        private fun logE(text: String) {
            Log.e("HyperRingNFC", "exception: $text")
        }
    }

    class NeedInitializeException: Exception("Need HyperRing NFC Initialize")
    class UnsupportedNFCException : Exception("Unsupported NFC Exception")
    class ReadOnlyNFCException : Exception("Read only NFC.")
    class OverMaxSizeMsgException(maxSize: Int, msgSize:Int) : Exception("NFC maxSize is $maxSize, Message maxSize is $msgSize.")

}

