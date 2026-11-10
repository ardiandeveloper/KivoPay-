package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.KivoDatabase
import com.example.data.local.SessionManager
import com.example.data.model.AdminConfigEntity
import com.example.data.model.AuditLogEntity
import com.example.data.model.BankChangeRequestEntity
import com.example.data.model.BannerEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserEntity
import com.example.data.repository.KivoRepository
import com.example.data.repository.ResultState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    SPLASH,
    AUTH,
    MAIN,
    TOPUP,
    TRANSFER,
    WITHDRAW,
    PAYMENT,
    QRIS_SCANNER,
    CHANGE_BANK,
    KIVOPLUS_UPGRADE,
    ADMIN_DASHBOARD,
    RECEIPT_DETAIL
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: KivoRepository
    private val sessionManager = SessionManager(application)
    private val networkMonitor = com.example.ui.util.NetworkMonitor(application)

    // Internet connectivity state
    val isNetworkConnected: StateFlow<Boolean> = networkMonitor.isConnectedFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, networkMonitor.isCurrentlyConnected())

    // Current screen starts with SPLASH
    private val _currentScreen = MutableStateFlow(AppScreen.SPLASH)
    val currentScreen = _currentScreen.asStateFlow()

    // Active User ID - restored from session
    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId = _currentUserId.asStateFlow()

    private val _activeTab = MutableStateFlow(0) // 0: Home, 1: Aktivitas, 2: Pay, 3: Notifikasi, 4: Profil
    val activeTab = _activeTab.asStateFlow()

    private val _selectedTransaction = MutableStateFlow<TransactionEntity?>(null)
    val selectedTransaction = _selectedTransaction.asStateFlow()

    private val _isBalanceHidden = MutableStateFlow(false)
    val isBalanceHidden = _isBalanceHidden.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    // QRIS Merchant Modal / Dialog State
    private val _showQrisMerchantComingSoon = MutableStateFlow(false)
    val showQrisMerchantComingSoon = _showQrisMerchantComingSoon.asStateFlow()

    init {
        val database = KivoDatabase.getInstance(application)
        repository = KivoRepository(database)
        viewModelScope.launch {
            repository.seedInitialData()
            // Check session
            val savedUserId = sessionManager.getValidSessionUserId()
            _currentUserId.value = savedUserId

            // Official requested duration: exactly 10 seconds (10.000 ms)
            delay(10000L)
            if (savedUserId != null) {
                _currentScreen.value = AppScreen.MAIN
            } else {
                _currentScreen.value = AppScreen.AUTH
            }
        }
    }

    fun checkNetworkRetry() {
        val connected = networkMonitor.isCurrentlyConnected()
        if (connected) {
            showToast("Koneksi internet terhubung kembali!")
        } else {
            showToast("Masih belum terhubung ke internet. Silakan cek data seluler / Wi-Fi.")
        }
    }

    // Reactive State Flows
    val currentUser: StateFlow<UserEntity?> = _currentUserId.flatMapLatest { userId ->
        if (userId != null) repository.getUserFlow(userId) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userTransactions: StateFlow<List<TransactionEntity>> = _currentUserId.flatMapLatest { userId ->
        if (userId != null) repository.getUserTransactionsFlow(userId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userNotifications: StateFlow<List<NotificationEntity>> = _currentUserId.flatMapLatest { userId ->
        if (userId != null) repository.getUserNotificationsFlow(userId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeBanners: StateFlow<List<BannerEntity>> = repository.activeBanners
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val withdrawFeeConfig: StateFlow<AdminConfigEntity?> = repository.getWithdrawFeeConfigFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val rcbApiKeyConfig: StateFlow<AdminConfigEntity?> = repository.getRcbApiKeyConfigFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Admin State Flows
    val allTransactions: StateFlow<List<TransactionEntity>> = repository.getAllTransactionsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.getAllUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingKycUsers: StateFlow<List<UserEntity>> = repository.getPendingKycUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingKivoPlusUsers: StateFlow<List<UserEntity>> = repository.getPendingKivoPlusUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val withdrawalRequests: StateFlow<List<TransactionEntity>> = repository.getWithdrawalsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userBankChangeRequests: StateFlow<List<BankChangeRequestEntity>> = _currentUserId.flatMapLatest { userId ->
        if (userId != null) repository.getUserBankChangeRequestsFlow(userId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBankChangeRequests: StateFlow<List<BankChangeRequestEntity>> = repository.getAllBankChangeRequestsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun navigateTo(screen: AppScreen) {
        sessionManager.updateLastActive()
        _currentScreen.value = screen
    }

    fun setActiveTab(tabIndex: Int) {
        sessionManager.updateLastActive()
        _activeTab.value = tabIndex
        if (_currentScreen.value != AppScreen.MAIN) {
            _currentScreen.value = AppScreen.MAIN
        }
    }

    fun toggleBalanceVisibility() {
        _isBalanceHidden.value = !_isBalanceHidden.value
    }

    fun setQrisMerchantDialogVisible(visible: Boolean) {
        _showQrisMerchantComingSoon.value = visible
    }

    fun viewReceipt(tx: TransactionEntity) {
        _selectedTransaction.value = tx
        _currentScreen.value = AppScreen.RECEIPT_DETAIL
    }

    fun showToast(message: String) {
        viewModelScope.launch {
            _toastEvent.emit(message)
        }
    }

    // Actions
    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            when (val res = repository.login(email, pass)) {
                is ResultState.Success -> {
                    _currentUserId.value = res.data.id
                    sessionManager.saveUserSession(res.data.id)
                    showToast("Selamat datang, ${res.data.fullName}!")
                    onSuccess()
                }
                is ResultState.Error -> showToast(res.message)
                else -> {}
            }
        }
    }

    fun register(
        fullName: String,
        email: String,
        phone: String,
        pass: String,
        pin: String,
        bankName: String,
        bankAccountNo: String,
        bankAccountHolder: String,
        bankBookUri: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            when (val res = repository.register(
                fullName,
                email,
                phone,
                pass,
                pin,
                bankName,
                bankAccountNo,
                bankAccountHolder,
                bankBookUri
            )) {
                is ResultState.Success -> {
                    _currentUserId.value = res.data.id
                    sessionManager.saveUserSession(res.data.id)
                    showToast("Registrasi berhasil! Menunggu persetujuan rekening oleh Admin.")
                    onSuccess()
                }
                is ResultState.Error -> showToast(res.message)
                else -> {}
            }
        }
    }

    fun logout() {
        _currentUserId.value = null
        sessionManager.clearSession()
        _currentScreen.value = AppScreen.AUTH
        showToast("Anda telah keluar dari akun KivoPay.")
    }

    // Admin KYC Actions
    fun adminApproveKyc(userId: String) {
        val adminUser = currentUser.value ?: return
        viewModelScope.launch {
            when (val res = repository.adminApproveKyc(adminUser.email, userId)) {
                is ResultState.Success -> showToast(res.data)
                is ResultState.Error -> showToast(res.message)
                else -> {}
            }
        }
    }

    fun adminRejectKyc(userId: String, reason: String) {
        val adminUser = currentUser.value ?: return
        viewModelScope.launch {
            when (val res = repository.adminRejectKyc(adminUser.email, userId, reason)) {
                is ResultState.Success -> showToast(res.data)
                is ResultState.Error -> showToast(res.message)
                else -> {}
            }
        }
    }

    fun topUp(amount: Double, paymentMethod: String, onComplete: () -> Unit) {
        val userId = _currentUserId.value ?: return
        sessionManager.updateLastActive()
        viewModelScope.launch {
            when (val res = repository.topUp(userId, amount, paymentMethod)) {
                is ResultState.Success -> {
                    viewReceipt(res.data)
                    showToast("Top Up Rp ${res.data.amount} Berhasil!")
                    com.example.ui.util.NotificationHelper.showTransactionNotification(
                        getApplication(),
                        "Top Up Berhasil! 💰",
                        "Saldo Rp ${String.format("%,d", amount.toLong()).replace(',', '.')} via $paymentMethod berhasil ditambahkan ke akun KivoPay."
                    )
                    onComplete()
                }
                is ResultState.Error -> showToast(res.message)
                else -> {}
            }
        }
    }

    fun transfer(
        destination: String,
        bankName: String,
        amount: Double,
        notes: String,
        pin: String,
        onComplete: () -> Unit
    ) {
        val userId = _currentUserId.value ?: return
        sessionManager.updateLastActive()
        viewModelScope.launch {
            when (val res = repository.transfer(userId, destination, bankName, amount, notes, pin)) {
                is ResultState.Success -> {
                    viewReceipt(res.data)
                    showToast("Transfer Rp ${res.data.amount} Berhasil!")
                    com.example.ui.util.NotificationHelper.showTransactionNotification(
                        getApplication(),
                        "Transfer Berhasil 🚀",
                        "Transfer Rp ${String.format("%,d", amount.toLong()).replace(',', '.')} ke ${res.data.recipientName} ($bankName) sukses dikirim."
                    )
                    onComplete()
                }
                is ResultState.Error -> showToast(res.message)
                else -> {}
            }
        }
    }

    fun withdraw(
        bankName: String,
        accountNo: String,
        recipientName: String,
        amount: Double,
        pin: String,
        onComplete: () -> Unit
    ) {
        val userId = _currentUserId.value ?: return
        sessionManager.updateLastActive()
        viewModelScope.launch {
            when (val res = repository.requestWithdrawal(userId, bankName, accountNo, recipientName, amount, pin)) {
                is ResultState.Success -> {
                    viewReceipt(res.data)
                    showToast("Penarikan Dana Berhasil Diajukan!")
                    com.example.ui.util.NotificationHelper.showTransactionNotification(
                        getApplication(),
                        "Penarikan Dana Diproses ⏳",
                        "Pengajuan penarikan Rp ${String.format("%,d", amount.toLong()).replace(',', '.')} ke $bankName sedang diproses sistem."
                    )
                    onComplete()
                }
                is ResultState.Error -> showToast(res.message)
                else -> {}
            }
        }
    }

    fun payBill(
        category: String,
        customerNo: String,
        amount: Double,
        serviceFee: Double,
        pin: String,
        onComplete: () -> Unit
    ) {
        val userId = _currentUserId.value ?: return
        sessionManager.updateLastActive()
        viewModelScope.launch {
            when (val res = repository.processPayment(userId, category, customerNo, amount, serviceFee, pin)) {
                is ResultState.Success -> {
                    viewReceipt(res.data)
                    showToast("Pembayaran $category Berhasil!")
                    com.example.ui.util.NotificationHelper.showTransactionNotification(
                        getApplication(),
                        "Pembayaran Berhasil! 🧾",
                        "Pembayaran $category senilai Rp ${String.format("%,d", (amount + serviceFee).toLong()).replace(',', '.')} telah berhasil."
                    )
                    onComplete()
                }
                is ResultState.Error -> showToast(res.message)
                else -> {}
            }
        }
    }

    fun approveWithdrawal(txId: String) {
        val adminUser = currentUser.value ?: return
        viewModelScope.launch {
            when (val res = repository.adminApproveWithdrawal(adminUser.email, txId)) {
                is ResultState.Success -> showToast(res.data)
                is ResultState.Error -> showToast(res.message)
                else -> {}
            }
        }
    }

    fun rejectWithdrawal(txId: String, reason: String) {
        val adminUser = currentUser.value ?: return
        viewModelScope.launch {
            when (val res = repository.adminRejectWithdrawal(adminUser.email, txId, reason)) {
                is ResultState.Success -> showToast(res.data)
                is ResultState.Error -> showToast(res.message)
                else -> {}
            }
        }
    }

    fun updateWithdrawFee(newFee: Double) {
        val adminUser = currentUser.value ?: return
        viewModelScope.launch {
            when (val res = repository.adminUpdateWithdrawFee(adminUser.email, newFee)) {
                is ResultState.Success -> showToast(res.data)
                is ResultState.Error -> showToast(res.message)
                else -> {}
            }
        }
    }

    fun updateRcbApiKey(newKey: String) {
        val adminUser = currentUser.value ?: return
        viewModelScope.launch {
            when (val res = repository.adminUpdateRcbApiKey(adminUser.email, newKey)) {
                is ResultState.Success -> showToast(res.data)
                is ResultState.Error -> showToast(res.message)
                else -> {}
            }
        }
    }

    fun adminAddBanner(title: String, subtitle: String, badgeText: String, bgHexColor: String) {
        val adminUser = currentUser.value ?: return
        viewModelScope.launch {
            when (val res = repository.adminAddBanner(adminUser.email, title, subtitle, badgeText, bgHexColor)) {
                is ResultState.Success -> showToast(res.data)
                is ResultState.Error -> showToast(res.message)
                else -> {}
            }
        }
    }

    fun adminDeleteBanner(bannerId: String) {
        val adminUser = currentUser.value ?: return
        viewModelScope.launch {
            when (val res = repository.adminDeleteBanner(adminUser.email, bannerId)) {
                is ResultState.Success -> showToast(res.data)
                is ResultState.Error -> showToast(res.message)
                else -> {}
            }
        }
    }

    fun updatePin(oldPin: String, newPin: String, onComplete: () -> Unit) {
        val userId = _currentUserId.value ?: return
        viewModelScope.launch {
            when (val res = repository.updateSecurityPin(userId, oldPin, newPin)) {
                is ResultState.Success -> {
                    showToast(res.data)
                    onComplete()
                }
                is ResultState.Error -> showToast(res.message)
                else -> {}
            }
        }
    }

    fun updatePassword(oldPass: String, newPass: String, onComplete: () -> Unit) {
        val userId = _currentUserId.value ?: return
        viewModelScope.launch {
            when (val res = repository.updatePassword(userId, oldPass, newPass)) {
                is ResultState.Success -> {
                    showToast(res.data)
                    onComplete()
                }
                is ResultState.Error -> showToast(res.message)
                else -> {}
            }
        }
    }

    fun markNotificationsAsRead() {
        val userId = _currentUserId.value ?: return
        viewModelScope.launch {
            repository.markNotificationsRead(userId)
        }
    }

    fun submitBankChangeRequest(
        newBankName: String,
        newAccountNumber: String,
        newAccountHolder: String,
        changeReason: String,
        ktpUri: String,
        selfieKtpUri: String,
        onSuccess: () -> Unit
    ) {
        val userId = _currentUserId.value ?: return
        viewModelScope.launch {
            when (val res = repository.submitBankChangeRequest(
                userId = userId,
                newBankName = newBankName,
                newAccountNumber = newAccountNumber,
                newAccountHolder = newAccountHolder,
                changeReason = changeReason,
                ktpDocumentUri = ktpUri,
                selfieWithKtpUri = selfieKtpUri
            )) {
                is ResultState.Success -> {
                    showToast("Permohonan berhasil dikirim. Silakan hubungi CS KivoPay.")
                    onSuccess()
                }
                is ResultState.Error -> showToast(res.message)
                else -> {}
            }
        }
    }

    fun adminApproveBankChange(requestId: String) {
        val adminUser = currentUser.value ?: return
        viewModelScope.launch {
            when (val res = repository.adminApproveBankChange(adminUser.email, requestId)) {
                is ResultState.Success -> showToast(res.data)
                is ResultState.Error -> showToast(res.message)
                else -> {}
            }
        }
    }

    fun adminRejectBankChange(requestId: String, reason: String) {
        val adminUser = currentUser.value ?: return
        viewModelScope.launch {
            when (val res = repository.adminRejectBankChange(adminUser.email, requestId, reason)) {
                is ResultState.Success -> showToast(res.data)
                is ResultState.Error -> showToast(res.message)
                else -> {}
            }
        }
    }

    // --- KivoPay+ Tier Upgrade ViewModel Actions ---

    fun submitKivoPlusUpgrade(
        nik: String,
        ktpUri: String,
        selfieUri: String,
        onSuccess: () -> Unit
    ) {
        val userId = _currentUserId.value ?: return
        viewModelScope.launch {
            when (val res = repository.submitKivoPlusUpgrade(userId, nik, ktpUri, selfieUri)) {
                is ResultState.Success -> {
                    showToast(res.data)
                    onSuccess()
                }
                is ResultState.Error -> showToast(res.message)
                else -> {}
            }
        }
    }

    fun adminApproveKivoPlus(userId: String) {
        val adminUser = currentUser.value ?: return
        viewModelScope.launch {
            when (val res = repository.adminApproveKivoPlus(adminUser.email, userId)) {
                is ResultState.Success -> showToast(res.data)
                is ResultState.Error -> showToast(res.message)
                else -> {}
            }
        }
    }

    fun adminRejectKivoPlus(userId: String, reason: String) {
        val adminUser = currentUser.value ?: return
        viewModelScope.launch {
            when (val res = repository.adminRejectKivoPlus(adminUser.email, userId, reason)) {
                is ResultState.Success -> showToast(res.data)
                is ResultState.Error -> showToast(res.message)
                else -> {}
            }
        }
    }
}
