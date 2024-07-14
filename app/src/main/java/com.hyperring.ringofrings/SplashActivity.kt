package com.hyperring.ringofrings
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
        val jwtKey: SecretKey = Jwts.SIG.HS256.key().build()
    }

    override fun onResume() {
        splashActivity = this
        super.onResume()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("JWT", "$jwtKey")
        splashActivity = this
        splashViewModel = ViewModelProvider(this)[SplashViewModel::class.java]
        lifecycleScope.launch {
            // If application is started, init nfc status
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                splashViewModel.initNFCStatus(this@SplashActivity)
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
            viewModel.updateTagId(text)
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
    var isWriteMode: Boolean = false,
    var targetWriteId: Long? = null,
    var targetReadId: Long? = null,
    var dataTagId: Long? = 10,
    val dateType: String = "AES",
    var nfcTagId: String = "0x81Ff4cac5Ad0e8E4b7D4D05bc22B4DdcB87599A3" //Temp
) {
}

class SplashViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    fun updateTagId(text: String) {
        _uiState.update { currentState ->
            currentState.copy(
                nfcTagId = text,
            )
        }
    }

    fun initNFCStatus(context: Context) {
        // init HyperRingNFC
        HyperRingNFC.initializeHyperRingNFC(context).let {
            _uiState.update { currentState ->
                currentState.copy(
                    nfcStatus = HyperRingNFC.getNFCStatus(),
                )
            }
        }
    }

    private fun onDiscovered(hyperRingTag: HyperRingTag) : HyperRingTag {
        if(_uiState.value.isWriteMode) {
            /// Writing Data to Any HyperRing NFC TAG
            val isWrite = HyperRingNFC.write(uiState.value.targetWriteId, hyperRingTag,
                // Default HyperRingData
//                HyperRingData.createData(10, mutableMapOf("age" to 25, "name" to "홍길동")))
                // Splash custom Data
                if(_uiState.value.dateType == "AES") AESWalletHRData.createData(uiState.value.dataTagId?:10, _uiState.value.nfcTagId)
//                if(_uiState.value.dateType == "AES") AESWalletHRData.createData(uiState.value.dataTagId?:10, " 0x81Ff4cac5Ad0e8E4b7D4D05bc22B4DdcB87599A3")
                else JWTHRData.createData(10,
                    "John Doe", SplashActivity.jwtKey)
            )

            if(isWrite && SplashActivity.splashActivity != null)
                showToast(SplashActivity.splashActivity!!, "[write] Success [${uiState.value.dataTagId}]")
        } else {
            if(hyperRingTag.isHyperRingTag()) {
                Log.d("SplashActivity", "hyperRingTag.data: ${hyperRingTag.data}")
                val readTag: HyperRingTag? = HyperRingNFC.read(uiState.value.targetReadId, hyperRingTag)
                if(readTag != null) {
                    if(SplashActivity.splashActivity != null) showToast(SplashActivity.splashActivity!!, "[read]${hyperRingTag.id}")
                    if(_uiState.value.dateType == "AES") {
                        val SplashNFCData = AESHRData(readTag.id, readTag.data.data)
                        Log.d("SplashActivity", "[READ-AES] : ${SplashNFCData.data} / ${SplashNFCData.decrypt(SplashNFCData.data)}")
                    } else {
                        val SplashNFCData = JWTHRData(readTag.id, readTag.data.data, SplashActivity.jwtKey)
                        Log.d("SplashActivity", "[READ-JWT]1 : ${SplashNFCData.data} / ${SplashNFCData.decrypt(SplashNFCData.data)}")
                    }
                }
            }
        }
        return hyperRingTag
    }

    fun startPolling(context: Context) {
        HyperRingNFC.startNFCTagPolling(
            context as Activity, onDiscovered = :: onDiscovered).let {
            _uiState.update { currentState ->
                currentState.copy(
                    isPolling = HyperRingNFC.isPolling
                )
            }
        }
    }

    fun stopPolling(context: Context) {
        HyperRingNFC.stopNFCTagPolling(context as Activity).let {
            _uiState.update { currentState ->
                currentState.copy(
                    isPolling = HyperRingNFC.isPolling
                )
            }
        }
    }

    fun toggleNFCMode() {
        _uiState.update { currentState ->
            currentState.copy(
                isWriteMode = !_uiState.value.isWriteMode
            )
        }
    }

    fun setDataType(dataType: String) {
        _uiState.update { currentState ->
            currentState.copy(
                dateType = dataType
            )
        }
    }

    fun setReadTargetId(id: Long?) {
        _uiState.update { currentState ->
            currentState.copy(
                targetReadId = id
            )
        }
    }

    fun setWriteTargetId(id: Long?, dataId: Long) {
        _uiState.update { currentState ->
            currentState.copy(
                targetWriteId = id,
                dataTagId = dataId
            )
        }
    }
}

