package com.hyperring.ringofrings.core.utils
import android.content.Context
import com.google.crypto.tink.subtle.Ed25519Sign
import com.google.crypto.tink.subtle.Ed25519Verify
import org.bouncycastle.math.ec.rfc8032.Ed25519
import org.web3j.crypto.Bip32ECKeyPair
import org.web3j.crypto.Bip39Wallet
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

        /// todo check it
        fun createWallet() {
            try {
                // 1. gen mnemonic
                val mnemonic = generateMnemonic()
                print("Generated Mnemonic: $mnemonic")

                // 2. gen seed
                val seed = MnemonicUtils.generateSeed(mnemonic, null)
                print("Seed: " + Numeric.toHexString(seed))

                // 3. BIP32 EC gen key pair
                val masterKeypair = Bip32ECKeyPair.generateKeyPair(seed)

                // 4. gen keyPair
                val keyPair = Bip32ECKeyPair.generateKeyPair(seed)

                // 5. 키 페어 생성
                val privateKey = keyPair.privateKeyBytes33
                val publicKey = keyPair.publicKeyPoint.getEncoded(true)
                print("Private Key: " + Numeric.toHexString(privateKey))
                print("Public Key: " + Numeric.toHexString(publicKey))

                // 6. Tink를 사용하여 서명 생성 및 검증
                val signer = Ed25519Sign(privateKey)
                val verifier = Ed25519Verify(publicKey)

                val message = "Hello, Ed25519!"
                val signature = signer.sign(message.toByteArray())
                verifier.verify(signature, message.toByteArray())

                print("Signature: " + Numeric.toHexString(signature))
                print("Signature verified successfully!")
            } catch (e: CipherException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: GeneralSecurityException) {
                e.printStackTrace()
            }
        }
    }
}