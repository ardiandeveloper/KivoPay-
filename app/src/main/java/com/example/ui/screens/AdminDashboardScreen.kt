package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuditLogEntity
import com.example.data.model.BankChangeRequestEntity
import com.example.data.model.BankChangeStatus
import com.example.data.model.BannerEntity
import com.example.data.model.KycStatus
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionStatus
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
fun AdminDashboardScreen(
    currentWithdrawFee: String = "3000",
    currentRcbApiKey: String = "rcb_live_usr_ffbffccebf4e",
    withdrawalRequests: List<TransactionEntity>,
    allUsers: List<UserEntity>,
    pendingKycUsers: List<UserEntity>,
    pendingKivoPlusUsers: List<UserEntity> = emptyList(),
    bankChangeRequests: List<BankChangeRequestEntity> = emptyList(),
    allTransactions: List<TransactionEntity>,
    auditLogs: List<AuditLogEntity>,
    activeBanners: List<BannerEntity> = emptyList(),
    onBack: () -> Unit,
    onApproveWithdraw: (txId: String) -> Unit,
    onRejectWithdraw: (txId: String, reason: String) -> Unit,
    onApproveKyc: (userId: String) -> Unit,
    onRejectKyc: (userId: String, reason: String) -> Unit,
    onApproveKivoPlus: (userId: String) -> Unit = {},
    onRejectKivoPlus: (userId: String, reason: String) -> Unit = { _, _ -> },
    onApproveBankChange: (requestId: String) -> Unit = {},
    onRejectBankChange: (requestId: String, reason: String) -> Unit = { _, _ -> },
    onSaveWithdrawFee: (newFee: Double) -> Unit,
    onSaveRcbApiKey: (newApiKey: String) -> Unit = {},
    onAddBanner: (title: String, subtitle: String, badge: String, colorHex: String) -> Unit = { _, _, _, _ -> },
    onDeleteBanner: (bannerId: String) -> Unit = {}
) {
    // 0: Verifikasi Rekening Baru (KYC), 1: KivoPay+ Upgrade, 2: Ubah Rekening, 3: Penarikan, 4: Kelola Banner, 5: Pengguna, 6: Biaya & Gateway, 7: Audit
    var activeAdminTab by remember { mutableStateOf(0) }
    var feeInput by remember { mutableStateOf(currentWithdrawFee) }
    var rcbApiKeyInput by remember(currentRcbApiKey) { mutableStateOf(currentRcbApiKey) }

    // Dialog state for rejecting KivoPay+
    var selectedUserForKivoPlusReject by remember { mutableStateOf<UserEntity?>(null) }
    var kivoPlusRejectReasonInput by remember { mutableStateOf("Foto e-KTP buram atau NIK tidak sesuai data pengguna") }

    // Dialog state for rejecting Bank Change
    var selectedBankChangeForReject by remember { mutableStateOf<BankChangeRequestEntity?>(null) }
    var bankChangeRejectReasonInput by remember { mutableStateOf("Foto e-KTP atau foto selfie tidak sesuai identitas akun") }

    // Dialog state for rejecting KYC with mandatory reason
    var selectedUserForKycReject by remember { mutableStateOf<UserEntity?>(null) }
    var kycRejectReasonInput by remember { mutableStateOf("Buku tabungan tidak terbaca / Nama berbeda") }

    // Dialog state for rejecting Withdrawal
    var selectedTxForReject by remember { mutableStateOf<TransactionEntity?>(null) }
    var withdrawRejectReasonInput by remember { mutableStateOf("Nomor rekening bank tujuan tidak valid") }

    // Banner Creation Form State
    var newBannerTitle by remember { mutableStateOf("") }
    var newBannerSubtitle by remember { mutableStateOf("") }
    var newBannerBadge by remember { mutableStateOf("PROMO") }
    var selectedBannerColorHex by remember { mutableStateOf("#1D61E7") }

    val bannerColorOptions = listOf(
        Pair("Royal Blue", "#1D61E7"),
        Pair("Deep Navy", "#0F47B8"),
        Pair("Sky Cyan", "#0284C7"),
        Pair("Emerald", "#059669"),
        Pair("Purple", "#7C3AED")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin", tint = PrimaryBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Admin Dashboard KivoPay", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimaryDark)
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
                .testTag("admin_dashboard_screen")
        ) {
            // Scrollable Row of Admin Tabs
            ScrollableTabRow(
                selectedTabIndex = activeAdminTab,
                containerColor = Color.White,
                contentColor = PrimaryBlue,
                edgePadding = 12.dp
            ) {
                Tab(
                    selected = activeAdminTab == 0,
                    onClick = { activeAdminTab = 0 },
                    text = { Text("Rekening Baru (${pendingKycUsers.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeAdminTab == 1,
                    onClick = { activeAdminTab = 1 },
                    text = { Text("KivoPay+ (${pendingKivoPlusUsers.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeAdminTab == 2,
                    onClick = { activeAdminTab = 2 },
                    text = { Text("Ubah Rekening (${bankChangeRequests.count { it.status == BankChangeStatus.PENDING_CS }})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeAdminTab == 3,
                    onClick = { activeAdminTab = 3 },
                    text = { Text("Tarik Dana (${withdrawalRequests.count { it.status == TransactionStatus.PENDING }})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeAdminTab == 4,
                    onClick = { activeAdminTab = 4 },
                    text = { Text("Banner (${activeBanners.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeAdminTab == 5,
                    onClick = { activeAdminTab = 5 },
                    text = { Text("Pengguna (${allUsers.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeAdminTab == 6,
                    onClick = { activeAdminTab = 6 },
                    text = { Text("Biaya", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeAdminTab == 7,
                    onClick = { activeAdminTab = 7 },
                    text = { Text("Audit", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }

            when (activeAdminTab) {
                0 -> {
                    // TAB 0: VERIFIKASI REKENING & BUKU TABUNGAN
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "Persetujuan Rekening & Buku Tabungan Pengguna Baru",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                            Text(
                                text = "Admin berwenang menyetujui atau menolak pendaftaran rekening pengguna baru. Bila ditolak, wajib menyertakan alasan.",
                                fontSize = 12.sp,
                                color = TextSecondaryMuted
                            )
                        }

                        if (pendingKycUsers.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(36.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Check, contentDescription = "Done", tint = EmeraldSuccess, modifier = Modifier.size(48.dp))
                                        Text("Semua pengajuan buku tabungan telah diproses!", fontSize = 13.sp, color = TextSecondaryMuted, modifier = Modifier.padding(top = 8.dp))
                                    }
                                }
                            }
                        } else {
                            items(pendingKycUsers) { user ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
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
                                                Icon(Icons.Default.Person, contentDescription = "User", tint = PrimaryBlue)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimaryDark)
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFFF59E0B).copy(alpha = 0.15f)
                                            ) {
                                                Text("Menunggu Review", color = Color(0xFFD97706), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text("Email: ${user.email} | HP: ${user.phoneNumber}", fontSize = 12.sp, color = TextSecondaryMuted)
                                        Text("No Akun KivoPay: ${user.accountId}", fontSize = 12.sp, color = PrimaryBlue, fontWeight = FontWeight.SemiBold)

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Bank details box
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(SoftBlueBg)
                                                .padding(12.dp)
                                        ) {
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.AccountBalance, contentDescription = "Bank", tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("${user.userBankName} - ${user.userBankAccountNumber}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimaryDark)
                                                }
                                                Text("A.N Pemilik: ${user.userBankAccountHolder}", fontSize = 12.sp, color = TextPrimaryDark)
                                                Text("Lampiran Kamera: ${user.bankBookDocumentUri}", fontSize = 11.sp, color = PrimaryBlue)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = EmeraldSuccess.copy(alpha = 0.12f)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(11.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Akan otomatis terdaftar di gateway pembayaran", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Action Buttons
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    selectedUserForKycReject = user
                                                },
                                                modifier = Modifier.weight(0.4f),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonError),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonError)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = "Tolak", modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Tolak", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = { onApproveKyc(user.id) },
                                                modifier = Modifier.weight(0.6f),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = "Setujui", modifier = Modifier.size(16.dp), tint = Color.White)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Setujui Rekening", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: PERMOHONAN UPGRADE KIVOPAY+
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "Permohonan Upgrade Akun KivoPay+ (Limit Rp 30 Juta)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                            Text(
                                text = "Periksa keabsahan NIK dan foto e-KTP serta foto selfie pengguna sebelum menyetujui penaikan limit.",
                                fontSize = 12.sp,
                                color = TextSecondaryMuted
                            )
                        }

                        if (pendingKivoPlusUsers.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(36.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Check, contentDescription = "Done", tint = EmeraldSuccess, modifier = Modifier.size(48.dp))
                                        Text("Tidak ada permohonan KivoPay+ yang pending.", fontSize = 13.sp, color = TextSecondaryMuted, modifier = Modifier.padding(top = 8.dp))
                                    }
                                }
                            }
                        } else {
                            items(pendingKivoPlusUsers) { user ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimaryDark)
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFFEF3C7)
                                            ) {
                                                Text("KivoPay+ Pending", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Email: ${user.email} | HP: ${user.phoneNumber}", fontSize = 12.sp, color = TextSecondaryMuted)
                                        Text("NIK KTP: ${user.kivoPlusNik}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = PrimaryBlue)
                                        Text("Saldo Saat Ini: Rp ${formatCurrency(user.saldoAvailable)}", fontSize = 12.sp, color = TextPrimaryDark)
                                        if (user.saldoHold > 0) {
                                            Text("Saldo Tertahan: Rp ${formatCurrency(user.saldoHold)} (Akan otomatis cair jika disetujui)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CrimsonError)
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Document Previews Mockup Container
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp),
                                            color = SoftBlueBg
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Security, contentDescription = "Doc", tint = PrimaryBlue, modifier = Modifier.size(24.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text("Dokumen: ${user.kivoPlusKtpUri.ifBlank { "ktp_verified.jpg" }}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                                                    Text("Selfie: ${user.kivoPlusSelfieUri.ifBlank { "selfie_ktp.jpg" }}", fontSize = 11.sp, color = TextSecondaryMuted)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Action buttons
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    selectedUserForKivoPlusReject = user
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonError),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonError)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = "Tolak", modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Tolak", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = { onApproveKivoPlus(user.id) },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = "Setujui", modifier = Modifier.size(16.dp), tint = Color.White)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Setujui Plus", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: PERMOHONAN UBAH REKENING BANK
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "Permohonan Perubahan Rekening Bank Terdaftar",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                            Text(
                                text = "Verifikasi kesesuaian identitas e-KTP dan foto selfie sebelum menyetujui pengalihan rekening resmi.",
                                fontSize = 12.sp,
                                color = TextSecondaryMuted
                            )
                        }

                        if (bankChangeRequests.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(40.dp))
                                        Text("Tidak ada permohonan perubahan rekening saat ini.", fontSize = 13.sp, color = TextSecondaryMuted, modifier = Modifier.padding(top = 8.dp))
                                    }
                                }
                            }
                        } else {
                            items(bankChangeRequests) { req ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Tiket: ${req.id}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryBlue
                                            )
                                            val (statusBg, statusColor, statusLabel) = when (req.status) {
                                                BankChangeStatus.PENDING_CS -> Triple(AmberWarning.copy(alpha = 0.15f), AmberWarning, "Menunggu CS & Admin")
                                                BankChangeStatus.APPROVED -> Triple(EmeraldSuccess.copy(alpha = 0.15f), EmeraldSuccess, "Disetujui")
                                                BankChangeStatus.REJECTED -> Triple(CrimsonError.copy(alpha = 0.15f), CrimsonError, "Ditolak")
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = statusBg
                                            ) {
                                                Text(
                                                    text = statusLabel,
                                                    color = statusColor,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = "Pemohon: ${req.userFullName} (${req.userEmail})",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimaryDark
                                        )
                                        Text(
                                            text = "No. HP / WhatsApp: ${req.userPhone}",
                                            fontSize = 12.sp,
                                            color = TextSecondaryMuted
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = SoftBlueBg,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(
                                                    text = "Rekening Lama: ${req.oldBankName} - ${req.oldAccountNumber} (a.n ${req.oldAccountHolder})",
                                                    fontSize = 11.sp,
                                                    color = TextSecondaryMuted
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Rekening Baru: ${req.newBankName} - ${req.newAccountNumber} (a.n ${req.newAccountHolder})",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PrimaryBlueDark
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Alasan: ${req.changeReason}",
                                                    fontSize = 11.sp,
                                                    color = TextPrimaryDark
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Security, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Bukti Dokumen: Foto e-KTP & Foto Selfie (Kamera)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = PrimaryBlue
                                            )
                                        }

                                        if (req.status == BankChangeStatus.REJECTED && req.rejectionReason.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Alasan Penolakan: ${req.rejectionReason}",
                                                fontSize = 11.sp,
                                                color = CrimsonError
                                            )
                                        }

                                        if (req.status == BankChangeStatus.PENDING_CS) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                OutlinedButton(
                                                    onClick = {
                                                        selectedBankChangeForReject = req
                                                    },
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(10.dp),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonError),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonError)
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Tolak", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }

                                                Button(
                                                    onClick = { onApproveBankChange(req.id) },
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(10.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                                                ) {
                                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Setujui", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // TAB 3: WITHDRAWAL REQUESTS
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "Kelola Permintaan Penarikan Dana (Withdrawal)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                            Text(
                                text = "Disetujui akan mengirim dana ke rekening, Ditolak akan melakukan refund saldo otomatis.",
                                fontSize = 12.sp,
                                color = TextSecondaryMuted
                            )
                        }

                        if (withdrawalRequests.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Tidak ada permintaan penarikan dana.", color = TextSecondaryMuted)
                                }
                            }
                        } else {
                            items(withdrawalRequests) { tx ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(tx.transactionId, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimaryDark)
                                            StatusChip(tx.status)
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Penerima: ${tx.recipientName} (${tx.bankName})", fontSize = 13.sp, color = TextPrimaryDark)
                                        Text("No. Rekening: ${tx.recipientAccount}", fontSize = 13.sp, color = TextSecondaryMuted)
                                        Text("Nominal Tarik: Rp ${formatCurrency(tx.amount)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
                                        Text("Biaya Layanan: Rp ${formatCurrency(tx.serviceFee)}", fontSize = 12.sp, color = PrimaryBlue)
                                        Text("Total Potong Saldo: Rp ${formatCurrency(tx.totalAmount)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CrimsonError)

                                        if (tx.status == TransactionStatus.PENDING || tx.status == TransactionStatus.PROCESSING) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                OutlinedButton(
                                                    onClick = { selectedTxForReject = tx },
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonError)
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = "Tolak", modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Tolak & Refund", fontSize = 12.sp)
                                                }

                                                Button(
                                                    onClick = { onApproveWithdraw(tx.transactionId) },
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                                                ) {
                                                    Icon(Icons.Default.Check, contentDescription = "Setujui", modifier = Modifier.size(16.dp), tint = Color.White)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Kirim Pencairan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                4 -> {
                    // TAB 4: KELOLA & UPLOAD BANNER PROMO
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "Unggah Banner Promo Baru",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                            Text(
                                text = "Banner yang diunggah akan langsung muncul pada Carousel Promo di Dashboard pengguna.",
                                fontSize = 12.sp,
                                color = TextSecondaryMuted
                            )
                        }

                        // Banner Creation Card Form
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Judul Banner Promo", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = newBannerTitle,
                                        onValueChange = { newBannerTitle = it },
                                        textStyle = kivoInputTextStyle(),
                                        colors = kivoTextFieldColors(),
                                        placeholder = { Text("Contoh: Bonus Saldo Rp 50.000") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text("Deskripsi / Subjudul Banner", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = newBannerSubtitle,
                                        onValueChange = { newBannerSubtitle = it },
                                        textStyle = kivoInputTextStyle(),
                                        colors = kivoTextFieldColors(),
                                        placeholder = { Text("Contoh: Top up minimal Rp 200rb hari ini!") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text("Badge Tag", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(listOf("PROMO", "HOT", "CASHBACK", "EVENT", "TIPS")) { badge ->
                                            val isSelected = newBannerBadge == badge
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (isSelected) PrimaryBlue else SoftBlueBg,
                                                modifier = Modifier.clickable { newBannerBadge = badge }
                                            ) {
                                                Text(
                                                    text = badge,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else PrimaryBlue,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Text("Warna Tema Banner", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(bannerColorOptions) { (label, hex) ->
                                            val isSelected = selectedBannerColorHex == hex
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = Color(android.graphics.Color.parseColor(hex)),
                                                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, TextPrimaryDark) else null,
                                                modifier = Modifier
                                                    .size(width = 80.dp, height = 34.dp)
                                                    .clickable { selectedBannerColorHex = hex }
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(label, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(18.dp))

                                    Button(
                                        onClick = {
                                            onAddBanner(newBannerTitle, newBannerSubtitle, newBannerBadge, selectedBannerColorHex)
                                            newBannerTitle = ""
                                            newBannerSubtitle = ""
                                        },
                                        enabled = newBannerTitle.isNotBlank() && newBannerSubtitle.isNotBlank(),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Unggah", tint = Color.White)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Unggah & Publikasikan Banner", fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "Daftar Banner Aktif (${activeBanners.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        }

                        items(activeBanners) { banner ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = PrimaryBlue.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = banner.badgeText,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryBlue,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(banner.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimaryDark)
                                        Text(banner.subtitle, fontSize = 12.sp, color = TextSecondaryMuted)
                                    }

                                    IconButton(onClick = { onDeleteBanner(banner.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = CrimsonError)
                                    }
                                }
                            }
                        }
                    }
                }

                5 -> {
                    // TAB 5: ALL USERS LIST
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text("Daftar Akun Pengguna KivoPay", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                        }

                        items(allUsers) { usr ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(usr.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimaryDark)
                                        Text(usr.email, fontSize = 12.sp, color = TextSecondaryMuted)
                                        Text("Bank: ${usr.userBankName} (${usr.userBankAccountNumber})", fontSize = 11.sp, color = PrimaryBlue)
                                        Text("Status Verifikasi: ${usr.kycStatus.name}", fontSize = 10.sp, color = if (usr.isKycVerified) EmeraldSuccess else CrimsonError, fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Saldo: Rp ${formatCurrency(usr.saldoAvailable)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EmeraldSuccess)
                                        if (usr.saldoHold > 0) {
                                            Text("Hold: Rp ${formatCurrency(usr.saldoHold)}", fontSize = 11.sp, color = Color(0xFFD97706))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                6 -> {
                    // TAB 6: WITHDRAW FEE CONFIGURATION
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp)
                    ) {
                        Text("Pengaturan Biaya Penarikan (Withdraw Fee)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                        Text(
                            text = "Konfigurasi biaya layanan flat per penarikan dana bank (Default Rp 3.000). Selisih biaya ini menjadi keuntungan KivoPay.",
                            fontSize = 12.sp,
                            color = TextSecondaryMuted,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = feeInput,
                            onValueChange = { input -> if (input.all { it.isDigit() }) feeInput = input },
                            textStyle = kivoInputTextStyle(),
                            colors = kivoTextFieldColors(),
                            label = { Text("Biaya Penarikan (Rp)") },
                            leadingIcon = { Text("Rp ", fontWeight = FontWeight.Bold, color = PrimaryBlue) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_withdraw_fee_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val valFee = feeInput.toDoubleOrNull() ?: 3000.0
                                onSaveWithdrawFee(valFee)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("save_admin_fee_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Simpan", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Simpan Biaya Penarikan Baru", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Dedicated RCB Pay Gateway Live Configuration Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryBlue.copy(alpha = 0.35f))
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Gateway Pembayaran API", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = EmeraldSuccess.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = "TERHUBUNG LIVE",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldSuccess,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "Base URL: https://api.ragaciptabersama.web.id/api/",
                                    fontSize = 11.sp,
                                    color = TextSecondaryMuted,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                                )

                                Text("API Key Produksi (x-api-key):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
                                Spacer(modifier = Modifier.height(6.dp))

                                OutlinedTextField(
                                    value = rcbApiKeyInput,
                                    onValueChange = { rcbApiKeyInput = it },
                                    textStyle = kivoInputTextStyle(),
                                    colors = kivoTextFieldColors(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("admin_rcb_api_key_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        onSaveRcbApiKey(rcbApiKeyInput.trim())
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("save_rcb_key_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = "Simpan Key", tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Simpan & Sinkronkan Kunci API", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }

                7 -> {
                    // TAB 7: AUDIT LOGS
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text("Audit Logs Aktivitas Sensitif KivoPay", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                        }

                        if (auditLogs.isEmpty()) {
                            item { Text("Belum ada log aktivitas admin.", color = TextSecondaryMuted) }
                        } else {
                            items(auditLogs) { log ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(log.action, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryBlue)
                                            Text(formatDate(log.timestamp), fontSize = 10.sp, color = TextSecondaryMuted)
                                        }
                                        Text(log.details, fontSize = 12.sp, color = TextPrimaryDark, modifier = Modifier.padding(top = 4.dp))
                                        Text("Pelaku: ${log.adminEmail}", fontSize = 10.sp, color = TextSecondaryMuted)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog: Reject KYC with Mandatory Reason
    if (selectedUserForKycReject != null) {
        val targetUser = selectedUserForKycReject!!
        AlertDialog(
            onDismissRequest = { selectedUserForKycReject = null },
            title = {
                Text(
                    text = "Tolak Verifikasi Rekening",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = CrimsonError
                )
            },
            text = {
                Column {
                    Text(
                        text = "Pengguna: ${targetUser.fullName} (${targetUser.email})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimaryDark
                    )
                    Text(
                        text = "Rekening: ${targetUser.userBankName} - ${targetUser.userBankAccountNumber}",
                        fontSize = 12.sp,
                        color = TextSecondaryMuted,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(
                        text = "Sertakan Alasan Penolakan (Wajib):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    OutlinedTextField(
                        value = kycRejectReasonInput,
                        onValueChange = { kycRejectReasonInput = it },
                        textStyle = kivoInputTextStyle(),
                        colors = kivoTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        shape = RoundedCornerShape(10.dp),
                        placeholder = { Text("Contoh: Foto buku tabungan buram / Nama tidak cocok") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRejectKyc(targetUser.id, kycRejectReasonInput)
                        selectedUserForKycReject = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonError),
                    enabled = kycRejectReasonInput.isNotBlank()
                ) {
                    Text("Tolak & Kirim Alasan", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedUserForKycReject = null }) {
                    Text("Batal")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Modal Dialog: Reject KivoPay+ with Mandatory Reason
    if (selectedUserForKivoPlusReject != null) {
        val targetUser = selectedUserForKivoPlusReject!!
        AlertDialog(
            onDismissRequest = { selectedUserForKivoPlusReject = null },
            title = {
                Text(
                    text = "Tolak Pengajuan KivoPay+",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = CrimsonError
                )
            },
            text = {
                Column {
                    Text(
                        text = "Pengguna: ${targetUser.fullName} (NIK: ${targetUser.kivoPlusNik})",
                        fontSize = 12.sp,
                        color = TextPrimaryDark,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Alasan Penolakan Resmi (Wajib):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    OutlinedTextField(
                        value = kivoPlusRejectReasonInput,
                        onValueChange = { kivoPlusRejectReasonInput = it },
                        textStyle = kivoInputTextStyle(),
                        colors = kivoTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        shape = RoundedCornerShape(10.dp),
                        placeholder = { Text("Contoh: Foto e-KTP buram / NIK tidak cocok") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRejectKivoPlus(targetUser.id, kivoPlusRejectReasonInput)
                        selectedUserForKivoPlusReject = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonError),
                    enabled = kivoPlusRejectReasonInput.isNotBlank()
                ) {
                    Text("Tolak & Kirim Alasan", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedUserForKivoPlusReject = null }) {
                    Text("Batal")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Modal Dialog: Reject Withdrawal
    if (selectedTxForReject != null) {
        val targetTx = selectedTxForReject!!
        AlertDialog(
            onDismissRequest = { selectedTxForReject = null },
            title = { Text("Tolak Penarikan Dana & Refund", fontWeight = FontWeight.Bold, color = CrimsonError) },
            text = {
                Column {
                    Text("ID Transaksi: ${targetTx.transactionId}", fontSize = 12.sp, color = TextPrimaryDark)
                    Text("Nominal: Rp ${formatCurrency(targetTx.totalAmount)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Alasan Penolakan:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                    OutlinedTextField(
                        value = withdrawRejectReasonInput,
                        onValueChange = { withdrawRejectReasonInput = it },
                        textStyle = kivoInputTextStyle(),
                        colors = kivoTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRejectWithdraw(targetTx.transactionId, withdrawRejectReasonInput)
                        selectedTxForReject = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonError)
                ) {
                    Text("Tolak & Refund", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedTxForReject = null }) { Text("Batal") }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Modal Dialog: Reject Bank Change with Mandatory Reason
    if (selectedBankChangeForReject != null) {
        val targetReq = selectedBankChangeForReject!!
        AlertDialog(
            onDismissRequest = { selectedBankChangeForReject = null },
            title = {
                Text(
                    text = "Tolak Perubahan Rekening",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = CrimsonError
                )
            },
            text = {
                Column {
                    Text(
                        text = "Pemohon: ${targetReq.userFullName} (${targetReq.newBankName} - ${targetReq.newAccountNumber})",
                        fontSize = 12.sp,
                        color = TextPrimaryDark,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text("Alasan Penolakan Resmi (Wajib):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                    OutlinedTextField(
                        value = bankChangeRejectReasonInput,
                        onValueChange = { bankChangeRejectReasonInput = it },
                        textStyle = kivoInputTextStyle(),
                        colors = kivoTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        shape = RoundedCornerShape(10.dp),
                        placeholder = { Text("Contoh: Foto e-KTP tidak jelas / Data tidak cocok") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRejectBankChange(targetReq.id, bankChangeRejectReasonInput)
                        selectedBankChangeForReject = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonError),
                    enabled = bankChangeRejectReasonInput.isNotBlank()
                ) {
                    Text("Tolak & Catat Alasan", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedBankChangeForReject = null }) { Text("Batal") }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun StatusChip(status: TransactionStatus) {
    val (bgColor, textColor, label) = when (status) {
        TransactionStatus.SUCCESS -> Triple(EmeraldSuccess.copy(alpha = 0.15f), EmeraldSuccess, "Sukses")
        TransactionStatus.PENDING -> Triple(Color(0xFFF59E0B).copy(alpha = 0.15f), Color(0xFFD97706), "Pending")
        TransactionStatus.PROCESSING -> Triple(Color(0xFF3B82F6).copy(alpha = 0.15f), Color(0xFF2563EB), "Diproses")
        TransactionStatus.FAILED -> Triple(Color(0xFFEF4444).copy(alpha = 0.15f), Color(0xFFDC2626), "Gagal")
        TransactionStatus.REJECTED -> Triple(Color(0xFFEF4444).copy(alpha = 0.15f), Color(0xFFDC2626), "Ditolak")
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = bgColor
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}
