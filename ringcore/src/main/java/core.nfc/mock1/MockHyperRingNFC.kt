package com.ringofrings.sdk.core.mock1
import android.app.Activity
import android.content.Context
import android.util.Log

class MockRingOfRingsNFC {
    companion object {
        private var initialized = false
        var isPolling: Boolean = false // Polling status

        /**
         * Initialized Mock RingOfRingsNFC
         */
       /* fun initializeMockRingOfRingsNFC(context: Context) {
            initialized = true
        }*/

        fun initializeMockRingOfRingsNFC(context: Context?) {
            initialized = context != null
        }

        /**
         * Get current NFC status
         *
         * @return NFCStatus
         * @exception NeedInitializeException If not initialized RingOfRingsNFC
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
        fun startNFCTagPolling(activity: Activity, onDiscovered: (MockRingOfRingsTag) -> MockRingOfRingsTag) {
            if (getNFCStatus() == MockNFCStatus.NFC_ENABLED) {
                isPolling = true
                logD("Start NFC Polling.")
                // Simulate NFC tag discovery
                val mockTag = MockRingOfRingsTag() // Create a mock tag
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
        fun write(RingOfRingsTagId: Long?, RingOfRingsTag: MockRingOfRingsTag, RingOfRingsData: MockRingOfRingsData): Boolean {
            // Simulate writing to NFC tag
            logD("[Mock Write] success.")
            return true
        }

        /**
         * Mock Read function
         */
        fun read(RingOfRingsTagId: Long?, RingOfRingsTag: MockRingOfRingsTag): MockRingOfRingsTag? {
            // Simulate reading from NFC tag
            return RingOfRingsTag
        }

        /**
         * Mock Logger
         */
        fun logD(text: String) {
            Log.d("MockRingOfRingsNFC", "log: $text")
        }
    }
}
