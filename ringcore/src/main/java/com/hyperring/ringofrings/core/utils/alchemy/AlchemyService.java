package com.hyperring.ringofrings.core.utils.alchemy;
import com.hyperring.ringofrings.core.utils.alchemy.data.BalancesJsonBody;
import com.hyperring.ringofrings.core.utils.alchemy.data.TokenBalances;
import com.hyperring.ringofrings.core.utils.alchemy.data.TokenMetaDataResult;
import com.hyperring.ringofrings.core.utils.alchemy.data.TokenMetadataJsonBody;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface AlchemyService {
    @POST("v2/{apiKey}")
    Call<TokenBalances> getTokenBalances(@Path("apiKey") String apiKey, @Body BalancesJsonBody jsonBody);

    @POST("v2/{apiKey}")
    Call<TokenMetaDataResult> getTokenMetaData(@Path("apiKey") String apiKey, @Body TokenMetadataJsonBody jsonBody);
}
