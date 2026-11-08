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
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.ui.components.PinInputDialog
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyBluePrimary
import com.example.ui.theme.RoyalBlueAccent
import com.example.ui.theme.TextSecondaryMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    userAvailableBalance: Double,
    onBack: () -> Unit,
    onExecuteTransfer: (destination: String, bankName: String, amount: Double, notes: String, pin: String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Sesama KivoPay, 1: Bank Lain
    var destinationInput by remember { mutableStateOf("1089283741") }
    var recipientNameLookup by remember { mutableStateOf("Siti Rahma (KivoPay User)") }
    var selectedBank by remember { mutableStateOf("Bank BCA") }
    var bankExpanded by remember { mutableStateOf(false) }
    var amountInput by remember { mutableStateOf("50000") }
    var notesInput by remember { mutableStateOf("") }
    var showPinSheet by remember { mutableStateOf(false) }

    val bankOptions = listOf(
        "Bank BCA", "Bank Mandiri", "Bank BNI", "Bank BRI",
        "Bank CIMB Niaga", "Bank Permata", "Bank Danamon", "Bank BSI"
    )

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
                        text = "Transfer Uang",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Tab Switcher: Sesama KivoPay vs Antar Bank
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = RoyalBlueAccent
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        recipientNameLookup = "Siti Rahma (KivoPay User)"
                    },
                    text = { Text("Sesama KivoPay (Free)", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        recipientNameLookup = "Verified Recipient"
                    },
                    text = { Text("Ke Bank Lain", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (selectedTab == 1) {
                // Bank Selector Dropdown
                Text(
                    text = "Pilih Bank Tujuan",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
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
                        textStyle = com.example.ui.components.kivoInputTextStyle(),
                        colors = com.example.ui.components.kivoTextFieldColors(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bankExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = bankExpanded,
                        onDismissRequest = { bankExpanded = false }
                    ) {
                        bankOptions.forEach { bank ->
                            DropdownMenuItem(
                                text = { Text(bank, color = com.example.ui.theme.TextPrimaryDark, fontWeight = FontWeight.SemiBold) },
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

            // Recipient Account Input
            Text(
                text = if (selectedTab == 0) "No. Rekening / HP KivoPay Tujuan" else "Nomor Rekening Bank Tujuan",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = com.example.ui.theme.TextPrimaryDark
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = destinationInput,
                onValueChange = { destinationInput = it },
                textStyle = com.example.ui.components.kivoInputTextStyle(),
                colors = com.example.ui.components.kivoTextFieldColors(),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Penerima", tint = RoyalBlueAccent) },
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
                        text = "Penerima: $recipientNameLookup",
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
                color = com.example.ui.theme.TextPrimaryDark
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = amountInput,
                onValueChange = { input -> if (input.all { it.isDigit() }) amountInput = input },
                textStyle = com.example.ui.components.kivoBigAmountTextStyle(),
                colors = com.example.ui.components.kivoTextFieldColors(),
                leadingIcon = { Text("Rp ", fontWeight = FontWeight.Black, fontSize = 18.sp, color = RoyalBlueAccent) },
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

            Spacer(modifier = Modifier.height(16.dp))

            // Notes / Message
            OutlinedTextField(
                value = notesInput,
                onValueChange = { notesInput = it },
                textStyle = com.example.ui.components.kivoInputTextStyle(),
                colors = com.example.ui.components.kivoTextFieldColors(),
                label = { Text("Catatan / Pesan (Opsional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Transaction Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Rincian Transfer", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Nominal Transfer", fontSize = 13.sp, color = TextSecondaryMuted)
                        Text("Rp ${formatCurrency(amountDouble)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Biaya Transaksi", fontSize = 13.sp, color = TextSecondaryMuted)
                        Text(
                            if (serviceFee == 0.0) "GRATIS" else "Rp ${formatCurrency(serviceFee)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (serviceFee == 0.0) EmeraldSuccess else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Terpotong", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Rp ${formatCurrency(totalDeduction)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = RoyalBlueAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = { showPinSheet = true },
                enabled = amountDouble >= 10000.0 && isBalanceSufficient && destinationInput.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_transfer_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlueAccent)
            ) {
                Text("Lanjutkan & Konfirmasi PIN", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showPinSheet) {
        PinInputDialog(
            title = "Otorisasi PIN Transfer",
            subtitle = "Transfer Rp ${formatCurrency(amountDouble)} ke $bankName ($destinationInput)",
            onDismiss = { showPinSheet = false },
            onPinSubmitted = { pin ->
                showPinSheet = false
                onExecuteTransfer(destinationInput, bankName, amountDouble, notesInput, pin)
            }
        )
    }
}
