package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.components.PinInputDialog
import com.example.ui.components.kivoBigAmountTextStyle
import com.example.ui.components.kivoTextFieldColors
import com.example.ui.theme.CardBorderColor
import com.example.ui.theme.CleanWhite
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyBluePrimary
import com.example.ui.theme.PageBackground
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueDark
import com.example.ui.theme.RoyalBlueAccent
import com.example.ui.theme.SoftBlueBg
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawScreen(
    user: UserEntity?,
    userAvailableBalance: Double,
    configuredFee: Double = 3000.0,
    onBack: () -> Unit,
    onNavigateToChangeBank: () -> Unit = {},
    onSubmitWithdrawal: (bankName: String, accountNo: String, recipientName: String, amount: Double, pin: String) -> Unit
) {
    // Destination Mode: 0 = Rekening Terdaftar, 1 = E-Wallet
    var destinationMode by remember { mutableStateOf(0) }

    // Bank Account Target
    val targetBank = user?.userBankName?.ifBlank { "Bank Central Asia (BCA)" } ?: "Bank Central Asia (BCA)"
    val targetAccountNo = user?.userBankAccountNumber?.ifBlank { "1234567890" } ?: "1234567890"
    val targetRecipientName = user?.userBankAccountHolder?.ifBlank { user?.fullName } ?: (user?.fullName ?: "Pengguna Terdaftar")

    // E-Wallet Target
    val eWalletOptions = listOf(
        Pair("OVO", "🔥 GRATIS Fee"),
        Pair("DANA", "Biaya Rp 3.000"),
        Pair("GoPay", "Biaya Rp 3.000"),
        Pair("ShopeePay", "Biaya Rp 3.000"),
        Pair("LinkAja", "Biaya Rp 3.000")
    )
    var selectedEWalletIndex by remember { mutableStateOf(0) }
    val selectedEWalletName = eWalletOptions[selectedEWalletIndex].first

    var eWalletPhoneInput by remember(user?.phoneNumber) { mutableStateOf(user?.phoneNumber ?: "") }
    var eWalletHolderInput by remember(user?.fullName) { mutableStateOf(user?.fullName ?: "") }

    var amountInput by remember { mutableStateOf("50000") }
    var showPinSheet by remember { mutableStateOf(false) }

    val amountDouble = amountInput.toDoubleOrNull() ?: 0.0

    // OVO is strictly FREE fee as requested ("klo sesama ovo gratis fee admin")
    val isOvo = destinationMode == 1 && selectedEWalletName == "OVO"
    val effectiveFee = if (isOvo) 0.0 else configuredFee
    val totalDeduction = amountDouble + effectiveFee
    val isBalanceSufficient = userAvailableBalance >= totalDeduction

    val activeDestinationName = if (destinationMode == 0) targetBank else "E-Wallet $selectedEWalletName"
    val activeDestinationAccount = if (destinationMode == 0) targetAccountNo else eWalletPhoneInput.trim()
    val activeDestinationHolder = if (destinationMode == 0) targetRecipientName else eWalletHolderInput.trim()

    val isInputValid = amountDouble >= 50000.0 && isBalanceSufficient &&
            (destinationMode == 0 || (eWalletPhoneInput.length >= 10 && eWalletHolderInput.isNotBlank()))

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Tarik Dana / Saldo",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = if (isOvo) "Sesama OVO: Bebas Biaya Admin" else "Pencairan Real-Time via RCB Payout",
                            fontSize = 11.sp,
                            color = if (isOvo) EmeraldSuccess else PrimaryBlue
                        )
                    }
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
            // Tab Selection: Rekening Bank vs E-Wallet
            TabRow(
                selectedTabIndex = destinationMode,
                containerColor = CleanWhite,
                contentColor = PrimaryBlue,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = destinationMode == 0,
                    onClick = { destinationMode = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Rekening Bank", fontWeight = if (destinationMode == 0) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
                        }
                    }
                )
                Tab(
                    selected = destinationMode == 1,
                    onClick = { destinationMode = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("E-Wallet", fontWeight = if (destinationMode == 1) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Fee & RCB Pay Gateway Header Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CleanWhite),
                border = BorderStroke(1.dp, if (isOvo) EmeraldSuccess.copy(alpha = 0.5f) else CardBorderColor)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(if (isOvo) EmeraldSuccess.copy(alpha = 0.15f) else SoftBlueBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isOvo) Icons.Default.CheckCircle else Icons.Default.Shield,
                            contentDescription = "Fee Info",
                            tint = if (isOvo) EmeraldSuccess else PrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isOvo) "Biaya Admin: GRATIS Rp 0 (Khusus OVO)" else "Biaya Admin KivoPay: Rp ${formatCurrency(effectiveFee)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOvo) EmeraldSuccess else TextPrimaryDark
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isOvo) EmeraldSuccess.copy(alpha = 0.15f) else PrimaryBlue.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = if (isOvo) "PROMO OVO" else "RCB Pay",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOvo) EmeraldSuccess else PrimaryBlue,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isOvo) "Penarikan ke sesama OVO 100% bebas biaya admin. Saldo langsung cair real-time." else "Penarikan diproses real-time langsung ke tujuan melalui gateway kliring RCB Pay.",
                            fontSize = 11.sp,
                            color = TextSecondaryMuted,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (destinationMode == 0) {
                // TAB 0: LOCKED REGISTERED BANK ACCOUNT CARD
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rekening Bank Resmi Terdaftar",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = EmeraldSuccess.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Terkunci Aman", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CleanWhite),
                    border = BorderStroke(1.5.dp, PrimaryBlue.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(SoftBlueBg, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AccountBalance, contentDescription = "Bank", tint = PrimaryBlue, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = targetBank,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark
                                )
                                Text(
                                    text = "No. $targetAccountNo",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryBlueDark
                                )
                                Text(
                                    text = "a.n $targetRecipientName",
                                    fontSize = 12.sp,
                                    color = TextSecondaryMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CardBorderColor))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ingin ganti rekening bank tujuan?",
                                fontSize = 11.sp,
                                color = TextSecondaryMuted
                            )
                            OutlinedButton(
                                onClick = onNavigateToChangeBank,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, PrimaryBlue),
                                modifier = Modifier.testTag("nav_change_bank_button")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ajukan Ubah Rekening", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                            }
                        }
                    }
                }
            } else {
                // TAB 1: E-WALLET DESTINATION CARD (OVO, DANA, GoPay, ShopeePay, LinkAja)
                Text(
                    text = "Pilih Layanan E-Wallet Tujuan",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    eWalletOptions.forEachIndexed { index, (walletName, badge) ->
                        val isSelected = selectedEWalletIndex == index
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedEWalletIndex = index },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) PrimaryBlue.copy(alpha = 0.08f) else CleanWhite
                            ),
                            border = BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) (if (walletName == "OVO") EmeraldSuccess else PrimaryBlue) else CardBorderColor
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(
                                                if (walletName == "OVO") EmeraldSuccess.copy(alpha = 0.15f) else SoftBlueBg,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.PhoneAndroid,
                                            contentDescription = walletName,
                                            tint = if (walletName == "OVO") EmeraldSuccess else PrimaryBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = walletName,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimaryDark
                                        )
                                        Text(
                                            text = if (walletName == "OVO") "Bebas Fee Admin KivoPay" else "Pencairan instan e-wallet",
                                            fontSize = 11.sp,
                                            color = TextSecondaryMuted
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (walletName == "OVO") EmeraldSuccess.copy(alpha = 0.15f) else PrimaryBlue.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = badge,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (walletName == "OVO") EmeraldSuccess else PrimaryBlue,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Input E-Wallet Phone & Holder
                Text("Nomor HP Akun $selectedEWalletName", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = eWalletPhoneInput,
                    onValueChange = { input -> if (input.all { it.isDigit() }) eWalletPhoneInput = input },
                    placeholder = { Text("Contoh: 081234567890") },
                    colors = kivoTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Nama Pemilik Akun $selectedEWalletName", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = eWalletHolderInput,
                    onValueChange = { eWalletHolderInput = it },
                    placeholder = { Text("Nama lengkap pemilik akun") },
                    colors = kivoTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Nominal Input
            Text("Nominal Penarikan (Min Rp 50.000)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = amountInput,
                onValueChange = { input -> if (input.all { it.isDigit() }) amountInput = input },
                textStyle = kivoBigAmountTextStyle(),
                colors = kivoTextFieldColors(),
                leadingIcon = { Text("Rp ", fontWeight = FontWeight.Black, fontSize = 18.sp, color = PrimaryBlue) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("withdraw_amount_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Text(
                text = "Saldo tersedia: Rp ${formatCurrency(userAvailableBalance)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isBalanceSufficient) TextSecondaryMuted else CrimsonError,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Rincian Penarikan Breakdown
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CleanWhite),
                border = BorderStroke(1.dp, CardBorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Rincian Penarikan Dana", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tujuan Pencairan", fontSize = 12.sp, color = TextSecondaryMuted)
                        Text(activeDestinationName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Nominal Penarikan", fontSize = 12.sp, color = TextSecondaryMuted)
                        Text("Rp ${formatCurrency(amountDouble)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Biaya Admin KivoPay", fontSize = 12.sp, color = TextSecondaryMuted)
                        if (isOvo) {
                            Text("GRATIS (Rp 0)", fontSize = 12.sp, fontWeight = FontWeight.Black, color = EmeraldSuccess)
                        } else {
                            Text("Rp ${formatCurrency(effectiveFee)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CardBorderColor))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Saldo Terpotong", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                        Text(
                            "Rp ${formatCurrency(totalDeduction)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isBalanceSufficient) PrimaryBlueDark else CrimsonError
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            Button(
                onClick = { showPinSheet = true },
                enabled = isInputValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_withdraw_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isOvo) EmeraldSuccess else PrimaryBlue)
            ) {
                Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isOvo) "Tarik ke OVO (Gratis Admin)" else "Tarik Dana Sekarang",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = CleanWhite
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }

    if (showPinSheet) {
        val feeLabel = if (isOvo) "Gratis Biaya Admin" else "Biaya Rp ${formatCurrency(effectiveFee)}"
        PinInputDialog(
            title = "Otorisasi PIN Penarikan",
            subtitle = "Penarikan Rp ${formatCurrency(amountDouble)} ($feeLabel) ke $activeDestinationName ($activeDestinationAccount)",
            onDismiss = { showPinSheet = false },
            onPinSubmitted = { pin ->
                showPinSheet = false
                onSubmitWithdrawal(activeDestinationName, activeDestinationAccount, activeDestinationHolder, amountDouble, pin)
            }
        )
    }
}
