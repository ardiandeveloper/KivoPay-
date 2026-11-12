package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Warning
import com.example.ui.components.kivoInputTextStyle
import com.example.ui.components.kivoTextFieldColors
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.BannerEntity
import com.example.data.model.KycStatus
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.UserEntity
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CardBorderColor
import com.example.ui.theme.CleanWhite
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PageBackground
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueDark
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.SoftBlueBg
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    user: UserEntity?,
    banners: List<BannerEntity>,
    recentTransactions: List<TransactionEntity>,
    isBalanceHidden: Boolean,
    onToggleBalance: () -> Unit,
    onNavigateTopUp: () -> Unit,
    onNavigateTransfer: () -> Unit,
    onNavigateWithdraw: () -> Unit,
    onNavigateHistory: () -> Unit,
    onNavigatePayment: () -> Unit,
    onNavigatePaymentWithCategory: (String) -> Unit = {},
    onNavigateQris: () -> Unit,
    onNavigateKivoPlus: () -> Unit = {},
    onNavigateAllServices: () -> Unit = {},
    onNavigateSettings: () -> Unit,
    onSelectTransaction: (TransactionEntity) -> Unit,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    var showQrisMerchantDialog by remember { mutableStateOf(false) }
    var showAllServicesSheet by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .testTag("home_screen_column"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Account Verification Status Alert (if pending or rejected)
        if (user != null && !user.isKycVerified) {
            item {
                KycAlertBanner(user = user)
            }
        }

        // Held Balance Alert (Saldo Tertahan melebihi limit Rp 4 Juta)
        if (user != null && user.saldoHold > 0 && !user.isKivoPlus) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { onNavigateKivoPlus() }
                        .testTag("saldo_hold_alert_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFF59E0B), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "Hold", tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Saldo Tertahan: Rp ${formatCurrency(user.saldoHold)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF92400E)
                            )
                            Text(
                                text = "Batas saldo akun reguler maksimal Rp 4 Juta. Upgrade ke KivoPay+ untuk mencairkan saldo tertahan ini & nikmati limit hingga Rp 30 Juta!",
                                fontSize = 11.sp,
                                color = Color(0xFFB45309),
                                lineHeight = 15.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ChevronRight, contentDescription = "Upgrade", tint = Color(0xFF92400E))
                    }
                }
            }
        }

        // Balance Card Header
        item {
            KivoBalanceCard(
                user = user,
                isBalanceHidden = isBalanceHidden,
                onToggleBalance = onToggleBalance,
                onTopUpClick = onNavigateTopUp,
                onTransferClick = onNavigateTransfer,
                onWithdrawClick = onNavigateWithdraw,
                onHistoryClick = onNavigateHistory,
                onUpgradeKivoPlus = onNavigateKivoPlus,
                onCopyAccount = { phoneId ->
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("ID KivoPay", phoneId)
                    clipboard.setPrimaryClip(clip)
                    onShowToast("ID KivoPay $phoneId tersalin!")
                }
            )
        }

        // QRIS Merchant Feature Row Card (Directly below balance card as requested)
        item {
            QrisMerchantCard(
                onClick = {
                    showQrisMerchantDialog = true
                }
            )
        }

        // Service Menu Grid Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Layanan Utama",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                ServiceMenuGrid(
                    onTopUp = onNavigateTopUp,
                    onTransfer = onNavigateTransfer,
                    onWithdraw = onNavigateWithdraw,
                    onQris = onNavigateQris,
                    onPln = { onNavigatePaymentWithCategory("Listrik PLN") },
                    onPulsa = { onNavigatePaymentWithCategory("Pulsa & Data") },
                    onPdam = { onNavigatePaymentWithCategory("PDAM Air") },
                    onAllServices = onNavigateAllServices
                )
            }
        }

        // Promo Banner Carousel
        item {
            if (banners.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Promo & Penawaran Spesial",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
                    )
                    BannerCarousel(banners = banners)
                }
            }
        }

        // Recent Transactions Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transaksi Terbaru",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
                Text(
                    text = "Lihat Semua",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryBlue,
                    modifier = Modifier.clickable { onNavigateHistory() }
                )
            }
        }

        if (recentTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada transaksi. Mulai Top Up atau Transfer!",
                        fontSize = 14.sp,
                        color = TextSecondaryMuted
                    )
                }
            }
        } else {
            items(recentTransactions.take(5)) { tx ->
                TransactionItemRow(
                    tx = tx,
                    isBalanceHidden = isBalanceHidden,
                    onClick = { onSelectTransaction(tx) }
                )
            }
        }
    }

    // Modal QRIS Merchant Dialog
    if (showQrisMerchantDialog) {
        AlertDialog(
            onDismissRequest = { showQrisMerchantDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(SoftBlueBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = "QRIS Merchant",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "QRIS Merchant",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimaryDark
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Akan Segera Hadir!",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = PrimaryBlue
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Fitur pendaftaran dan penerimaan pembayaran QRIS Merchant untuk toko fisik dan UMKM sedang dalam tahap akhir integrasi regulasi perbankan KivoPay. Nantikan update segera!",
                        fontSize = 13.sp,
                        color = TextSecondaryMuted,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showQrisMerchantDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Mengerti", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // GoPay-Style "Semua Layanan" All Services Modal
    if (showAllServicesSheet) {
        AllServicesModal(
            onDismiss = { showAllServicesSheet = false },
            onTopUp = {
                showAllServicesSheet = false
                onNavigateTopUp()
            },
            onTransfer = {
                showAllServicesSheet = false
                onNavigateTransfer()
            },
            onWithdraw = {
                showAllServicesSheet = false
                onNavigateWithdraw()
            },
            onQris = {
                showAllServicesSheet = false
                onNavigateQris()
            },
            onHistory = {
                showAllServicesSheet = false
                onNavigateHistory()
            },
            onSelectPaymentCategory = { cat ->
                showAllServicesSheet = false
                onNavigatePaymentWithCategory(cat)
            }
        )
    }
}

@Composable
fun KycAlertBanner(user: UserEntity) {
    val isPending = user.kycStatus == KycStatus.PENDING
    val isRejected = user.kycStatus == KycStatus.REJECTED

    val bgColor = if (isPending) SoftBlueBg else CrimsonError.copy(alpha = 0.1f)
    val borderColor = if (isPending) PrimaryBlue.copy(alpha = 0.3f) else CrimsonError.copy(alpha = 0.4f)
    val iconColor = if (isPending) PrimaryBlue else CrimsonError

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = if (isPending) Icons.Default.Info else Icons.Default.Warning,
                contentDescription = "Status",
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = if (isPending) "Verifikasi Buku Tabungan Sedang Ditinjau Admin" else "Verifikasi Rekening Ditolak",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextPrimaryDark
                )
                Text(
                    text = if (isPending)
                        "Rekening ${user.userBankName} (${user.userBankAccountNumber}) sedang diverifikasi oleh admin. Mohon tunggu persetujuan."
                    else
                        "Alasan penolakan: ${user.kycRejectionReason}. Silakan hubungi admin atau ajukan ulang.",
                    fontSize = 11.sp,
                    color = TextSecondaryMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun QrisMerchantCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() }
            .testTag("qris_merchant_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(SoftBlueBg, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = "QRIS Merchant",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "QRIS Merchant",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimaryBlue.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "Akan Segera Hadir",
                                color = PrimaryBlue,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Terima pembayaran QRIS langsung ke rekening tokomu",
                        fontSize = 11.sp,
                        color = TextSecondaryMuted
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Buka",
                tint = TextSecondaryMuted
            )
        }
    }
}

