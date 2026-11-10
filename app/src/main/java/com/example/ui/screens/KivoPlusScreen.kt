package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KivoPlusStatus
import com.example.data.model.UserEntity
import com.example.ui.components.kivoInputTextStyle
import com.example.ui.components.kivoTextFieldColors
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CardBorderColor
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PageBackground
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueDark
import com.example.ui.theme.SoftBlueBg
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KivoPlusScreen(
    user: UserEntity?,
    onBack: () -> Unit,
    onSubmitUpgrade: (nik: String, ktpUri: String, selfieUri: String, onSuccess: () -> Unit) -> Unit,
    onShowToast: (String) -> Unit
) {
    val isKivoPlus = user?.isKivoPlus == true
    val kivoStatus = user?.kivoPlusStatus ?: KivoPlusStatus.NOT_UPGRADED

    var nikInput by remember { mutableStateOf(user?.kivoPlusNik ?: "") }
    var capturedKtpBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var ktpFileName by remember { mutableStateOf<String?>(null) }
    var capturedSelfieBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selfieFileName by remember { mutableStateOf<String?>(null) }

    val ktpCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            capturedKtpBitmap = bitmap
            ktpFileName = "ktp_${System.currentTimeMillis().toString().takeLast(6)}.jpg"
        }
    }

    val selfieCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            capturedSelfieBitmap = bitmap
            selfieFileName = "selfie_${System.currentTimeMillis().toString().takeLast(6)}.jpg"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(
                                    brush = Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA500))),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "KivoPay+",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1E3A8A)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Upgrade Akun",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    }
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
                .padding(16.dp)
        ) {
            // Hero Status Card
            if (isKivoPlus) {
                // Active KivoPay+ Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2B5C))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    listOf(Color(0xFF0F2B5C), Color(0xFF1D61E7), Color(0xFF2563EB))
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = "Star",
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Akun KivoPay+ Aktif",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        color = Color.White
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("TERVERIFIKASI", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Batas Limit Menerima Uang:",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "Rp 30.000.000",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFD700)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "NIK Terdaftar: ${user?.kivoPlusNik?.ifBlank { "31710XXXXXXXXXXX" }}",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            } else if (kivoStatus == KivoPlusStatus.PENDING_VERIFICATION) {
                // Pending Verification Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftBlueBg),
                    border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.HourglassTop,
                            contentDescription = "Pending",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Pengajuan KivoPay+ Sedang Ditinjau Admin",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimaryDark
                            )
                            Text(
                                text = "Dokumen e-KTP dan foto selfie Anda sedang diverifikasi. Limit transaksi akan otomatis ditingkatkan menjadi Rp 30.000.000 begitu disetujui.",
                                fontSize = 12.sp,
                                color = TextSecondaryMuted,
                                lineHeight = 17.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else if (kivoStatus == KivoPlusStatus.REJECTED) {
                // Rejected Alert Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CrimsonError.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, CrimsonError.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Rejected",
                            tint = CrimsonError,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Pengajuan KivoPay+ Ditolak",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimaryDark
                            )
                            Text(
                                text = "Alasan: ${user?.kivoPlusRejectionReason?.ifBlank { "Foto e-KTP tidak jelas / NIK tidak cocok" }}. Silakan ambil ulang foto e-KTP yang terang dan tajam.",
                                fontSize = 12.sp,
                                color = CrimsonError,
                                lineHeight = 17.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Comparison Table Card (Regular vs KivoPay+)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, CardBorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Perbandingan Benefit Akun",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimaryDark
                    )
                    Text(
                        text = "Nikmati batas penerimaan dana hingga Rp 30 Juta dan fitur prioritas",
                        fontSize = 12.sp,
                        color = TextSecondaryMuted,
                        modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                    )

                    // Header row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SoftBlueBg, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Fitur & Limit",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlueDark,
                            modifier = Modifier.weight(1.8f)
                        )
                        Text(
                            text = "Standar",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondaryMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1.2f)
                        )
                        Text(
                            text = "KivoPay+",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1.4f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Benefit 1: Limit Menerima Uang
                    BenefitRow(
                        feature = "Batas Limit Menerima Uang",
                        standardValue = "Rp 4.000.000",
                        plusValue = "Rp 30.000.000",
                        isHighlight = true
                    )

                    // Benefit 2: Penahanan Saldo Lebih
                    BenefitRow(
                        feature = "Pencairan Saldo Tertahan",
                        standardValue = "Tertahan jika >4Jt",
                        plusValue = "Otomatis Cair s/d 30Jt",
                        isHighlight = false
                    )

                    // Benefit 3: Bebas Transfer Antar Bank & Sesama
                    BenefitCheckRow(
                        feature = "Transfer ke Sesama Bebas Biaya",
                        standardSupported = true,
                        plusSupported = true
                    )

                    // Benefit 4: Tarik Dana ke Rekening Pribadi
                    BenefitCheckRow(
                        feature = "Tarik Dana ke Bank Pribadi",
                        standardSupported = true,
                        plusSupported = true
                    )

                    // Benefit 5: Saldo & Transaksi Prioritas
                    BenefitCheckRow(
                        feature = "Prioritas Layanan Customer Support",
                        standardSupported = false,
                        plusSupported = true
                    )

                    // Benefit 6: Akses QRIS Merchant Eksklusif
                    BenefitCheckRow(
                        feature = "Akses Prioritas Fitur QRIS Merchant",
                        standardSupported = false,
                        plusSupported = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Registration / Resubmission Form (if not yet upgraded or if rejected)
            if (!isKivoPlus && kivoStatus != KivoPlusStatus.PENDING_VERIFICATION) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, CardBorderColor)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = "Security", tint = PrimaryBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Formulir Upgrade KivoPay+",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimaryDark
                            )
                        }

                        Text(
                            text = "Wajib melampirkan NIK dan foto e-KTP asli melalui kamera langsung demi keamanan regulasi Bank Sentral.",
                            fontSize = 12.sp,
                            color = TextSecondaryMuted,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                        )

                        // NIK Input
                        Text(
                            text = "Nomor Induk Kependudukan (NIK 16 Digit) *",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = TextPrimaryDark,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = nikInput,
                            onValueChange = { if (it.length <= 16 && it.all { char -> char.isDigit() }) nikInput = it },
                            placeholder = { Text("Contoh: 3171012345670001", color = TextSecondaryMuted, fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            textStyle = kivoInputTextStyle(),
                            colors = kivoTextFieldColors(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Camera Upload 1: Foto e-KTP
                        Text(
                            text = "1. Foto e-KTP Asli (Kamera Langsung) *",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = TextPrimaryDark,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        if (capturedKtpBitmap != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, PrimaryBlue, RoundedCornerShape(12.dp))
                            ) {
                                Image(
                                    bitmap = capturedKtpBitmap!!.asImageBitmap(),
                                    contentDescription = "Foto e-KTP",
                                    modifier = Modifier.fillMaxSize()
                                )
                                Surface(
                                    color = Color(0x99000000),
                                    shape = RoundedCornerShape(bottomStart = 12.dp),
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .clickable { ktpCameraLauncher.launch(null) }
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Retake", tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Ulangi", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = { ktpCameraLauncher.launch(null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, PrimaryBlue),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = SoftBlueBg)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = PrimaryBlue)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ambil Foto e-KTP via Kamera", color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Camera Upload 2: Foto Selfie memegang e-KTP
                        Text(
                            text = "2. Foto Selfie Memegang e-KTP *",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = TextPrimaryDark,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        if (capturedSelfieBitmap != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, PrimaryBlue, RoundedCornerShape(12.dp))
                            ) {
                                Image(
                                    bitmap = capturedSelfieBitmap!!.asImageBitmap(),
                                    contentDescription = "Foto Selfie e-KTP",
                                    modifier = Modifier.fillMaxSize()
                                )
                                Surface(
                                    color = Color(0x99000000),
                                    shape = RoundedCornerShape(bottomStart = 12.dp),
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .clickable { selfieCameraLauncher.launch(null) }
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Retake", tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Ulangi", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = { selfieCameraLauncher.launch(null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, PrimaryBlue),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = SoftBlueBg)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = PrimaryBlue)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ambil Foto Selfie Memegang e-KTP", color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                if (nikInput.length != 16) {
                                    onShowToast("Nomor NIK harus 16 digit sesuai e-KTP")
                                    return@Button
                                }
                                if (capturedKtpBitmap == null || capturedSelfieBitmap == null) {
                                    onShowToast("Wajib mengambil kedua foto dokumen via kamera langsung")
                                    return@Button
                                }

                                val ktpUri = ktpFileName ?: "ktp_${System.currentTimeMillis()}.jpg"
                                val selfieUri = selfieFileName ?: "selfie_${System.currentTimeMillis()}.jpg"

                                onSubmitUpgrade(nikInput, ktpUri, selfieUri) {
                                    onShowToast("Pengajuan KivoPay+ berhasil dikirim ke Admin!")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_submit_kivoplus"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Kirim Pengajuan KivoPay+",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BenefitRow(
    feature: String,
    standardValue: String,
    plusValue: String,
    isHighlight: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = feature,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimaryDark,
            modifier = Modifier.weight(1.8f)
        )
        Text(
            text = standardValue,
            fontSize = 11.sp,
            color = TextSecondaryMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1.2f)
        )
        Text(
            text = plusValue,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isHighlight) PrimaryBlue else EmeraldSuccess,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1.4f)
        )
    }
}

@Composable
fun BenefitCheckRow(
    feature: String,
    standardSupported: Boolean,
    plusSupported: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = feature,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimaryDark,
            modifier = Modifier.weight(1.8f)
        )
        Box(
            modifier = Modifier.weight(1.2f),
            contentAlignment = Alignment.Center
        ) {
            if (standardSupported) {
                Icon(Icons.Default.Check, contentDescription = "Supported", tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
            } else {
                Icon(Icons.Default.Close, contentDescription = "Not supported", tint = CrimsonError.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
            }
        }
        Box(
            modifier = Modifier.weight(1.4f),
            contentAlignment = Alignment.Center
        ) {
            if (plusSupported) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Supported", tint = PrimaryBlue, modifier = Modifier.size(20.dp))
            } else {
                Icon(Icons.Default.Close, contentDescription = "Not supported", tint = CrimsonError, modifier = Modifier.size(18.dp))
            }
        }
    }
}
