package com.hyperring.ringofrings.core

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.hyperring.ringofrings.core.utils.CryptoUtil

class RingCore {
    companion object {
        private const val FILE_NAME = "ring_of_rings"
        var sharedPrefs : EncryptedSharedPreferences? = null
    }

    fun createWallet(context: Context) {
        initSharedPrefs(context)

//        // Check Network ???
//        var isNetworkAvailable = NetworkUtil.isNetworkAvailable(context)
//        if(!isNetworkAvailable) {
//            // todo network err
//        }
        // Create Mnemonic
        var mnemonic = CryptoUtil.generateMnemonic()
        // Get Private Key
        // Get Public Key
    }

    private fun initSharedPrefs(context: Context) {
        if(sharedPrefs == null) {
            val masterKeyAlias = MasterKey
                .Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                FILE_NAME,
                masterKeyAlias,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }

    fun importWalletAddress() {

    }

    fun getWalletData() {

    }

    fun setWalletData() {

    }

    fun setWalletDataToRing() {

    }

    fun startPollingRingWalletData() {

    }

    fun stopPollingRingWalletData() {

    }

    fun setAlchemyAPIKey() {

    }

    fun setTokenImport() {

    }

    fun getMyTokens() {

    }

    fun signing() {

    }

    fun transactionToken() {

    }
}