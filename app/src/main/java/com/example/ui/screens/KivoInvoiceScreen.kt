package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.KivoInvoiceData
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CardBorderColor
import com.example.ui.theme.CleanWhite
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PageBackground
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueDark
import com.example.ui.theme.SoftBlueBg
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryMuted
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KivoInvoiceScreen(
    invoice: KivoInvoiceData?,
    onBack: () -> Unit,
    onCheckStatus: (onPaid: () -> Unit) -> Unit,
    onManualConfirm: (onComplete: () -> Unit) -> Unit
) {
    if (invoice == null) {
        onBack()
        return
    }

    val context = LocalContext.current
    var isCheckingStatus by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableStateOf(900) } // 15:00 countdown timer

    // Countdown Timer
    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000L)
            secondsLeft--
        }
    }

    val minutes = secondsLeft / 60
    val secs = secondsLeft % 60
    val formattedTimer = String.format("%02d:%02d", minutes, secs)

    val isQris = invoice.channelCode.contains("qris")
    val isVa = invoice.channelCode.contains("va") || invoice.channelCode == "va"
    val isUsdt = invoice.channelCode == "usdt"
    val isPulsa = invoice.channelCode == "pulsa"

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label berhasil disalin!", Toast.LENGTH_SHORT).show()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Invoice Pembayaran KivoPay",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = "PT. KivoPay Digital Indonesia • Gateway RCB Pay",
                            fontSize = 11.sp,
                            color = TextSecondaryMuted
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
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            // CARD 1: Status & Total Nominal Tagihan
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CleanWhite),
                border = BorderStroke(1.5.dp, PrimaryBlue.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Status Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = AmberWarning.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, AmberWarning.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.HourglassEmpty,
                                contentDescription = null,
                                tint = AmberWarning,
                                modifier = Modifier
                                    .size(15.dp)
                                    .scale(pulseScale)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MENUNGGU PEMBAYARAN",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmberWarning
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Total Nominal Pembayaran",
                        fontSize = 12.sp,
                        color = TextSecondaryMuted
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Rp ${formatCurrency(invoice.totalAmount)}",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = PrimaryBlueDark
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SoftBlueBg
                    ) {
                        Text(
                            text = "Harap transfer tepat hingga 3 digit terakhir agar sistem otomatis mendeteksi.",
                            fontSize = 11.sp,
                            color = PrimaryBlue,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CardBorderColor))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Order ID Row with Copy Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("ID Pesanan (RCB Order ID):", fontSize = 11.sp, color = TextSecondaryMuted)
                            Text(invoice.orderId, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                        }

                        OutlinedButton(
                            onClick = { copyToClipboard("Order ID", invoice.orderId) },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, PrimaryBlue),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Salin ID", fontSize = 11.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // CARD 2: Channel Spesifik (QRIS / VA / USDT / Pulsa)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CleanWhite),
                border = BorderStroke(1.dp, CardBorderColor)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(SoftBlueBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val icon = when {
                                isQris -> Icons.Default.QrCodeScanner
                                isVa -> Icons.Default.AccountBalance
                                isUsdt -> Icons.Default.CurrencyBitcoin
                                else -> Icons.Default.PhoneAndroid
                            }
                            Icon(icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(invoice.channelName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                            Text("Metode Resmi RCB Pay Gateway", fontSize = 11.sp, color = TextSecondaryMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // IF QRIS: DISPLAY QRIS VIEWER
                    if (isQris) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC), RoundedCornerShape(14.dp))
                                .border(1.dp, CardBorderColor, RoundedCornerShape(14.dp))
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Scan QRIS di bawah ini dengan E-Wallet atau M-Banking",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimaryDark,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .size(220.dp)
                                    .background(CleanWhite, RoundedCornerShape(12.dp))
                                    .border(2.dp, PrimaryBlue, RoundedCornerShape(12.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!invoice.qrisUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = invoice.qrisUrl,
                                        contentDescription = "QRIS Code",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    // Fallback styled dynamic QR representation
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Default.QrCode,
                                            contentDescription = "QR Code",
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(140.dp)
                                        )
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_qris_logo_white),
                                            contentDescription = "QRIS Logo",
                                            modifier = Modifier.height(20.dp).background(Color(0xFF0F172A), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        copyToClipboard("Data QRIS", invoice.qrisString ?: invoice.orderId)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, PrimaryBlue)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Salin Data QR", fontSize = 11.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // IF VIRTUAL ACCOUNT: DISPLAY VA NUMBER BOX
                    if (isVa) {
                        val vaNum = invoice.vaNumber ?: "988123" + invoice.orderId.takeLast(8)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SoftBlueBg, RoundedCornerShape(14.dp))
                                .border(1.dp, PrimaryBlue.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                .padding(16.dp)
                        ) {
                            Text("Nomor Virtual Account:", fontSize = 11.sp, color = TextSecondaryMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = vaNum,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PrimaryBlueDark,
                                    fontFamily = FontFamily.Monospace
                                )

                                Button(
                                    onClick = { copyToClipboard("Nomor VA", vaNum) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = CleanWhite, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Salin", fontSize = 12.sp, color = CleanWhite, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.8f)))
                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Nama Rekening / Merchant:", fontSize = 11.sp, color = TextSecondaryMuted)
                            Text("KivoPay - ${invoice.customerName}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                        }
                    }

                    // IF USDT CRYPTO
                    if (isUsdt) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFEF3C7), RoundedCornerShape(14.dp))
                                .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(14.dp))
                                .padding(16.dp)
                        ) {
                            Text("Alamat Deposit USDT (TRC20):", fontSize = 11.sp, color = Color(0xFF92400E))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "TRCB9xPzKivoPay77218991LiveWallet",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF78350F)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { copyToClipboard("Alamat USDT", "TRCB9xPzKivoPay77218991LiveWallet") },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Salin Alamat USDT", fontSize = 11.sp, color = CleanWhite, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // IF PULSA
                    if (isPulsa) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SoftBlueBg, RoundedCornerShape(14.dp))
                                .padding(16.dp)
                        ) {
                            Text("Kirimkan pulsa sesuai nominal ke nomor gateway resmi:", fontSize = 12.sp, color = TextPrimaryDark)
                            Text("0812-9876-5432 (KivoPay Gateway)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryBlueDark)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // CARD 3: Countdown Timer & Panduan
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CleanWhite),
                border = BorderStroke(1.dp, CardBorderColor)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Sisa Waktu Pembayaran:", fontSize = 12.sp, color = TextSecondaryMuted)
                        Text(
                            text = formattedTimer,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = if (secondsLeft < 300) Color.Red else PrimaryBlue
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryBlue.copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Otomatis 24/7", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ACTION BUTTON 1: CEK STATUS PEMBAYARAN REAL-TIME DARI RCB API
            Button(
                onClick = {
                    isCheckingStatus = true
                    onCheckStatus {
                        isCheckingStatus = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("check_payment_status_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (isCheckingStatus) {
                    CircularProgressIndicator(color = CleanWhite, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Menghubungi Server RCB...", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CleanWhite)
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = CleanWhite, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cek Status Pembayaran (Real-Time RCB)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CleanWhite)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ACTION BUTTON 2: SAYA SUDAH BAYAR (VERIFIKASI & SELESAIKAN)
            OutlinedButton(
                onClick = {
                    onManualConfirm {
                        onBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("already_paid_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, PrimaryBlue)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Saya Sudah Bayar (Kreditkan Saldo)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
            }

            // OPTIONAL: OPEN EXTERNAL CHECKOUT URL
            if (!invoice.paymentUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(invoice.paymentUrl))
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CardBorderColor)
                ) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = TextSecondaryMuted, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Buka Link Checkout RCB Web", fontSize = 12.sp, color = TextSecondaryMuted, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
