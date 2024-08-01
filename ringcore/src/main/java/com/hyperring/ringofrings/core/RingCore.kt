package com.hyperring.ringofrings.core
import NetworkUtil
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.hyperring.ringofrings.core.utils.alchemy.AlchemyApi
import com.hyperring.ringofrings.core.utils.crypto.CryptoUtil
import com.hyperring.ringofrings.core.utils.crypto.data.RingCryptoResponse
import com.hyperring.ringofrings.core.utils.nfc.NFCUtil
import com.hyperring.sdk.core.nfc.HyperRingData
import com.hyperring.sdk.core.nfc.HyperRingNFC
import com.hyperring.sdk.core.nfc.HyperRingTag
import org.web3j.crypto.Credentials

class RingCore {
    companion object {
        private const val FILE_NAME = "ring_of_rings"
        const val DEFAULT_ALCHEMY_KEY = "AXym-2aqo9_9icvXfUeVE_GNQj7-hdLj"
        var sharedPrefs : SharedPreferences? = null

        /**
         * Check network connectivity
         */
        fun isNetworkAvailable(context: Context): Boolean {
            return NetworkUtil.isNetworkAvailable(context)
        }

        /**
         * Get alchemy key from local db
         */
        fun getAlchemyKey(context: Context): String? {
            initSharedPrefs(context).let {
                var alchemyKey: String? = sharedPrefs?.getString("alchemy_key", DEFAULT_ALCHEMY_KEY)
                return alchemyKey
            }
        }

        /**
         * Init local db (SharedPreferences)
         */
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

        /**
         * Check has wallet
         */
        fun hasWallet(): Boolean {
            return CryptoUtil.hasWallet()
        }

        /**
         * Create wallet
         */
        fun createWallet(context: Context): RingCryptoResponse? {
            initSharedPrefs(context)

            val isNetworkAvailable = NetworkUtil.isNetworkAvailable(context)
            if(!isNetworkAvailable) {
                showToast(context, "Network Error.")
                return null
            }
            val wallet = CryptoUtil.createWallet(context)
            setWalletData(context, wallet)
            return wallet
        }

        fun resetWallet(context: Context) {
            setWalletData(context, null)
        }

        /**
         * todo vault address is public key check it
         */
        fun importWalletAddress(context: Context, privateKey: String): RingCryptoResponse? {
            var response: RingCryptoResponse? = null
            try {
                val credentials: Credentials = Credentials.create(privateKey)
                response = RingCryptoResponse()
                response.setPrivateKey(privateKey)
                response.setAddress(credentials.address)
                response.setPublicKey(credentials.ecKeyPair.publicKey.toString(16))
            } catch (e: Exception) {
                e.printStackTrace()
                showToast(context, "Error loading wallet from private key")
                return null
            }
            return response
        }

        /**
         * Get wallet data from local db
         */
        fun getWalletData(): RingCryptoResponse? {
            return CryptoUtil.getWallet()
        }

        /**
         * Set wallet data to local db
         */
        fun setWalletData(context: Context, data: RingCryptoResponse?) {
            CryptoUtil.setWalletData(context, data)
        }

        fun setWalletDataToRing(context: Context, hrData: HyperRingData){
            val walletData = getWalletData()
            if(walletData == null) {
                showToast(context, "No Wallet Data")
                return
            }
            Log.d("RingCore", "${walletData.getMnemonic()}")
            fun onDiscovered(tag: HyperRingTag): HyperRingTag {
                Log.d("onDiscovered", "$tag / ${tag.data} / ${tag.data.data}")
                showToast(context, "success")
//                NFCUtil.stopPolling(context)
//                val tagId = 10L
                val tagId = null
                HyperRingNFC.write(tagId, tag, hrData)
                return tag
            }
            NFCUtil.stopPolling(context)
            NFCUtil.startPolling(context, onDiscovered = ::onDiscovered)
        }

        /**
         * Start polling Wallet Data. If get Data. listening event at onDiscovered
         */
        fun startPollingRingWalletData(context: Context, onDiscovered: (tag: HyperRingTag) -> HyperRingTag) {
            NFCUtil.stopPolling(context)
            NFCUtil.startPolling(context, onDiscovered = onDiscovered)
        }

        /**
         * Stop polling NFC
         */
        fun stopPollingRingWalletData(context: Context) {
            NFCUtil.stopPolling(context)
        }

        /**
         * Set Alchemy API Key in Local Database
         */
        fun setAlchemyAPIKey(key: String?) {
            sharedPrefs?.edit()?.putString("alchemy_key", key)?.apply()
        }

        fun setTokenImport(key: String?) {
            sharedPrefs?.edit()?.putString("alchemy_key", key)?.apply()
        }

        fun getMyTokens() {
            AlchemyApi
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