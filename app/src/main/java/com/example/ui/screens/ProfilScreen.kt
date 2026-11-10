package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UserEntity
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
fun ProfilScreen(
    user: UserEntity?,
    onNavigateAdmin: () -> Unit,
    onNavigateChangeBank: () -> Unit = {},
    onNavigateKivoPlus: () -> Unit = {},
    onLogout: () -> Unit,
    onUpdatePin: (String, String) -> Unit,
    onUpdatePassword: (String, String) -> Unit,
    onShowToast: (String) -> Unit
) {
    var showPinDialog by remember { mutableStateOf(false) }
    var showPassDialog by remember { mutableStateOf(false) }
    var biometricEnabled by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .testTag("profil_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // User Profile Header Card in White & Blue Gradient
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(PrimaryBlue, PrimaryBlueDark)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.kivopay_icon),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = user?.fullName ?: "Pengguna KivoPay",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = CleanWhite
                        )

                        Text(
                            text = user?.email ?: "user@kivopay.id",
                            fontSize = 13.sp,
                            color = CleanWhite.copy(alpha = 0.9f)
                        )

                        Text(
                            text = user?.phoneNumber ?: "081298765432",
                            fontSize = 12.sp,
                            color = CleanWhite.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // ID KivoPay (Nomor Handphone) Tag
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "ID KivoPay (Nomor HP): ${user?.phoneNumber?.ifBlank { "081298765432" } ?: "081298765432"}",
                                color = CleanWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Bank Account Information Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(SoftBlueBg, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = "Bank",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Rekening Resmi: ${user?.userBankName?.ifBlank { "Bank BCA" }}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                            Text(
                                text = "No. ${user?.userBankAccountNumber?.ifBlank { "0889218274" }} (a.n ${user?.userBankAccountHolder?.ifBlank { user?.fullName } ?: user?.fullName})",
                                fontSize = 12.sp,
                                color = TextSecondaryMuted
                            )
                            Text(
                                text = if (user?.isKycVerified == true) "Status: Disetujui Admin (Aktif)" else "Status: Menunggu Persetujuan",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (user?.isKycVerified == true) EmeraldSuccess else Color(0xFFD97706)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    androidx.compose.material3.OutlinedButton(
                        onClick = onNavigateChangeBank,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ajukan Perubahan Rekening Bank", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    }
                }
            }
        }

        // Settings Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Pengaturan Keamanan & Akun",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Admin Dashboard Button (always accessible or for admin account)
                if (user?.isAdmin == true) {
                    ProfileSettingRow(
                        title = "Admin Dashboard",
                        subtitle = "Verifikasi buku tabungan, penarikan, biaya & audit",
                        icon = Icons.Default.AdminPanelSettings,
                        iconColor = PrimaryBlue,
                        onClick = onNavigateAdmin
                    )
                }

                // KivoPay+ Tier Upgrade Row
                ProfileSettingRow(
                    title = if (user?.isKivoPlus == true) "KivoPay+ (Aktif)" else "Upgrade ke KivoPay+",
                    subtitle = if (user?.isKivoPlus == true) "Batas limit Rp 30.000.000 aktif" else "Tingkatkan limit s/d Rp 30 Juta & verifikasi e-KTP",
                    icon = Icons.Default.Star,
                    iconColor = if (user?.isKivoPlus == true) Color(0xFFD97706) else PrimaryBlue,
                    onClick = onNavigateKivoPlus
                )

                ProfileSettingRow(
                    title = "Ubah 6-Digit PIN Transaksi",
                    subtitle = "PIN digunakan untuk otorisasi transfer & penarikan",
                    icon = Icons.Default.Lock,
                    iconColor = PrimaryBlue,
                    onClick = { showPinDialog = true }
                )

                ProfileSettingRow(
                    title = "Ubah Password Akun",
                    subtitle = "Perbarui katasandi login akun KivoPay",
                    icon = Icons.Default.Key,
                    iconColor = PrimaryBlue,
                    onClick = { showPassDialog = true }
                )

                // Biometric Toggle Row
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    tonalElevation = 1.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(SoftBlueBg, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = "Biometrik",
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Login Biometrik / Fingerprint",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark
                                )
                                Text(
                                    text = "Gunakan sidik jari untuk verifikasi cepat",
                                    fontSize = 11.sp,
                                    color = TextSecondaryMuted
                                )
                            }
                        }

                        Switch(
                            checked = biometricEnabled,
                            onCheckedChange = {
                                biometricEnabled = it
                                onShowToast(if (it) "Otentikasi biometrik aktif." else "Biometrik nonaktif.")
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Logout Button
                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("logout_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonError.copy(alpha = 0.12f),
                        contentColor = CrimsonError
                    )
                ) {
                    Icon(imageVector = Icons.Default.Logout, contentDescription = "Keluar")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Keluar dari Akun", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }

    if (showPinDialog) {
        ChangePinBottomSheet(
            onDismiss = { showPinDialog = false },
            onSubmit = { oldPin, newPin ->
                onUpdatePin(oldPin, newPin)
                showPinDialog = false
            }
        )
    }

    if (showPassDialog) {
        ChangePasswordBottomSheet(
            onDismiss = { showPassDialog = false },
            onSubmit = { oldPass, newPass ->
                onUpdatePassword(oldPass, newPass)
                showPassDialog = false
            }
        )
    }
}

@Composable
fun ProfileSettingRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
            .testTag("setting_row_$title"),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        tonalElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(iconColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = TextSecondaryMuted
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Panah",
                tint = TextSecondaryMuted
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePinBottomSheet(
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Ubah 6-Digit PIN Transaksi",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = oldPin,
                onValueChange = { if (it.length <= 6) oldPin = it },
                textStyle = com.example.ui.components.kivoInputTextStyle(),
                colors = com.example.ui.components.kivoTextFieldColors(),
                label = { Text("PIN Lama (6 Digit)") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = newPin,
                onValueChange = { if (it.length <= 6) newPin = it },
                textStyle = com.example.ui.components.kivoInputTextStyle(),
                colors = com.example.ui.components.kivoTextFieldColors(),
                label = { Text("PIN Baru (6 Digit)") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onSubmit(oldPin, newPin) },
                enabled = oldPin.length == 6 && newPin.length == 6,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Simpan PIN Baru", fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordBottomSheet(
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Ubah Password Akun",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = oldPass,
                onValueChange = { oldPass = it },
                textStyle = com.example.ui.components.kivoInputTextStyle(),
                colors = com.example.ui.components.kivoTextFieldColors(),
                label = { Text("Password Lama") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = newPass,
                onValueChange = { newPass = it },
                textStyle = com.example.ui.components.kivoInputTextStyle(),
                colors = com.example.ui.components.kivoTextFieldColors(),
                label = { Text("Password Baru") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onSubmit(oldPass, newPass) },
                enabled = oldPass.isNotEmpty() && newPass.length >= 6,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Simpan Password Baru", fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
