package com.hyperring.ringofrings.core.utils.crypto
import android.content.Context
import android.util.Log
import com.hyperring.ringofrings.core.RingCore
import com.hyperring.ringofrings.core.utils.crypto.data.RingCryptoResponse
import org.bitcoinj.crypto.MnemonicCode
import org.web3j.crypto.Bip32ECKeyPair
import org.web3j.crypto.CipherException
import org.web3j.crypto.Credentials
import org.web3j.utils.Numeric
import java.io.IOException
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
                return crypto
            } catch (e: Exception) {
                Log.e("getWallet", "$e")
                return null
            }
        }

        fun setWalletData(context: Context, data: RingCryptoResponse?) {
            RingCore.initSharedPrefs(context).let {
                if(data?.getMnemonic() != null) {
                    RingCore.sharedPrefs!!.edit().putString(MNEMONIC, data.getMnemonic()).apply()
                }
                RingCore.sharedPrefs!!.edit().putString(PUBLIC_KEY, data?.getPublicKey()).apply()
                RingCore.sharedPrefs!!.edit().putString(PRIVATE_KEY, data?.getPrivateKey()).apply()
                RingCore.sharedPrefs!!.edit().putString(ADDRESS, data?.getAddress()).apply()
                Log.d("setWalletData", "wallet data: ${it}")
            }
        }
    }
}