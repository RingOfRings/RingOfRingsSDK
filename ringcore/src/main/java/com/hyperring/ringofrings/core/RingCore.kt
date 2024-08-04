package com.hyperring.ringofrings.core
import NetworkUtil
import android.app.Activity
import android.app.Dialog
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
import com.hyperring.ringofrings.core.utils.alchemy.data.TokenAmountResult
import com.hyperring.ringofrings.core.utils.alchemy.data.TokenBalanceJsonBody
import com.hyperring.ringofrings.core.utils.alchemy.data.TokenMetaDataResult
import com.hyperring.ringofrings.core.utils.alchemy.data.TokenBalances
import com.hyperring.ringofrings.core.utils.alchemy.data.TokenMetadataJsonBody
import com.hyperring.ringofrings.core.utils.crypto.CryptoUtil
import com.hyperring.ringofrings.core.utils.crypto.data.RingCryptoResponse
import com.hyperring.ringofrings.core.utils.nfc.NFCUtil
import com.hyperring.sdk.core.data.HyperRingMFAChallengeInterface
import com.hyperring.sdk.core.data.MFAChallengeResponse
import com.hyperring.sdk.core.mfa.HyperRingMFA
import com.hyperring.sdk.core.nfc.HyperRingData
import com.hyperring.sdk.core.nfc.HyperRingNFC
import com.hyperring.sdk.core.nfc.HyperRingTag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.crypto.Credentials
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.tx.RawTransactionManager
import org.web3j.abi.datatypes.Function
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger

