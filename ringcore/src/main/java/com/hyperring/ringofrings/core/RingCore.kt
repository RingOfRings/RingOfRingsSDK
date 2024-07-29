package com.hyperring.ringofrings.core
import NetworkUtil
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.hyperring.ringofrings.core.utils.crypto.CryptoUtil
import com.hyperring.ringofrings.core.utils.crypto.data.RingCryptoResponse
import com.hyperring.ringofrings.core.utils.nfc.NFCUtil
import com.hyperring.sdk.core.nfc.HyperRingNFC
import com.hyperring.sdk.core.nfc.HyperRingTag

class RingCore {
    companion object {
        private const val FILE_NAME = "ring_of_rings"
        private const val DEFAULT_ALCHEMY_KEY = "AXym-2aqo9_9icvXfUeVE_GNQj7-hdLj"
        var sharedPrefs : SharedPreferences? = null

        fun isNetworkAvailable(context: Context): Boolean {
            initSharedPrefs(context)
            return NetworkUtil.isNetworkAvailable(context)
        }

        fun checkHasAlchemyKey(context: Context): Boolean {
            initSharedPrefs(context).let {
                var alchemyKey: String? = sharedPrefs?.getString("alchemy_key", DEFAULT_ALCHEMY_KEY)
                return alchemyKey != null
            }
        }

        fun initSharedPrefs(context: Context) {
            if(sharedPrefs == null) {
                val masterKeyAlias = MasterKey
                    .Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

                sharedPrefs = EncryptedSharedPreferences.create(
                    context,
                    FILE_NAME,
                    masterKeyAlias,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            }
        }

        fun hasWallet(context: Context): Boolean {
            return CryptoUtil.hasWallet()
        }

        fun createWallet(context: Context): RingCryptoResponse? {
            initSharedPrefs(context)

        // Check Network ???
            val isNetworkAvailable = NetworkUtil.isNetworkAvailable(context)
            if(!isNetworkAvailable) {
                showToast(context, "Network Error.")
                return null
            }
            val wallet = CryptoUtil.createWallet(context)
            setWalletData(context, wallet)
            return wallet
        }

        /**
         * todo vault address is public key check it
         */
        fun importWalletAddress() {

        }

        fun getWalletData(): RingCryptoResponse? {
            return CryptoUtil.getWallet()
        }

        fun setWalletData(context: Context, data: RingCryptoResponse?) {
            CryptoUtil.setWalletData(context, data)
        }

        fun setWalletDataToRing(context: Context){
            val walletData = getWalletData()
            if(walletData == null) {
                showToast(context, "No Wallet Data")
                return
            }
            Log.d("RingCore", "${walletData.getMnemonic()}")
            fun onDiscovered(tag: HyperRingTag): HyperRingTag {
                Log.d("onDiscovered", "$tag")
                showToast(context, "success")
                NFCUtil.stopPolling(context)
                return tag
            }
            NFCUtil.startPolling(context, onDiscovered = ::onDiscovered)
        }

        fun startPollingRingWalletData(context: Context, onDiscovered: (tag: HyperRingTag) -> HyperRingTag) {
            NFCUtil.startPolling(context, onDiscovered)
        }

        fun stopPollingRingWalletData(context: Context) {
            NFCUtil.stopPolling(context)
        }

        fun setAlchemyAPIKey(key: String?) {
            sharedPrefs?.edit()?.putString("alchemy_key", key)?.apply()
        }

        fun setTokenImport(key: String?) {
            sharedPrefs?.edit()?.putString("alchemy_key", key)?.apply()
        }

        fun getMyTokens() {

        }

        fun signing() {

        }

        fun transactionToken() {

        }

        fun showToast(context: Context, text: String) {
            Log.d("RingCore", "text: $text")
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show() }, 0)
        }
    }
}