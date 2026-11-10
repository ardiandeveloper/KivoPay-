package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
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
    // Destination is LOCKED exclusively to user's registered & verified bank account as per prompt
    val targetBank = user?.userBankName?.ifBlank { "Bank Central Asia (BCA)" } ?: "Bank Central Asia (BCA)"
    val targetAccountNo = user?.userBankAccountNumber?.ifBlank { "1234567890" } ?: "1234567890"
    val targetRecipientName = user?.userBankAccountHolder?.ifBlank { user?.fullName } ?: (user?.fullName ?: "Pengguna Terdaftar")

    var amountInput by remember { mutableStateOf("50000") }
    var showPinSheet by remember { mutableStateOf(false) }

    val amountDouble = amountInput.toDoubleOrNull() ?: 0.0
    val totalDeduction = amountDouble + configuredFee
    val isBalanceSufficient = userAvailableBalance >= totalDeduction

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tarik Dana ke Rekening",
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
            // Fee & RCB Pay Gateway Header Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CleanWhite),
                border = BorderStroke(1.dp, CardBorderColor)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(SoftBlueBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Fee Info",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Biaya Layanan Admin: Rp ${formatCurrency(configuredFee)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = PrimaryBlue.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "RCB Pay",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Penarikan diproses real-time langsung ke rekening Anda melalui gateway kliring RCB Pay.",
                            fontSize = 11.sp,
                            color = TextSecondaryMuted,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // LOCKED REGISTERED BANK ACCOUNT CARD (Cannot be changed directly here!)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rekening Resmi Terdaftar",
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
                        Text("Nominal Penarikan", fontSize = 12.sp, color = TextSecondaryMuted)
                        Text("Rp ${formatCurrency(amountDouble)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Biaya Admin KivoPay", fontSize = 12.sp, color = TextSecondaryMuted)
                        Text("Rp ${formatCurrency(configuredFee)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CardBorderColor))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Saldo Terpotong", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                        Text(
                            "Rp ${formatCurrency(totalDeduction)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = CrimsonError
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            Button(
                onClick = { showPinSheet = true },
                enabled = amountDouble >= 50000.0 && isBalanceSufficient,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_withdraw_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tarik Dana Sekarang", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CleanWhite)
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }

    if (showPinSheet) {
        PinInputDialog(
            title = "Otorisasi PIN Penarikan",
            subtitle = "Penarikan Rp ${formatCurrency(amountDouble)} (Biaya Rp ${formatCurrency(configuredFee)}) ke $targetBank ($targetAccountNo)",
            onDismiss = { showPinSheet = false },
            onPinSubmitted = { pin ->
                showPinSheet = false
                onSubmitWithdrawal(targetBank, targetAccountNo, targetRecipientName, amountDouble, pin)
            }
        )
    }
}
