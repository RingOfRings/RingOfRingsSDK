package com.ringofrings.sdk.core.nfc
import android.app.Activity
import android.content.Context
import android.nfc.NfcAdapter
import android.util.Log
import com.ringofrings.sdk.core.data.RingOfRingsDataNFCInterface

class RingOfRingsNFC {
    companion object {
        private var initialized = false
        private var adapter: NfcAdapter? = null
        var isPolling: Boolean = false // Polling status

        /**
         * Initialized RingOfRingsNFC
         */
        fun initializeRingOfRingsNFC(context: Context) {
            initialized = true
            adapter = NfcAdapter.getDefaultAdapter(context)
        }

        /**
         * Get current NFC status
         *
         * @return NFCStatus
         * @exception NeedInitializeException If not initialized RingOfRingsNFC
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
        fun startNFCTagPolling(activity: Activity, onDiscovered: (RingOfRingsTag) -> RingOfRingsTag ) {
            if(getNFCStatus() == NFCStatus.NFC_ENABLED) {
                logD( "Start NFC Polling.")
                adapter?.enableReaderMode(activity, {
                    val scannedRingOfRingsTag = RingOfRingsTag(it)
                    onDiscovered(scannedRingOfRingsTag)
                }, RingOfRingsTag.flags, null)
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
         * Write data to RingOfRingsTag
         * If RingOfRingsTagId is null, write data to Regardless of RingOfRings iD
         * else RingOfRingsTagId has ID, write data to RingOfRings with only the same Ring ID
         *
         * @param RingOfRingsTagId RingOfRings tag ID
         * @param RingOfRingsTag RingOfRings data.
         * @param RingOfRingsData: RingOfRingsDataInterface
         */
        fun write(RingOfRingsTagId: Long?, RingOfRingsTag: RingOfRingsTag, RingOfRingsData: RingOfRingsData): Boolean {
            if(RingOfRingsTagId == null) {
                // Write data to Any RingOfRings NFC Device
            } else if(RingOfRingsTag.id != RingOfRingsTagId) {
                // Not matched RingOfRingsTagId (Other RingOfRingsTag Tagged)
                logD("[Write] tag id is not matched.")
                return false
            }

            if(RingOfRingsTag.isRingOfRingsTag()) {
                val ndef = RingOfRingsTag.getNDEF()
                if (ndef != null) {
                    if(!ndef.isWritable) {
                        throw ReadOnlyNFCException()
                    }
                    if(ndef.maxSize <= RingOfRingsData.ndefMessageBody().toByteArray().size) {
                        throw OverMaxSizeMsgException(ndef.maxSize, RingOfRingsData.ndefMessageBody().toByteArray().size)
                    }
                    try {
                        ndef.connect()
                        ndef.writeNdefMessage(RingOfRingsData.ndefMessageBody())
                        logD("[Write] success. [${RingOfRingsData.ndefMessageBody().records.get(0).tnf}] [${RingOfRingsData.ndefMessageBody().records.get(0).payload}]")
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
         * If RingOfRingsTagId is same RingOfRingsData`s inner RingOfRingsTagId return Data
         *
         * @param RingOfRingsTagId
         * @param RingOfRingsTag
         */
        fun read(RingOfRingsTagId: Long?, RingOfRingsTag: RingOfRingsTag): RingOfRingsTag? {
            if(RingOfRingsTagId == null) {
                return RingOfRingsTag
            } else if(RingOfRingsTag.id == RingOfRingsTagId) {
                return RingOfRingsTag
            }
            return null
        }

        /**
         * RingOfRingsNFC Logger
         */
        fun logD(text: String) {
            Log.d("RingOfRingsNFC", "log: $text")
        }

        private fun logE(text: String) {
            Log.e("RingOfRingsNFC", "exception: $text")
        }
    }

    class NeedInitializeException: Exception("Need RingOfRings NFC Initialize")
    class UnsupportedNFCException : Exception("Unsupported NFC Exception")
    class ReadOnlyNFCException : Exception("Read only NFC.")
    class OverMaxSizeMsgException(maxSize: Int, msgSize:Int) : Exception("NFC maxSize is $maxSize, Message maxSize is $msgSize.")

}

