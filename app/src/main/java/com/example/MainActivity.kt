package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.KivoHeader
import com.example.ui.components.ModernBottomNavBar
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AktivitasScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ChangeBankAccountScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.KivoPlusScreen
import com.example.ui.screens.NoInternetScreen
import com.example.ui.screens.NotifikasiScreen
import com.example.ui.screens.PembayaranScreen
import com.example.ui.screens.ProfilScreen
import com.example.ui.screens.QrisScannerScreen
import com.example.ui.screens.ReceiptScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.TopUpScreen
import com.example.ui.screens.TransferScreen
import com.example.ui.screens.WithdrawScreen
import com.example.ui.theme.KivoPayTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KivoPayTheme {
                KivoPayApp()
            }
        }
    }
}

@Composable
fun KivoPayApp(viewModel: MainViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val isNetworkConnected by viewModel.isNetworkConnected.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val userTransactions by viewModel.userTransactions.collectAsStateWithLifecycle()
    val userNotifications by viewModel.userNotifications.collectAsStateWithLifecycle()
    val activeBanners by viewModel.activeBanners.collectAsStateWithLifecycle()
    val withdrawFeeConfig by viewModel.withdrawFeeConfig.collectAsStateWithLifecycle()
    val rcbApiKeyConfig by viewModel.rcbApiKeyConfig.collectAsStateWithLifecycle()
    val isBalanceHidden by viewModel.isBalanceHidden.collectAsStateWithLifecycle()
    val selectedTransaction by viewModel.selectedTransaction.collectAsStateWithLifecycle()

    // Admin State
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val pendingKycUsers by viewModel.pendingKycUsers.collectAsStateWithLifecycle()
    val pendingKivoPlusUsers by viewModel.pendingKivoPlusUsers.collectAsStateWithLifecycle()
    val withdrawalRequests by viewModel.withdrawalRequests.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val userBankChangeRequests by viewModel.userBankChangeRequests.collectAsStateWithLifecycle()
    val allBankChangeRequests by viewModel.allBankChangeRequests.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Observe Toast Messages
    LaunchedEffect(Unit) {
        viewModel.toastEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val unreadNotificationsCount = userNotifications.count { !it.isRead }

    // Handle Back Press on Secondary Screens
    if (currentScreen != AppScreen.MAIN && currentScreen != AppScreen.SPLASH && currentScreen != AppScreen.AUTH) {
        BackHandler {
            viewModel.navigateTo(AppScreen.MAIN)
        }
    }

    // If internet connection is lost, display dedicated No Internet screen
    if (!isNetworkConnected && currentScreen != AppScreen.SPLASH) {
        NoInternetScreen(
            onRetry = {
                viewModel.checkNetworkRetry()
            }
        )
        return
    }

    when (currentScreen) {
        AppScreen.SPLASH -> {
            SplashScreen()
        }

        AppScreen.AUTH -> {
            AuthScreen(
                onLogin = { email, pass ->
                    viewModel.login(email, pass) { viewModel.navigateTo(AppScreen.MAIN) }
                },
                onRegister = { fullName, email, phone, pass, pin, bankName, bankAccNo, bankAccHolder, bankBookUri ->
                    viewModel.register(
                        fullName,
                        email,
                        phone,
                        pass,
                        pin,
                        bankName,
                        bankAccNo,
                        bankAccHolder,
                        bankBookUri
                    ) {
                        viewModel.navigateTo(AppScreen.MAIN)
                    }
                }
            )
        }

        else -> {
            Scaffold(
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    if (currentScreen == AppScreen.MAIN) {
                        KivoHeader(
                            userName = currentUser?.fullName ?: "Alex Wijaya",
                            unreadNotificationsCount = unreadNotificationsCount,
                            onNotificationClick = {
                                viewModel.setActiveTab(3)
                                viewModel.markNotificationsAsRead()
                            },
                            onProfileClick = { viewModel.setActiveTab(4) }
                        )
                    }
                },
                bottomBar = {
                    if (currentScreen == AppScreen.MAIN) {
                        ModernBottomNavBar(
                            activeTab = activeTab,
                            unreadNotificationCount = unreadNotificationsCount,
                            onTabSelected = { tab ->
                                viewModel.setActiveTab(tab)
                                if (tab == 3) viewModel.markNotificationsAsRead()
                            },
                            onPayFabClick = { viewModel.navigateTo(AppScreen.QRIS_SCANNER) }
                        )
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    var selectedPaymentCategory by remember { mutableStateOf("Listrik PLN") }

                    when (currentScreen) {
                        AppScreen.MAIN -> {
                            when (activeTab) {
                                0 -> HomeScreen(
                                    user = currentUser,
                                    banners = activeBanners,
                                    recentTransactions = userTransactions,
                                    isBalanceHidden = isBalanceHidden,
                                    onToggleBalance = { viewModel.toggleBalanceVisibility() },
                                    onNavigateTopUp = { viewModel.navigateTo(AppScreen.TOPUP) },
                                    onNavigateTransfer = { viewModel.navigateTo(AppScreen.TRANSFER) },
                                    onNavigateWithdraw = { viewModel.navigateTo(AppScreen.WITHDRAW) },
                                    onNavigateHistory = { viewModel.setActiveTab(1) },
                                    onNavigatePayment = {
                                        selectedPaymentCategory = "Listrik PLN"
                                        viewModel.navigateTo(AppScreen.PAYMENT)
                                    },
                                    onNavigatePaymentWithCategory = { cat ->
                                        selectedPaymentCategory = cat
                                        viewModel.navigateTo(AppScreen.PAYMENT)
                                    },
                                    onNavigateQris = { viewModel.navigateTo(AppScreen.QRIS_SCANNER) },
                                    onNavigateKivoPlus = { viewModel.navigateTo(AppScreen.KIVOPLUS_UPGRADE) },
                                    onNavigateSettings = { viewModel.setActiveTab(4) },
                                    onSelectTransaction = { tx -> viewModel.viewReceipt(tx) },
                                    onShowToast = { msg -> viewModel.showToast(msg) }
                                )

                                1 -> AktivitasScreen(
                                    transactions = userTransactions,
                                    isBalanceHidden = isBalanceHidden,
                                    onSelectTransaction = { tx -> viewModel.viewReceipt(tx) }
                                )

                                2 -> QrisScannerScreen(
                                    userAvailableBalance = currentUser?.saldoAvailable ?: 0.0,
                                    onBack = { viewModel.setActiveTab(0) },
                                    onExecuteQrisPay = { merchant, amount, pin ->
                                        viewModel.payBill("QRIS $merchant", "ID10293847561", amount, 0.0, pin) {}
                                    }
                                )

                                3 -> NotifikasiScreen(
                                    notifications = userNotifications,
                                    onMarkAllRead = { viewModel.markNotificationsAsRead() },
                                    onShowToast = { msg -> viewModel.showToast(msg) }
                                )

                                4 -> ProfilScreen(
                                    user = currentUser,
                                    onNavigateAdmin = { viewModel.navigateTo(AppScreen.ADMIN_DASHBOARD) },
                                    onNavigateChangeBank = { viewModel.navigateTo(AppScreen.CHANGE_BANK) },
                                    onNavigateKivoPlus = { viewModel.navigateTo(AppScreen.KIVOPLUS_UPGRADE) },
                                    onLogout = { viewModel.logout() },
                                    onUpdatePin = { oldPin, newPin -> viewModel.updatePin(oldPin, newPin) {} },
                                    onUpdatePassword = { oldPass, newPass -> viewModel.updatePassword(oldPass, newPass) {} },
                                    onShowToast = { msg -> viewModel.showToast(msg) }
                                )
                            }
                        }

                        AppScreen.TOPUP -> TopUpScreen(
                            userBalance = currentUser?.saldoAvailable ?: 0.0,
                            onBack = { viewModel.navigateTo(AppScreen.MAIN) },
                            onSubmitTopUp = { amount, method ->
                                viewModel.topUp(amount, method) {}
                            }
                        )

                        AppScreen.TRANSFER -> TransferScreen(
                            userAvailableBalance = currentUser?.saldoAvailable ?: 0.0,
                            onBack = { viewModel.navigateTo(AppScreen.MAIN) },
                            onExecuteTransfer = { destination, bankName, amount, notes, pin ->
                                viewModel.transfer(destination, bankName, amount, notes, pin) {}
                            }
                        )

                        AppScreen.WITHDRAW -> WithdrawScreen(
                            user = currentUser,
                            userAvailableBalance = currentUser?.saldoAvailable ?: 0.0,
                            configuredFee = withdrawFeeConfig?.value?.toDoubleOrNull() ?: 3000.0,
                            onBack = { viewModel.navigateTo(AppScreen.MAIN) },
                            onNavigateToChangeBank = { viewModel.navigateTo(AppScreen.CHANGE_BANK) },
                            onSubmitWithdrawal = { bankName, accountNo, recipientName, amount, pin ->
                                viewModel.withdraw(bankName, accountNo, recipientName, amount, pin) {}
                            }
                        )

                        AppScreen.CHANGE_BANK -> ChangeBankAccountScreen(
                            user = currentUser,
                            activeRequests = userBankChangeRequests,
                            onBack = { viewModel.navigateTo(AppScreen.MAIN) },
                            onSubmitRequest = { newBank, newAccNo, newAccHolder, reason, ktpUri, selfieUri, onSuccess ->
                                viewModel.submitBankChangeRequest(newBank, newAccNo, newAccHolder, reason, ktpUri, selfieUri, onSuccess)
                            },
                            onShowToast = { msg -> viewModel.showToast(msg) }
                        )

                        AppScreen.PAYMENT -> PembayaranScreen(
                            userAvailableBalance = currentUser?.saldoAvailable ?: 0.0,
                            initialCategoryName = selectedPaymentCategory,
                            onBack = { viewModel.navigateTo(AppScreen.MAIN) },
                            onExecutePayment = { category, customerNo, amount, fee, pin ->
                                viewModel.payBill(category, customerNo, amount, fee, pin) {}
                            }
                        )

                        AppScreen.QRIS_SCANNER -> QrisScannerScreen(
                            userAvailableBalance = currentUser?.saldoAvailable ?: 0.0,
                            onBack = { viewModel.navigateTo(AppScreen.MAIN) },
                            onExecuteQrisPay = { merchant, amount, pin ->
                                viewModel.payBill("QRIS $merchant", "ID10293847561", amount, 0.0, pin) {}
                            }
                        )

                        AppScreen.ADMIN_DASHBOARD -> AdminDashboardScreen(
                            currentWithdrawFee = withdrawFeeConfig?.value ?: "3000",
                            currentRcbApiKey = rcbApiKeyConfig?.value ?: "rcb_live_usr_ffbffccebf4e",
                            withdrawalRequests = withdrawalRequests,
                            allUsers = allUsers,
                            pendingKycUsers = pendingKycUsers,
                            pendingKivoPlusUsers = pendingKivoPlusUsers,
                            bankChangeRequests = allBankChangeRequests,
                            allTransactions = allTransactions,
                            auditLogs = auditLogs,
                            activeBanners = activeBanners,
                            onBack = { viewModel.navigateTo(AppScreen.MAIN) },
                            onApproveWithdraw = { txId -> viewModel.approveWithdrawal(txId) },
                            onRejectWithdraw = { txId, reason -> viewModel.rejectWithdrawal(txId, reason) },
                            onApproveKyc = { userId -> viewModel.adminApproveKyc(userId) },
                            onRejectKyc = { userId, reason -> viewModel.adminRejectKyc(userId, reason) },
                            onApproveKivoPlus = { userId -> viewModel.adminApproveKivoPlus(userId) },
                            onRejectKivoPlus = { userId, reason -> viewModel.adminRejectKivoPlus(userId, reason) },
                            onApproveBankChange = { reqId -> viewModel.adminApproveBankChange(reqId) },
                            onRejectBankChange = { reqId, reason -> viewModel.adminRejectBankChange(reqId, reason) },
                            onSaveWithdrawFee = { newFee -> viewModel.updateWithdrawFee(newFee) },
                            onSaveRcbApiKey = { newKey -> viewModel.updateRcbApiKey(newKey) },
                            onAddBanner = { title, subtitle, badge, color ->
                                viewModel.adminAddBanner(title, subtitle, badge, color)
                            },
                            onDeleteBanner = { bannerId ->
                                viewModel.adminDeleteBanner(bannerId)
                            }
                        )

                        AppScreen.KIVOPLUS_UPGRADE -> KivoPlusScreen(
                            user = currentUser,
                            onBack = { viewModel.navigateTo(AppScreen.MAIN) },
                            onSubmitUpgrade = { nik, ktpUri, selfieUri, onSuccess ->
                                viewModel.submitKivoPlusUpgrade(nik, ktpUri, selfieUri, onSuccess)
                            },
                            onShowToast = { msg -> viewModel.showToast(msg) }
                        )

                        AppScreen.RECEIPT_DETAIL -> ReceiptScreen(
                            transaction = selectedTransaction,
                            onDone = { viewModel.navigateTo(AppScreen.MAIN) },
                            onShowToast = { msg -> viewModel.showToast(msg) }
                        )

                        else -> {}
                    }
                }
            }
        }
    }
}