//package com.hyperring.ringofrings
//import android.app.Activity
//import android.app.Dialog
//import android.content.Context
//import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
//import android.util.Log
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.layout.wrapContentWidth
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.FilledTonalButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.lifecycle.Lifecycle
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import androidx.lifecycle.lifecycleScope
//import androidx.lifecycle.repeatOnLifecycle
//import com.hyperring.ringofrings.data.mfa.AESMFAChallengeData
//import com.hyperring.ringofrings.data.mfa.JWTMFAChallengeData
//import com.hyperring.ringofrings.data.nfc.AESHRData
//import com.hyperring.ringofrings.data.nfc.AESWalletHRData
//import com.hyperring.ringofrings.data.nfc.JWTHRData
//import com.hyperring.ringofrings.ui.theme.RingOfRingsTheme
//import com.hyperring.sdk.core.data.HyperRingMFAChallengeInterface
//import com.hyperring.sdk.core.data.MFAChallengeResponse
//import com.hyperring.sdk.core.mfa.HyperRingMFA
//import com.hyperring.sdk.core.nfc.HyperRingNFC
//import com.hyperring.sdk.core.nfc.HyperRingTag
//import com.hyperring.sdk.core.nfc.NFCStatus
//import io.jsonwebtoken.Jwts
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.update
//import kotlinx.coroutines.launch
//import javax.crypto.SecretKey
//
//
///**
// * Splash Application
// */
//class SplashActivity : ComponentActivity() {
//    private lateinit var SplashViewModel : SplashViewModel
//
//    companion object {
//        var SplashActivity: ComponentActivity? = null
//        val jwtKey: SecretKey = Jwts.SIG.HS256.key().build()
//    }
//
//    override fun onResume() {
//        SplashActivity = this
//        super.onResume()
//    }
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        Log.d("JWT", "$jwtKey")
//        SplashActivity = this
//        SplashViewModel = ViewModelProvider(this)[SplashViewModel::class.java]
//        lifecycleScope.launch {
//            // If application is started, init nfc status
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                SplashViewModel.initNFCStatus(this@SplashActivity)
//            }
//        }
//
//        setContent {
//            RingOfRingsTheme {
//                // A surface container using the 'background' color from the theme
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    Column {
//                        TextEditBox(viewModel = SplashViewModel)
//                        NFCBox(context = LocalContext.current, viewModel = SplashViewModel)
//                        MFABox()
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun TextEditBox(modifier: Modifier = Modifier, viewModel: SplashViewModel) {
//    var text by remember { mutableStateOf("0x81Ff4cac5Ad0e8E4b7D4D05bc22B4DdcB87599A3") }
//    OutlinedTextField(
//        value = text,
//        onValueChange = {
//            text = it
//            viewModel.updateTagId(text)
//        },
//        label = { Text("Write NFT ID") }
//    )
//}
//
//@Composable
//fun MFABox(modifier: Modifier = Modifier) {
//    Column(modifier = modifier.padding(10.dp)) {
//        Box(modifier = modifier
//            .background(Color.LightGray)
//            .padding(10.dp)
//            .fillMaxWidth()
//            .height((200.dp))) {
//            Column(
//                modifier = modifier
//                    .align(Alignment.TopCenter)
//            ) {
//                Box(modifier = modifier
//                    .fillMaxWidth()
//                    .height(40.dp)
//                ) {
//                    Text(
//                        text = "MFA",
//                        modifier = modifier.fillMaxWidth(),
//                        style = TextStyle(fontSize = 22.sp),
//                        textAlign = TextAlign.Center,
//                    )
//                }
//                Box(modifier = modifier
//                    .fillMaxWidth()
//                    .height(60.dp)
//                ) {
//                    Text(
//                        text = "If HyperRing has Data(id:10), Success.\nNFC Tab -> Writing Mode -> [Write] to Any Tag(data 10)",
//                        modifier = modifier.fillMaxWidth(),
//                        style = TextStyle(fontSize = 15.sp),
//                        textAlign = TextAlign.Center,
//                    )
//                }
//                FilledTonalButton(
//                    modifier = modifier.fillMaxWidth(),
//                    onClick = {
//                        requestMFADialog()
//                    }) {
//                    Text("Open requestAuthPage(autoDismiss=false)", textAlign = TextAlign.Center)
//                }
//                FilledTonalButton(
//                    modifier = modifier.fillMaxWidth(),
//                    onClick = {
//                        requestMFADialog(autoDismiss=true)
//                    }) {
//                    Text("Open requestAuthPage(autoDismiss=true)", textAlign = TextAlign.Center)
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun NFCBox(context: Context, modifier: Modifier = Modifier, viewModel: SplashViewModel) {
//    Column(modifier = modifier.padding(10.dp)) {
//        Box(modifier = modifier
//            .background(Color.LightGray)
//            .padding(10.dp)
//            .fillMaxWidth()
//            .height((340.dp))) {
//            Column(
//                modifier = modifier
//                    .align(Alignment.TopCenter)
//            ) {
//                Box(modifier = modifier
//                    .fillMaxWidth()
//                    .height(40.dp)
//                ) {
//                    Text(
//                        text = "NFC",
//                        modifier = modifier.fillMaxWidth(),
//                        style = TextStyle(fontSize = 22.sp),
//                        textAlign = TextAlign.Center,
//                    )
//                }
//                FilledTonalButton(
//                    modifier = modifier.fillMaxWidth(),
//                    onClick = {
//                        checkAvailable(context, viewModel)
//                    }) {
//                    Text("isAvailable(): ${viewModel.uiState.collectAsState().value.nfcStatus.name}", textAlign = TextAlign.Center)
//                }
//                Box(
//                    modifier = modifier
//                        .background(Color.LightGray)
//                        .height(10.dp)
//                        .fillMaxWidth())
//                Row() {
//                    Box(
//                        modifier = Modifier
//                            .weight(1f)
//                            .wrapContentWidth(Alignment.Start)) {
//                        FilledTonalButton(
//                            modifier = modifier.fillMaxWidth(),
//                            onClick = {
//                                togglePolling(context, viewModel, viewModel.uiState.value.isPolling)
//                            }) {
//                            Text("[readHyperRing]\n" + "isPolling: ${viewModel.uiState.collectAsState().value.isPolling}", textAlign = TextAlign.Center)
//                        }
//                    }
//                    Box(modifier = modifier
//                        .background(Color.LightGray)
//                        .width(10.dp))
//                    Box(
//                        modifier = Modifier
//                            .weight(1f)
//                            .wrapContentWidth(Alignment.Start)) {
//                        FilledTonalButton(
//                            modifier = modifier.fillMaxWidth(),
//                            onClick = {
//                                toggleNFCMode(viewModel)
//                            }
//                        ) {
//                            Text("[HyperRing] ${if(viewModel.uiState.collectAsState().value.isWriteMode)"Writing Mode" else "Reading Mode"}", textAlign = TextAlign.Center)
//                        }
//                    }
//                }
//                Box(
//                    modifier = modifier
//                        .background(Color.LightGray)
//                        .height(10.dp)
//                        .fillMaxWidth())
//                    if(!viewModel.uiState.collectAsState().value.isWriteMode) Row() {
//                    Box(
//                        modifier = Modifier
//                            .weight(1f)
//                            .wrapContentWidth(Alignment.Start)) {
//                        FilledTonalButton(
//                            modifier = modifier.fillMaxWidth(),
//                            colors = if(
//                                viewModel.uiState.collectAsState().value.dateType == "AES"
//                                && viewModel.uiState.collectAsState().value.targetReadId == 10L)
//                                ButtonDefaults.filledTonalButtonColors(containerColor = Color.Green)
//                            else ButtonDefaults.outlinedButtonColors(),
//                            onClick = {
//                                setDataType(viewModel, "AES")
//                                setReadTargetId(viewModel, 10)
//                            }) {
//                            Text("[Read-AES] Read to only ID-10 TAG", textAlign = TextAlign.Center)
//                        }
//                    }
//                    Box(modifier = modifier
//                        .background(Color.LightGray)
//                        .width(10.dp))
//                    Box(
//                        modifier = Modifier
//                            .weight(1f)
//                            .wrapContentWidth(Alignment.Start)) {
//                        FilledTonalButton(
//                            modifier = modifier.fillMaxWidth(),
//                            colors = if(
//                                viewModel.uiState.collectAsState().value.dateType == "AES"
//                                && viewModel.uiState.collectAsState().value.targetReadId == null)
//                                ButtonDefaults.filledTonalButtonColors(containerColor = Color.Green)
//                            else ButtonDefaults.outlinedButtonColors(),
//                            onClick = {
//                                setDataType(viewModel, "AES")
//                                setReadTargetId(viewModel, null)
//                            }
//                        ) {
//                            Text("[Read-AES] Read to Any HyperRing TAG", textAlign = TextAlign.Center)
//                        }
//                    }
//                }
//
//                Box(
//                    modifier = modifier
//                        .background(Color.LightGray)
//                        .height(10.dp)
//                        .fillMaxWidth())
//                if(!viewModel.uiState.collectAsState().value.isWriteMode) Row() {
//                    Box(
//                        modifier = Modifier
//                            .weight(1f)
//                            .wrapContentWidth(Alignment.Start)) {
//                        FilledTonalButton(
//                            modifier = modifier.fillMaxWidth(),
//                            colors = if(
//                                viewModel.uiState.collectAsState().value.dateType == "JWT"
//                                && viewModel.uiState.collectAsState().value.targetReadId == null)
//                                ButtonDefaults.filledTonalButtonColors(containerColor = Color.Green)
//                            else ButtonDefaults.outlinedButtonColors(),
//                            onClick = {
//                                setDataType(viewModel, "JWT")
//                                setReadTargetId(viewModel, null)
//                            }
//                        ) {
//                            Text("[Read-JWT] Read to Any HyperRing TAG", textAlign = TextAlign.Center)
//                        }
//                    }
//                    Box(modifier = modifier
//                        .background(Color.LightGray)
//                        .width(10.dp))
//                    Box(
//                        modifier = Modifier
//                            .weight(1f)
//                            .wrapContentWidth(Alignment.Start)) {
//                    }
//                }
//                if(viewModel.uiState.collectAsState().value.isWriteMode) Row() {
//                    Box(
//                        modifier = Modifier
//                            .weight(1f)
//                            .wrapContentWidth(Alignment.Start)
//                    ) {
//                        FilledTonalButton(
//                            modifier = modifier.fillMaxWidth(),
//                            colors = if (
//                                viewModel.uiState.collectAsState().value.dateType == "AES"
//                                && viewModel.uiState.collectAsState().value.targetWriteId == null
//                                && viewModel.uiState.collectAsState().value.dataTagId == 10L)
//                                ButtonDefaults.filledTonalButtonColors(containerColor = Color.Green)
//                            else ButtonDefaults.outlinedButtonColors(),
//                            onClick = {
//                                setDataType(viewModel,"AES")
//                                setWriteTargetId(viewModel, null, 10)
//                            }
//                        ) {
////                            Text("[Write-AES] to Any TAG(data 10)", textAlign = TextAlign.Center)
//                            Text("[태그 입력] 태그에 ${viewModel.uiState.collectAsState().value.nfcTagId} 쓰기", textAlign = TextAlign.Center)
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//fun requestMFADialog(autoDismiss: Boolean=false) {
//    if(SplashActivity.SplashActivity != null) {
//        val mfaData: MutableList<HyperRingMFAChallengeInterface> = mutableListOf()
//        // Custom Challenge
//        // AES Type
//        mfaData.add(AESMFAChallengeData(10, "dIW6SbrLx+dfb2ckLIMwDOScxw/4RggwXMPnrFSZikA\u003d\n", null))
//        // JWT Type
//        mfaData.add(JWTMFAChallengeData(15, "John Doe", null, SplashActivity.jwtKey))
//        HyperRingMFA.initializeHyperRingMFA(mfaData= mfaData.toList())
//
//        fun onDiscovered(dialog: Dialog?, response: MFAChallengeResponse?) {
//            Log.d("SplashActivity", "requestMFADialog result: ${response}}")
//            HyperRingMFA.verifyHyperRingMFAAuthentication(response).let {
//                showToast(SplashActivity.SplashActivity!!, if(it) "Success" else "Failed")
//                if(it && autoDismiss) {
//                    dialog?.dismiss()
//                }
//            }
//        }
//
//        HyperRingMFA.requestHyperRingMFAAuthentication(
//            activity = SplashActivity.SplashActivity!!,
//            onNFCDiscovered = ::onDiscovered,
//            autoDismiss = autoDismiss)
//    }
//}
//
//fun setReadTargetId(viewModel: SplashViewModel, id: Long?) {
//    viewModel.setReadTargetId(id)
//}
//
//fun setWriteTargetId(viewModel: SplashViewModel, id: Long?, dataId: Long) {
//    viewModel.setWriteTargetId(id, dataId)
//}
//
//fun setDataType(viewModel: SplashViewModel, dataType: String) {
//    viewModel.setDataType(dataType)
//}
//
//fun toggleNFCMode(viewModel: SplashViewModel) {
//    viewModel.toggleNFCMode()
//}
//
//fun togglePolling(context: Context, viewModel: SplashViewModel, isPolling: Boolean) {
//    if(isPolling) {
//        stopPolling(context, viewModel)
//    } else {
//        startPolling(context, viewModel)
//    }
//}
//
//fun startPolling(context: Context, viewModel: SplashViewModel) {
//    viewModel.startPolling(context)
//}
//
//fun stopPolling(context: Context, viewModel: SplashViewModel) {
//    viewModel.stopPolling(context)
//}
//
//fun checkAvailable(context: Context, viewModel: SplashViewModel) {
//    viewModel.initNFCStatus(context)
//}
//
//private fun showToast(context: Context, text: String) {
//    Log.d("SplashActivity", "text: $text")
//    val handler = Handler(Looper.getSplashLooper())
//    handler.postDelayed({
//        Toast.makeText(context, text, Toast.LENGTH_SHORT).show() }, 0)
//}
//
//data class SplashUiState(
//    // UI state flags
//    val nfcStatus: NFCStatus = NFCStatus.NFC_UNSUPPORTED,
//    val isPolling: Boolean = false,
//    var isWriteMode: Boolean = false,
//    var targetWriteId: Long? = null,
//    var targetReadId: Long? = null,
//    var dataTagId: Long? = 10,
//    val dateType: String = "AES",
//    var nfcTagId: String = "0x81Ff4cac5Ad0e8E4b7D4D05bc22B4DdcB87599A3" //Temp
//) {
//}
//
//class SplashViewModel : ViewModel() {
//    private val _uiState = MutableStateFlow(SplashUiState())
//    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()
//
//    fun updateTagId(text: String) {
//        _uiState.update { currentState ->
//            currentState.copy(
//                nfcTagId = text,
//            )
//        }
//    }
//
//    fun initNFCStatus(context: Context) {
//        // init HyperRingNFC
//        HyperRingNFC.initializeHyperRingNFC(context).let {
//            _uiState.update { currentState ->
//                currentState.copy(
//                    nfcStatus = HyperRingNFC.getNFCStatus(),
//                )
//            }
//        }
//    }
//
//    private fun onDiscovered(hyperRingTag: HyperRingTag) : HyperRingTag {
//        if(_uiState.value.isWriteMode) {
//            /// Writing Data to Any HyperRing NFC TAG
//            val isWrite = HyperRingNFC.write(uiState.value.targetWriteId, hyperRingTag,
//                // Default HyperRingData
////                HyperRingData.createData(10, mutableMapOf("age" to 25, "name" to "홍길동")))
//                // Splash custom Data
//                if(_uiState.value.dateType == "AES") AESWalletHRData.createData(uiState.value.dataTagId?:10, _uiState.value.nfcTagId)
////                if(_uiState.value.dateType == "AES") AESWalletHRData.createData(uiState.value.dataTagId?:10, " 0x81Ff4cac5Ad0e8E4b7D4D05bc22B4DdcB87599A3")
//                else JWTHRData.createData(10,
//                    "John Doe", SplashActivity.jwtKey)
//            )
//
//            if(isWrite && SplashActivity.SplashActivity != null)
//                showToast(SplashActivity.SplashActivity!!, "[write] Success [${uiState.value.dataTagId}]")
//        } else {
//            if(hyperRingTag.isHyperRingTag()) {
//                Log.d("SplashActivity", "hyperRingTag.data: ${hyperRingTag.data}")
//                val readTag: HyperRingTag? = HyperRingNFC.read(uiState.value.targetReadId, hyperRingTag)
//                if(readTag != null) {
//                    if(SplashActivity.SplashActivity != null) showToast(SplashActivity.SplashActivity!!, "[read]${hyperRingTag.id}")
//                    if(_uiState.value.dateType == "AES") {
//                        val SplashNFCData = AESHRData(readTag.id, readTag.data.data)
//                        Log.d("SplashActivity", "[READ-AES] : ${SplashNFCData.data} / ${SplashNFCData.decrypt(SplashNFCData.data)}")
//                    } else {
//                        val SplashNFCData = JWTHRData(readTag.id, readTag.data.data, SplashActivity.jwtKey)
//                        Log.d("SplashActivity", "[READ-JWT]1 : ${SplashNFCData.data} / ${SplashNFCData.decrypt(SplashNFCData.data)}")
//                    }
//                }
//            }
//        }
//        return hyperRingTag
//    }
//
//    fun startPolling(context: Context) {
//        HyperRingNFC.startNFCTagPolling(
//            context as Activity, onDiscovered = :: onDiscovered).let {
//            _uiState.update { currentState ->
//                currentState.copy(
//                    isPolling = HyperRingNFC.isPolling
//                )
//            }
//        }
//    }
//
//    fun stopPolling(context: Context) {
//        HyperRingNFC.stopNFCTagPolling(context as Activity).let {
//            _uiState.update { currentState ->
//                currentState.copy(
//                    isPolling = HyperRingNFC.isPolling
//                )
//            }
//        }
//    }
//
//    fun toggleNFCMode() {
//        _uiState.update { currentState ->
//            currentState.copy(
//                isWriteMode = !_uiState.value.isWriteMode
//            )
//        }
//    }
//
//    fun setDataType(dataType: String) {
//        _uiState.update { currentState ->
//            currentState.copy(
//                dateType = dataType
//            )
//        }
//    }
//
//    fun setReadTargetId(id: Long?) {
//        _uiState.update { currentState ->
//            currentState.copy(
//                targetReadId = id
//            )
//        }
//    }
//
//    fun setWriteTargetId(id: Long?, dataId: Long) {
//        _uiState.update { currentState ->
//            currentState.copy(
//                targetWriteId = id,
//                dataTagId = dataId
//            )
//        }
//    }
//}