@Composable
fun KivoBalanceCard(
    user: UserEntity?,
    isBalanceHidden: Boolean,
    onToggleBalance: () -> Unit,
    onTopUpClick: () -> Unit,
    onTransferClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onUpgradeKivoPlus: () -> Unit = {},
    onCopyAccount: (String) -> Unit
) {
    val availableBalance = user?.saldoAvailable ?: 0.0
    val holdBalance = user?.saldoHold ?: 0.0
    val isPlus = user?.isKivoPlus == true

    // High-contrast clean white and blue gradient card
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .shadow(8.dp, shape = RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryBlue)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = if (isPlus) listOf(Color(0xFF0F2B5C), Color(0xFF1D61E7)) else listOf(PrimaryBlue, PrimaryBlueDark)
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                // Header: Account Number + Tier Badge + Hide Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val userPhoneId = user?.phoneNumber?.ifBlank { "081298765432" } ?: "081298765432"
                        Text(
                            text = "ID KivoPay: $userPhoneId",
                            color = CleanWhite.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Salin ID KivoPay",
                            tint = CleanWhite,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { onCopyAccount(userPhoneId) }
                        )

                        // KivoPay+ Tier Badge
                        Surface(
                            modifier = Modifier.clickable { onUpgradeKivoPlus() },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isPlus) Color(0xFFFFD700) else Color(0x33FFFFFF)
                        ) {
                            Text(
                                text = if (isPlus) "KivoPay+ 🌟" else "Upgrade Plus 🚀",
                                color = if (isPlus) Color(0xFF0F2B5C) else Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onToggleBalance,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isBalanceHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Sembunyikan/Tampilkan Nominal",
                            tint = CleanWhite
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Saldo Utama
                Text(
                    text = "Total Saldo KivoPay",
                    color = CleanWhite.copy(alpha = 0.85f),
                    fontSize = 12.sp
                )

                Text(
                    text = if (isBalanceHidden) "Rp ••••••••" else "Rp ${formatCurrency(availableBalance)}",
                    color = CleanWhite,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.testTag("home_balance_text")
                )

                if (holdBalance > 0) {
                    Text(
                        text = if (isBalanceHidden) "Saldo Tertahan: Rp ••••••" else "Saldo Tertahan (Penarikan): Rp ${formatCurrency(holdBalance)}",
                        color = Color(0xFFFDE047),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 4 Quick Action Buttons inside Card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickActionButton(
                        label = "Top Up",
                        icon = Icons.Default.AddCard,
                        onClick = onTopUpClick
                    )
                    QuickActionButton(
                        label = "Transfer",
                        icon = Icons.Default.Send,
                        onClick = onTransferClick
                    )
                    QuickActionButton(
                        label = "Tarik Dana",
                        icon = Icons.Default.Payments,
                        onClick = onWithdrawClick
                    )
                    QuickActionButton(
                        label = "Riwayat",
                        icon = Icons.Default.History,
                        onClick = onHistoryClick
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .testTag("quick_action_$label")
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(Color.White.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
fun ServiceMenuGrid(
    onTopUp: () -> Unit,
    onTransfer: () -> Unit,
    onWithdraw: () -> Unit,
    onQris: () -> Unit,
    onPln: () -> Unit,
    onPulsa: () -> Unit,
    onPdam: () -> Unit,
    onAllServices: () -> Unit
) {
    val row1 = listOf(
        ServiceMenuItem("Top Up", Icons.Default.AddCard, PrimaryBlue, onTopUp),
        ServiceMenuItem("Transfer", Icons.Default.Send, PrimaryBlueLight, onTransfer),
        ServiceMenuItem("Tarik Dana", Icons.Default.Payments, Color(0xFF0284C7), onWithdraw),
        ServiceMenuItem("e-Wallet", Icons.Default.AccountBalanceWallet, PrimaryBlueDark, { onPulsa() })
    )

    val row2 = listOf(
        ServiceMenuItem("Listrik PLN", Icons.Default.ElectricBolt, Color(0xFFF59E0B), onPln),
        ServiceMenuItem("Pulsa & Data", Icons.Default.PhoneAndroid, Color(0xFF10B981), onPulsa),
        ServiceMenuItem("PDAM Air", Icons.Default.WaterDrop, Color(0xFF06B6D4), onPdam),
        ServiceMenuItem("Lainnya", Icons.Default.GridView, PrimaryBlue, onAllServices)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        row1.forEach { item ->
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                ServiceGridItem(item)
            }
        }
    }
    Spacer(modifier = Modifier.height(14.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        row2.forEach { item ->
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                ServiceGridItem(item)
            }
        }
    }
}

@Composable
fun AllServicesModal(
    onDismiss: () -> Unit,
    onTopUp: () -> Unit,
    onTransfer: () -> Unit,
    onWithdraw: () -> Unit,
    onQris: () -> Unit,
    onHistory: () -> Unit,
    onSelectPaymentCategory: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Semua Layanan KivoPay",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondaryMuted)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Search Field
                androidx.compose.material3.OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    textStyle = kivoInputTextStyle(),
                    colors = kivoTextFieldColors(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari", tint = PrimaryBlue) },
                    placeholder = { Text("Cari layanan (PLN, Pulsa, dll)...", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Section 1: Transfer & Keuangan
                Text("Transfer & Keuangan", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        ServiceGridItem(ServiceMenuItem("Top Up", Icons.Default.AddCard, PrimaryBlue, onTopUp))
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        ServiceGridItem(ServiceMenuItem("Transfer Bank", Icons.Default.Send, PrimaryBlueLight, onTransfer))
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        ServiceGridItem(ServiceMenuItem("Tarik Dana", Icons.Default.Payments, Color(0xFF0284C7), onWithdraw))
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        ServiceGridItem(ServiceMenuItem("Riwayat", Icons.Default.ReceiptLong, EmeraldSuccess, onHistory))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section 2: Tagihan & Pembayaran
                Text("Tagihan & Pembayaran", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        ServiceGridItem(ServiceMenuItem("Listrik PLN", Icons.Default.ElectricBolt, Color(0xFFF59E0B)) {
                            onSelectPaymentCategory("Listrik PLN")
                        })
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        ServiceGridItem(ServiceMenuItem("Pulsa & Data", Icons.Default.PhoneAndroid, Color(0xFF10B981)) {
                            onSelectPaymentCategory("Pulsa & Data")
                        })
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        ServiceGridItem(ServiceMenuItem("PDAM Air", Icons.Default.WaterDrop, Color(0xFF06B6D4)) {
                            onSelectPaymentCategory("PDAM Air")
                        })
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        ServiceGridItem(ServiceMenuItem("BPJS Kesehatan", Icons.Default.HealthAndSafety, Color(0xFFEC4899)) {
                            onSelectPaymentCategory("BPJS Kesehatan")
                        })
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        ServiceGridItem(ServiceMenuItem("Internet & TV", Icons.Default.Tv, Color(0xFF8B5CF6)) {
                            onSelectPaymentCategory("Internet & TV")
                        })
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        ServiceGridItem(ServiceMenuItem("QRIS Pay", Icons.Default.QrCodeScanner, PrimaryBlueDark, onQris))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section 3: Hiburan, Sosial & Proteksi
                Text("Sosial & Asuransi", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        ServiceGridItem(ServiceMenuItem("Zakat/Donasi", Icons.Default.VolunteerActivism, Color(0xFF10B981)) {
                            onSelectPaymentCategory("Donasi & Zakat")
                        })
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        ServiceGridItem(ServiceMenuItem("Proteksi", Icons.Default.Shield, PrimaryBlue) {
                            onSelectPaymentCategory("Proteksi KivoPay")
                        })
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Tutup", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(22.dp)
    )
}

data class ServiceMenuItem(
    val title: String,
    val icon: ImageVector,
    val iconBgColor: Color,
    val onClick: () -> Unit
)

@Composable
fun ServiceGridItem(item: ServiceMenuItem) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { item.onClick() }
            .padding(vertical = 4.dp)
            .testTag("service_menu_${item.title}")
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(item.iconBgColor.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                .border(1.dp, item.iconBgColor.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = item.iconBgColor,
                modifier = Modifier.size(26.dp)
            )
        }
        Text(
            text = item.title,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimaryDark,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
fun BannerCarousel(banners: List<BannerEntity>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(banners) { banner ->
            Card(
                modifier = Modifier
                    .width(280.dp)
                    .height(115.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryBlue)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(PrimaryBlue, PrimaryBlueDark)
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.align(Alignment.CenterStart)) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Text(
                                text = banner.badgeText,
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = banner.title,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = banner.subtitle,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            maxLines = 2,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItemRow(
    tx: TransactionEntity,
    isBalanceHidden: Boolean,
    onClick: () -> Unit
) {
    val isIncoming = tx.type == TransactionType.TOPUP || tx.type == TransactionType.TRANSFER_IN || tx.type == TransactionType.REFUND
    val amountPrefix = if (isIncoming) "+ " else "- "
    val amountColor = if (isIncoming) EmeraldSuccess else TextPrimaryDark

    val icon = when (tx.type) {
        TransactionType.TOPUP -> Icons.Default.AddCard
        TransactionType.TRANSFER_OUT -> Icons.Default.ArrowUpward
        TransactionType.TRANSFER_IN -> Icons.Default.ArrowDownward
        TransactionType.WITHDRAWAL -> Icons.Default.Payments
        TransactionType.PAYMENT -> Icons.Default.ElectricBolt
        TransactionType.REFUND -> Icons.Default.AddCard
    }

    val iconBg = if (isIncoming) EmeraldSuccess.copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)
    val iconTint = if (isIncoming) EmeraldSuccess else Color(0xFFEF4444)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
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
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(iconBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = tx.category,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = tx.category,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Text(
                        text = formatDate(tx.timestamp),
                        fontSize = 11.sp,
                        color = TextSecondaryMuted
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isBalanceHidden) "Rp ••••••" else "$amountPrefix Rp ${formatCurrency(tx.amount)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )

                val (statusBg, statusText) = when (tx.status) {
                    TransactionStatus.SUCCESS -> Pair(EmeraldSuccess.copy(alpha = 0.15f), "Sukses")
                    TransactionStatus.PENDING -> Pair(Color(0xFFF59E0B).copy(alpha = 0.15f), "Pending")
                    TransactionStatus.PROCESSING -> Pair(Color(0xFF3B82F6).copy(alpha = 0.15f), "Diproses")
                    TransactionStatus.FAILED -> Pair(Color(0xFFEF4444).copy(alpha = 0.15f), "Gagal")
                    TransactionStatus.REJECTED -> Pair(Color(0xFFEF4444).copy(alpha = 0.15f), "Ditolak")
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = statusBg,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusTextTextColor(tx.status),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

private fun statusTextTextColor(status: TransactionStatus): Color = when (status) {
    TransactionStatus.SUCCESS -> EmeraldSuccess
    TransactionStatus.PENDING -> Color(0xFFD97706)
    TransactionStatus.PROCESSING -> Color(0xFF2563EB)
    TransactionStatus.FAILED, TransactionStatus.REJECTED -> Color(0xFFDC2626)
}

fun formatCurrency(amount: Double): String {
    return String.format("%,d", amount.toLong()).replace(',', '.')
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
    return sdf.format(Date(timestamp))
}
