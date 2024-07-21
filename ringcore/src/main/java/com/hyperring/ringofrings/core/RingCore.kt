package com.hyperring.ringofrings.core
import NetworkUtil
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.hyperring.ringofrings.core.utils.crypto.CryptoUtil
import com.hyperring.ringofrings.core.utils.crypto.data.RingCryptoResponse

class RingCore {
    companion object {
        private const val FILE_NAME = "ring_of_rings"
        var sharedPrefs : EncryptedSharedPreferences? = null
        fun isNetworkAvailable(context: Context): Boolean {
            initSharedPrefs(context)
            return NetworkUtil.isNetworkAvailable(context)
        }

        fun checkHasAlchemyKey(context: Context): Boolean {
            initSharedPrefs(context).let {
                var alchemyKey: String? = sharedPrefs?.getString("alchemy_key", null)
                return alchemyKey != null
            }
        }

        private fun initSharedPrefs(context: Context) {
            if(sharedPrefs == null) {
                val masterKeyAlias = MasterKey
                    .Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

                EncryptedSharedPreferences.create(
                    context,
                    FILE_NAME,
                    masterKeyAlias,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            }
        }

        fun hasWallet(context: Context): Boolean {
            return CryptoUtil.hasWallet(sharedPrefs)
        }

        fun createWallet(context: Context): RingCryptoResponse? {
            initSharedPrefs(context)

        // Check Network ???
        val isNetworkAvailable = NetworkUtil.isNetworkAvailable(context)
        if(!isNetworkAvailable) {
            showToast(context, "Network Error.")
            return null
        }
        return CryptoUtil.createWallet()
        }

        /**
         * todo vault address is public key check it
         */
        fun importWalletAddress() {

        }

        fun getWalletData() {

        }

        fun setWalletData() {

        }

        fun setWalletDataToRing() {

        }

        fun startPollingRingWalletData() {

        }

        fun stopPollingRingWalletData() {

        }

        fun setAlchemyAPIKey() {

        }

        fun setTokenImport() {

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