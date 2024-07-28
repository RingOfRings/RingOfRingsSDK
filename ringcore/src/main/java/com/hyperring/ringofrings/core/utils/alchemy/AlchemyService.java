package com.hyperring.ringofrings.core.utils.alchemy;
import com.hyperring.ringofrings.core.utils.alchemy.data.BalancesJsonBody;
import com.hyperring.ringofrings.core.utils.alchemy.data.TokenBalances;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface AlchemyService {
    @POST("v2/{apiKey}")
    Call<TokenBalances> getTokenBalances(@Path("apiKey") String apiKey, @Body BalancesJsonBody jsonBody);

    @POST("v2/{apiKey}")
    Call<TokenBalances> importTokens(@Path("apiKey") String apiKey);
}
