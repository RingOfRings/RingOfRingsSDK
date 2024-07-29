package com.hyperring.ringofrings
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.hyperring.ringofrings.core.RingCore
import com.hyperring.ringofrings.core.utils.alchemy.data.TokenBalance
import com.hyperring.ringofrings.core.utils.crypto.data.RingCryptoResponse
import com.hyperring.ringofrings.core.utils.nfc.NFCUtil
import com.hyperring.ringofrings.ui.theme.RingOfRingsTheme
import com.hyperring.sdk.core.nfc.NFCStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


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
        RingCore.initSharedPrefs(this@SplashActivity)
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
                        SplashBox(viewModel = splashViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun TextEditBox(modifier: Modifier = Modifier, viewModel: SplashViewModel) {
    val masterKeyAlias = MasterKey
        .Builder(LocalContext.current, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    val sharedPrefs = EncryptedSharedPreferences.create(
        LocalContext.current,
        "Ring_of_rings_demo",
        masterKeyAlias,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    var text by remember { mutableStateOf(sharedPrefs.getString("Alchemy API Key", "")) }
    OutlinedTextField(
        value = text?:"",
        onValueChange = {
            text = it
            viewModel.updateAlchemyKey(text?:"", sharedPrefs)
        },
        label = { Text("Alchemy API Key") }
    )
}

@Composable
fun SplashBox(modifier: Modifier = Modifier, viewModel: SplashViewModel) {
    val context = LocalContext.current
    var wallet = viewModel.wallet.collectAsState()

    Column(modifier = modifier.padding(10.dp)) {
        Box(modifier = modifier
            .background(Color(0xFF66BB6A))
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
                        text = "Wallet Info",
                        modifier = modifier.fillMaxWidth(),
                        style = TextStyle(fontSize = 22.sp),
                        textAlign = TextAlign.Center,
                    )
                }
                FilledTonalButton(
                    modifier = modifier.fillMaxWidth(),
                    onClick = {
                        generateWallet(context)
                    }) {
                    Text("Generate Wallet", textAlign = TextAlign.Center)
                }
                Column (
                    modifier = modifier.align(Alignment.Start),
                ) {
                    Text(
                        text = "Wallet Mnemonic: ${wallet.value?.getMnemonic()}",
                        modifier = modifier.fillMaxWidth(),
                        style = TextStyle(fontSize = 14.sp),
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Wallet Private Key: ${wallet.value?.getPrivateKey()}",
                        modifier = modifier.fillMaxWidth(),
                        style = TextStyle(fontSize = 14.sp),
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Wallet Address: ${wallet.value?.getAddress()}",
                        modifier = modifier.fillMaxWidth(),
                        style = TextStyle(fontSize = 14.sp),
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Wallet Public Key: ${wallet.value?.getPublicKey()}",
                        modifier = modifier.fillMaxWidth(),
                        style = TextStyle(fontSize = 14.sp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        Box(modifier = modifier
            .background(Color(0xFF33BB6A))
            .padding(10.dp)
            .fillMaxWidth()
            .height((200.dp))) {
            val context = LocalContext.current
            val activity = context as? Activity

            Column(
                modifier = modifier
                    .align(Alignment.TopCenter)
            ) {
                FilledTonalButton(
                    modifier = modifier.fillMaxWidth(),
                    onClick = {
                        writeWalletToTag(context)
                    }) {
                    Text("Show list", textAlign = TextAlign.Center)
                }
                Row {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        FilledTonalButton(
                            modifier = modifier.fillMaxWidth(),
                            onClick = {
                                writeWalletToTag(context)
                            }) {
                            Text("Write wallet data to Tag", textAlign = TextAlign.Center)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        FilledTonalButton(
                            modifier = modifier.fillMaxWidth(),
                            onClick = {

                            }) {
                            Text("read Wallet data from Tag", textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}

fun generateWallet(context: Context) {
    if(RingCore.hasWallet(context)) {
        showToast(context, "Wallet is exist")
        return
    }
    RingCore.createWallet(context)
}

fun writeWalletToTag(context: Context) {
    RingCore.setWalletDataToRing(context)
}

data class SplashUiState(
    // UI state flags
    val nfcStatus: NFCStatus = NFCStatus.NFC_UNSUPPORTED,
    val isPolling: Boolean = false,
)

class SplashViewModel : ViewModel() {
    fun updateAlchemyKey(text: String, sharedPrefs: SharedPreferences) {
        sharedPrefs.edit().putString("Alchemy API Key", text).apply()
    }

    /// 1.check Network Connecting
    /// 2.check alchemy key
    /// 3.check Wallet Info
    /// if 1.2.3 exist. move to MainActivity
    /// else if 1 is false. finish app and showNetworkIssue Error
    /// else if 2 or 3 is false. move to InputActivity
    fun checkStatus(activity: Activity) {
        NFCUtil.initNFCStatus(activity).let {
            if(NFCUtil.nfcStatus == NFCStatus.NFC_ENABLED) {
                var isNetworkConnected: Boolean = RingCore.isNetworkAvailable(activity)
                var hasWallet: Boolean = RingCore.hasWallet(activity)
                if(!isNetworkConnected) {
                    showToast(activity, "Network Not Connected")
                    activity.finish()
                }

                if(hasWallet) {
                    val intent= Intent(
                        activity,
                        MainActivity::class.java
                    )
                    activity.startActivity(intent)
                }
            } else {
                showToast(activity, "NFC is not enabled.")
                activity.finish()
            }

        }
    }

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    private val _wallet = MutableStateFlow<RingCryptoResponse?>(null)
    val wallet: StateFlow<RingCryptoResponse?> = _wallet

    init {
        fetchWallet()
    }

    private fun fetchWallet() {
        _wallet.value = RingCore.getWalletData()
    }
}