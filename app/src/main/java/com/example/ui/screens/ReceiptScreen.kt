package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionStatus
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.LightBgBackground
import com.example.ui.theme.NavyBluePrimary
import com.example.ui.theme.RoyalBlueAccent
import com.example.ui.theme.TextSecondaryMuted

@Composable
fun ReceiptScreen(
    transaction: TransactionEntity?,
    onDone: () -> Unit,
    onShowToast: (String) -> Unit
) {
    if (transaction == null) return
    val context = LocalContext.current

    val (statusIcon, statusColor, statusLabel) = when (transaction.status) {
        TransactionStatus.SUCCESS -> Triple(Icons.Default.CheckCircle, EmeraldSuccess, "Transaksi Berhasil")
        TransactionStatus.PENDING -> Triple(Icons.Default.HourglassTop, Color(0xFFF59E0B), "Penarikan Diproses")
        TransactionStatus.PROCESSING -> Triple(Icons.Default.HourglassTop, Color(0xFF3B82F6), "Sedang Diproses")
        TransactionStatus.FAILED -> Triple(Icons.Default.Close, Color(0xFFEF4444), "Transaksi Gagal")
        TransactionStatus.REJECTED -> Triple(Icons.Default.Close, Color(0xFFEF4444), "Penarikan Ditolak")
    }

    Scaffold(
        containerColor = LightBgBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDone) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup")
                }
            }

            // Receipt Paper Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("receipt_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Logo & Status
                    Image(
                        painter = painterResource(id = R.drawable.kivopay_logo),
                        contentDescription = "KivoPay Logo",
                        modifier = Modifier
                            .height(36.dp)
                            .padding(bottom = 8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(statusColor.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = statusLabel,
                            tint = statusColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = statusLabel,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )

                    Text(
                        text = transaction.category,
                        fontSize = 13.sp,
                        color = TextSecondaryMuted,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Text(
                        text = "Rp ${formatCurrency(transaction.totalAmount)}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Receipt Details Table
                    ReceiptDetailRow("ID Transaksi", transaction.transactionId, isCopyable = true, onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("ID Transaksi", transaction.transactionId))
                        onShowToast("ID Transaksi tersalin!")
                    })

                    ReceiptDetailRow("Waktu & Tanggal", formatDate(transaction.timestamp))

                    if (transaction.recipientName.isNotEmpty()) {
                        ReceiptDetailRow("Penerima", transaction.recipientName)
                    }

                    if (transaction.recipientAccount.isNotEmpty()) {
                        ReceiptDetailRow("No. Rekening / ID", transaction.recipientAccount)
                    }

                    if (transaction.bankName.isNotEmpty()) {
                        ReceiptDetailRow("Bank / Provider", transaction.bankName)
                    }

                    if (transaction.paymentMethod.isNotEmpty()) {
                        ReceiptDetailRow("Metode Pembayaran", transaction.paymentMethod)
                    }

                    ReceiptDetailRow("Nominal Pokok", "Rp ${formatCurrency(transaction.amount)}")
                    ReceiptDetailRow(
                        "Biaya Layanan",
                        if (transaction.serviceFee == 0.0) "GRATIS" else "Rp ${formatCurrency(transaction.serviceFee)}"
                    )

                    if (transaction.notes.isNotEmpty()) {
                        ReceiptDetailRow("Catatan", transaction.notes)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NavyBluePrimary.copy(alpha = 0.08f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Struk sah diterbitkan secara elektronik oleh KivoPay Digital Payment System.",
                            fontSize = 10.sp,
                            color = NavyBluePrimary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons: Share & Done
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onShowToast("Struk transaksi berhasil disalin / dibagikan!") },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Bagikan")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bagikan")
                }

                Button(
                    onClick = onDone,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("receipt_done_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlueAccent)
                ) {
                    Text("Selesai", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ReceiptDetailRow(
    label: String,
    value: String,
    isCopyable: Boolean = false,
    onCopy: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = TextSecondaryMuted)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (isCopyable) {
                IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Salin",
                        tint = RoyalBlueAccent,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
