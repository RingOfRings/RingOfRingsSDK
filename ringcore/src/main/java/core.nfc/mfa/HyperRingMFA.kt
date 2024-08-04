package com.ringofrings.sdk.core.mfa
import android.app.Activity
import android.app.Dialog
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import com.ringofrings.ringofrings.core.R
import com.ringofrings.sdk.core.data.RingOfRingsDataInterface
import com.ringofrings.sdk.core.data.RingOfRingsMFAChallengeInterface
import com.ringofrings.sdk.core.data.MFAChallengeResponse
import com.ringofrings.sdk.core.nfc.RingOfRingsNFC
import com.ringofrings.sdk.core.nfc.RingOfRingsTag
import com.ringofrings.sdk.core.nfc.NFCStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * jwt base demo
 */
class RingOfRingsMFA {
    companion object {
        //  MFA Data (key: RingOfRingsTagId, value: data)
        private var mfaData: MutableMap<Long, RingOfRingsMFAChallengeInterface> = mutableMapOf()

        /**
         * Init MFA MFAData (When veryfyRingOfRingsMFA, Using this MFA Data)
         */
        fun initializeRingOfRingsMFA(mfaData: List<RingOfRingsMFAChallengeInterface>): Boolean {
            try {
                mfaData.forEach {
                    if(it.id != null) {
                        this.mfaData[it.id!!] = it
                    }
                }
                if(this.mfaData.isEmpty()) {
                    throw MFAInitializationFailure("mfaData is Empty.")
                }
                return true
            } catch (e: Exception) {
                //todo If throw exception, return always true.
                throw MFAInitializationFailure("${e.message}")
            }
        }

        /**
         * Clear MFA data (if RingIds item is exist)
         * If @param RingId is null, clear every MFA data
         * todo New Function. talk to manager
         *
         * @param ringId
         */
        fun clearMFAData(ringIds: List<Long>?) {
            if (ringIds == null) {
                mfaData.clear()
            }
            ringIds!!.forEach {
                if (mfaData.containsKey(it)) {
                    mfaData.remove(it)
                }
            }
        }

        /**
         * Update MFA Data
         */
        fun updateMFAData(mfaDataList: List<RingOfRingsMFAChallengeInterface>) {
            mfaDataList.forEach {
                if(it.id != null) {
                    mfaData[it.id!!] = it
                }
            }
        }

        /**
         * Compare RingOfRingsTag`s MFA Data and Saved MFA(from initializeRingOfRingsMFA function)
         */
        fun verifyRingOfRingsMFAAuthentication(response: MFAChallengeResponse?): Boolean {
            if(response == null) {
                return false
            }
            try {
                if (mfaData.containsKey(response.id)) {
                    return isValidResponse(response)
                }
            } catch (e: Exception) {
                throw VerificationFailure("Verification process failed")
            }
            return false
        }

        /**
         * Implement the logic to verify the MFA response here
         * Example: Checking the timeliness and integrity of the response
         */
        private fun isValidResponse(response: MFAChallengeResponse): Boolean {
            // Implement the actual response verification logic
            // Assume always returning true as a placeholder
            val result = processMFAChallenge(response)
            return result.isSuccess?:false
        }

        /**
         * Open MFA UserInterface.
         * If idList is empty or null. Listening every scanned tag event
         * else idList is exist. Check only that ids with saved MFA data
         *
         * @param activity
         * @param onNFCDiscovered NFC onDiscovered Code. return Dialog, Response
         */
        fun requestRingOfRingsMFAAuthentication(
            activity: Activity,
            onNFCDiscovered: (Dialog?, MFAChallengeResponse?) -> Unit?,
            autoDismiss: Boolean = false
            ) {
            showMFADialog(activity, eventListener = onNFCDiscovered, autoDismiss = autoDismiss)
        }

        private fun showMFADialog(
            activity: Activity,
            eventListener: (Dialog?, MFAChallengeResponse?) -> Unit?,
            autoDismiss: Boolean = true
            ) {
            var mfaChallengeResponse: MFAChallengeResponse? = null

            RingOfRingsNFC.initializeRingOfRingsNFC(activity)
            if (RingOfRingsNFC.getNFCStatus() == NFCStatus.NFC_UNSUPPORTED) {
                throw RingOfRingsNFC.UnsupportedNFCException()
            }

            if (RingOfRingsNFC.getNFCStatus() == NFCStatus.NFC_DISABLED) {
                Toast.makeText(activity, "Please enable NFC", Toast.LENGTH_SHORT).show()
                return
            }

            var dialog: Dialog? = null

            fun onDiscovered(tag: RingOfRingsTag): RingOfRingsTag {
                mfaChallengeResponse = processMFAChallenge(tag.data)
                eventListener(dialog, mfaChallengeResponse)
                val image: ImageView? = dialog?.findViewById(R.id.image)
                if(mfaChallengeResponse?.isSuccess == true) {
                    activity.runOnUiThread {
                        image?.setImageResource(R.drawable.img_success)
                        Handler(Looper.getMainLooper()).postDelayed(Runnable {
                            if(dialog?.isShowing == true) {
                                if(autoDismiss) {
                                    dialog?.dismiss()
                                }
                                image?.setImageResource(R.drawable.img_ready)
                            }
                        }, 1000)
                    }
                } else {
                    // Failed
                    activity.runOnUiThread {
                        image?.setImageResource(R.drawable.img_failed)
                        Handler(Looper.getMainLooper()).postDelayed(Runnable {
                            image?.setImageResource(R.drawable.img_ready)
                        }, 1000)
                    }

                }
                return tag
            }

            CoroutineScope(Dispatchers.Main).launch {
                activity.runOnUiThread {
                    dialog = Dialog(activity)
                    dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    dialog?.setCancelable(true)
                    dialog?.setContentView(R.layout.custom_layout)
                    val lp = WindowManager.LayoutParams()
                    lp.copyFrom(dialog?.window?.attributes)
                    lp.width = WindowManager.LayoutParams.MATCH_PARENT
                    dialog?.show()
                    dialog?.window?.setAttributes(lp)
                    dialog?.setOnDismissListener {
                        RingOfRingsNFC.stopNFCTagPolling(activity)
                    }
                }
                RingOfRingsNFC.startNFCTagPolling(activity, onDiscovered = ::onDiscovered)
            }
        }

        /**
         * Return challenge RingOfRingsData with mfaData[RingOfRingsData.id]
         */
        private fun processMFAChallenge(RingOfRingsData: RingOfRingsDataInterface?): MFAChallengeResponse {
            if(RingOfRingsData?.id != null && mfaData.containsKey(RingOfRingsData.id)) {
                try {
                    return mfaData[RingOfRingsData.id]!!.challenge(RingOfRingsData)
                } catch (e: Exception) {
                    Log.e("RingOfRingsMFA", "$e")
                }
            }
            return MFAChallengeResponse(RingOfRingsData!!.id, RingOfRingsData.data, isSuccess = false)
        }

    }

    class MFAInitializationFailure(message: String) : Exception(message)
    class VerificationFailure(message: String) : Exception(message)
}