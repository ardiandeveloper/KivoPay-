package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BankChangeRequestEntity
import com.example.data.model.BankChangeStatus
import com.example.data.model.IndonesianBanks
import com.example.data.model.UserEntity
import com.example.ui.components.kivoInputTextStyle
import com.example.ui.components.kivoTextFieldColors
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CardBorderColor
import com.example.ui.theme.CleanWhite
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
fun ChangeBankAccountScreen(
    user: UserEntity?,
    activeRequests: List<BankChangeRequestEntity>,
    onBack: () -> Unit,
    onSubmitRequest: (
        newBank: String,
        newAccountNo: String,
        newAccountHolder: String,
        reason: String,
        ktpUri: String,
        selfieKtpUri: String,
        onSuccess: () -> Unit
    ) -> Unit,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val latestPending = activeRequests.firstOrNull { it.status == BankChangeStatus.PENDING_CS }
    val latestRejected = activeRequests.firstOrNull { it.status == BankChangeStatus.REJECTED }

    // Form states
    var bankSearchQuery by remember { mutableStateOf("") }
    var selectedBank by remember { mutableStateOf("Bank Central Asia (BCA)") }
    var bankDropdownExpanded by remember { mutableStateOf(false) }

    var newAccountNo by remember { mutableStateOf("") }
    var newAccountHolder by remember { mutableStateOf(user?.fullName ?: "") }
    var changeReasonCategory by remember { mutableStateOf("Rekening lama ditutup / tidak aktif") }
    var changeReasonCustom by remember { mutableStateOf("") }
    var agreementChecked by remember { mutableStateOf(false) }

    // Direct Camera Captures (No gallery, for regulatory security)
    var capturedKtpBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var ktpFileName by remember { mutableStateOf<String?>(null) }

    var capturedSelfieBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selfieFileName by remember { mutableStateOf<String?>(null) }

    // Success CS Dialog
    var showCsInstructionDialog by remember { mutableStateOf(false) }

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
            selfieFileName = "selfie_ktp_${System.currentTimeMillis().toString().takeLast(6)}.jpg"
        }
    }

    val filteredBanks = remember(bankSearchQuery) {
        if (bankSearchQuery.isBlank()) {
            IndonesianBanks.allBanks
        } else {
            IndonesianBanks.allBanks.filter { it.contains(bankSearchQuery, ignoreCase = true) }
        }
    }

    val standardReasons = listOf(
        "Rekening lama ditutup / tidak aktif",
        "Pindah ke rekening gaji (payroll) utama",
        "Buku tabungan / kartu ATM lama hilang",
        "Mengganti ke bank digital yang lebih sering digunakan",
        "Alasan lainnya"
    )

    fun openCustomerServiceWhatsApp() {
        val csNumber = "+6281288880099"
        val message = "Halo Customer Service KivoPay, saya ${user?.fullName} (${user?.phoneNumber}) telah mengajukan permohonan perubahan rekening bank terdaftar di akun saya. Mohon bantuan panduan tahap verifikasi identitas lanjutan. Terima kasih."
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://wa.me/6281288880099?text=${Uri.encode(message)}")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("CS KivoPay", "081288880099"))
            onShowToast("Nomor CS KivoPay (+62 812-8888-0099) berhasil disalin!")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ubah Rekening Bank Resmi",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CleanWhite)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(PageBackground)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // IF A REQUEST IS ACTIVELY PENDING CS CONFIRMATION
            if (latestPending != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CleanWhite),
                    border = BorderStroke(1.5.dp, AmberWarning)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(AmberWarning.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = "Pending", tint = AmberWarning, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Permohonan Dalam Peninjauan",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark
                                )
                                Text(
                                    text = "Kode Tiket: ${latestPending.id}",
                                    fontSize = 11.sp,
                                    color = TextSecondaryMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // High Priority Customer Service Box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SoftBlueBg,
                            border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.HeadsetMic, contentDescription = "CS", tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Wajib Hubungi Customer Service (CS)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlueDark
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Demi mencegah kejahatan perbankan & pengambilalihan akun (account takeover), Anda diwajibkan melakukan konfirmasi identitas tatap muka/telepon ke Customer Service resmi KivoPay sebelum permohonan disetujui Admin.",
                                    fontSize = 12.sp,
                                    color = TextPrimaryDark,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { openCustomerServiceWhatsApp() },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.HeadsetMic, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Chat CS Sekarang", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("CS KivoPay", "+62 812-8888-0099"))
                                            onShowToast("Nomor CS KivoPay disalin!")
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Rincian Rekening Baru yang Diajukan:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                        Spacer(modifier = Modifier.height(6.dp))
                        DetailRow("Bank Baru", latestPending.newBankName)
                        DetailRow("Nomor Rekening", latestPending.newAccountNumber)
                        DetailRow("Nama Pemilik", latestPending.newAccountHolder)
                        DetailRow("Alasan", latestPending.changeReason)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // IF PREVIOUS REQUEST WAS REJECTED
            if (latestRejected != null && latestPending == null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CrimsonError.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, CrimsonError.copy(alpha = 0.3f))
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Warning, contentDescription = "Ditolak", tint = CrimsonError, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Permohonan Terakhir Ditolak Admin", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CrimsonError)
                            Text(
                                text = "Alasan: ${latestRejected.rejectionReason.ifBlank { "Dokumen foto identitas atau e-KTP tidak terbaca jelas." }}",
                                fontSize = 12.sp,
                                color = TextPrimaryDark,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Text(
                                text = "Silakan isi kembali formulir di bawah ini dengan dokumen e-KTP dan foto selfie yang jelas.",
                                fontSize = 11.sp,
                                color = TextSecondaryMuted,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // Current Registered Bank Card
            Text(
                text = "1. Rekening Resmi Terdaftar Saat Ini",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
            Spacer(modifier = Modifier.height(6.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CleanWhite),
                border = BorderStroke(1.dp, CardBorderColor)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(SoftBlueBg, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AccountBalance, contentDescription = "Bank", tint = PrimaryBlue)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = user?.userBankName?.ifBlank { "Bank Central Asia (BCA)" } ?: "Bank Central Asia (BCA)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.Lock, contentDescription = "Terkunci", tint = PrimaryBlue, modifier = Modifier.size(14.dp))
                        }
                        Text(
                            text = "No. Rekening: ${user?.userBankAccountNumber?.ifBlank { "1234567890" }}",
                            fontSize = 12.sp,
                            color = TextSecondaryMuted
                        )
                        Text(
                            text = "Nama: ${user?.userBankAccountHolder?.ifBlank { user?.fullName } ?: user?.fullName}",
                            fontSize = 12.sp,
                            color = TextSecondaryMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section 2: Form Input Rekening Baru
            Text(
                text = "2. Data Rekening Bank Baru",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
            Text(
                text = "Pastikan nama pemilik rekening baru sesuai dengan identitas pemilik akun.",
                fontSize = 11.sp,
                color = TextSecondaryMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Bank Selector Dropdown from all Indonesian Banks
            ExposedDropdownMenuBox(
                expanded = bankDropdownExpanded,
                onExpandedChange = { bankDropdownExpanded = !bankDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = selectedBank,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Pilih Bank Tujuan Baru") },
                    leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = "Bank", tint = PrimaryBlue) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bankDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .testTag("new_bank_selector"),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = kivoInputTextStyle(),
                    colors = kivoTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = bankDropdownExpanded,
                    onDismissRequest = { bankDropdownExpanded = false },
                    modifier = Modifier.height(300.dp)
                ) {
                    // Search bar inside dropdown
                    OutlinedTextField(
                        value = bankSearchQuery,
                        onValueChange = { bankSearchQuery = it },
                        placeholder = { Text("Cari nama bank...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryBlue) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        singleLine = true,
                        textStyle = kivoInputTextStyle(),
                        colors = kivoTextFieldColors()
                    )

                    if (bankSearchQuery.isNotBlank() && !IndonesianBanks.allBanks.any { it.equals(bankSearchQuery, ignoreCase = true) }) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Gunakan Bank Kustom: \"$bankSearchQuery\"", color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            },
                            onClick = {
                                selectedBank = bankSearchQuery.trim()
                                bankDropdownExpanded = false
                            }
                        )
                    }

                    filteredBanks.take(80).forEach { bank ->
                        DropdownMenuItem(
                            text = { Text(bank, color = TextPrimaryDark, fontSize = 13.sp) },
                            onClick = {
                                selectedBank = bank
                                bankDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = newAccountNo,
                onValueChange = { input -> if (input.all { it.isDigit() }) newAccountNo = input },
                label = { Text("Nomor Rekening Baru") },
                placeholder = { Text("Contoh: 8271928371") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("new_account_number_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                textStyle = kivoInputTextStyle(),
                colors = kivoTextFieldColors(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = newAccountHolder,
                onValueChange = { newAccountHolder = it },
                label = { Text("Nama Pemilik Rekening Baru (Sesuai KTP)") },
                placeholder = { Text("Nama lengkap pemilik rekening") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("new_account_holder_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                textStyle = kivoInputTextStyle(),
                colors = kivoTextFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Alasan Pengajuan
            Text("Alasan Pengajuan Perubahan:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
            Spacer(modifier = Modifier.height(4.dp))
            standardReasons.forEach { reason ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { changeReasonCategory = reason }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.RadioButton(
                        selected = changeReasonCategory == reason,
                        onClick = { changeReasonCategory = reason }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(reason, fontSize = 13.sp, color = TextPrimaryDark)
                }
            }

            if (changeReasonCategory == "Alasan lainnya") {
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = changeReasonCustom,
                    onValueChange = { changeReasonCustom = it },
                    label = { Text("Tuliskan alasan lengkap...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = kivoInputTextStyle(),
                    colors = kivoTextFieldColors()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 3: Unggah Bukti Identitas (e-KTP & Selfie) - Strictly Direct Camera
            Text(
                text = "3. Bukti Verifikasi Identitas Resmi",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
            Text(
                text = "Demi standar keamanan perlindungan konsumen, dokumen wajib difoto langsung menggunakan kamera perangkat.",
                fontSize = 11.sp,
                color = TextSecondaryMuted,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // KTP Upload Card
            DocumentCaptureCard(
                title = "1. Foto e-KTP Asli Pemilik Akun",
                instruction = "Pastikan seluruh teks NIK, nama, dan foto terlihat jelas tanpa pantulan kilap cahaya.",
                capturedBitmap = capturedKtpBitmap,
                fileName = ktpFileName,
                onCapture = { ktpCameraLauncher.launch(null) },
                testTag = "capture_ktp_button"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Selfie with KTP Upload Card
            DocumentCaptureCard(
                title = "2. Foto Selfie Memegang e-KTP",
                instruction = "Posisikan wajah dan e-KTP Anda di depan kamera dengan pencahayaan yang terang.",
                capturedBitmap = capturedSelfieBitmap,
                fileName = selfieFileName,
                onCapture = { selfieCameraLauncher.launch(null) },
                testTag = "capture_selfie_button"
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Legal Agreement Checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { agreementChecked = !agreementChecked }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = agreementChecked,
                    onCheckedChange = { agreementChecked = it },
                    colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Saya menyatakan bahwa data rekening bank dan identitas yang dilampirkan adalah benar, sah, dan milik saya pribadi.",
                    fontSize = 12.sp,
                    color = TextPrimaryDark,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button
            val finalReason = if (changeReasonCategory == "Alasan lainnya") changeReasonCustom else changeReasonCategory
            val isFormValid = newAccountNo.isNotBlank() &&
                    newAccountHolder.isNotBlank() &&
                    capturedKtpBitmap != null &&
                    capturedSelfieBitmap != null &&
                    agreementChecked &&
                    finalReason.isNotBlank()

            Button(
                onClick = {
                    if (!isFormValid) {
                        onShowToast("Mohon lengkapi seluruh kolom formulir dan foto e-KTP serta selfie.")
                        return@Button
                    }
                    onSubmitRequest(
                        selectedBank,
                        newAccountNo,
                        newAccountHolder,
                        finalReason,
                        ktpFileName ?: "ktp.jpg",
                        selfieFileName ?: "selfie_ktp.jpg"
                    ) {
                        showCsInstructionDialog = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_bank_change_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                enabled = isFormValid
            ) {
                Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Kirim Pengajuan Resmi",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = CleanWhite
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Official Post-Submission Modal
    if (showCsInstructionDialog) {
        AlertDialog(
            onDismissRequest = {
                showCsInstructionDialog = false
                onBack()
            },
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(EmeraldSuccess.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(36.dp))
                }
            },
            title = {
                Text(
                    text = "Pengajuan Berhasil Dikirim",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimaryDark
                )
            },
            text = {
                Column {
                    Text(
                        text = "Permohonan ubah rekening Anda telah tercatat di sistem.",
                        fontSize = 13.sp,
                        color = TextPrimaryDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SoftBlueBg,
                        border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Langkah Wajib Berikutnya:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlueDark
                            )
                            Text(
                                text = "Silakan hubungi Customer Service (CS) KivoPay via WhatsApp (+62 812-8888-0099) untuk konfirmasi verifikasi lanjutan.",
                                fontSize = 12.sp,
                                color = TextPrimaryDark,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCsInstructionDialog = false
                        openCustomerServiceWhatsApp()
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(Icons.Default.HeadsetMic, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Hubungi CS Sekarang")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCsInstructionDialog = false
                        onBack()
                    }
                ) {
                    Text("Kembali ke Dashboard", color = TextSecondaryMuted)
                }
            },
            containerColor = CleanWhite,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
private fun DocumentCaptureCard(
    title: String,
    instruction: String,
    capturedBitmap: Bitmap?,
    fileName: String?,
    onCapture: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CleanWhite),
        border = BorderStroke(1.dp, if (capturedBitmap != null) EmeraldSuccess else CardBorderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
            Text(text = instruction, fontSize = 11.sp, color = TextSecondaryMuted, modifier = Modifier.padding(top = 2.dp, bottom = 8.dp))

            if (capturedBitmap != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldSuccess.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        bitmap = capturedBitmap.asImageBitmap(),
                        contentDescription = title,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, EmeraldSuccess, RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Foto Berhasil Diambil", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                        }
                        Text(fileName ?: "dokumen.jpg", fontSize = 11.sp, color = TextSecondaryMuted)
                    }
                    IconButton(onClick = onCapture) {
                        Icon(Icons.Default.Refresh, contentDescription = "Ambil Ulang", tint = PrimaryBlue)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SoftBlueBg)
                        .border(1.dp, PrimaryBlue.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .clickable { onCapture() }
                        .padding(16.dp)
                        .testTag(testTag),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = "Kamera", tint = PrimaryBlue, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ambil Foto Langsung via Kamera", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = TextSecondaryMuted)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
    }
}
