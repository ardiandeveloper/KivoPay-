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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.RcbChannelInfo
import com.example.data.remote.RcbChannels
import com.example.ui.components.kivoBigAmountTextStyle
import com.example.ui.components.kivoTextFieldColors
import com.example.ui.theme.CardBorderColor
import com.example.ui.theme.CleanWhite
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
fun TopUpScreen(
    userBalance: Double,
    onBack: () -> Unit,
    onCreateInvoice: (amount: Double, channelCode: String, channelName: String) -> Unit
) {
    var amountInput by remember { mutableStateOf("100000") }
    var selectedChannel by remember { mutableStateOf(RcbChannels.allChannels.first()) }
    var selectedCategoryIndex by remember { mutableStateOf(0) }
    var isSubmitting by remember { mutableStateOf(false) }

    val presetNominals = listOf(20000.0, 50000.0, 100000.0, 200000.0, 500000.0, 1000000.0)

    val categoryTabs = listOf("Semua (14)", "QRIS & E-Wallet", "Virtual Account", "Kripto & Pulsa")

    val filteredChannels = remember(selectedCategoryIndex) {
        when (selectedCategoryIndex) {
            1 -> RcbChannels.allChannels.filter { it.category == "QRIS & E-Wallet" }
            2 -> RcbChannels.allChannels.filter { it.category == "Virtual Account Bank" }
            3 -> RcbChannels.allChannels.filter { it.category == "Kripto & Pulsa" }
            else -> RcbChannels.allChannels
        }
    }

    val amountDouble = amountInput.toDoubleOrNull() ?: 0.0
    val serviceFee = 0.0 // Promo Bebas Biaya Layanan KivoPay
    val totalPayment = amountDouble + serviceFee

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Top Up Saldo KivoPay",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = "Terintegrasi Sistem Pembayaran Digital Instan",
                            fontSize = 11.sp,
                            color = PrimaryBlue
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
            // Balance Preview Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CleanWhite),
                border = BorderStroke(1.dp, CardBorderColor)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Saldo KivoPay Anda:", fontSize = 12.sp, color = TextSecondaryMuted)
                        Text("Rp ${formatCurrency(userBalance)}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = PrimaryBlueDark)
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = EmeraldSuccess.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "Verifikasi Instan",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSuccess,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Nominal Input
            Text(
                text = "Nominal Deposit (Min Rp 10.000)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = amountInput,
                onValueChange = { input ->
                    if (input.all { it.isDigit() }) amountInput = input
                },
                textStyle = kivoBigAmountTextStyle(),
                colors = kivoTextFieldColors(),
                leadingIcon = { Text("Rp ", fontWeight = FontWeight.Black, fontSize = 18.sp, color = PrimaryBlue) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("topup_nominal_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Preset Nominal Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(presetNominals) { nominal ->
                    val isSelected = amountDouble == nominal
                    Surface(
                        modifier = Modifier
                            .clickable { amountInput = nominal.toLong().toString() },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) PrimaryBlue else CleanWhite,
                        border = BorderStroke(1.dp, if (isSelected) PrimaryBlue else CardBorderColor)
                    ) {
                        Text(
                            text = "Rp ${formatCurrency(nominal)}",
                            color = if (isSelected) CleanWhite else TextPrimaryDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // PAYMENT CHANNELS SELECTOR
            Text(
                text = "Pilih Metode Pembayaran Top Up",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
            Text(
                text = "Pilih saluran pembayaran resmi untuk menerbitkan invoice KivoPay",
                fontSize = 11.sp,
                color = TextSecondaryMuted,
                modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
            )

            // Category Filter Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedCategoryIndex,
                containerColor = CleanWhite,
                contentColor = PrimaryBlue,
                edgePadding = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, CardBorderColor, RoundedCornerShape(10.dp))
            ) {
                categoryTabs.forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedCategoryIndex == idx,
                        onClick = { selectedCategoryIndex = idx },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedCategoryIndex == idx) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Channels List
            filteredChannels.forEach { channel ->
                val isSelected = selectedChannel.code == channel.code
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { selectedChannel = channel }
                        .testTag("channel_${channel.code}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) PrimaryBlue.copy(alpha = 0.06f) else CleanWhite
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) PrimaryBlue else CardBorderColor
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        if (isSelected) PrimaryBlue.copy(alpha = 0.15f) else SoftBlueBg,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                val icon = when (channel.iconType) {
                                    "QRIS" -> Icons.Default.QrCodeScanner
                                    "VA" -> Icons.Default.AccountBalance
                                    "CRYPTO" -> Icons.Default.CurrencyBitcoin
                                    else -> Icons.Default.PhoneAndroid
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = channel.name,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = channel.name,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        color = TextPrimaryDark
                                    )
                                    if (channel.isRecommended) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = EmeraldSuccess.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "REKOMENDASI",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldSuccess,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = channel.description,
                                    fontSize = 11.sp,
                                    color = TextSecondaryMuted,
                                    lineHeight = 14.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedChannel = channel },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Invoice Summary Breakdown
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CleanWhite),
                border = BorderStroke(1.dp, CardBorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Rincian Tagihan Invoice",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Nominal Top Up", fontSize = 12.sp, color = TextSecondaryMuted)
                        Text("Rp ${formatCurrency(amountDouble)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Saluran Pembayaran", fontSize = 12.sp, color = TextSecondaryMuted)
                        Text(selectedChannel.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PrimaryBlue)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Biaya Layanan Gateway", fontSize = 12.sp, color = TextSecondaryMuted)
                        Text("GRATIS (Promo)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CardBorderColor))
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Pembayaran", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                        Text(
                            "Rp ${formatCurrency(totalPayment)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryBlueDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Button: Buat Invoice Pembayaran RCB
            Button(
                onClick = {
                    isSubmitting = true
                    onCreateInvoice(amountDouble, selectedChannel.code, selectedChannel.name)
                },
                enabled = amountDouble >= 10000.0 && !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_topup_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = CleanWhite, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Menerbitkan Invoice Pembayaran...", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CleanWhite)
                } else {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = CleanWhite, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Buat Invoice Pembayaran",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = CleanWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
