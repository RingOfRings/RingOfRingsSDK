package com.hyperring.ringofrings.core.utils.alchemy;

import com.hyperring.ringofrings.core.utils.alchemy.data.BlockResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AlchemyService {
    @GET("v2/{apiKey}/getBlock")
    Call<BlockResponse> getBlock(@Path("apiKey") String apiKey, @Query("blockNumber") String blockNumber);
}
