package com.hyperring.ringofrings
import NetworkUtil
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hyperring.ringofrings.core.RingCore
import com.hyperring.ringofrings.core.utils.CryptoUtil
import com.hyperring.ringofrings.data.nfc.AESHRData
import com.hyperring.ringofrings.data.nfc.AESWalletHRData
import com.hyperring.ringofrings.data.nfc.JWTHRData
import com.hyperring.ringofrings.ui.theme.RingOfRingsTheme
import com.hyperring.sdk.core.nfc.HyperRingNFC
import com.hyperring.sdk.core.nfc.HyperRingTag
import com.hyperring.sdk.core.nfc.NFCStatus
import io.jsonwebtoken.Jwts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.crypto.SecretKey


/**
 * Splash Activity
 * Check NFC Status
 * Check SharedPreference Wallet Data
 *
 * Check Alchemy API Key
 */
class SplashActivity : ComponentActivity() {
    private lateinit var splashViewModel : SplashViewModel

    companion object {
        var splashActivity: ComponentActivity? = null
    }

    override fun onResume() {
        splashActivity = this
        super.onResume()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splashActivity = this
        splashViewModel = ViewModelProvider(this)[SplashViewModel::class.java]
        lifecycleScope.launch {
            // If application is started, init nfc status
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                splashViewModel.checkStatus(this@SplashActivity)
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
                        TextEditBox(viewModel = splashViewModel)
                        SplashBox()
                    }
                }
            }
        }
    }
}

@Composable
fun TextEditBox(modifier: Modifier = Modifier, viewModel: SplashViewModel) {
    var text by remember { mutableStateOf("") }
    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            viewModel.updateAlchemyKey(text)
        },
        label = { Text("Alchemy API Key") }
    )
}

@Composable
fun SplashBox(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(10.dp)) {
        Box(modifier = modifier
            .background(Color.LightGray)
            .padding(10.dp)
            .fillMaxWidth()
            .height((200.dp))) {
            Column(
                modifier = modifier
                    .align(Alignment.TopCenter)
            ) {
                Box(modifier = modifier
                    .fillMaxWidth()
                    .height(40.dp)
                ) {
                    Text(
                        text = "Wallet",
                        modifier = modifier.fillMaxWidth(),
                        style = TextStyle(fontSize = 22.sp),
                        textAlign = TextAlign.Center,
                    )
                }
                FilledTonalButton(
                    modifier = modifier.fillMaxWidth(),
                    onClick = {

                    }) {
                    Text("Generate Wallet", textAlign = TextAlign.Center)
                }
            }
        }
    }
}

data class SplashUiState(
    // UI state flags
    val nfcStatus: NFCStatus = NFCStatus.NFC_UNSUPPORTED,
    val isPolling: Boolean = false,
)

class SplashViewModel : ViewModel() {
    fun updateAlchemyKey(text: String) {
    }

    /// 1.check Network Connecting
    /// 2.check alchemy key
    /// 3.check Wallet Info
    /// if 1.2.3 exist. move to MainActivity
    /// else if 1 is false. finish app and showNetworkIssue Error
    /// else if 2 or 3 is false. move to InputActivity
    fun checkStatus(splashActivity: SplashActivity) {
        var isNetworkConnected: Boolean = RingCore.isNetworkAvailable(splashActivity)
        var hasAlchemyKey: Boolean = RingCore.checkHasAlchemyKey(splashActivity)
        var hasWallet: Boolean = RingCore.hasWallet(splashActivity)
    }

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()
}