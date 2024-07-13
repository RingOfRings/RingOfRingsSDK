package com.hyperring.ringofrings.core

import NetworkUtil
import android.content.Context
import com.hyperring.ringofrings.core.utils.CryptoUtil

class RingCore {
    fun createWallet(context: Context) {
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