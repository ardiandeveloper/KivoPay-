package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.LightIceBlue
import com.example.ui.theme.NavyBluePrimary
import com.example.ui.theme.RoyalBlueAccent
import com.example.ui.theme.TextSecondaryMuted

@Composable
fun AktivitasScreen(
    transactions: List<TransactionEntity>,
    isBalanceHidden: Boolean,
    onSelectTransaction: (TransactionEntity) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("Semua") }
    var searchQuery by remember { mutableStateOf("") }

    val filterOptions = listOf("Semua", "Top Up", "Transfer", "Tarik Dana", "Pembayaran", "Refund")

    val filteredTransactions = transactions.filter { tx ->
        val matchesCategory = when (selectedFilter) {
            "Top Up" -> tx.type == TransactionType.TOPUP
            "Transfer" -> tx.type == TransactionType.TRANSFER_OUT || tx.type == TransactionType.TRANSFER_IN
            "Tarik Dana" -> tx.type == TransactionType.WITHDRAWAL
            "Pembayaran" -> tx.type == TransactionType.PAYMENT
            "Refund" -> tx.type == TransactionType.REFUND
            else -> true
        }

        val matchesQuery = searchQuery.isEmpty() ||
                tx.category.contains(searchQuery, ignoreCase = true) ||
                tx.recipientName.contains(searchQuery, ignoreCase = true) ||
                tx.transactionId.contains(searchQuery, ignoreCase = true)

        matchesCategory && matchesQuery
    }

    val totalIncome = transactions.filter {
        it.type == TransactionType.TOPUP || it.type == TransactionType.TRANSFER_IN || it.type == TransactionType.REFUND
    }.sumOf { it.amount }

    val totalExpense = transactions.filter {
        it.type == TransactionType.TRANSFER_OUT || it.type == TransactionType.WITHDRAWAL || it.type == TransactionType.PAYMENT
    }.sumOf { it.amount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("aktivitas_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Aktivitas & Riwayat Transaksi",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Pantau seluruh mutasi masuk dan keluar rekening Anda",
                    fontSize = 12.sp,
                    color = TextSecondaryMuted
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Summary Card (In vs Out)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyBluePrimary)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Total Income
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(EmeraldSuccess.copy(alpha = 0.2f), shape = RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = "Pemasukan",
                                    tint = EmeraldSuccess
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = "Total Masuk", fontSize = 11.sp, color = LightIceBlue)
                                Text(
                                    text = if (isBalanceHidden) "Rp ••••••" else "Rp ${formatCurrency(totalIncome)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldSuccess
                                )
                            }
                        }

                        // Total Expense
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFFEF4444).copy(alpha = 0.2f), shape = RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "Pengeluaran",
                                    tint = Color(0xFFEF4444)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = "Total Keluar", fontSize = 11.sp, color = LightIceBlue)
                                Text(
                                    text = if (isBalanceHidden) "Rp ••••••" else "Rp ${formatCurrency(totalExpense)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari transaksi, merchant, ID...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("history_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoyalBlueAccent,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Filter Chips Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filterOptions) { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoyalBlueAccent,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        if (filteredTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Empty",
                            tint = TextSecondaryMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Tidak ada transaksi ditemukan",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondaryMuted,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        } else {
            items(filteredTransactions) { tx ->
                TransactionItemRow(
                    tx = tx,
                    isBalanceHidden = isBalanceHidden,
                    onClick = { onSelectTransaction(tx) }
                )
            }
        }
    }
}
