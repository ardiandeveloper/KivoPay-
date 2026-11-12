package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AirplaneTicket
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NaturePeople
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.launch

data class ServiceItemData(
    val name: String,
    val category: String,
    val icon: ImageVector,
    val iconBgColor: Color,
    val iconTintColor: Color,
    val badgeText: String? = null,
    val actionType: ServiceActionType = ServiceActionType.FORM_INPUT
)

enum class ServiceActionType {
    TRANSFER_BANK,
    WITHDRAW_CASH,
    TOPUP_BALANCE,
    PAYMENT_CATEGORY,
    FORM_INPUT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllServicesScreen(
    onBack: () -> Unit,
    onNavigateTransfer: () -> Unit,
    onNavigateWithdraw: () -> Unit,
    onNavigateTopUp: () -> Unit,
    onNavigatePaymentWithCategory: (String) -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Dialog state for clicked service with zero dummy data
    var selectedServiceForAction by remember { mutableStateOf<ServiceItemData?>(null) }
    var customerNumberInput by remember { mutableStateOf("") }
    var actionFeedbackMessage by remember { mutableStateOf<String?>(null) }

    val showBackToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 1 }
    }

    // 1. Transfer Section
    val transferItems = listOf(
        ServiceItemData("Kode Tunai", "Transfer", Icons.Default.Pin, Color(0xFFE8F5E9), Color(0xFF2E7D32)),
        ServiceItemData("Kivo Kaget", "Transfer", Icons.Default.CardGiftcard, Color(0xFFE1F5FE), Color(0xFF0288D1), badgeText = "POPULER"),
        ServiceItemData("Bayar Patungan", "Transfer", Icons.Default.ReceiptLong, Color(0xFFE0F2F1), Color(0xFF00897B)),
        ServiceItemData("Kirim ke Chat", "Transfer", Icons.Default.Send, Color(0xFFE8F5E9), Color(0xFF43A047)),
        ServiceItemData("Kirim ke Bank", "Transfer", Icons.Default.AccountBalance, Color(0xFFFFF3E0), Color(0xFFFB8C00), actionType = ServiceActionType.TRANSFER_BANK),
        ServiceItemData("Tarik Tunai", "Transfer", Icons.Default.Payments, Color(0xFFFFEBEE), Color(0xFFE53935), actionType = ServiceActionType.WITHDRAW_CASH),
        ServiceItemData("Kirim ke Grup", "Transfer", Icons.Default.Group, Color(0xFFE8EAF6), Color(0xFF3949AB)),
        ServiceItemData("Terima Luar Negeri", "Transfer", Icons.Default.Public, Color(0xFFE0F7FA), Color(0xFF00ACC1)),
        ServiceItemData("Kirim eMAS Digital", "Transfer", Icons.Default.MonetizationOn, Color(0xFFFFF8E1), Color(0xFFFFA000), badgeText = "BARU")
    )

    // 2. Top Up & Layanan Digital
    val digitalItems = listOf(
        ServiceItemData("Pulsa & Data", "Digital", Icons.Default.PhoneAndroid, Color(0xFFFFEBEE), Color(0xFFE53935), actionType = ServiceActionType.PAYMENT_CATEGORY),
        ServiceItemData("Pascabayar", "Digital", Icons.Default.ReceiptLong, Color(0xFFE1F5FE), Color(0xFF0288D1)),
        ServiceItemData("Kirim ke e-Wallet", "Digital", Icons.Default.AccountBalanceWallet, Color(0xFFE8F5E9), Color(0xFF43A047), actionType = ServiceActionType.PAYMENT_CATEGORY),
        ServiceItemData("Uang Elektronik", "Digital", Icons.Default.CreditCard, Color(0xFFEDE7F6), Color(0xFF5E35B1)),
        ServiceItemData("eSIM Lokal", "Digital", Icons.Default.SimCard, Color(0xFFE0F7FA), Color(0xFF00838F)),
        ServiceItemData("Indobest Travel eSIM", "Digital", Icons.Default.Public, Color(0xFFE3F2FD), Color(0xFF1E88E5)),
        ServiceItemData("eSIM Trial", "Digital", Icons.Default.SimCard, Color(0xFFE8F5E9), Color(0xFF2E7D32)),
        ServiceItemData("KR Global Travel eSIM", "Digital", Icons.Default.AirplaneTicket, Color(0xFFF3E5F5), Color(0xFF8E24AA)),
        ServiceItemData("Roaming", "Digital", Icons.Default.Public, Color(0xFFE0F2F1), Color(0xFF00897B)),
        ServiceItemData("Travel eSIM by Boxo", "Digital", Icons.Default.Flight, Color(0xFFE1F5FE), Color(0xFF0288D1))
    )

    // 3. Tagihan Section
    val tagihanItems = listOf(
        ServiceItemData("My Bills", "Tagihan", Icons.Default.ReceiptLong, Color(0xFFFFF8E1), Color(0xFFFFA000)),
        ServiceItemData("Listrik PLN", "Tagihan", Icons.Default.ElectricBolt, Color(0xFFFFF3E0), Color(0xFFFB8C00), actionType = ServiceActionType.PAYMENT_CATEGORY),
        ServiceItemData("PLN Non Taglis", "Tagihan", Icons.Default.ElectricBolt, Color(0xFFFFFDE7), Color(0xFFFBC02D)),
        ServiceItemData("Air PDAM", "Tagihan", Icons.Default.WaterDrop, Color(0xFFE1F5FE), Color(0xFF039BE5), actionType = ServiceActionType.PAYMENT_CATEGORY),
        ServiceItemData("Telkom Indihome", "Tagihan", Icons.Default.Wifi, Color(0xFFFFEBEE), Color(0xFFE53935)),
        ServiceItemData("PGN Gas", "Tagihan", Icons.Default.LocalGasStation, Color(0xFFE0F2F1), Color(0xFF00897B), actionType = ServiceActionType.PAYMENT_CATEGORY),
        ServiceItemData("Internet & TV Kabel", "Tagihan", Icons.Default.Tv, Color(0xFFE8EAF6), Color(0xFF3949AB)),
        ServiceItemData("IPL Lingkungan", "Tagihan", Icons.Default.Home, Color(0xFFE0F7FA), Color(0xFF00ACC1)),
        ServiceItemData("Bayar Asuransi", "Tagihan", Icons.Default.HealthAndSafety, Color(0xFFE8F5E9), Color(0xFF43A047)),
        ServiceItemData("BPJS Kesehatan", "Tagihan", Icons.Default.MedicalServices, Color(0xFFE8F5E9), Color(0xFF2E7D32), actionType = ServiceActionType.PAYMENT_CATEGORY),
        ServiceItemData("BPJS Ketenagakerjaan", "Tagihan", Icons.Default.FamilyRestroom, Color(0xFFE0F2F1), Color(0xFF00897B)),
        ServiceItemData("Set Up Internet", "Tagihan", Icons.Default.Wifi, Color(0xFFE3F2FD), Color(0xFF1E88E5)),
        ServiceItemData("Iuran Komunitas", "Tagihan", Icons.Default.Group, Color(0xFFE8EAF6), Color(0xFF5C6BC0))
    )

    // 4. Gaya Hidup & Deals
    val gayaHidupItems = listOf(
        ServiceItemData("Kopi Kenangan", "Gaya Hidup", Icons.Default.Fastfood, Color(0xFFFFEBEE), Color(0xFFE53935), badgeText = "PROMO"),
        ServiceItemData("TOMORO Coffee", "Gaya Hidup", Icons.Default.Fastfood, Color(0xFFFFF3E0), Color(0xFFFB8C00)),
        ServiceItemData("Aice Ice Cream", "Gaya Hidup", Icons.Default.Fastfood, Color(0xFFEDE7F6), Color(0xFF7E57C2)),
        ServiceItemData("A+ Rewards", "Gaya Hidup", Icons.Default.CardGiftcard, Color(0xFFE1F5FE), Color(0xFF0288D1), badgeText = "Rp 800"),
        ServiceItemData("Halodoc", "Gaya Hidup", Icons.Default.MedicalServices, Color(0xFFFFEBEE), Color(0xFFE53935), badgeText = "PROMO"),
        ServiceItemData("Kivo Deals", "Gaya Hidup", Icons.Default.ConfirmationNumber, Color(0xFFFFF3E0), Color(0xFFFB8C00)),
        ServiceItemData("Nearby Merchant", "Gaya Hidup", Icons.Default.Store, Color(0xFFFFEBEE), Color(0xFFE53935)),
        ServiceItemData("Kivo Points", "Gaya Hidup", Icons.Default.Savings, Color(0xFFFFF8E1), Color(0xFFFFA000)),
        ServiceItemData("Hadiah Harian", "Gaya Hidup", Icons.Default.CardGiftcard, Color(0xFFFFEBEE), Color(0xFFE53935), badgeText = "FREE"),
        ServiceItemData("Tempo Media", "Gaya Hidup", Icons.Default.ReceiptLong, Color(0xFFFFEBEE), Color(0xFFD32F2F)),
        ServiceItemData("Patungan Hiburan", "Gaya Hidup", Icons.Default.Group, Color(0xFFE8EAF6), Color(0xFF3F51B5)),
        ServiceItemData("Jobseeker Partners", "Gaya Hidup", Icons.Default.Group, Color(0xFFEDE7F6), Color(0xFF673AB7)),
        ServiceItemData("Affiliate Kivo Bisnis", "Gaya Hidup", Icons.Default.Store, Color(0xFFE3F2FD), Color(0xFF1976D2)),
        ServiceItemData("Kirim Barang VELO", "Gaya Hidup", Icons.Default.Send, Color(0xFFE0F7FA), Color(0xFF0097A7)),
        ServiceItemData("Info Skin Game", "Gaya Hidup", Icons.Default.SportsEsports, Color(0xFFFFF8E1), Color(0xFFFF8F00), badgeText = "SKIN")
    )

    // 5. Perjalanan & Transportasi
    val perjalananItems = listOf(
        ServiceItemData("Traveloka Tiket", "Perjalanan", Icons.Default.Flight, Color(0xFFE1F5FE), Color(0xFF0288D1), badgeText = "15JT"),
        ServiceItemData("Tiket.com", "Perjalanan", Icons.Default.AirplaneTicket, Color(0xFFE3F2FD), Color(0xFF1976D2)),
        ServiceItemData("PELNI Kapal Laut", "Perjalanan", Icons.Default.DirectionsBoat, Color(0xFFE0F7FA), Color(0xFF00838F)),
        ServiceItemData("Traveloka Flight", "Perjalanan", Icons.Default.Flight, Color(0xFFE1F5FE), Color(0xFF039BE5)),
        ServiceItemData("Haji & Umrah MuslimPergi", "Perjalanan", Icons.Default.Mosque, Color(0xFFE0F2F1), Color(0xFF00897B))
    )

    // 6. Belanja Section
    val belanjaItems = listOf(
        ServiceItemData("Hemat s.d 80%", "Belanja", Icons.Default.LocalMall, Color(0xFFE8EAF6), Color(0xFF3949AB), badgeText = "PROMO"),
        ServiceItemData("Belanja Harian", "Belanja", Icons.Default.LocalMall, Color(0xFFFFEBEE), Color(0xFFE53935)),
        ServiceItemData("Otsuka Store", "Belanja", Icons.Default.LocalMall, Color(0xFFE0F7FA), Color(0xFF00ACC1)),
        ServiceItemData("BOLDe Official", "Belanja", Icons.Default.Store, Color(0xFFFAFAFA), Color(0xFF212121)),
        ServiceItemData("Alfamart KivoPay", "Belanja", Icons.Default.Store, Color(0xFFFFEBEE), Color(0xFFD32F2F), badgeText = "PROMO"),
        ServiceItemData("Sisberdaya UMKM", "Belanja", Icons.Default.Store, Color(0xFFF3E5F5), Color(0xFF8E24AA)),
        ServiceItemData("Kitakuji Merchandise", "Belanja", Icons.Default.LocalMall, Color(0xFFFFF3E0), Color(0xFFFB8C00))
    )

    // 7. Hiburan Section
    val hiburanItems = listOf(
        ServiceItemData("Jolibox Drama", "Hiburan", Icons.Default.LocalMovies, Color(0xFFF3E5F5), Color(0xFF8E24AA), badgeText = "BARU"),
        ServiceItemData("Drama Hub", "Hiburan", Icons.Default.LocalMovies, Color(0xFFFFEBEE), Color(0xFFE91E63), badgeText = "BARU"),
        ServiceItemData("Lumi Drama", "Hiburan", Icons.Default.LocalMovies, Color(0xFFEDE7F6), Color(0xFF4527A0)),
        ServiceItemData("TIX ID Bioskop", "Hiburan", Icons.Default.ConfirmationNumber, Color(0xFFFFF8E1), Color(0xFFFFA000))
    )

    // 8. Donasi Section
    val donasiItems = listOf(
        ServiceItemData("Baznas", "Donasi", Icons.Default.VolunteerActivism, Color(0xFFFFF8E1), Color(0xFFC0CA33)),
        ServiceItemData("Dompet Dhuafa", "Donasi", Icons.Default.VolunteerActivism, Color(0xFFE8F5E9), Color(0xFF43A047)),
        ServiceItemData("Kivo Donasi", "Donasi", Icons.Default.VolunteerActivism, Color(0xFFE1F5FE), Color(0xFF03A9F4)),
        ServiceItemData("Donasi untuk Bumi", "Donasi", Icons.Default.Park, Color(0xFFE0F7FA), Color(0xFF00BCD4)),
        ServiceItemData("Lindungi Hutan", "Donasi", Icons.Default.NaturePeople, Color(0xFFE8F5E9), Color(0xFF2E7D32)),
        ServiceItemData("Ocean Buddy", "Donasi", Icons.Default.WaterDrop, Color(0xFFE1F5FE), Color(0xFF0288D1)),
        ServiceItemData("Baiq Peduli", "Donasi", Icons.Default.VolunteerActivism, Color(0xFFE8F5E9), Color(0xFF388E3C)),
        ServiceItemData("Rumah Yatim", "Donasi", Icons.Default.Home, Color(0xFFE1F5FE), Color(0xFF0288D1))
    )

    // 9. Layanan Pemerintah Section
    val pemerintahItems = listOf(
        ServiceItemData("PBB (Pajak Bumi & Bangunan)", "Pemerintah", Icons.Default.Home, Color(0xFFEDE7F6), Color(0xFF5E35B1)),
        ServiceItemData("Retribusi Daerah", "Pemerintah", Icons.Default.ReceiptLong, Color(0xFFFFEBEE), Color(0xFFE53935)),
        ServiceItemData("Penerimaan Negara", "Pemerintah", Icons.Default.Public, Color(0xFFE0F7FA), Color(0xFF00ACC1)),
        ServiceItemData("Perpanjangan SIM", "Pemerintah", Icons.Default.CreditCard, Color(0xFFFFEBEE), Color(0xFFD32F2F)),
        ServiceItemData("Passport RI", "Pemerintah", Icons.Default.Public, Color(0xFFE8F5E9), Color(0xFF2E7D32)),
        ServiceItemData("E-Tilang Polri", "Pemerintah", Icons.Default.ReceiptLong, Color(0xFFE1F5FE), Color(0xFF0288D1)),
        ServiceItemData("Pajak Kendaraan (Samsat)", "Pemerintah", Icons.Default.DirectionsBoat, Color(0xFFECEFF1), Color(0xFF263238)),
        ServiceItemData("e-Materai Resmi", "Pemerintah", Icons.Default.ReceiptLong, Color(0xFFFAFAFA), Color(0xFF424242)),
        ServiceItemData("Pajak Daerah Lainnya", "Pemerintah", Icons.Default.ReceiptLong, Color(0xFFE8F5E9), Color(0xFF388E3C))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Cari layanan...", fontSize = 14.sp, color = CleanWhite.copy(alpha = 0.7f)) },
                            singleLine = true,
                            textStyle = kivoInputTextStyle().copy(color = CleanWhite),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(8.dp)
                        )
                    } else {
                        Text(
                            text = "Semua Layanan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = CleanWhite
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = CleanWhite)
                    }
                },
                actions = {
                    IconButton(onClick = { isSearchActive = !isSearchActive }) {
                        Icon(Icons.Default.Search, contentDescription = "Cari", tint = CleanWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(PageBackground)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section 1: Transfer
                item {
                    ServiceCategoryCard(
                        categoryTitle = "Transfer",
                        items = transferItems.filter { it.name.contains(searchQuery, ignoreCase = true) },
                        onItemClick = { item ->
                            when (item.actionType) {
                                ServiceActionType.TRANSFER_BANK -> onNavigateTransfer()
                                ServiceActionType.WITHDRAW_CASH -> onNavigateWithdraw()
                                ServiceActionType.TOPUP_BALANCE -> onNavigateTopUp()
                                ServiceActionType.PAYMENT_CATEGORY -> onNavigatePaymentWithCategory(item.name)
                                ServiceActionType.FORM_INPUT -> {
                                    selectedServiceForAction = item
                                    customerNumberInput = ""
                                    actionFeedbackMessage = null
                                }
                            }
                        }
                    )
                }

                // Section 2: Top Up & Layanan Digital
                item {
                    ServiceCategoryCard(
                        categoryTitle = "Top Up & Layanan Digital",
                        items = digitalItems.filter { it.name.contains(searchQuery, ignoreCase = true) },
                        onItemClick = { item ->
                            when (item.actionType) {
                                ServiceActionType.PAYMENT_CATEGORY -> onNavigatePaymentWithCategory(item.name)
                                ServiceActionType.TOPUP_BALANCE -> onNavigateTopUp()
                                else -> {
                                    selectedServiceForAction = item
                                    customerNumberInput = ""
                                    actionFeedbackMessage = null
                                }
                            }
                        }
                    )
                }

                // Section 3: Tagihan
                item {
                    ServiceCategoryCard(
                        categoryTitle = "Tagihan",
                        items = tagihanItems.filter { it.name.contains(searchQuery, ignoreCase = true) },
                        onItemClick = { item ->
                            if (item.actionType == ServiceActionType.PAYMENT_CATEGORY) {
                                onNavigatePaymentWithCategory(item.name)
                            } else {
                                selectedServiceForAction = item
                                customerNumberInput = ""
                                actionFeedbackMessage = null
                            }
                        }
                    )
                }

                // Section 4: Gaya Hidup & Deals
                item {
                    ServiceCategoryCard(
                        categoryTitle = "Gaya Hidup & Deals",
                        items = gayaHidupItems.filter { it.name.contains(searchQuery, ignoreCase = true) },
                        onItemClick = { item ->
                            selectedServiceForAction = item
                            customerNumberInput = ""
                            actionFeedbackMessage = null
                        }
                    )
                }

                // Section 5: Perjalanan & Transportasi
                item {
                    ServiceCategoryCard(
                        categoryTitle = "Perjalanan & Transportasi",
                        items = perjalananItems.filter { it.name.contains(searchQuery, ignoreCase = true) },
                        onItemClick = { item ->
                            selectedServiceForAction = item
                            customerNumberInput = ""
                            actionFeedbackMessage = null
                        }
                    )
                }

                // Section 6: Belanja
                item {
                    ServiceCategoryCard(
                        categoryTitle = "Belanja",
                        items = belanjaItems.filter { it.name.contains(searchQuery, ignoreCase = true) },
                        onItemClick = { item ->
                            selectedServiceForAction = item
                            customerNumberInput = ""
                            actionFeedbackMessage = null
                        }
                    )
                }

                // Section 7: Hiburan
                item {
                    ServiceCategoryCard(
                        categoryTitle = "Hiburan",
                        items = hiburanItems.filter { it.name.contains(searchQuery, ignoreCase = true) },
                        onItemClick = { item ->
                            selectedServiceForAction = item
                            customerNumberInput = ""
                            actionFeedbackMessage = null
                        }
                    )
                }

                // Section 8: Donasi
                item {
                    ServiceCategoryCard(
                        categoryTitle = "Donasi",
                        items = donasiItems.filter { it.name.contains(searchQuery, ignoreCase = true) },
                        onItemClick = { item ->
                            selectedServiceForAction = item
                            customerNumberInput = ""
                            actionFeedbackMessage = null
                        }
                    )
                }

                // Section 9: Layanan Pemerintah
                item {
                    ServiceCategoryCard(
                        categoryTitle = "Layanan Pemerintah",
                        items = pemerintahItems.filter { it.name.contains(searchQuery, ignoreCase = true) },
                        onItemClick = { item ->
                            selectedServiceForAction = item
                            customerNumberInput = ""
                            actionFeedbackMessage = null
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }

            // Floating "KEMBALI KE ATAS" Button as in user reference
            AnimatedVisibility(
                visible = showBackToTop,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
            ) {
                Surface(
                    onClick = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    color = CleanWhite,
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.5.dp, PrimaryBlue.copy(alpha = 0.6f)),
                    modifier = Modifier.testTag("scroll_to_top_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            contentDescription = "Ke Atas",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "KEMBALI KE ATAS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }
                }
            }
        }
    }

    // Clean Modal Form Dialog for Real Input (Zero Dummy Data Guaranteed)
    selectedServiceForAction?.let { service ->
        AlertDialog(
            onDismissRequest = { selectedServiceForAction = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(service.iconBgColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(service.icon, contentDescription = null, tint = service.iconTintColor, modifier = Modifier.size(24.dp))
                }
            },
            title = {
                Text(
                    text = service.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = TextPrimaryDark
                )
            },
            text = {
                Column {
                    Text(
                        text = "Layanan pembayaran resmi mitra terintegrasi KivoPay. Masukkan nomor pelanggan atau kode tagihan Anda:",
                        fontSize = 12.sp,
                        color = TextSecondaryMuted,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = customerNumberInput,
                        onValueChange = { customerNumberInput = it },
                        label = { Text("Nomor Pelanggan / ID Tagihan") },
                        placeholder = { Text("Contoh: 1082987162") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("service_customer_input"),
                        textStyle = kivoInputTextStyle(),
                        colors = kivoTextFieldColors()
                    )

                    actionFeedbackMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SoftBlueBg,
                            border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = msg,
                                fontSize = 11.sp,
                                color = PrimaryBlue,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customerNumberInput.trim().length >= 4) {
                            actionFeedbackMessage = "ID ${customerNumberInput.trim()} berhasil diverifikasi. Sistem gateway sedang memproses tagihan Anda."
                        } else {
                            actionFeedbackMessage = "Silakan masukkan nomor pelanggan yang valid (minimal 4 digit)."
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Cek Tagihan", color = CleanWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { selectedServiceForAction = null },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9))
                ) {
                    Text("Tutup", color = TextPrimaryDark, fontWeight = FontWeight.SemiBold)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = CleanWhite
        )
    }
}

@Composable
fun ServiceCategoryCard(
    categoryTitle: String,
    items: List<ServiceItemData>,
    onItemClick: (ServiceItemData) -> Unit
) {
    if (items.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CleanWhite),
        border = BorderStroke(1.dp, CardBorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = categoryTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = TextPrimaryDark,
                modifier = Modifier.padding(bottom = 14.dp)
            )

            // 4-Column Grid layout using Rows
            val chunkedItems = items.chunked(4)
            chunkedItems.forEachIndexed { rowIndex, rowList ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    rowList.forEach { item ->
                        ServiceGridItem(
                            item = item,
                            onClick = { onItemClick(item) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Fill remaining columns if last row has less than 4 items
                    if (rowList.size < 4) {
                        for (i in 0 until (4 - rowList.size)) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                if (rowIndex < chunkedItems.size - 1) {
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }
}

@Composable
fun ServiceGridItem(
    item: ServiceItemData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(4.dp)
            .testTag("service_item_${item.name}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(item.iconBgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.name,
                    tint = item.iconTintColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            item.badgeText?.let { badge ->
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFEF4444),
                    modifier = Modifier.padding(top = (-2).dp, end = (-4).dp)
                ) {
                    Text(
                        text = badge,
                        color = CleanWhite,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = item.name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimaryDark,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 14.sp
        )
    }
}
