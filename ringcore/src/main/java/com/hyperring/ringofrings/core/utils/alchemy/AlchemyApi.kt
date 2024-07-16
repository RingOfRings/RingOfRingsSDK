package com.hyperring.ringofrings.core.utils.alchemy

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class AlchemyApi(apiKey: String) {
    val service: AlchemyService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(getNFTListURL(apiKey))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        service = retrofit.create(AlchemyService::class.java)
    }

    companion object {
        fun getNFTListURL(key: String) : String {
            return "https://eth-mainnet.g.alchemy.com/v2/$key/getNFTs/?owner=vitalik.eth"
        }
//        curl '/getNFTs/?owner=vitalik.eth'

    }
}