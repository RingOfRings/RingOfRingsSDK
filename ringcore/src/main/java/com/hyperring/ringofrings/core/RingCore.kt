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
import com.hyperring.ringofrings.core.utils.alchemy.data.BalancesJsonBody
import com.hyperring.ringofrings.core.utils.alchemy.data.TokenMetaDataResult
import com.hyperring.ringofrings.core.utils.alchemy.data.TokenBalances
import com.hyperring.ringofrings.core.utils.alchemy.data.TokenMetadataJsonBody
import com.hyperring.ringofrings.core.utils.crypto.CryptoUtil
import com.hyperring.ringofrings.core.utils.crypto.data.RingCryptoResponse
import com.hyperring.ringofrings.core.utils.nfc.NFCUtil
import com.hyperring.sdk.core.nfc.HyperRingData
import com.hyperring.sdk.core.nfc.HyperRingNFC
import com.hyperring.sdk.core.nfc.HyperRingTag
import org.web3j.crypto.Credentials
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric

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

        /**
         * If address parameter is not null
         * get address token info.
         * else call wallet address
         */
        fun getMyTokens(context: Context, address: String?): TokenBalances? {
            if(getWalletData() == null) {
                return null
            }
//            val params = listOf("0x607f4c5bb672230e8672085532f7e901544a7375")
            val _address: String? = address ?: getWalletData()!!.getAddress()
            if(_address == null) {
                showToast(context, "Address not exist")
                return null
            }
            val params = listOf(_address)
            Log.d("getMyTokens", "address: $_address")
            val jsonBody : BalancesJsonBody = BalancesJsonBody(params = params)
            var result = AlchemyApi().service.getTokenBalances(getAlchemyKey(context), jsonBody).execute()
            Log.d("token result", "result:  ${result.isSuccessful}")
            Log.d("token result", "result: ${result.body()}")
            return result.body()
        }

        /**
         * Get token`s Metadata using address
         */
        fun getTokenMetadata(context: Context, address: String): TokenMetaDataResult? {
            val params = listOf(address)
            val jsonBody : TokenMetadataJsonBody = TokenMetadataJsonBody(params = params)
            var result = AlchemyApi().service.getTokenMetaData(getAlchemyKey(context), jsonBody).execute()
            Log.d("token result", "metadata: ${result.body()}")
            return result.body()
        }

        /**
         * Signing
         */
        fun signing(context: Context, privateKey: String): Boolean {
            try {
                if(getWalletData() == null) {
                    showToast(context, "Wallet not eixst")
                    return false
                }
                return getWalletData()!!.getPrivateKey() == privateKey
            } catch (e: Exception) {
                e.printStackTrace()
                showToast(context, "Signing Exception")
            }
            return false
        }

//        fun signing(context: Context, privateKey: String, publicKey: String): String? {
//            try {
//                val credentials = Credentials.create(privateKey)
//                val signMessage = Sign.signPrefixedMessage(publicKey.toByteArray(), credentials.ecKeyPair)
//                val signature = signMessage.r + signMessage.s + signMessage.v
//                val signedData: String = Numeric.toHexString(signature)
//                return signedData
//            } catch (e: Exception) {
//                e.printStackTrace()
//                showToast(context, "Error signing")
//            }
//            return null
//        }

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