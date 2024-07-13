package com.hyperring.ringofrings.core.utils
import org.web3j.crypto.Bip39Wallet
import org.web3j.crypto.CipherException
import org.web3j.crypto.Credentials
import org.web3j.crypto.WalletUtils
import java.io.File
import java.io.IOException

class CryptoUtil {
    companion object {
        fun generateMnemonic(): String? {
            try {
                // 니모닉 생성
                val bip39Wallet: Bip39Wallet = WalletUtils.generateBip39Wallet("yourpassword", File("."))
                val mnemonic: String = bip39Wallet.getMnemonic()

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