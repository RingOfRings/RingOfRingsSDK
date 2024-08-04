package com.hyperring.ringofrings
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.hyperring.ringofrings.core.RingCore
import com.hyperring.ringofrings.core.RingCore.Companion.showToast
import com.hyperring.ringofrings.core.utils.alchemy.data.TokenAmountResult
import com.hyperring.ringofrings.core.utils.alchemy.data.TokenBalance
import com.hyperring.ringofrings.core.utils.alchemy.data.TokenBalances
import com.hyperring.ringofrings.core.utils.alchemy.data.TokenMetaDataResult
import com.hyperring.ringofrings.ui.theme.RingOfRingsTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.BigInteger


/**
 * Crypto List Application
 */
class CryptoActivity : ComponentActivity() {
    private lateinit var cryptoViewModel : CryptoViewModel
    private
    companion object {
        var cryptoActivity: ComponentActivity? = null
    }

    override fun onResume() {
        cryptoActivity = this
        super.onResume()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val address: String? = intent.getStringExtra("address")
        Log.d("CryptoActivity", "crypto address: $address")
        if(address == null) {
            showToast(this, "No address")
            finish()
        }
        cryptoActivity = this
        cryptoViewModel = ViewModelProvider(this)[CryptoViewModel::class.java]
        cryptoViewModel.fetchItems(this, address)
        lifecycleScope.launch {
            // If application is started, init nfc status
            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                cryptoViewModel.initNFCStatus(this@CryptoActivity)
            }
        }

        setContent {
            RingOfRingsTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
//                        TotalETH(viewModel = cryptoViewModel)
                        CryptoList(viewModel = cryptoViewModel)
                    }
                }
            }
        }
    }
}

//@Composable
//fun TotalETH(viewModel: CryptoViewModel) {
//    Box(modifier = Modifier
//        .background(color = Color.LightGray)
//        .clip(RoundedCornerShape(10.dp))
//        .padding(10.dp)
//    ) {
//        BasicText(text = "TOTAL: ${getETHStr(RingCore..getAddress())} ETH}")
//    }
//}

@Composable
fun CryptoList(viewModel: CryptoViewModel) {
    var tokenBalances = viewModel.items.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    if (tokenBalances.value.isEmpty()) BasicText(text = "Empty!", modifier = Modifier.padding(16.dp))
    else LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(tokenBalances.value.size) { item ->
            Box(modifier = Modifier
                .padding(5.dp)
//                .background(if(item % 2 != 0) Color.Gray else Color.LightGray)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xEE55CC7A))
                .padding(10.dp)
                .fillMaxWidth()
                .clickable {
                    val intent: Intent = Intent(activity, CryptoActivity::class.java)
                    intent.putExtra("address", tokenBalances.value.get(item).contractAddress)
                    activity?.startActivity(intent)
                },
                ) {
                Column {
                    BasicText(text = "[Token Balances] ${getTokenAmount(tokenBalances.value.get(item).tokenBalance)}",
                        modifier = Modifier
                            .padding(4.dp)
                            .background(Color(0xFF55CC7A))
                    )
                    BasicText(text = "(${tokenBalances.value.get(item).tokenBalance})",
                        style = TextStyle(fontSize = 10.sp),
                        modifier = Modifier
                            .padding(4.dp)
                            .background(Color(0xFF55CC7A)))
                    BasicText(text = "[Contract Address] ${tokenBalances.value.get(item).contractAddress}",
                        modifier = Modifier
                            .background(Color(0xFFFFFFFF))
                            .padding(10.dp)
                            .clip(RoundedCornerShape(10.dp)))
                    CryptoDetailBox(viewModel = viewModel, address = tokenBalances.value.get(item).contractAddress)
                }
            }
        }
    }
}

