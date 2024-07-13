package com.hyperring.ringofrings.core.utils
import org.web3j.crypto.CipherException
import org.web3j.crypto.MnemonicUtils
import java.io.IOException
import java.security.SecureRandom

class CryptoUtil {
    companion object {
        private val secureRandom: SecureRandom = SecureRandom()
        fun generateMnemonic(): String? {
            try {
                // 니모닉 생성
                val initialEntropy = ByteArray(16)
                secureRandom.nextBytes(initialEntropy)
                val mnemonic = MnemonicUtils.generateMnemonic(initialEntropy)

                // 니모닉 출력
                println("Generated Mnemonic: $mnemonic")
                return mnemonic
            } catch (e: CipherException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }
    }

}