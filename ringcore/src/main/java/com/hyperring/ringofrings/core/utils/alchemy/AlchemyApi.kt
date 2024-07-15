package com.hyperring.ringofrings.core.utils.alchemy

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class AlchemyApi(apiKey: String?) {
    val service: AlchemyService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        service = retrofit.create(AlchemyService::class.java)
    }

    companion object {
        private const val BASE_URL = "https://eth-mainnet.alchemyapi.io/"
    }
}