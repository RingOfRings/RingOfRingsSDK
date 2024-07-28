package com.hyperring.ringofrings.core.utils.crypto
import androidx.security.crypto.EncryptedSharedPreferences
import com.hyperring.ringofrings.core.utils.crypto.data.RingCryptoResponse
import org.bitcoinj.crypto.MnemonicCode
import org.web3j.crypto.Bip32ECKeyPair
import org.web3j.crypto.CipherException
import org.web3j.crypto.Credentials
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
        fun createWallet(): RingCryptoResponse? {
            val crypto: RingCryptoResponse = RingCryptoResponse()
            try {
                // 1. gen mnemonic
                val mnemonic = generateMnemonic()
                print("Generated Mnemonic: $mnemonic")
                crypto.setMnemonic(mnemonic)
                // 2. gen seed
                val seed = mnemonicToSeed(mnemonic)
                print("Seed: " + Numeric.toHexString(seed))

                // 3. BIP32 EC gen key pair
                val keyPair = generateKeyPairFromSeed(seed)
                val credentials = generateCredentials(keyPair)

                // 4. Create keys
                val privateKey = credentials.ecKeyPair.privateKey.toString(16)
                val publicKey = credentials.ecKeyPair.publicKey.toString(16)
                val address = credentials.address
//                print("Private Key: " + Numeric.toHexString(privateKey))
//                print("Public Key: " + Numeric.toHexString(publicKey))
                crypto.setPrivateKey(privateKey)
                crypto.setPublicKey(publicKey)
                crypto.setAddress(address)
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
                crypto?.setMnemonic(mnemonic!!.split(""))
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