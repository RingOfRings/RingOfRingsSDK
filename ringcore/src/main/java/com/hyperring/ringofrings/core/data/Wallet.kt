package com.hyperring.ringofrings.core.data

///Wallet Data
class Wallet() {
    var mnemonic: String? = null
    var public: String? = null
    var private: String? = null
    var address: String? = null

    fun setMnemonic(data: String?) {
        mnemonic = data
    }

    fun setPublic(data: String?) {
        public = data
    }

    fun setPrivate(data: String?) {
        private = data
    }

    fun setAddress(data: String?) {
        address = data
    }

}