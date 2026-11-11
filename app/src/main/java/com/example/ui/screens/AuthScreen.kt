package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import com.example.data.model.IndonesianBanks
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CardBorderColor
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PageBackground
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SoftBlueBg
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLogin: (email: String, pass: String) -> Unit,
    onRegister: (
        fullName: String,
        email: String,
        phone: String,
        pass: String,
        pin: String,
        bankName: String,
        bankAccountNo: String,
        bankAccountHolder: String,
        bankBookUri: String
    ) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Login, 1: Register

    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }

    // Popup forgot password
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    // Register fields
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regPasswordVisible by remember { mutableStateOf(false) }
    var regPin by remember { mutableStateOf("") }

    // Bank Account & Bank Book fields (strictly camera-only, no dummy prefill)
    val supportedBanks = IndonesianBanks.allBanks
    var bankSearchQuery by remember { mutableStateOf("") }
    val filteredBanks = remember(bankSearchQuery) {
        if (bankSearchQuery.isBlank()) supportedBanks else supportedBanks.filter { it.contains(bankSearchQuery, ignoreCase = true) }
    }
    var selectedBank by remember { mutableStateOf(supportedBanks[0]) }
    var bankDropdownExpanded by remember { mutableStateOf(false) }
    var bankAccountNo by remember { mutableStateOf("") }
    var bankAccountHolder by remember { mutableStateOf("") }

    // Real camera capture result (no dummy!)
    var capturedBankBookBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var bankBookPhotoName by remember { mutableStateOf<String?>(null) }

    // Camera Launcher - Takes real photo directly from device camera (blocks gallery picker for security)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            capturedBankBookBitmap = bitmap
            bankBookPhotoName = "buku_rekening_${System.currentTimeMillis().toString().takeLast(6)}.jpg"
        }
    }

    // High contrast text style for high visibility
    val inputTextStyle = TextStyle(
        color = TextPrimaryDark,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium
    )

    val customTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = TextPrimaryDark,
        unfocusedTextColor = TextPrimaryDark,
        focusedBorderColor = PrimaryBlue,
        unfocusedBorderColor = CardBorderColor,
        focusedLabelColor = PrimaryBlue,
        unfocusedLabelColor = TextSecondaryMuted,
        focusedLeadingIconColor = PrimaryBlue,
        unfocusedLeadingIconColor = PrimaryBlue,
        focusedTrailingIconColor = PrimaryBlue,
        unfocusedTrailingIconColor = TextSecondaryMuted,
        cursorColor = PrimaryBlue,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White
    )

    Scaffold(
        containerColor = PageBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Branding Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.kivopay_logo),
                        contentDescription = "KivoPay Logo",
                        modifier = Modifier.height(42.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Modern Digital Banking & Payment Platform",
                        color = PrimaryBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Main Auth Container
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = SoftBlueBg,
                        contentColor = PrimaryBlue
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Masuk / Login", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Daftar Akun Baru", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (selectedTab == 0) {
                        // ================= LOGIN FORM =================
                        Text(
                            text = "Selamat Datang Kembali",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = "Silakan login dengan email & password akun KivoPay",
                            fontSize = 12.sp,
                            color = TextSecondaryMuted,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = loginEmail,
                            onValueChange = { loginEmail = it },
                            label = { Text("Alamat Email") },
                            placeholder = { Text("nama@email.com atau admin@kivopay.id") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_email_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            textStyle = inputTextStyle,
                            colors = customTextFieldColors
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            label = { Text("Password Akun") },
                            placeholder = { Text("Masukkan password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                            trailingIcon = {
                                IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                    Icon(
                                        imageVector = if (loginPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Password"
                                    )
                                }
                            },
                            visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            textStyle = inputTextStyle,
                            colors = customTextFieldColors
                        )

                        // Lupa Password link
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "Lupa Password?",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryBlue,
                                modifier = Modifier
                                    .clickable { showForgotPasswordDialog = true }
                                    .padding(vertical = 4.dp, horizontal = 2.dp)
                                    .testTag("forgot_password_button")
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { onLogin(loginEmail, loginPassword) },
                            enabled = loginEmail.isNotBlank() && loginPassword.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_submit_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue,
                                disabledContainerColor = PrimaryBlue.copy(alpha = 0.4f)
                            )
                        ) {
                            Text("Masuk ke KivoPay", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Belum memiliki akun? ", fontSize = 12.sp, color = TextSecondaryMuted)
                            Text(
                                text = "Daftar di sini",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue,
                                modifier = Modifier
                                    .clickable { selectedTab = 1 }
                                    .padding(4.dp)
                            )
                        }
                    } else {
                        // ================= REGISTER FORM =================
                        Text(
                            text = "Pendaftaran Akun KivoPay",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = "Lengkapi data diri dan foto buku rekening langsung lewat kamera",
                            fontSize = 12.sp,
                            color = TextSecondaryMuted,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Section 1: Data Diri
                        Text("1. Data Diri & Keamanan", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = regName,
                            onValueChange = {
                                regName = it
                                if (bankAccountHolder.isBlank()) bankAccountHolder = it
                            },
                            label = { Text("Nama Lengkap Sesuai KTP") },
                            placeholder = { Text("Nama Anda") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Nama") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_name_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            textStyle = inputTextStyle,
                            colors = customTextFieldColors
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = { Text("Alamat Email") },
                            placeholder = { Text("contoh@email.com") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_email_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            textStyle = inputTextStyle,
                            colors = customTextFieldColors
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regPhone,
                            onValueChange = { regPhone = it },
                            label = { Text("Nomor Handphone (WhatsApp)") },
                            placeholder = { Text("08xxxxxxxxxx") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_phone_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            textStyle = inputTextStyle,
                            colors = customTextFieldColors,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            label = { Text("Password Akun (Min 6 Karakter)") },
                            placeholder = { Text("Buat password aman") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Pass") },
                            trailingIcon = {
                                IconButton(onClick = { regPasswordVisible = !regPasswordVisible }) {
                                    Icon(
                                        imageVector = if (regPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Password"
                                    )
                                }
                            },
                            visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_password_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            textStyle = inputTextStyle,
                            colors = customTextFieldColors
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regPin,
                            onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) regPin = it },
                            label = { Text("PIN Transaksi (6 Digit Angka)") },
                            placeholder = { Text("6 digit angka rahasia") },
                            leadingIcon = { Icon(Icons.Default.Shield, contentDescription = "PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_pin_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            textStyle = inputTextStyle,
                            colors = customTextFieldColors,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Section 2: Data Rekening Bank & Live Camera Upload
                        Text("2. Rekening Bank & Foto Buku Tabungan", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                        Spacer(modifier = Modifier.height(8.dp))

                        ExposedDropdownMenuBox(
                            expanded = bankDropdownExpanded,
                            onExpandedChange = { bankDropdownExpanded = !bankDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedBank,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Pilih Bank Anda") },
                                leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = "Bank") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bankDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = inputTextStyle,
                                colors = customTextFieldColors
                            )
                            ExposedDropdownMenu(
                                expanded = bankDropdownExpanded,
                                onDismissRequest = { bankDropdownExpanded = false },
                                modifier = Modifier.height(300.dp)
                            ) {
                                OutlinedTextField(
                                    value = bankSearchQuery,
                                    onValueChange = { bankSearchQuery = it },
                                    placeholder = { Text("Cari nama bank...") },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryBlue) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    singleLine = true,
                                    textStyle = inputTextStyle,
                                    colors = customTextFieldColors
                                )
                                if (bankSearchQuery.isNotBlank() && !supportedBanks.any { it.equals(bankSearchQuery, ignoreCase = true) }) {
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

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = bankAccountNo,
                            onValueChange = { input -> if (input.all { it.isDigit() }) bankAccountNo = input },
                            label = { Text("Nomor Rekening Bank") },
                            placeholder = { Text("Nomor rekening bank Anda") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_bank_account_no"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            textStyle = inputTextStyle,
                            colors = customTextFieldColors,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = bankAccountHolder,
                            onValueChange = { bankAccountHolder = it },
                            label = { Text("Nama Pemilik Sesuai Buku Rekening") },
                            placeholder = { Text("Nama lengkap pemilik rekening") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_bank_account_holder"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            textStyle = inputTextStyle,
                            colors = customTextFieldColors
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Live Camera Capture Component (NO dummy, NO gallery access for security)
                        Text(
                            text = "Foto Buku Rekening (Wajib Kamera Langsung)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = "Demi keamanan verifikasi perbankan, dokumen wajib difoto langsung menggunakan kamera perangkat (akses galeri dinonaktifkan).",
                            fontSize = 11.sp,
                            color = TextSecondaryMuted,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (capturedBankBookBitmap != null) EmeraldSuccess.copy(alpha = 0.08f) else SoftBlueBg)
                                .border(
                                    1.5.dp,
                                    if (capturedBankBookBitmap != null) EmeraldSuccess else PrimaryBlue,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    cameraLauncher.launch(null)
                                }
                                .padding(14.dp)
                                .testTag("camera_capture_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (capturedBankBookBitmap != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        bitmap = capturedBankBookBitmap!!.asImageBitmap(),
                                        contentDescription = "Hasil Foto Kamera",
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.dp, EmeraldSuccess, RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Success",
                                                tint = EmeraldSuccess,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Foto Kamera Berhasil Diambil",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldSuccess
                                            )
                                        }
                                        Text(
                                            text = bankBookPhotoName ?: "foto_kamera.jpg",
                                            fontSize = 11.sp,
                                            color = TextSecondaryMuted
                                        )
                                        Text(
                                            text = "Klik di sini untuk foto ulang",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = PrimaryBlue,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Buka Kamera",
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Ambil Foto Buku Rekening Sekarang",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryBlue
                                        )
                                        Text(
                                            text = "Buka kamera & arahkan ke nomor & nama rekening",
                                            fontSize = 11.sp,
                                            color = TextSecondaryMuted
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Always Visible Register Button with explicit status
                        val isRegisterFormValid = regName.isNotBlank() &&
                                regEmail.isNotBlank() &&
                                regPassword.length >= 6 &&
                                regPin.length == 6 &&
                                bankAccountNo.isNotBlank() &&
                                bankAccountHolder.isNotBlank() &&
                                capturedBankBookBitmap != null

                        Button(
                            onClick = {
                                onRegister(
                                    regName,
                                    regEmail,
                                    regPhone,
                                    regPassword,
                                    regPin,
                                    selectedBank,
                                    bankAccountNo,
                                    bankAccountHolder,
                                    bankBookPhotoName ?: "buku_rekening_kamera.jpg"
                                )
                            },
                            enabled = isRegisterFormValid,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("register_submit_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue,
                                disabledContainerColor = PrimaryBlue.copy(alpha = 0.45f)
                            )
                        ) {
                            Text(
                                text = if (isRegisterFormValid) "Daftar & Ajukan Verifikasi" else "Lengkapi Form & Foto Kamera untuk Daftar",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Sudah memiliki akun? ", fontSize = 12.sp, color = TextSecondaryMuted)
                            Text(
                                text = "Login di sini",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue,
                                modifier = Modifier
                                    .clickable { selectedTab = 0 }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal Popup Lupa Password
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(SoftBlueBg, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = "CS KivoPay",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(30.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Reset Password Akun",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextPrimaryDark
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Reset password saat ini hanya tersedia melalui Customer Service (CS) KivoPay resmi demi perlindungan dana nasabah.",
                        fontSize = 13.sp,
                        color = TextPrimaryDark,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SoftBlueBg)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("WhatsApp CS: +62 812-9876-5432", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                            Text("Email: support@kivopay.id", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PrimaryBlue)
                            Text("Jam Operasional: 24 Jam Nonstop", fontSize = 11.sp, color = TextSecondaryMuted)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showForgotPasswordDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Hubungi CS / Tutup", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        )
    }
}
