package com.hyperring.sdk.core.mock1
import android.app.Activity
import android.content.Context
import android.util.Log

class MockHyperRingNFC {
    companion object {
        private var initialized = false
        var isPolling: Boolean = false // Polling status

        /**
         * Initialized Mock HyperRingNFC
         */
       /* fun initializeMockHyperRingNFC(context: Context) {
            initialized = true
        }*/

        fun initializeMockHyperRingNFC(context: Context?) {
            initialized = context != null
        }

        /**
         * Get current NFC status
         *
         * @return NFCStatus
         * @exception NeedInitializeException If not initialized HyperRingNFC
         */
        fun getNFCStatus(): MockNFCStatus {
            return if (initialized) {
                MockNFCStatus.NFC_ENABLED
            } else {
                MockNFCStatus.NFC_UNSUPPORTED
            }
        }
        /**
         * Start NFCTag Polling
         *
         * @param activity NFC adapter need Android Activity
         * @param onDiscovered When NFC tagged. return tag data
         */
        fun startNFCTagPolling(activity: Activity, onDiscovered: (MockHyperRingTag) -> MockHyperRingTag) {
            if (getNFCStatus() == MockNFCStatus.NFC_ENABLED) {
                isPolling = true
                logD("Start NFC Polling.")
                // Simulate NFC tag discovery
                val mockTag = MockHyperRingTag() // Create a mock tag
                onDiscovered(mockTag) // Simulate tag discovery
            }
        }
        /**
         * Stop NFC Tag Polling
         *
         * @param activity
         */
        fun stopNFCTagPolling(activity: Activity) {
            isPolling = false
            logD("Stop NFC Polling.")
        }

        /**
         * Mock Write function
         */
        fun write(hyperRingTagId: Long?, hyperRingTag: MockHyperRingTag, hyperRingData: MockHyperRingData): Boolean {
            // Simulate writing to NFC tag
            logD("[Mock Write] success.")
            return true
        }

        /**
         * Mock Read function
         */
        fun read(hyperRingTagId: Long?, hyperRingTag: MockHyperRingTag): MockHyperRingTag? {
            // Simulate reading from NFC tag
            return hyperRingTag
        }

        /**
         * Mock Logger
         */
        fun logD(text: String) {
            Log.d("MockHyperRingNFC", "log: $text")
        }
    }
}
