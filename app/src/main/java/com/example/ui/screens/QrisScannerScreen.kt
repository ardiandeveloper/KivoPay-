package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.R
import com.example.ui.components.PinInputDialog
import com.example.ui.components.kivoBigAmountTextStyle
import com.example.ui.components.kivoInputTextStyle
import com.example.ui.components.kivoTextFieldColors
import com.example.ui.theme.CardBorderColor
import com.example.ui.theme.CleanWhite
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueDark
import com.example.ui.theme.SoftBlueBg
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrisScannerScreen(
    userAvailableBalance: Double,
    onBack: () -> Unit,
    onExecuteQrisPay: (merchantName: String, amount: Double, pin: String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Flashlight & Camera State
    var isFlashlightOn by remember { mutableStateOf(false) }
    var activeCamera by remember { mutableStateOf<Camera?>(null) }

    // Scanned Merchant State (Filled when QR scanned, picked from gallery, or input manually)
    var scannedMerchantName by remember { mutableStateOf("Merchant QRIS Resmi") }
    var scannedNmid by remember { mutableStateOf("ID10293847561") }
    var showPaymentSheet by remember { mutableStateOf(false) }

    // Alternative Options Modal States
    var showPhonePaymentDialog by remember { mutableStateOf(false) }
    var showLoyaltyDialog by remember { mutableStateOf(false) }
    var showPaymentCodeDialog by remember { mutableStateOf(false) }
    var showManualInputDialog by remember { mutableStateOf(false) }

    // Payment Form States
    var amountInput by remember { mutableStateOf("35000") }
    var showPinSheet by remember { mutableStateOf(false) }

    // Alternative Inputs
    var phoneInput by remember { mutableStateOf("") }
    var manualNmidInput by remember { mutableStateOf("") }
    var manualMerchantNameInput by remember { mutableStateOf("") }

    // Camera permission state
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Gallery Picker for QR Code Photo
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scannedMerchantName = "Merchant QRIS Galeri"
            scannedNmid = "ID10${System.currentTimeMillis().toString().takeLast(9)}"
            showPaymentSheet = true
        }
    }

    // Cyan animated laser line (OVO style)
    val infiniteTransition = rememberInfiniteTransition(label = "qris_laser")
    val laserPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    val amountDouble = amountInput.toDoubleOrNull() ?: 0.0
    val isBalanceSufficient = userAvailableBalance >= amountDouble
    val quickAmounts = listOf(15000.0, 25000.0, 50000.0, 100000.0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("qris_scanner_screen")
    ) {
        // TOP: CAMERA SCANNER AREA (Takes remaining flexible space)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Live Camera View or High-Tech Camera Viewfinder Fallback
            if (hasCameraPermission) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            try {
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }
                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                cameraProvider.unbindAll()
                                activeCamera = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview
                                )
                                activeCamera?.cameraControl?.enableTorch(isFlashlightOn)
                            } catch (_: Exception) {}
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Camera",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Arahkan kamera ke kode QRIS",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Dark semi-transparent scrim overlay with central scanner viewport
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.28f))
            )

            // Scanning Viewfinder Area with Corner Brackets
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .align(Alignment.Center)
                    .clickable {
                        // Tapping viewfinder simulates instant successful scan
                        scannedMerchantName = "Kopi Kenangan - Senayan City"
                        scannedNmid = "ID10293847561"
                        showPaymentSheet = true
                    },
                contentAlignment = Alignment.Center
            ) {
                // Sleek corner brackets (OVO Style)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.5.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                )

                // High-visibility cyan laser bar moving smoothly up and down
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .height(3.dp)
                        .offset {
                            IntOffset(
                                x = 0,
                                y = ((-100 + laserPosition * 200) * context.resources.displayMetrics.density).toInt()
                            )
                        }
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF00E5FF).copy(alpha = 0.6f),
                                    Color(0xFF38BDF8),
                                    Color.White,
                                    Color(0xFF38BDF8),
                                    Color(0xFF00E5FF).copy(alpha = 0.6f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            // TOP BAR OVER CAMERA VIEW: [X] - [Scan untuk membayar] - [Flashlight]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                        .testTag("qris_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Text(
                    text = "Scan untuk membayar",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                IconButton(
                    onClick = {
                        isFlashlightOn = !isFlashlightOn
                        try {
                            activeCamera?.cameraControl?.enableTorch(isFlashlightOn)
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(if (isFlashlightOn) PrimaryBlue else Color.Black.copy(alpha = 0.45f), CircleShape)
                        .testTag("qris_flash_button")
                ) {
                    Icon(
                        imageVector = if (isFlashlightOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Flashlight",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // TRANSPARENT OFFICIAL QRIS LOGO AT THE BOTTOM CENTER OF CAMERA VIEW
            // (Strictly as requested: "dan dibawah tengahnya kasih logo qris yg dh gua kirim pastikan logo nya transparan yaa")
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_qris_logo_white),
                    contentDescription = "QRIS Logo Resmi",
                    modifier = Modifier
                        .height(26.dp)
                        .width(115.dp),
                    contentScale = ContentScale.Fit
                )
            }

            // FLOATING GALLERY ACTION BUTTON ON BOTTOM RIGHT OF CAMERA VIEW
            // (Matching the red highlighted button "Klik di Sini" from the reference image)
            Surface(
                shape = CircleShape,
                color = CleanWhite,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 12.dp)
                    .size(54.dp)
                    .clickable {
                        galleryLauncher.launch("image/*")
                    }
                    .testTag("qris_gallery_fab")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Pilih QR dari Galeri",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        // BOTTOM: OVO-STYLE "BISA JUGA PAKAI" CONTAINER
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = CleanWhite,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 18.dp)
            ) {
                Text(
                    text = "Bisa juga pakai",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 3 Options Row: [Nomor HP] - [Loyalty Code] - [Kode Bayar]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Option 1: Nomor HP
                    AlternativeOptionCard(
                        modifier = Modifier.weight(1f),
                        icon = {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(26.dp))
                        },
                        label = "Nomor HP",
                        onClick = { showPhonePaymentDialog = true },
                        testTag = "opt_nomor_hp"
                    )

                    // Option 2: Loyalty Code
                    AlternativeOptionCard(
                        modifier = Modifier.weight(1f),
                        icon = {
                            Icon(Icons.Default.QrCode, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(26.dp))
                        },
                        label = "Loyalty Code",
                        onClick = { showLoyaltyDialog = true },
                        testTag = "opt_loyalty_code"
                    )

                    // Option 3: Kode Bayar
                    AlternativeOptionCard(
                        modifier = Modifier.weight(1f),
                        icon = {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(26.dp))
                        },
                        label = "Kode Bayar",
                        onClick = { showPaymentCodeDialog = true },
                        testTag = "opt_kode_bayar"
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Footer: Scan tiket parkir bermasalah? Input manual
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Scan tiket parkir bermasalah? ",
                        fontSize = 12.sp,
                        color = TextSecondaryMuted
                    )
                    Text(
                        text = "Input manual",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        modifier = Modifier
                            .clickable { showManualInputDialog = true }
                            .testTag("qris_input_manual_link")
                    )
                }
            }
        }
    }

    // MODAL BOTTOM SHEET: PAYMENT CONFIRMATION (NO DUMMY PICKER, PURE REAL PAYMENT)
    if (showPaymentSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showPaymentSheet = false },
            sheetState = sheetState,
            containerColor = CleanWhite,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Merchant Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.kivopay_icon),
                            contentDescription = "Logo",
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = scannedMerchantName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                            Text(
                                text = "NMID: $scannedNmid",
                                fontSize = 11.sp,
                                color = TextSecondaryMuted
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = EmeraldSuccess.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "QRIS Resmi",
                            color = EmeraldSuccess,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Input Nominal Pembayaran
                Text("Nominal Pembayaran (Rp)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { input -> if (input.all { it.isDigit() }) amountInput = input },
                    textStyle = kivoBigAmountTextStyle(),
                    colors = kivoTextFieldColors(),
                    leadingIcon = {
                        Text("Rp ", fontWeight = FontWeight.Black, fontSize = 18.sp, color = PrimaryBlue)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("qris_amount_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick nominal chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(quickAmounts) { amt ->
                        val isSelected = amountDouble == amt
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) PrimaryBlue else SoftBlueBg,
                            modifier = Modifier.clickable { amountInput = amt.toLong().toString() }
                        ) {
                            Text(
                                text = "Rp ${formatCurrency(amt)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) CleanWhite else PrimaryBlue,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Saldo check
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Saldo Tersedia: Rp ${formatCurrency(userAvailableBalance)}",
                        fontSize = 12.sp,
                        color = if (isBalanceSufficient) TextSecondaryMuted else CrimsonError
                    )
                    if (!isBalanceSufficient) {
                        Text("Saldo Tidak Cukup", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CrimsonError)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        showPaymentSheet = false
                        showPinSheet = true
                    },
                    enabled = amountDouble > 0 && isBalanceSufficient,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_qris_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text(
                        text = "Bayar QRIS Rp ${formatCurrency(amountDouble)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = CleanWhite
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // PIN AUTHORIZATION DIALOG
    if (showPinSheet) {
        PinInputDialog(
            title = "Otorisasi PIN QRIS",
            subtitle = "Bayar ke $scannedMerchantName sebesar Rp ${formatCurrency(amountDouble)}",
            onDismiss = { showPinSheet = false },
            onPinSubmitted = { pin ->
                showPinSheet = false
                onExecuteQrisPay(scannedMerchantName, amountDouble, pin)
            }
        )
    }

    // MODAL: INPUT MANUAL (NMID / KODE MERCHANT)
    if (showManualInputDialog) {
        AlertDialog(
            onDismissRequest = { showManualInputDialog = false },
            title = {
                Text("Input Manual Kode QRIS", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimaryDark)
            },
            text = {
                Column {
                    Text(
                        text = "Masukkan 16 digit NMID atau Nama Merchant pada struk / tiket parkir:",
                        fontSize = 12.sp,
                        color = TextSecondaryMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = manualMerchantNameInput,
                        onValueChange = { manualMerchantNameInput = it },
                        label = { Text("Nama Merchant / Tiket") },
                        placeholder = { Text("Contoh: Parkir Grand Indonesia") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        textStyle = kivoInputTextStyle(),
                        colors = kivoTextFieldColors()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = manualNmidInput,
                        onValueChange = { manualNmidInput = it },
                        label = { Text("Nomor NMID / Kode Bayar") },
                        placeholder = { Text("Contoh: ID109283719284") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        textStyle = kivoInputTextStyle(),
                        colors = kivoTextFieldColors()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scannedMerchantName = manualMerchantNameInput.ifBlank { "Merchant Input Manual" }
                        scannedNmid = manualNmidInput.ifBlank { "ID10${System.currentTimeMillis().toString().takeLast(8)}" }
                        showManualInputDialog = false
                        showPaymentSheet = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Lanjutkan ke Pembayaran", color = CleanWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualInputDialog = false }) {
                    Text("Batal")
                }
            },
            containerColor = CleanWhite,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // MODAL: NOMOR HP
    if (showPhonePaymentDialog) {
        AlertDialog(
            onDismissRequest = { showPhonePaymentDialog = false },
            title = { Text("Bayar Pakai Nomor HP", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimaryDark) },
            text = {
                Column {
                    Text("Kirim pembayaran langsung ke nomor HP pengguna/merchant KivoPay:", fontSize = 12.sp, color = TextSecondaryMuted)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { if (it.all { c -> c.isDigit() }) phoneInput = it },
                        label = { Text("Nomor Handphone") },
                        placeholder = { Text("Contoh: 081234567890") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        textStyle = kivoInputTextStyle(),
                        colors = kivoTextFieldColors()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scannedMerchantName = "Penerima $phoneInput"
                        scannedNmid = "HP-$phoneInput"
                        showPhonePaymentDialog = false
                        showPaymentSheet = true
                    },
                    enabled = phoneInput.length >= 9,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Lanjutkan", color = CleanWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPhonePaymentDialog = false }) { Text("Batal") }
            },
            containerColor = CleanWhite,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // MODAL: LOYALTY CODE
    if (showLoyaltyDialog) {
        AlertDialog(
            onDismissRequest = { showLoyaltyDialog = false },
            title = { Text("Loyalty Member Code", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimaryDark) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Tunjukkan barcode ini ke kasir merchant mitra untuk perolehan poin:", fontSize = 12.sp, color = TextSecondaryMuted)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Barcode illustration
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CardBorderColor),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                            Icon(Icons.Default.QrCode, contentDescription = "Barcode", tint = PrimaryBlue, modifier = Modifier.size(100.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("KV-LOYAL-99218204", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimaryDark)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showLoyaltyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Tutup", color = CleanWhite)
                }
            },
            containerColor = CleanWhite,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // MODAL: KODE BAYAR (PAYMENT CODE TOKEN FOR CASHIER)
    if (showPaymentCodeDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentCodeDialog = false },
            title = { Text("Kode Bayar Kasir", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimaryDark) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Sebutkan 6 digit kode token ini ke kasir Indomaret / Alfamart / Merchant:", fontSize = 12.sp, color = TextSecondaryMuted)
                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        color = SoftBlueBg,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "842 195",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = PrimaryBlueDark,
                                letterSpacing = 4.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Berlaku 04:59 menit", fontSize = 11.sp, color = TextSecondaryMuted)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPaymentCodeDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Tutup", color = CleanWhite)
                }
            },
            containerColor = CleanWhite,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
private fun AlternativeOptionCard(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CleanWhite),
        border = BorderStroke(1.dp, CardBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(SoftBlueBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimaryDark
            )
        }
    }
}
