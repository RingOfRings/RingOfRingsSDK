package ringofrings
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ringofrings.ringofrings.core.RingCore
import com.ringofrings.ringofrings.core.RingCore.Companion.showToast
import com.ringofrings.ringofrings.core.utils.crypto.data.RingCryptoResponse
import com.ringofrings.ringofrings.core.utils.nfc.NFCUtil
import com.ringofrings.sdk.core.data.RingOfRingsMFAChallengeInterface
import ringofrings.data.mfa.AESMFAChallengeData
import ringofrings.data.nfc.AESHRData
import ringofrings.ui.theme.RingOfRingsTheme
import com.ringofrings.sdk.core.nfc.RingOfRingsNFC
import com.ringofrings.sdk.core.nfc.RingOfRingsTag
import com.ringofrings.sdk.core.nfc.NFCStatus
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
    return Box(modifier = modifier.padding(10.dp)) {
        Box(modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xEE55CC7A))
            .fillMaxWidth()
            .padding(10.dp)
            .height((70.dp))) {
            Row(modifier = modifier.align(Alignment.Center)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    OutlinedTextField(
                        value = text ?: "",
                        shape = RoundedCornerShape(10.dp),
                        textStyle = TextStyle(fontSize = 12.sp),
                        onValueChange = {
                            text = it
                            viewModel.updateAlchemyKey(text ?: "")
                        },
                        label = { Text("Alchemy API Key") }
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(start = 5.dp)
                        .height(70.dp)
                        .width(85.dp),
                    contentAlignment = Alignment.Center
                ) {
                    FilledTonalButton(
                        modifier = modifier
                            .width(85.dp)
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
    }
}

@Composable
fun SplashBox(modifier: Modifier = Modifier, viewModel: SplashViewModel) {
    val context = LocalContext.current
    var wallet = viewModel.wallet.collectAsState()
    var showImportWalletDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val lastLogText = viewModel.lastLogText.collectAsState()

    Column(modifier = modifier
        .padding(10.dp)
        .verticalScroll(scrollState)) {
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
                            Text("Generate\nWallet", textAlign = TextAlign.Center, style = TextStyle(fontSize = 13.sp))
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
                            Text("Reset\nWallet", textAlign = TextAlign.Center, style = TextStyle(fontSize = 13.sp))
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
                            Text("Import\nWallet", textAlign = TextAlign.Center, style = TextStyle(fontSize = 13.sp))
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
            .background(Color(0xFF33AA8A))
            .padding(10.dp)
            .fillMaxWidth()
            .height((295.dp))) {
            val context = LocalContext.current
            val activity = context as? Activity

            Column(
                modifier = modifier
                    .align(Alignment.TopCenter)
            ) {
                Row() {
                    Box(modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FilledTonalButton(
                            modifier = modifier.fillMaxWidth(),
                            onClick = {
                                val intent: Intent = Intent(activity, CryptoActivity::class.java)
                                intent.putExtra("address", RingCore.getWalletData()?.getAddress())
                                activity?.startActivity(intent)
                            }) {
                            Text("Fetch\nTokens", textAlign = TextAlign.Center, style = TextStyle(fontSize = 13.sp))
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
                                fun afterDiscovered(success: Boolean): Boolean {
                                    showToast(context, "Signinig: $success")
                                    return success
                                }
                                signing(context, afterDiscovered = :: afterDiscovered)
                            }) {
                            Text("Signing\nWith Ring", textAlign = TextAlign.Center, style = TextStyle(fontSize = 13.sp))
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
                                transactionToken(context)
                            }) {
                            Text("Transfer\nTokens", textAlign = TextAlign.Center, style = TextStyle(fontSize = 13.sp))
                        }
                    }
                }
                Row{
                    Box(
                        modifier = Modifier
                            .height(200.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xEE55CC7A))
                            .padding(5.dp)
                        ,
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = lastLogText.value?:"empty")
                    }
                }
                Box(modifier = modifier.height(3.dp))
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
                                writeWalletToTag(context, viewModel)
                            }) {
                            Text("Write wallet data to TAG", textAlign = TextAlign.Center)
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
                                fun readTag(tag: RingOfRingsTag): RingOfRingsTag {
                                    Log.d("readTag", "tag: ${tag.isRingOfRingsTag()}")
                                    Log.d("readTag", "tag: ${tag.id}")
                                    Log.d("readTag", "tag: ${tag.data.ndefMessageBody()}")
                                    Log.d("readTag", "tag: ${tag.data.data}")
                                    Log.d("readTag", "decrypt: ${AESHRData.decrypt(tag.data.data)}")
                                    showToast(context, "${AESHRData.decrypt(tag.data.data)}")
                                    viewModel.updateLastLog("${AESHRData.decrypt(tag.data.data)}")
                                    return tag
                                }
                                viewModel.updateLastLog("Please start Tag polling for read")
                                readWalletFromTag(context, readTag = ::readTag)
                            }) {
                            Text("read Wallet data from TAG", textAlign = TextAlign.Center)
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

fun transactionToken(context: Context) {
    fun buildChallengeData(ringCryptoResponse: RingCryptoResponse?): RingOfRingsMFAChallengeInterface? {
        if(ringCryptoResponse?.getPrivateKey() == null) {
            return null
        }
        val data = AESHRData.createData(10L, ringCryptoResponse.getPrivateKey()!!)
        val challengeData = AESMFAChallengeData(10L, data.data!!, null)
        return challengeData
    }
    RingCore.transactionTokenWithMFA(context, buildChallengeData = :: buildChallengeData)
}

fun signing(context: Context, afterDiscovered: (Boolean) -> Boolean) {
    val hrData = AESHRData.createData(10L, RingCore.getWalletData()!!.getPrivateKey()!!)
    val challengeData = AESMFAChallengeData(10L, hrData.data!!, null)
    RingCore.signing(context, challengeData, afterDiscovered = afterDiscovered, autoDismiss = true)
}

@Composable
fun WalletImportDialog(onDismiss: () -> Unit) {
    // test empty wallet
//    var walletInput by remember { mutableStateOf("35718dbbcfc6e3046b61ae05305f8acdde2dbbf3daa16884c0a1353eb0f8f83a") }
    // moro`s wallet
    // todo check it, change or delete it
    var walletInput by remember { mutableStateOf("4cd6d40291a22f8fc0895b2f1cf1cedb61b43a07d7d5701dcd24fc7c54079d35") }

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
                    label = { Text("Private Key") },
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
                    RingCore.importWallet(context, walletInput).let {
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

fun writeWalletToTag(context: Context, viewModel: SplashViewModel) {
    if(RingCore.getWalletData() == null) {
        showToast(context, "Wallet not exist")
        viewModel.updateLastLog("Wallet not exist")
        return
    }
    viewModel.updateLastLog("Please start Tag polling for write")
    Log.d("writeWalletToTag", "walletData: ${RingCore.getWalletData()}")
    val hrData = AESHRData.createData(null, RingCore.getWalletData()!!.getPrivateKey()!!)
    fun onDiscovered(tag: RingOfRingsTag): RingOfRingsTag {
        Log.d("onDiscovered", "$tag / ${tag.data} / ${tag.data.data}")
        val tagId = null
        RingOfRingsNFC.write(null, tag, hrData).let {
            if(it) {
                showToast(context, "success")
                viewModel.updateLastLog("id:$tagId, data:${RingCore.getWalletData()?.getPrivateKey()}")
            } else {
                showToast(context, "try again")
            }
        }
        return tag
    }

    RingCore.setWalletDataToRing(context, hrData, onDiscovered = :: onDiscovered)
}

fun readWalletFromTag(context: Context, readTag: (RingOfRingsTag) -> RingOfRingsTag) {
    RingCore.startPollingRingWalletData(context, onDiscovered = readTag)
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

    private val _lastLogText = MutableStateFlow<String?>(null)
    val lastLogText: StateFlow<String?> = _lastLogText

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

    fun updateLastLog(text: String) {
        _lastLogText.value = text
    }
}