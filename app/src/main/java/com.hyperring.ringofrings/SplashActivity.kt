package com.hyperring.ringofrings
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
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
import com.hyperring.ringofrings.data.nfc.AESHRData
import com.hyperring.ringofrings.ui.theme.RingOfRingsTheme
import com.hyperring.sdk.core.nfc.HyperRingTag
import com.hyperring.sdk.core.nfc.NFCStatus
import io.jsonwebtoken.Jwts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        RingCore.initSharedPrefs(this@SplashActivity).let {
            splashViewModel = ViewModelProvider(this)[SplashViewModel::class.java]
            splashViewModel.initAlchemyKey(this)
        }


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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
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
    val alchemyKey = viewModel.alchemyKey.collectAsState()
    var text by remember { mutableStateOf(RingCore.sharedPrefs?.getString("alchemy_key", alchemyKey.value?:"")) }
    return Row() {
        Box(modifier = Modifier
            .weight(1f)
            .height(65.dp),
            contentAlignment = Alignment.Center
        ) {
            OutlinedTextField(
                value = text?:"",
                shape = RoundedCornerShape(10.dp),
                textStyle = TextStyle(fontSize = 13.sp),
                onValueChange = {
                    text = it
                    viewModel.updateAlchemyKey(text?:"")
                },
                label = { Text("Alchemy API Key") }
            )
        }
        Box(modifier = Modifier
            .height(70.dp)
            .padding(start = 5.dp)
            .width(90.dp),
            contentAlignment = Alignment.Center
        ) {
            FilledTonalButton(
                modifier = modifier
                    .fillMaxWidth()
                    .height(50.dp),
                onClick = {
                    viewModel.updateAlchemyKey(RingCore.DEFAULT_ALCHEMY_KEY)
                    text = RingCore.DEFAULT_ALCHEMY_KEY
                }) {
                Text("Reset", textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun SplashBox(modifier: Modifier = Modifier, viewModel: SplashViewModel) {
    val context = LocalContext.current
    var wallet = viewModel.wallet.collectAsState()
    var showImportWalletDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.padding(10.dp)) {
        Box(modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF66BB6A))
            .fillMaxWidth()
            .padding(10.dp)
            .height((380.dp))) {
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
                Row() {
                    Box(modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FilledTonalButton(
                            modifier = modifier.fillMaxWidth(),
                            onClick = {
                                viewModel.generateWallet(context)
                            }) {
                            Text("Generate Wallet", textAlign = TextAlign.Center)
                        }
                    }
                    Box(modifier = modifier.width(5.dp))
                    Box(modifier = Modifier
                        .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        FilledTonalButton(
                            modifier = modifier.fillMaxWidth(),
                            onClick = {
                                viewModel.resetWallet(context)
                            }) {
                            Text("Reset Wallet", textAlign = TextAlign.Center)
                        }
                    }
                    Box(modifier = modifier.width(5.dp))
                    Box(modifier = Modifier
                        .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        FilledTonalButton(
                            modifier = modifier.fillMaxWidth(),
                            onClick = {
                                showImportWalletDialog = true
//                                viewModel.importWallet(context)
                            }) {
                            Text("Import Wallet", textAlign = TextAlign.Center)
                        }
                    }
                }
                Column (
                    modifier = modifier.align(Alignment.Start),
                ) {
                    Box(modifier = modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .padding(10.dp)
                        .fillMaxWidth()
                        .height((50.dp))) {
                        Text(
                            text = "Wallet Mnemonic: ${wallet.value?.getMnemonic()}",
                            modifier = modifier.fillMaxWidth(),
                            style = TextStyle(fontSize = 14.sp),
                            textAlign = TextAlign.Center,
                        )
                    }
                    Box(modifier = modifier.height(5.dp))
                    Box(modifier = modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .padding(10.dp)
                        .fillMaxWidth()
                        .height((50.dp))) {
                        Text(
                            text = "Wallet Private Key: ${wallet.value?.getPrivateKey()}",
                            modifier = modifier.fillMaxWidth(),
                            style = TextStyle(fontSize = 14.sp),
                            textAlign = TextAlign.Center,
                        )
                    }
                    Box(modifier = modifier.height(5.dp))
                    Box(modifier = modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .padding(10.dp)
                        .fillMaxWidth()
                        .height((30.dp))) {
                        Text(
                            text = "Wallet Address: ${wallet.value?.getAddress()}",
                            modifier = modifier.fillMaxWidth(),
                            style = TextStyle(fontSize = 14.sp),
                            textAlign = TextAlign.Center,
                        )
                    }
                    Box(modifier = modifier.height(5.dp))
                    Box(modifier = modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .padding(10.dp)
                        .fillMaxWidth()
                        .height((50.dp))) {
                        Text(
                            text = "Wallet Public Key: ${wallet.value?.getPublicKey()}",
                            modifier = modifier.fillMaxWidth(),
                            style = TextStyle(fontSize = 14.sp),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
        Box(modifier = modifier.height(10.dp))
        Box(modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF33BB6A))
            .padding(10.dp)
            .fillMaxWidth()
            .height((100.dp))) {
            val context = LocalContext.current
            val activity = context as? Activity

            Column(
                modifier = modifier
                    .align(Alignment.TopCenter)
            ) {
                FilledTonalButton(
                    modifier = modifier.fillMaxWidth(),
                    onClick = {
//                        writeWalletToTag(context)
                        val intent: Intent = Intent(activity, CryptoActivity::class.java)
                        activity?.startActivity(intent)
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
                                readWalletFromTag(context)
                            }) {
                            Text("read Wallet data from Tag", textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }

    if (showImportWalletDialog) {
        WalletImportDialog(onDismiss = {
            showImportWalletDialog = false
            viewModel.refreshWallet()
        })
    }
}

@Composable
fun WalletImportDialog(onDismiss: () -> Unit) {
    var walletInput by remember { mutableStateOf("35718dbbcfc6e3046b61ae05305f8acdde2dbbf3daa16884c0a1353eb0f8f83a") }
//    var passwordInput by remember { mutableStateOf("") }
    var walletAddress by remember { mutableStateOf("") }
    var showResult by remember { mutableStateOf(false) }
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Import Wallet")
        },
        text = {
            Column {
                TextField(
                    value = walletInput,
                    onValueChange = { walletInput = it },
                    label = { Text("Mnemonic or Private Key") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
//                TextField(
//                    value = passwordInput,
//                    onValueChange = { passwordInput = it },
//                    label = { Text("Wallet Password") },
//                    modifier = Modifier.fillMaxWidth(),
//                    visualTransformation = PasswordVisualTransformation()
//                )
                if (showResult) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Wallet Address: $walletAddress")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    RingCore.importWalletAddress(context, walletInput).let {
                        if(it?.getAddress() != null) {
                            walletAddress = it.getAddress()!!
                            RingCore.setWalletData(context, it)
                            onDismiss()
                        }
                    }
//                    walletAddress = if (walletInput.contains(" ")) {
//                        importWalletFromMnemonic(walletInput, passwordInput)
//                    } else {
//                        importWalletFromPrivateKey(walletInput)
//                    }
                    showResult = true
                }
            ) {
                Text("Import")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun writeWalletToTag(context: Context) {
    if(RingCore.getWalletData() == null) {
        showToast(context, "Wallet not exist")
        return
    }
    val hrData = AESHRData.createData(20L, RingCore.getWalletData()!!.getMnemonic()!!)
    RingCore.setWalletDataToRing(context, hrData)
}

fun readWalletFromTag(context: Context) {
    fun readTag(tag: HyperRingTag): HyperRingTag {
        Log.d("readTag", "tag: ${tag.isHyperRingTag()}")
        Log.d("readTag", "tag: ${tag.id}")
        Log.d("readTag", "tag: ${tag.data.ndefMessageBody()}")
        Log.d("readTag", "tag: ${tag.data.data}")
        Log.d("readTag", "decrypt: ${AESHRData.decrypt(tag.data.data)}")
        showToast(context, "${AESHRData.decrypt(tag.data.data)}")
        return tag
    }
    RingCore.startPollingRingWalletData(context, onDiscovered = ::readTag)
}

data class SplashUiState(
    // UI state flags
    val nfcStatus: NFCStatus = NFCStatus.NFC_UNSUPPORTED,
    val isPolling: Boolean = false,
)

class SplashViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    private val _wallet = MutableStateFlow<RingCryptoResponse?>(null)
    val wallet: StateFlow<RingCryptoResponse?> = _wallet

    private val _alchemyKey = MutableStateFlow<String?>(null)
    val alchemyKey: StateFlow<String?> = _alchemyKey

    init {
        fetchWallet()
    }

    fun initAlchemyKey(context: Context) {
        _alchemyKey.value = RingCore.getAlchemyKey(context)
    }

    fun updateAlchemyKey(text: String) {
        RingCore.sharedPrefs?.edit()?.putString("alchemy_key", text)?.apply()
        _alchemyKey.value = text
    }

    /**
     * 1.check Network Connecting
     * 2.init sharedpreferences
     * 3.check Wallet Info
     */
    fun checkStatus(activity: Activity) {
        NFCUtil.initNFCStatus(activity).let {
            if(NFCUtil.nfcStatus == NFCStatus.NFC_ENABLED) {
                RingCore.initSharedPrefs(activity)

                val isNetworkConnected: Boolean = RingCore.isNetworkAvailable(activity)
                if(!isNetworkConnected) {
                    showToast(activity, "Network Not Connected")
                    activity.finish()
                }

                val hasWallet: Boolean = RingCore.hasWallet()
                if(!hasWallet) {
                    showToast(activity, "Wallet not exist")
                }
            } else {
                showToast(activity, "NFC is not enabled.")
                activity.finish()
            }

        }
    }

    private fun fetchWallet() {
        _wallet.value = RingCore.getWalletData()
    }

    fun generateWallet(context: Context) : RingCryptoResponse? {
        if(RingCore.hasWallet()) {
            showToast(context, "Wallet is exist")
            return null
        }
        RingCore.createWallet(context).let {
            _wallet.value = it
            return it
        }
    }

    fun resetWallet(context: Context) {
        if(!RingCore.hasWallet()) {
            showToast(context, "Wallet not exist")
            return
        }
        RingCore.resetWallet(context).let {
            _wallet.value = null
        }
    }

    fun refreshWallet() {
        _wallet.value = RingCore.getWalletData()
    }
}