@Composable
fun CryptoDetailBox(modifier: Modifier = Modifier, viewModel: CryptoViewModel, address: String) {
    var metaDataDetail = viewModel.metaData.collectAsState()
    var tokenAmountDetail = viewModel.tokenBalances.collectAsState()

//    var metaDataDetail = remember { mutableStateOf(mapOf<String, TokenMetaDataResult>()) }
    val context = LocalContext.current
    val activity = context as? Activity

    Box(modifier = modifier
        .background(color = Color.LightGray)
        .clip(RoundedCornerShape(10.dp))
        .padding(10.dp)
    ) {
        Column (
            modifier = modifier,
        ) {
            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .padding(5.dp)
                    .fillMaxWidth()
//                    .height((25.dp))
            ) {
                Row() {
                    Text(
                        text = "Amount: ${tokenAmountDetail.value[address]?.result?:"--"} wei",
                        modifier = modifier
                            .fillMaxWidth()
                            .weight(1f),
                        style = TextStyle(fontSize = 14.sp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Box(modifier = modifier.height(5.dp))
            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .padding(5.dp)
                    .fillMaxWidth()
//                    .height((25.dp))
            ) {
                Row() {
                    Text(
                        text = "NAME: ${metaDataDetail.value[address]?.result?.name?:"--"}",
                        modifier = modifier
                            .fillMaxWidth()
                            .weight(1f),
                        style = TextStyle(fontSize = 14.sp),
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Demicals: ${metaDataDetail.value[address]?.result?.decimals?:"--"}",
                        modifier = modifier
                            .fillMaxWidth()
                            .weight(1f),
                        style = TextStyle(fontSize = 14.sp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Box(modifier = modifier.height(5.dp))
            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .padding(5.dp)
                    .fillMaxWidth()
//                    .height((25.dp))
            ) {
                Row() {
                    Text(
                        text = "Logo: ${metaDataDetail.value[address]?.result?.logo?:"--"}",
                        modifier = modifier
                            .fillMaxWidth()
                            .weight(1f),
                        style = TextStyle(fontSize = 14.sp),
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "symbol: ${metaDataDetail.value[address]?.result?.symbol?:"--"}",
                        modifier = modifier
                            .fillMaxWidth()
                            .weight(1f),
                        style = TextStyle(fontSize = 14.sp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

fun getETHStr(result: String?): String {
    Log.d("getETHStr", "ethstr: $result")
    if(result == null) {
        return ""
    }
    val wei = BigInteger(result.substring(2), 16) // Hex to BigInteger
    return weiToEth(wei).toString()
}

fun weiToEth(wei: BigInteger): BigDecimal {
    return RingCore.weiToEth(wei)
}


fun getTokenAmount(tokenBalance: String): String {
    val decimalValue = BigInteger(tokenBalance.substring(2), 16)
    val weiValue = BigDecimal(decimalValue)
    val etherValue = weiValue.divide(BigDecimal.TEN.pow(18))
    return etherValue.toString()
}

data class CryptoUiState(
    var tokens: TokenBalances? = null,
    var metaDataMap: MutableMap<String, TokenMetaDataResult?> = mutableMapOf(),
    var tokenBalanceMap: MutableMap<String, TokenAmountResult?> = mutableMapOf(),
) {
}

class CryptoViewModel : ViewModel() {
    val _uiState = MutableStateFlow(CryptoUiState())
    val uiState: StateFlow<CryptoUiState> = _uiState

    private val _items = MutableStateFlow<List<TokenBalance>>(emptyList())
    val items: StateFlow<List<TokenBalance>> = _items

    private val _metaData = MutableStateFlow<Map<String, TokenMetaDataResult?>>(mapOf())
    val metaData: StateFlow<Map<String, TokenMetaDataResult?>> = _metaData

    private val _tokenBalances = MutableStateFlow<Map<String, TokenAmountResult?>>(mapOf())
    val tokenBalances: StateFlow<Map<String, TokenAmountResult?>> = _tokenBalances

    private fun getApiKey(context: Context): String {
        return RingCore.getAlchemyKey(context)!!
//        return "AXym-2aqo9_9icvXfUeVE_GNQj7-hdLj"
    }

    fun fetchItems(context: Context, address: String?) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // 네트워크 요청을 수행합니다.
                    var result = RingCore.getTokenBalances(context, address)
                    uiState.value.tokens = result
//                    _items.value = result?.result?.tokenBalances?: emptyList()
                    _items.value = result?.result?.tokenBalances?: emptyList()
                    _items.value.forEach {
                        fetchMetaData(context, it.contractAddress)
                        fetchTokenAmount(context, it.contractAddress)
                    }
                }

                for (tokenBalance in uiState.value.tokens?.result?.tokenBalances!!) {
                    Log.d("token","tokens: ${tokenBalance.contractAddress}")
                }
            } catch (e: Exception) {
                // Handle the exception
                Log.e("token","tokens ex: ${e.toString()}")
            }
        }
    }

    fun fetchMetaData(context: Context, address: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // 네트워크 요청을 수행합니다.
                    var result = RingCore.getTokenMetadata(context, address)
                    uiState.value.metaDataMap[address] = result
                    _metaData.value = _metaData.value.toMutableMap().apply {
                        put(address, result)
                    }
                    Log.d("token","fetchMetaData($address): ${result?.result}")
                }
            } catch (e: Exception) {
                // Handle the exception
                Log.e("token","fetchMetaData ex: ${e.toString()}")
            }
        }
    }

    fun fetchTokenAmount(context: Context, address: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    var result = RingCore.getTokenBalance(context, address)
                    uiState.value.tokenBalanceMap[address] = result
                    _tokenBalances.value = _tokenBalances.value.toMutableMap().apply {
                        put(address, result)
                    }
                    Log.d("token","fetchAmount($address): ${result?.result}")
                }
            } catch (e: Exception) {
                // Handle the exception
                Log.e("token","fetchAmount ex: ${e.toString()}")
            }
        }
    }
}