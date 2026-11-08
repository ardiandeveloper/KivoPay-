package com.example.ui.screens

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.example.data.model.IndonesianBanks
import com.example.ui.components.PinInputDialog
import com.example.ui.components.kivoBigAmountTextStyle
import com.example.ui.components.kivoInputTextStyle
import com.example.ui.components.kivoTextFieldColors
import com.example.ui.theme.CardBorderColor
import com.example.ui.theme.CleanWhite
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PageBackground
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueDark
import com.example.ui.theme.SoftBlueBg
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    userAvailableBalance: Double,
    onBack: () -> Unit,
    onExecuteTransfer: (destination: String, bankName: String, amount: Double, notes: String, pin: String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Sesama KivoPay (ID HP), 1: Bank Lain
    var destinationInput by remember { mutableStateOf("081298765432") }
    var recipientNameLookup by remember { mutableStateOf("Siti Rahma (Pengguna KivoPay)") }
    var selectedBank by remember { mutableStateOf("Bank Central Asia (BCA)") }
    var bankSearchQuery by remember { mutableStateOf("") }
    var bankExpanded by remember { mutableStateOf(false) }
    var amountInput by remember { mutableStateOf("50000") }
    var notesInput by remember { mutableStateOf("") }
    var showPinSheet by remember { mutableStateOf(false) }

    val filteredBanks = remember(bankSearchQuery) {
        if (bankSearchQuery.isBlank()) {
            IndonesianBanks.allBanks
        } else {
            IndonesianBanks.allBanks.filter { it.contains(bankSearchQuery, ignoreCase = true) }
        }
    }

    val amountDouble = amountInput.toDoubleOrNull() ?: 0.0
    val bankName = if (selectedTab == 0) "KivoPay" else selectedBank
    val serviceFee = if (selectedTab == 0) 0.0 else 2500.0
    val totalDeduction = amountDouble + serviceFee
    val isBalanceSufficient = userAvailableBalance >= totalDeduction

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Transfer Saldo",
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
            // Tab Switcher: Sesama KivoPay vs Antar Bank
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CleanWhite,
                contentColor = PrimaryBlue
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        destinationInput = "081298765432"
                        recipientNameLookup = "Siti Rahma (Pengguna KivoPay)"
                    },
                    text = { Text("Sesama KivoPay (Gratis)", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        destinationInput = "5271829102"
                        recipientNameLookup = "PT Mitra Rekening Penerima"
                    },
                    text = { Text("Ke Bank Lain", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (selectedTab == 1) {
                // Bank Selector Dropdown from all Indonesian Banks
                Text(
                    text = "Pilih Bank Tujuan Transfer",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
                Spacer(modifier = Modifier.height(6.dp))

                ExposedDropdownMenuBox(
                    expanded = bankExpanded,
                    onExpandedChange = { bankExpanded = !bankExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedBank,
                        onValueChange = {},
                        readOnly = true,
                        textStyle = kivoInputTextStyle(),
                        colors = kivoTextFieldColors(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bankExpanded) },
                        leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = PrimaryBlue) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = bankExpanded,
                        onDismissRequest = { bankExpanded = false },
                        modifier = Modifier.height(280.dp)
                    ) {
                        OutlinedTextField(
                            value = bankSearchQuery,
                            onValueChange = { bankSearchQuery = it },
                            placeholder = { Text("Cari nama bank di Indonesia...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryBlue) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            singleLine = true,
                            textStyle = kivoInputTextStyle(),
                            colors = kivoTextFieldColors()
                        )

                        filteredBanks.take(50).forEach { bank ->
                            DropdownMenuItem(
                                text = { Text(bank, color = TextPrimaryDark, fontSize = 13.sp) },
                                onClick = {
                                    selectedBank = bank
                                    bankExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Recipient Account Input - Explicitly ID KivoPay (Nomor Handphone) for e-wallet
            Text(
                text = if (selectedTab == 0) "ID KivoPay (Nomor Handphone Penerima)" else "Nomor Rekening Bank Tujuan",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = destinationInput,
                onValueChange = { input -> if (input.all { it.isDigit() }) destinationInput = input },
                textStyle = kivoInputTextStyle(),
                colors = kivoTextFieldColors(),
                placeholder = {
                    Text(if (selectedTab == 0) "Contoh: 081234567890" else "Contoh: 1234567890")
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (selectedTab == 0) Icons.Default.Phone else Icons.Default.Person,
                        contentDescription = "Penerima",
                        tint = PrimaryBlue
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transfer_destination_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Real Lookup Badge
            if (destinationInput.length >= 8) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldSuccess.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Penerima Terverifikasi: $recipientNameLookup",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSuccess
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Nominal Input
            Text(
                text = "Nominal Transfer",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = amountInput,
                onValueChange = { input -> if (input.all { it.isDigit() }) amountInput = input },
                textStyle = kivoBigAmountTextStyle(),
                colors = kivoTextFieldColors(),
                leadingIcon = { Text("Rp ", fontWeight = FontWeight.Black, fontSize = 18.sp, color = PrimaryBlue) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transfer_amount_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Text(
                text = "Saldo tersedia: Rp ${formatCurrency(userAvailableBalance)}",
                fontSize = 11.sp,
                color = if (isBalanceSufficient) TextSecondaryMuted else Color(0xFFEF4444),
                modifier = Modifier.padding(top = 4.dp)
            )

            // Quick Nominal Chips
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(20000, 50000, 100000, 200000).forEach { quickNominal ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(SoftBlueBg, RoundedCornerShape(8.dp))
                            .border(1.dp, PrimaryBlue.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .clickable { amountInput = quickNominal.toString() }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${quickNominal / 1000}rb",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notes / Catatan
            Text("Catatan Transfer (Opsional)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = notesInput,
                onValueChange = { notesInput = it },
                textStyle = kivoInputTextStyle(),
                colors = kivoTextFieldColors(),
                placeholder = { Text("Contoh: Bayar makan siang") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Cost Breakdown Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CleanWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Rincian Transaksi", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tujuan", fontSize = 12.sp, color = TextSecondaryMuted)
                        Text(
                            if (selectedTab == 0) "Sesama KivoPay" else selectedBank,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimaryDark
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Nominal", fontSize = 12.sp, color = TextSecondaryMuted)
                        Text("Rp ${formatCurrency(amountDouble)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Biaya Layanan", fontSize = 12.sp, color = TextSecondaryMuted)
                        Text(
                            if (serviceFee == 0.0) "GRATIS" else "Rp ${formatCurrency(serviceFee)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (serviceFee == 0.0) EmeraldSuccess else PrimaryBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CardBorderColor))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Pembayaran", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                        Text("Rp ${formatCurrency(totalDeduction)}", fontSize = 15.sp, fontWeight = FontWeight.Black, color = PrimaryBlueDark)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { showPinSheet = true },
                enabled = amountDouble >= 10000.0 && isBalanceSufficient && destinationInput.length >= 8,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_transfer_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Lanjutkan Transfer", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CleanWhite)
            }
        }
    }

    if (showPinSheet) {
        PinInputDialog(
            title = "Konfirmasi PIN Keamanan",
            subtitle = "Transfer Rp ${formatCurrency(amountDouble)} ke $recipientNameLookup",
            onDismiss = { showPinSheet = false },
            onPinSubmitted = { pin ->
                showPinSheet = false
                onExecuteTransfer(destinationInput, bankName, amountDouble, notesInput, pin)
            }
        )
    }
}
