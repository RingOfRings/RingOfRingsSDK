package com.hyperring.ringofrings.core.utils.crypto
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import com.google.crypto.tink.subtle.Ed25519Sign
import com.google.crypto.tink.subtle.Ed25519Verify
import com.hyperring.ringofrings.core.RingCore
import com.hyperring.ringofrings.core.utils.crypto.data.RingCryptoResponse
import org.web3j.crypto.Bip32ECKeyPair
import org.web3j.crypto.CipherException
import org.web3j.crypto.MnemonicUtils
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
        fun generateMnemonic(): String? {
            try {
                // gen mnemomonic
                val initialEntropy = ByteArray(16)
                secureRandom.nextBytes(initialEntropy)
                val mnemonic = MnemonicUtils.generateMnemonic(initialEntropy)
                println("Generated Mnemonic: $mnemonic")
                return mnemonic
            } catch (e: CipherException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }

        fun createWallet(): RingCryptoResponse? {
            val crypto: RingCryptoResponse = RingCryptoResponse()
            try {
                // 1. gen mnemonic
                val mnemonic = generateMnemonic()
                print("Generated Mnemonic: $mnemonic")
                crypto.setMnemonic(mnemonic)
                // 2. gen seed
                val seed = MnemonicUtils.generateSeed(mnemonic, null)
                print("Seed: " + Numeric.toHexString(seed))

                // 3. BIP32 EC gen key pair
                val keyPair = Bip32ECKeyPair.generateKeyPair(seed)

                // 4. Create keys
                val privateKey = keyPair.privateKeyBytes33
                val publicKey = keyPair.publicKeyPoint.getEncoded(true)
                val address = keyPair.publicKeyPoint.getEncoded(true)
                print("Private Key: " + Numeric.toHexString(privateKey))
                print("Public Key: " + Numeric.toHexString(publicKey))
                crypto.setPrivateKey(Numeric.toHexString(privateKey))
                crypto.setPublicKey(Numeric.toHexString(publicKey))
                crypto.setAddress(Numeric.toHexString(address))

//                // 5. todo -> Check this part / Using Tink, verify
//                val signer = Ed25519Sign(privateKey)
//                val verifier = Ed25519Verify(publicKey)
//
//                val message = "Hello, Ed25519!"
//                val signature = signer.sign(message.toByteArray())
//                verifier.verify(signature, message.toByteArray())
//
//                print("Signature: " + Numeric.toHexString(signature))
//                print("Signature verified successfully!")
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

        fun hasWallet(sharedPrefs: EncryptedSharedPreferences?): Boolean {
             try {
                 if(getWallet(sharedPrefs) != null) {
                     return true
                 }
             } catch (e: Exception) {
                 return false
             }
            return false
        }

        fun getWallet(sharedPrefs: EncryptedSharedPreferences?): RingCryptoResponse? {
            try {
                var crypto : RingCryptoResponse? = RingCryptoResponse()
                val mnemonic = sharedPrefs?.getString(MNEMONIC, null)
                val publicKey = sharedPrefs?.getString(PUBLIC_KEY, null)
                val privateKey = sharedPrefs?.getString(PRIVATE_KEY, null)
                val address = sharedPrefs?.getString(ADDRESS, null)
                crypto?.setMnemonic(mnemonic)
                crypto?.setPublicKey(publicKey)
                crypto?.setPrivateKey(privateKey)
                crypto?.setAddress(address)
                return crypto
            } catch (e: Exception) {
                return null
            }
        }

        fun setWalletData(sharedPrefs: EncryptedSharedPreferences?, data: RingCryptoResponse?) {

        }
    }
}