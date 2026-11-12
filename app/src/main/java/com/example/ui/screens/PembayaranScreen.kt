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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PinInputDialog
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoyalBlueAccent
import com.example.ui.theme.TextSecondaryMuted

data class PayCategory(
    val name: String,
    val icon: ImageVector,
    val defaultFee: Double = 2500.0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PembayaranScreen(
    userAvailableBalance: Double,
    initialCategoryName: String = "Listrik PLN",
    userFullName: String = "",
    onBack: () -> Unit,
    onExecutePayment: (category: String, customerNo: String, amount: Double, fee: Double, pin: String) -> Unit
) {
    val categories = listOf(
        PayCategory("Listrik PLN", Icons.Default.ElectricBolt, 2500.0),
        PayCategory("PDAM Air", Icons.Default.WaterDrop, 2500.0),
        PayCategory("Pulsa & Data", Icons.Default.PhoneAndroid, 1500.0),
        PayCategory("BPJS Kesehatan", Icons.Default.HealthAndSafety, 2500.0),
        PayCategory("Internet & TV", Icons.Default.Tv, 2500.0)
    )

    var selectedCat by remember(initialCategoryName) {
        mutableStateOf(categories.find { it.name.equals(initialCategoryName, ignoreCase = true) } ?: categories[0])
    }
    var customerNoInput by remember { mutableStateOf("530099881122") }
    var amountInput by remember { mutableStateOf("100000") }
    var showPinSheet by remember { mutableStateOf(false) }

    val amountDouble = amountInput.toDoubleOrNull() ?: 0.0
    val totalDeduction = amountDouble + selectedCat.defaultFee
    val isBalanceSufficient = userAvailableBalance >= totalDeduction

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pembayaran Tagihan & Pulsa",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = com.example.ui.theme.TextPrimaryDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = com.example.ui.theme.TextPrimaryDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = com.example.ui.theme.PageBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Pilih Kategori Pembayaran", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = com.example.ui.theme.TextPrimaryDark)
            Spacer(modifier = Modifier.height(10.dp))

            // Category Selector Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(categories) { cat ->
                    val isSelected = selectedCat.name == cat.name
                    Surface(
                        modifier = Modifier.clickable { selectedCat = cat },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) RoyalBlueAccent else Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) RoyalBlueAccent else com.example.ui.theme.CardBorderColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = cat.icon,
                                contentDescription = cat.name,
                                tint = if (isSelected) Color.White else RoyalBlueAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = cat.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else com.example.ui.theme.TextPrimaryDark
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Customer ID / Meter ID
            Text(
                text = "Nomor Pelanggan / ID Meter / No. HP",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = com.example.ui.theme.TextPrimaryDark
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = customerNoInput,
                onValueChange = { customerNoInput = it },
                textStyle = com.example.ui.components.kivoInputTextStyle(),
                colors = com.example.ui.components.kivoTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("payment_customer_no_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            if (customerNoInput.length >= 8) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Valid", tint = EmeraldSuccess, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Tagihan Ditemukan: ${selectedCat.name} (A.N ${userFullName.ifBlank { "Pelanggan KivoPay" }})",
                        fontSize = 12.sp,
                        color = EmeraldSuccess,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nominal / Package Amount
            Text("Nominal Pembayaran / Token", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = com.example.ui.theme.TextPrimaryDark)
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = amountInput,
                onValueChange = { input -> if (input.all { it.isDigit() }) amountInput = input },
                textStyle = com.example.ui.components.kivoBigAmountTextStyle(),
                colors = com.example.ui.components.kivoTextFieldColors(),
                leadingIcon = { Text("Rp ", fontWeight = FontWeight.Black, fontSize = 18.sp, color = RoyalBlueAccent) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("payment_amount_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Text(
                text = "Saldo Anda: Rp ${formatCurrency(userAvailableBalance)}",
                fontSize = 11.sp,
                color = if (isBalanceSufficient) TextSecondaryMuted else Color(0xFFEF4444),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Summary Breakdown
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Rincian Tagihan", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(selectedCat.name, fontSize = 13.sp, color = TextSecondaryMuted)
                        Text("Rp ${formatCurrency(amountDouble)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Biaya Admin", fontSize = 13.sp, color = TextSecondaryMuted)
                        Text("Rp ${formatCurrency(selectedCat.defaultFee)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Tagihan", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Rp ${formatCurrency(totalDeduction)}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = RoyalBlueAccent)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = { showPinSheet = true },
                enabled = amountDouble > 0 && isBalanceSufficient && customerNoInput.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_payment_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlueAccent)
            ) {
                Text("Bayar Sekarang", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showPinSheet) {
        PinInputDialog(
            title = "Otorisasi Pembayaran",
            subtitle = "Bayar ${selectedCat.name} ID $customerNoInput sebesar Rp ${formatCurrency(totalDeduction)}",
            onDismiss = { showPinSheet = false },
            onPinSubmitted = { pin ->
                showPinSheet = false
                onExecutePayment(selectedCat.name, customerNoInput, amountDouble, selectedCat.defaultFee, pin)
            }
        )
    }
}
