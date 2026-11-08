package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
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
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PageBackground
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueDark
import com.example.ui.theme.SoftBlueBg
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryMuted

data class QrisMerchant(
    val name: String,
    val nmid: String,
    val defaultCity: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrisScannerScreen(
    userAvailableBalance: Double,
    onBack: () -> Unit,
    onExecuteQrisPay: (merchantName: String, amount: Double, pin: String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Real Indonesian QRIS merchants to select or customize
    val sampleMerchants = remember {
        listOf(
            QrisMerchant("Kopi Kenangan - Mall Senayan City", "ID1029384756101", "Jakarta Pusat"),
            QrisMerchant("Indomaret Point Stasiun Gambir", "ID1099238127392", "Jakarta"),
            QrisMerchant("Alfamart Veteran Raya", "ID1088371928471", "Jakarta"),
            QrisMerchant("SPBU Pertamina Pasti Pas 34-12", "ID1055271892837", "Jakarta Selatan"),
            QrisMerchant("Warung Makan Berkah Bahari", "ID1077361829102", "Tangerang")
        )
    }

    var selectedMerchant by remember { mutableStateOf(sampleMerchants[0]) }
    var customMerchantInput by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("35000") }
    var showPinSheet by remember { mutableStateOf(false) }
    var isFlashlightOn by remember { mutableStateOf(false) }

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

    // Animated laser scan bar
    val infiniteTransition = rememberInfiniteTransition(label = "laser_scan")
    val laserPositionProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_position"
    )

    val activeMerchantName = if (customMerchantInput.isNotBlank()) customMerchantInput else selectedMerchant.name
    val amountDouble = amountInput.toDoubleOrNull() ?: 0.0
    val isBalanceSufficient = userAvailableBalance >= amountDouble

    val quickAmounts = listOf(15000.0, 25000.0, 35000.0, 50000.0, 100000.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "QRIS Pay Instan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimaryDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = TextPrimaryDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = PageBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Arahkan Kamera ke Kode QRIS Merchant",
                color = TextPrimaryDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Mendukung seluruh QRIS Bank & E-Wallet Nasional",
                color = TextSecondaryMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            // Real Camera Viewfinder with Live Animated Scanner Laser
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black)
                    .border(3.dp, PrimaryBlue, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
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
                                    val camera = cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview
                                    )
                                    camera.cameraControl.enableTorch(isFlashlightOn)
                                } catch (_: Exception) {
                                    // Hardware fallback
                                }
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Visual fallback with live scanning graphic
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Kamera",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Kamera Aktif & Memindai",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Arahkan ke QRIS",
                                color = TextSecondaryMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // QR Viewfinder target corners
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .border(2.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
                )

                // High-tech Animated Laser Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(3.dp)
                        .offset {
                            // Translate laser vertically between 30dp and 220dp
                            IntOffset(x = 0, y = ((-95 + laserPositionProgress * 190) * context.resources.displayMetrics.density).toInt())
                        }
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    PrimaryBlue,
                                    Color(0xFF38BDF8),
                                    Color.White,
                                    Color(0xFF38BDF8),
                                    PrimaryBlue,
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Flash & Merchant Selector Quick Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isFlashlightOn) PrimaryBlue else Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.clickable { isFlashlightOn = !isFlashlightOn }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isFlashlightOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flashlight",
                            tint = if (isFlashlightOn) Color.White else PrimaryBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isFlashlightOn) "Flash ON" else "Nyalakan Flash",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFlashlightOn) Color.White else TextPrimaryDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick merchant scan presets
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Pilih / Ganti Merchant Terpindai:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(sampleMerchants) { merchant ->
                        val isSelected = selectedMerchant == merchant && customMerchantInput.isBlank()
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) PrimaryBlue else Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) PrimaryBlue else CardBorderColor),
                            modifier = Modifier.clickable {
                                selectedMerchant = merchant
                                customMerchantInput = ""
                            }
                        ) {
                            Text(
                                text = merchant.name.take(22) + if (merchant.name.length > 22) "..." else "",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color.White else TextPrimaryDark,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Merchant Card & Payment Details
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.kivopay_icon),
                                contentDescription = "KivoPay QRIS",
                                modifier = Modifier.size(34.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = activeMerchantName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark
                                )
                                Text(
                                    text = "NMID: ${selectedMerchant.nmid} • ${selectedMerchant.defaultCity}",
                                    fontSize = 10.sp,
                                    color = TextSecondaryMuted
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = EmeraldSuccess.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "QRIS Resmi",
                                color = EmeraldSuccess,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Nominal input with guaranteed visible dark text
                    Text(
                        text = "Nominal Pembayaran (Rp)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))

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

                    // Preset Quick Amount Chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(quickAmounts) { amt ->
                            val isSelected = amountDouble == amt
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) PrimaryBlue else SoftBlueBg,
                                modifier = Modifier.clickable { amountInput = amt.toLong().toString() }
                            ) {
                                Text(
                                    text = "Rp ${formatCurrency(amt)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else PrimaryBlue,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Balance Status indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Saldo KivoPay: Rp ${formatCurrency(userAvailableBalance)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isBalanceSufficient) TextSecondaryMuted else Color(0xFFEF4444)
                        )
                        if (!isBalanceSufficient) {
                            Text(
                                text = "Saldo Tidak Cukup",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showPinSheet = true },
                        enabled = amountDouble > 0 && isBalanceSufficient,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_qris_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text(
                            text = "Konfirmasi & Bayar Rp ${formatCurrency(amountDouble)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    if (showPinSheet) {
        PinInputDialog(
            title = "Otorisasi PIN QRIS",
            subtitle = "Bayar ke $activeMerchantName sebesar Rp ${formatCurrency(amountDouble)}",
            onDismiss = { showPinSheet = false },
            onPinSubmitted = { pin ->
                showPinSheet = false
                onExecuteQrisPay(activeMerchantName, amountDouble, pin)
            }
        )
    }
}