/***
 * Management Every Ring Core Functions
 */
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
         * If user not setting. Return default alchemy key
         */
        fun getAlchemyKey(context: Context): String? {
            initSharedPrefs(context).let {
                var alchemyKey: String? = sharedPrefs?.getString("alchemy_key", DEFAULT_ALCHEMY_KEY)
                return alchemyKey
            }
        }

        /**
         * Init local db (EncryptedSharedPreferences)
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
         * Create wallet(Local DB)
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

        /**
         * Reset wallet data (Local DB)
         */
        fun resetWallet(context: Context) {
            setWalletData(context, null)
        }

        /**
         * Import Wallet (using private key)
         */
        fun importWallet(context: Context, privateKey: String): RingCryptoResponse? {
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

        /**
         * To NFC Tag, Set wallet data
         */
        fun setWalletDataToRing(context: Context, hrData: HyperRingData, onDiscovered: (HyperRingTag) -> HyperRingTag){
            val walletData = getWalletData()
            if(walletData == null) {
                showToast(context, "No Wallet Data")
                return
            }
            Log.d("RingCore", "${walletData.getMnemonic()}")
//            fun onDiscovered(tag: HyperRingTag): HyperRingTag {
//                Log.d("onDiscovered", "$tag / ${tag.data} / ${tag.data.data}")
//                showToast(context, "success")
//                val tagId = null
//                HyperRingNFC.write(tagId, tag, hrData)
//                return tag
//            }
            stopPollingRingWalletData(context)
            NFCUtil.startPolling(context, onDiscovered = onDiscovered)
        }

        /**
         * Start polling Wallet Data. If get Data. listening event at onDiscovered
         */
        fun startPollingRingWalletData(context: Context, onDiscovered: (tag: HyperRingTag) -> HyperRingTag) {
            stopPollingRingWalletData(context)
            NFCUtil.startPolling(context, onDiscovered = onDiscovered)
        }

        /**
         * Stop polling NFC
         */
        fun stopPollingRingWalletData(context: Context) {
            NFCUtil.stopPolling(context)
        }

        /**
         * If address parameter is not null
         * get address token info.
         * else call using wallet address
         */
        fun getTokenBalances(context: Context, address: String?): TokenBalances? {
            if(getWalletData() == null) {
                return null
            }
            val _address: String? = address ?: getWalletData()!!.getAddress()
            if(_address == null) {
                showToast(context, "Address not exist")
                return null
            }
            val params = listOf(_address)
            Log.d("getMyTokens", "address: $_address")
            val jsonBody : BalancesJsonBody = BalancesJsonBody(params = params)
            var result = AlchemyApi().service.getTokenBalances(getAlchemyKey(context), jsonBody).execute()
            Log.d("getMyTokens", "result:  ${result.isSuccessful}")
            Log.d("getMyTokens", "result: ${result.body()}")
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
         * get Token Balance amount
         */
        fun getTokenBalance(context: Context, address: String): TokenAmountResult? {
            Log.d("getTokenBalance", "address: ${address}")
            val params = listOf(address)
            val jsonBody : TokenBalanceJsonBody = TokenBalanceJsonBody(params = params)
            var result = AlchemyApi().service.getTokenBalance(getAlchemyKey(context), jsonBody).execute()
            Log.d("getTokenBalance", "get balance: ${result.body()}")
            return result.body()
        }

        /**
         * Signing
         * If scanned Tag`s data is RingCore data and RingCore Wallet data has same App user Wallet data
         */
        fun signing(context: Context, hrChallenge: HyperRingMFAChallengeInterface, autoDismiss: Boolean=false, afterDiscovered: (Boolean) -> Boolean) {
            val mfaData: MutableList<HyperRingMFAChallengeInterface> = mutableListOf()
            // AES Type
            mfaData.add(hrChallenge)
            HyperRingMFA.initializeHyperRingMFA(mfaData= mfaData.toList())

            fun onDiscovered(dialog: Dialog?, response: MFAChallengeResponse?) {
                Log.d("Signing", "requestMFADialog result: ${response}}")
                HyperRingMFA.verifyHyperRingMFAAuthentication(response).let {
                    showToast(context, if(it) "Success" else "Failed")
                    afterDiscovered(it)
                    if(it && autoDismiss) {
                        dialog?.dismiss().let {
                            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                                HyperRingNFC.startNFCTagPolling(context as Activity, { tag -> tag })
                            }, 100)
                        }
                    }
                }
            }

            HyperRingMFA.requestHyperRingMFAAuthentication(
                activity = context as Activity,
                onNFCDiscovered = ::onDiscovered,
                autoDismiss = autoDismiss)
        }

        /**
         * transaction with MFA UI
         */
        fun transactionTokenWithMFA(context: Context) {
            if(getAlchemyKey(context) == null) {
                showToast(context, "Alchemy key error")
                return
            }
            if(getWalletData() == null) {
                showToast(context, "No wallet data")
                return
            }
            fun inputCompletedListener(
                toAddress: String, amount: BigInteger,
                defaultGasPrice: BigDecimal = AlchemyApi.DEFAULT_GAS_PRICE
            ) {
                if(amount == BigInteger.ZERO){
                    showToast(context, "amount is 0")
                    return
                }
                if(toAddress == ""){
                    showToast(context, "toAddress is empty")
                    return
                }
                transactionToken(context, toAddress, amount, defaultGasPrice)
            }

            CryptoUtil.showTransactionTokenDialog(activity = context as Activity, eventListener = :: inputCompletedListener)
        }

        /**
         * transactionToken
         * If you want to make MFA UI yourself. use this function
         */
        fun transactionToken(context: Context, toAddress: String, amount: BigInteger, defaultGasPrice: BigDecimal = AlchemyApi.DEFAULT_GAS_PRICE) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val function = Function(
                        "transfer",
                        listOf<Type<*>>(org.web3j.abi.datatypes.Address(toAddress), Uint256(amount)),
                        listOf<TypeReference<*>>()
                    )
                    val credentials = Credentials.create(getWalletData()!!.getPrivateKey())
                    val encodedFunction = FunctionEncoder.encode(function)
                    val web3j = AlchemyApi.getWeb3j(getAlchemyKey(context)!!)
                    val transactionManager = RawTransactionManager(
                        web3j, credentials, AlchemyApi.getChainId()
                    )

                    val gasPrice = Convert.toWei(defaultGasPrice, Convert.Unit.GWEI).toBigInteger()
                    val gasLimit = BigInteger.valueOf(60000)

//                BigInteger gasPrice, BigInteger gasLimit, String to, String data, BigInteger value)
                    val hash = transactionManager.sendTransaction(
                        gasPrice,
                        gasLimit,
                        toAddress,
                        encodedFunction,
                        Convert.toWei(BigDecimal(amount), Convert.Unit.GWEI).toBigInteger()
                    ).transactionHash

                    showToast(context, "transaction Success.\nHash: $hash")
                    Log.d("transaction hash", "Hash: $hash")
                } catch (e: Exception) {
                    Log.e("transactionToken", "e: $e")
                }
            }
        }

        fun showToast(context: Context, text: String) {
            Log.d("RingCore", "text: $text")
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show() }, 0)
        }

        fun weiToEth(wei: BigInteger): BigDecimal {
            return Convert.fromWei(wei.toBigDecimal(), Convert.Unit.ETHER)
        }
    }
}