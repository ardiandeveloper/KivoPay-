package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
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
import com.example.ui.theme.NavyBluePrimary
import com.example.ui.theme.RoyalBlueAccent
import com.example.ui.theme.TextSecondaryMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawScreen(
    userAvailableBalance: Double,
    configuredFee: Double = 3000.0,
    onBack: () -> Unit,
    onSubmitWithdrawal: (bankName: String, accountNo: String, recipientName: String, amount: Double, pin: String) -> Unit
) {
    var selectedBank by remember { mutableStateOf("Bank BCA") }
    var bankExpanded by remember { mutableStateOf(false) }
    var accountNoInput by remember { mutableStateOf("0889218274") }
    var recipientNameInput by remember { mutableStateOf("Alex Wijaya") }
    var amountInput by remember { mutableStateOf("50000") }
    var showPinSheet by remember { mutableStateOf(false) }

    val bankOptions = listOf(
        "Bank BCA", "Bank Mandiri", "Bank BNI", "Bank BRI",
        "Bank CIMB Niaga", "Bank Permata", "Bank Danamon", "Bank BSI"
    )

    val amountDouble = amountInput.toDoubleOrNull() ?: 0.0
    val totalDeduction = amountDouble + configuredFee
    val isBalanceSufficient = userAvailableBalance >= totalDeduction

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tarik Dana / Withdrawal",
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
            // Fee Info Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = RoyalBlueAccent.copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Fee Info",
                        tint = RoyalBlueAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Biaya Penarikan Flat Rp ${formatCurrency(configuredFee)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Penarikan diproses secara resmi via transfer bank dengan sistem verifikasi backend.",
                            fontSize = 11.sp,
                            color = TextSecondaryMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bank Selector
            Text("Bank Tujuan Penarikan", fontSize = 14.sp, fontWeight = FontWeight.Bold)
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

            Spacer(modifier = Modifier.height(14.dp))

            // Account Number
            Text("Nomor Rekening Bank", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = com.example.ui.theme.TextPrimaryDark)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = accountNoInput,
                onValueChange = { accountNoInput = it },
                textStyle = com.example.ui.components.kivoInputTextStyle(),
                colors = com.example.ui.components.kivoTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("withdraw_account_no_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Account Owner Name
            Text("Nama Pemilik Rekening", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = com.example.ui.theme.TextPrimaryDark)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = recipientNameInput,
                onValueChange = { recipientNameInput = it },
                textStyle = com.example.ui.components.kivoInputTextStyle(),
                colors = com.example.ui.components.kivoTextFieldColors(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Nominal Input
            Text("Nominal Penarikan (Min Rp 50.000)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = com.example.ui.theme.TextPrimaryDark)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = amountInput,
                onValueChange = { input -> if (input.all { it.isDigit() }) amountInput = input },
                textStyle = com.example.ui.components.kivoBigAmountTextStyle(),
                colors = com.example.ui.components.kivoTextFieldColors(),
                leadingIcon = { Text("Rp ", fontWeight = FontWeight.Black, fontSize = 18.sp, color = RoyalBlueAccent) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("withdraw_amount_input"),
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

            Spacer(modifier = Modifier.height(24.dp))

            // Rincian Penarikan Breakdown (Required by Prompt Rule 8)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Rincian Penarikan Dana", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Nominal Penarikan", fontSize = 13.sp, color = TextSecondaryMuted)
                        Text("Rp ${formatCurrency(amountDouble)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Biaya Layanan Penarikan", fontSize = 13.sp, color = TextSecondaryMuted)
                        Text("Rp ${formatCurrency(configuredFee)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RoyalBlueAccent)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Saldo Terpotong", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Rp ${formatCurrency(totalDeduction)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = { showPinSheet = true },
                enabled = amountDouble >= 50000.0 && isBalanceSufficient && accountNoInput.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_withdraw_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlueAccent)
            ) {
                Text("Tarik Dana Sekarang", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showPinSheet) {
        PinInputDialog(
            title = "Otorisasi PIN Penarikan",
            subtitle = "Penarikan Rp ${formatCurrency(amountDouble)} (Biaya Rp ${formatCurrency(configuredFee)}) ke $selectedBank",
            onDismiss = { showPinSheet = false },
            onPinSubmitted = { pin ->
                showPinSheet = false
                onSubmitWithdrawal(selectedBank, accountNoInput, recipientNameInput, amountDouble, pin)
            }
        )
    }
}
