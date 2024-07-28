package com.hyperring.ringofrings
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.hyperring.ringofrings.core.utils.alchemy.AlchemyApi
import com.hyperring.ringofrings.core.utils.alchemy.data.BalancesJsonBody
import com.hyperring.ringofrings.core.utils.alchemy.data.TokenBalance
import com.hyperring.ringofrings.core.utils.alchemy.data.TokenBalanceResult
import com.hyperring.ringofrings.core.utils.alchemy.data.TokenBalances
import com.hyperring.ringofrings.ui.theme.RingOfRingsTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * Crypto List Application
 */
class CryptoActivity : ComponentActivity() {
    private lateinit var cryptoViewModel : CryptoViewModel

    companion object {
        var cryptoActivity: ComponentActivity? = null
    }

    override fun onResume() {
        cryptoActivity = this
        super.onResume()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cryptoActivity = this
        cryptoViewModel = ViewModelProvider(this)[CryptoViewModel::class.java]
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
                        CryptoList(viewModel = cryptoViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun CryptoList(viewModel: CryptoViewModel) {
    var tokenBalances = viewModel.items.collectAsState()

    if (tokenBalances.value.isEmpty()) BasicText(text = "Empty!", modifier = Modifier.padding(16.dp))
    else LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(tokenBalances.value.size) { item ->
            Box(modifier = Modifier
                .background(if(item % 2 != 0) Color.Gray else Color.LightGray)
                .padding(20.dp)
                .fillMaxWidth()
                ) {
                Column {
                    BasicText(text = "[Token Balances]\n${tokenBalances.value.get(item).tokenBalances}", modifier = Modifier.padding(16.dp))
                    BasicText(text = "[Contract Address]\n${tokenBalances.value.get(item).contractAddress}", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

data class CryptoUiState(
    var tokens: TokenBalances? = null,
) {
}

class CryptoViewModel : ViewModel() {
    val _uiState = MutableStateFlow(CryptoUiState())
    val uiState: StateFlow<CryptoUiState> = _uiState

    private val _items = MutableStateFlow<List<TokenBalance>>(emptyList())
    val items: StateFlow<List<TokenBalance>> = _items

    init {
        fetchItems()
    }

    private fun getApiKey(): String {
        return "wcdDFTfh6RhkUf14WB61TTU8tPt_ww-r"
    }

    private fun fetchItems() {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    // 네트워크 요청을 수행합니다.
                    val params = listOf("0x607f4c5bb672230e8672085532f7e901544a7375")
                    val jsonBody : BalancesJsonBody = BalancesJsonBody(params = params)
                    var result = AlchemyApi().service.getTokenBalances(getApiKey(), jsonBody).execute()
                    Log.d("token result", "result${result.isSuccessful}")
                    Log.d("token result", "result${result.body()}")
                    uiState.value.tokens = result.body()
                    _items.value = result.body()?.result?.tokenBalances?: emptyList()
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
}