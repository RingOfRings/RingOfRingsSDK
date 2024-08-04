package com.hyperring.ringofrings.core.utils.crypto
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import com.hyperring.ringofrings.core.R
import com.hyperring.ringofrings.core.RingCore
import com.hyperring.ringofrings.core.utils.crypto.data.RingCryptoResponse
import com.hyperring.sdk.core.data.MFAChallengeResponse
import com.hyperring.sdk.core.nfc.HyperRingNFC
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bitcoinj.crypto.MnemonicCode
import org.web3j.crypto.Bip32ECKeyPair
import org.web3j.crypto.CipherException
import org.web3j.crypto.Credentials
import org.web3j.utils.Numeric
import java.io.IOException
import java.math.BigInteger
import java.security.GeneralSecurityException
import java.security.SecureRandom


class CryptoUtil {
    private val secureRandom: SecureRandom = SecureRandom()
    companion object {
        private val secureRandom: SecureRandom = SecureRandom()
        private const val MNEMONIC = "mnemonic"
        private const val PUBLIC_KEY = "public_key"
        private const val PRIVATE_KEY = "private_key"
        private const val ADDRESS = "address"
        fun generateMnemonic(): List<String> {
            // gen mnemomonic
            val initialEntropy = ByteArray(16)
            secureRandom.nextBytes(initialEntropy)
            val mnemonic = MnemonicCode.INSTANCE.toMnemonic(initialEntropy)
            println("Generated Mnemonic: $mnemonic")
            return mnemonic
        }

        fun mnemonicToSeed(mnemonic: List<String>, passphrase: String = ""): ByteArray {
            val seed = MnemonicCode.toSeed(mnemonic, passphrase)
            return seed
        }

        fun generateKeyPairFromSeed(seed: ByteArray): Bip32ECKeyPair {
            return Bip32ECKeyPair.generateKeyPair(seed)
        }

        fun generateCredentials(keyPair: Bip32ECKeyPair): Credentials {
            val path = intArrayOf(44 or Bip32ECKeyPair.HARDENED_BIT, 60 or Bip32ECKeyPair.HARDENED_BIT, 0 or Bip32ECKeyPair.HARDENED_BIT)
            val privateKeyPair = Bip32ECKeyPair.deriveKeyPair(keyPair, path)
            return Credentials.create(privateKeyPair)
        }
        fun createWallet(context: Context): RingCryptoResponse? {
            val crypto: RingCryptoResponse = RingCryptoResponse()
            try {
                // 1. gen mnemonic
                val mnemonic = generateMnemonic()
                crypto.setMnemonic(mnemonic)
                // 2. gen seed
                val seed = mnemonicToSeed(mnemonic)
                Log.d("createWallet", "Seed: " + Numeric.toHexString(seed))

                // 3. BIP32 EC gen key pair
                val keyPair = generateKeyPairFromSeed(seed)
                val credentials = generateCredentials(keyPair)

                // 4. Create keys
                val privateKey = credentials.ecKeyPair.privateKey.toString(16)
                val publicKey = credentials.ecKeyPair.publicKey.toString(16)
                val address = credentials.address
                crypto.setPrivateKey(privateKey)
                crypto.setPublicKey(publicKey)
                crypto.setAddress(address)
                setWalletData(context, crypto)
                return crypto
            } catch (e: CipherException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: GeneralSecurityException) {
                e.printStackTrace()
            }
            return null
        }

        fun hasWallet(): Boolean {
             try {
                 if(getWallet() != null) {
                     return true
                 }
             } catch (e: Exception) {
                 return false
             }
            return false
        }

        fun getWallet(): RingCryptoResponse? {
            try {
                var crypto : RingCryptoResponse? = RingCryptoResponse()
                val mnemonic = RingCore.sharedPrefs?.getString(MNEMONIC, null)
                val publicKey = RingCore.sharedPrefs?.getString(PUBLIC_KEY, null)
                val privateKey = RingCore.sharedPrefs?.getString(PRIVATE_KEY, null)
                val address = RingCore.sharedPrefs?.getString(ADDRESS, null)
                Log.d("getWallet", "mnemonic: ${mnemonic}")
                if(mnemonic != null) {
                    crypto?.setMnemonic(mnemonic!!.split(" "))
                }
                crypto?.setPublicKey(publicKey)
                crypto?.setPrivateKey(privateKey)
                crypto?.setAddress(address)
                Log.d("getWallet", "$crypto")
                if(crypto?.getAddress() == null) {
                    return null
                }
                return crypto
            } catch (e: Exception) {
                Log.e("getWallet", "$e")
                return null
            }
        }

        fun setWalletData(context: Context, data: RingCryptoResponse?) {
//            if(data == null) {
//                RingCore.sharedPrefs!!.edit().clear().apply()
//            }
            RingCore.initSharedPrefs(context).let {
                RingCore.sharedPrefs!!.edit().putString(PUBLIC_KEY, data?.getPublicKey()).apply()
                RingCore.sharedPrefs!!.edit().putString(PRIVATE_KEY, data?.getPrivateKey()).apply()
                RingCore.sharedPrefs!!.edit().putString(ADDRESS, data?.getAddress()).apply()
                if(data?.getMnemonic() != null) {
                    RingCore.sharedPrefs!!.edit().putString(MNEMONIC, data.getMnemonic()).apply()
                }
                Log.d("setWalletData", "wallet data: ${it}")
            }
        }

        fun showTransactionTokenDialog(
            activity: Activity,
            eventListener: (toAddress: String, amount: BigInteger) -> Unit?,
            autoDismiss: Boolean = true
        ) {
            var dialog: Dialog? = null
            CoroutineScope(Dispatchers.Main).launch {
                activity.runOnUiThread {
                    dialog = Dialog(activity)
                    dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    dialog?.setCancelable(true)
                    dialog?.setContentView(R.layout.transaction_token_dlg_layout)
                    val lp = WindowManager.LayoutParams()
                    lp.copyFrom(dialog?.window?.attributes)
                    lp.width = WindowManager.LayoutParams.MATCH_PARENT
                    dialog?.show()
                    dialog?.window?.setAttributes(lp)
                    dialog?.window?.findViewById<Button>(R.id.sendButton)?.setOnClickListener {
                        val toAddress = dialog?.window?.findViewById<EditText>(R.id.toAddressInput)?.text.toString()
                        var amount = dialog?.window?.findViewById<EditText>(R.id.amountInput)?.text.toString()
                        if(amount == "") {
                            amount = 0.toString()
                        }
                        Log.d("CryptoUtil", "toAddress: $toAddress, amount: $amount")
                        dialog?.dismiss()
                        eventListener(toAddress, BigInteger(amount))
                    }
                    dialog?.setOnDismissListener {

                    }
                }
            }
        }
    }
}