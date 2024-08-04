package com.ringofrings.ringofrings.core.utils.alchemy;
import com.ringofrings.ringofrings.core.utils.alchemy.data.BalancesJsonBody;
import com.ringofrings.ringofrings.core.utils.alchemy.data.TokenAmountResult;
import com.ringofrings.ringofrings.core.utils.alchemy.data.TokenBalanceJsonBody;
import com.ringofrings.ringofrings.core.utils.alchemy.data.TokenBalances;
import com.ringofrings.ringofrings.core.utils.alchemy.data.TokenMetaDataResult;
import com.ringofrings.ringofrings.core.utils.alchemy.data.TokenMetadataJsonBody;

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

    @POST("v2/{apiKey}")
    Call<TokenAmountResult> getTokenBalance(@Path("apiKey") String apiKey, @Body TokenBalanceJsonBody jsonBody);
}
