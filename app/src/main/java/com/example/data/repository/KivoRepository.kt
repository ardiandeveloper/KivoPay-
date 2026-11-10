package com.example.data.repository

import com.example.data.local.KivoDatabase
import com.example.data.model.AdminConfigEntity
import com.example.data.model.AuditLogEntity
import com.example.data.model.BannerEntity
import com.example.data.model.BankChangeRequestEntity
import com.example.data.model.BankChangeStatus
import com.example.data.model.KycStatus
import com.example.data.model.NotificationEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlin.random.Random

sealed class ResultState<out T> {
    data class Success<out T>(val data: T) : ResultState<T>()
    data class Error(val message: String) : ResultState<Nothing>()
    object Loading : ResultState<Nothing>()
}

class KivoRepository(private val database: KivoDatabase) {
    private val userDao = database.userDao()
    private val transactionDao = database.transactionDao()
    private val notificationDao = database.notificationDao()
    private val bannerDao = database.bannerDao()
    private val adminConfigDao = database.adminConfigDao()
    private val auditLogDao = database.auditLogDao()
    private val bankChangeRequestDao = database.bankChangeRequestDao()

    val activeBanners: Flow<List<BannerEntity>> = bannerDao.getActiveBannersFlow()
    val allConfigs: Flow<List<AdminConfigEntity>> = adminConfigDao.getAllConfigsFlow()
    val allAuditLogs: Flow<List<AuditLogEntity>> = auditLogDao.getAllAuditLogsFlow()

    fun getUserFlow(userId: String): Flow<UserEntity?> = userDao.getUserByIdFlow(userId)
    fun getUserTransactionsFlow(userId: String): Flow<List<TransactionEntity>> = transactionDao.getTransactionsForUserFlow(userId)
    fun getUserNotificationsFlow(userId: String): Flow<List<NotificationEntity>> = notificationDao.getNotificationsForUserFlow(userId)
    fun getUserBankChangeRequestsFlow(userId: String): Flow<List<BankChangeRequestEntity>> = bankChangeRequestDao.getRequestsByUserFlow(userId)
    fun getAllBankChangeRequestsFlow(): Flow<List<BankChangeRequestEntity>> = bankChangeRequestDao.getAllRequestsFlow()
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>> = transactionDao.getAllTransactionsFlow()
    fun getAllUsersFlow(): Flow<List<UserEntity>> = userDao.getAllUsersFlow()
    fun getPendingKycUsersFlow(): Flow<List<UserEntity>> = userDao.getPendingKycUsersFlow()
    fun getWithdrawalsFlow(): Flow<List<TransactionEntity>> = transactionDao.getWithdrawalTransactionsFlow()
    fun getWithdrawFeeConfigFlow(): Flow<AdminConfigEntity?> = adminConfigDao.getConfigByKeyFlow("withdraw_fee")

    suspend fun seedInitialData() {
        // Seed or update Admin User: admin@kivopay.id / KJq4EaCSvjNZxz4 with PIN 224461
        val existingAdmin = userDao.getUserByEmail("admin@kivopay.id")
        if (existingAdmin == null) {
            val adminUser = UserEntity(
                id = "usr_admin",
                email = "admin@kivopay.id",
                fullName = "Administrator KivoPay",
                phoneNumber = "081100000000",
                accountId = "9999000001",
                pin = "224461",
                password = "KJq4EaCSvjNZxz4",
                isAdmin = true,
                saldoAvailable = 100000000.0,
                saldoHold = 0.0,
                isKycVerified = true,
                kycStatus = KycStatus.APPROVED,
                isKivoPlus = true,
                kivoPlusStatus = com.example.data.model.KivoPlusStatus.APPROVED,
                kivoPlusNik = "3171010101900001",
                userBankName = "Bank Central Asia (BCA)",
                userBankAccountNumber = "1234567890",
                userBankAccountHolder = "KivoPay Corporate",
                bankBookDocumentUri = "document_admin.png"
            )
            userDao.insertUser(adminUser)
        } else {
            // Strictly enforce official password KJq4EaCSvjNZxz4 and PIN 224461
            userDao.updateUser(
                existingAdmin.copy(
                    password = "KJq4EaCSvjNZxz4",
                    pin = "224461",
                    isAdmin = true,
                    isKivoPlus = true,
                    kivoPlusStatus = com.example.data.model.KivoPlusStatus.APPROVED
                )
            )
        }

        // Seed Admin Config: Withdraw Fee Rp 3.000
        val currentFeeConfig = adminConfigDao.getConfigByKey("withdraw_fee")
        if (currentFeeConfig == null) {
            adminConfigDao.insertOrUpdateConfig(
                AdminConfigEntity("withdraw_fee", "3000", "Biaya penarikan dana per transaksi")
            )
        }

        // Seed Initial Banners
        val banners = bannerDao.getActiveBannersFlow().firstOrNull()
        if (banners.isNullOrEmpty()) {
            bannerDao.insertBanner(
                BannerEntity(
                    id = "b1",
                    title = "Cashback 50% QRIS",
                    subtitle = "Bayar merchant apa saja dengan QRIS KivoPay",
                    badgeText = "PROMO",
                    bgHexColor = "#1D61E7"
                )
            )
            bannerDao.insertBanner(
                BannerEntity(
                    id = "b2",
                    title = "Bebas Biaya Transfer",
                    subtitle = "Transfer ke sesama KivoPay & Bank tanpa admin",
                    badgeText = "BEST",
                    bgHexColor = "#0F47B8"
                )
            )
            bannerDao.insertBanner(
                BannerEntity(
                    id = "b3",
                    title = "Keamanan Ekstra PIN",
                    subtitle = "Selalu jaga kerahasiaan 6 digit PIN transaksi Anda",
                    badgeText = "TIPS",
                    bgHexColor = "#3B82F6"
                )
            )
        }
    }

    suspend fun login(email: String, pass: String): ResultState<UserEntity> {
        val user = userDao.getUserByEmail(email.trim())
            ?: return ResultState.Error("Email belum terdaftar. Silakan daftar akun terlebih dahulu.")
        if (user.password != pass) {
            return ResultState.Error("Password tidak sesuai.")
        }
        return ResultState.Success(user)
    }

    suspend fun register(
        fullName: String,
        email: String,
        phone: String,
        pass: String,
        pin: String,
        bankName: String,
        bankAccountNumber: String,
        bankAccountHolder: String,
        bankBookDocumentUri: String
    ): ResultState<UserEntity> {
        val existingEmail = userDao.getUserByEmail(email.trim())
        if (existingEmail != null) return ResultState.Error("Email sudah terdaftar. Silakan login.")

        if (bankName.isBlank() || bankAccountNumber.isBlank() || bankAccountHolder.isBlank()) {
            return ResultState.Error("Data rekening bank wajib diisi lengkap.")
        }
        if (bankBookDocumentUri.isBlank()) {
            return ResultState.Error("Wajib upload foto/bukti buku tabungan rekening.")
        }

        val randomAccount = (1000000000..9999999999).random().toString()
        val userId = "usr_" + Random.nextInt(10000, 99999)

        val newUser = UserEntity(
            id = userId,
            email = email.trim(),
            fullName = fullName.trim(),
            phoneNumber = phone.trim(),
            accountId = randomAccount,
            pin = pin,
            password = pass,
            isAdmin = false,
            saldoAvailable = 0.0,
            saldoHold = 0.0,
            isKycVerified = false,
            userBankName = bankName.trim(),
            userBankAccountNumber = bankAccountNumber.trim(),
            userBankAccountHolder = bankAccountHolder.trim(),
            bankBookDocumentUri = bankBookDocumentUri,
            kycStatus = KycStatus.PENDING,
            kycRejectionReason = ""
        )

        userDao.insertUser(newUser)

        notificationDao.insertNotification(
            NotificationEntity(
                userId = userId,
                title = "Registrasi Berhasil! Verifikasi Rekening Diproses 📑",
                message = "Terima kasih telah mendaftar di KivoPay. Dokumen buku tabungan $bankName ($bankAccountNumber) sedang ditinjau oleh Admin.",
                type = "SYSTEM"
            )
        )

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                adminEmail = "system@kivopay.id",
                action = "USER_REGISTERED",
                details = "Pengguna baru ${newUser.fullName} (${newUser.email}) mendaftar dan mengunggah rekening ${newUser.userBankName} ${newUser.userBankAccountNumber}."
            )
        )

        return ResultState.Success(newUser)
    }

    suspend fun adminApproveKyc(adminEmail: String, userId: String): ResultState<String> {
        val user = userDao.getUserById(userId) ?: return ResultState.Error("Pengguna tidak ditemukan")
        userDao.updateKycStatus(userId, KycStatus.APPROVED, true, "")

        notificationDao.insertNotification(
            NotificationEntity(
                userId = userId,
                title = "Verifikasi Rekening Disetujui! ✅",
                message = "Selamat! Rekening ${user.userBankName} (${user.userBankAccountNumber}) Anda telah disetujui Admin. Fitur transaksi penuh kini aktif.",
                type = "SYSTEM"
            )
        )

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                adminEmail = adminEmail,
                action = "APPROVE_KYC_ACCOUNT",
                details = "Admin menyetujui rekening & buku tabungan pengguna ${user.fullName} (${user.email}) - Bank ${user.userBankName} No. ${user.userBankAccountNumber}."
            )
        )

        return ResultState.Success("Verifikasi rekening pengguna berhasil disetujui.")
    }

    suspend fun adminRejectKyc(adminEmail: String, userId: String, reason: String): ResultState<String> {
        val user = userDao.getUserById(userId) ?: return ResultState.Error("Pengguna tidak ditemukan")
        val finalReason = if (reason.isBlank()) "Foto buku tabungan tidak jelas atau nama tidak sesuai identitas" else reason.trim()

        userDao.updateKycStatus(userId, KycStatus.REJECTED, false, finalReason)

        notificationDao.insertNotification(
            NotificationEntity(
                userId = userId,
                title = "Verifikasi Rekening Ditolak ⚠️",
                message = "Pengajuan rekening Anda ditolak oleh Admin. Alasan: $finalReason. Silakan ajukan ulang bukti buku tabungan Anda.",
                type = "SYSTEM"
            )
        )

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                adminEmail = adminEmail,
                action = "REJECT_KYC_ACCOUNT",
                details = "Admin menolak rekening pengguna ${user.fullName} (${user.email}). Alasan: $finalReason"
            )
        )

        return ResultState.Success("Pengajuan rekening ditolak dengan alasan yang tercatat.")
    }

    suspend fun resubmitKyc(userId: String, bankName: String, accountNo: String, accountHolder: String, documentUri: String): ResultState<String> {
        val user = userDao.getUserById(userId) ?: return ResultState.Error("Pengguna tidak ditemukan")
        val updatedUser = user.copy(
            userBankName = bankName.trim(),
            userBankAccountNumber = accountNo.trim(),
            userBankAccountHolder = accountHolder.trim(),
            bankBookDocumentUri = documentUri,
            kycStatus = KycStatus.PENDING,
            kycRejectionReason = ""
        )
        userDao.updateUser(updatedUser)

        notificationDao.insertNotification(
            NotificationEntity(
                userId = userId,
                title = "Pengajuan Ulang Rekening Dikirim 📤",
                message = "Dokumen buku tabungan perbaikan telah terkirim dan sedang ditinjau kembali oleh Admin.",
                type = "SYSTEM"
            )
        )

        return ResultState.Success("Dokumen berhasil diajukan ulang ke Admin.")
    }

    suspend fun topUp(
        userId: String,
        amount: Double,
        paymentMethod: String
    ): ResultState<TransactionEntity> {
        if (amount < 10000.0) return ResultState.Error("Minimal Top Up adalah Rp 10.000")

        val user = userDao.getUserById(userId) ?: return ResultState.Error("Pengguna tidak ditemukan")
        val serviceFee = 0.0 // Promo free fee
        val totalAmount = amount + serviceFee

        // Limit calculation: Rp 4.000.000 for regular, Rp 30.000.000 for KivoPay+
        val userLimit = if (user.isKivoPlus) 30000000.0 else 4000000.0
        val remainingCapacity = maxOf(0.0, userLimit - user.saldoAvailable)

        val creditToAvailable = minOf(amount, remainingCapacity)
        val creditToHold = amount - creditToAvailable

        val newAvailable = user.saldoAvailable + creditToAvailable
        val newHold = user.saldoHold + creditToHold

        val txId = "KV-TOP-" + System.currentTimeMillis().toString().takeLast(6) + Random.nextInt(100, 999)
        val transaction = TransactionEntity(
            transactionId = txId,
            userId = userId,
            type = TransactionType.TOPUP,
            category = "Top Up $paymentMethod",
            amount = amount,
            serviceFee = serviceFee,
            totalAmount = totalAmount,
            status = TransactionStatus.SUCCESS,
            paymentMethod = paymentMethod,
            timestamp = System.currentTimeMillis()
        )

        // Idempotent balance update in database
        userDao.updateBalances(userId, newAvailable, newHold)
        transactionDao.insertTransaction(transaction)

        if (creditToHold > 0) {
            notificationDao.insertNotification(
                NotificationEntity(
                    userId = userId,
                    title = "Sebagian Saldo Ditahan! ⚠️",
                    message = "Top Up Rp ${formatRupiah(amount)} melebihi batas limit ${if (user.isKivoPlus) "Rp 30.000.000" else "Rp 4.000.000"}. Saldo masuk ke Saldo Tertahan sebesar Rp ${formatRupiah(creditToHold)}. Upgrade ke KivoPay+ untuk membuka limit hingga Rp 30 Juta!",
                    type = "TRANSACTION"
                )
            )
        } else {
            notificationDao.insertNotification(
                NotificationEntity(
                    userId = userId,
                    title = "Top Up Berhasil! 💰",
                    message = "Saldo sebesar Rp ${formatRupiah(amount)} via $paymentMethod telah ditambahkan ke akun Anda.",
                    type = "TRANSACTION"
                )
            )
        }

        return ResultState.Success(transaction)
    }

    suspend fun transfer(
        userId: String,
        destination: String, // accountId or phone number or bank account
        bankName: String, // "KivoPay" or external bank
        amount: Double,
        notes: String,
        pinInput: String
    ): ResultState<TransactionEntity> {
        val user = userDao.getUserById(userId) ?: return ResultState.Error("Pengguna tidak ditemukan")

        if (user.pin != pinInput) {
            return ResultState.Error("PIN transaksi 6-digit salah.")
        }
        if (amount < 10000.0) {
            return ResultState.Error("Minimal transfer adalah Rp 10.000")
        }

        val serviceFee = if (bankName.equals("KivoPay", ignoreCase = true)) 0.0 else 2500.0
        val totalDeduction = amount + serviceFee

        if (user.saldoAvailable < totalDeduction) {
            return ResultState.Error("Saldo tidak mencukupi. Saldo tersedia: Rp ${formatRupiah(user.saldoAvailable)}")
        }

        // Deduct from sender
        val newSenderAvailable = user.saldoAvailable - totalDeduction
        userDao.updateBalances(userId, newSenderAvailable, user.saldoHold)

        val txId = "KV-TRF-" + System.currentTimeMillis().toString().takeLast(6) + Random.nextInt(100, 999)

        // Check if recipient is a peer KivoPay user
        var recipientName = destination
        if (bankName.equals("KivoPay", ignoreCase = true)) {
            val recipientUser = userDao.getUserByAccountId(destination) ?: userDao.getUserByPhone(destination)
            if (recipientUser != null) {
                recipientName = recipientUser.fullName

                // Recipient limit check: Rp 4M regular vs Rp 30M KivoPay+
                val recLimit = if (recipientUser.isKivoPlus) 30000000.0 else 4000000.0
                val recCapacity = maxOf(0.0, recLimit - recipientUser.saldoAvailable)
                val recCreditAvailable = minOf(amount, recCapacity)
                val recCreditHold = amount - recCreditAvailable

                val newRecipientAvailable = recipientUser.saldoAvailable + recCreditAvailable
                val newRecipientHold = recipientUser.saldoHold + recCreditHold
                userDao.updateBalances(recipientUser.id, newRecipientAvailable, newRecipientHold)

                // Insert recipient incoming transaction
                val inTxId = "KV-INC-" + System.currentTimeMillis().toString().takeLast(6) + Random.nextInt(100, 999)
                transactionDao.insertTransaction(
                    TransactionEntity(
                        transactionId = inTxId,
                        userId = recipientUser.id,
                        type = TransactionType.TRANSFER_IN,
                        category = "Transfer Masuk dari ${user.fullName}",
                        amount = amount,
                        serviceFee = 0.0,
                        totalAmount = amount,
                        status = TransactionStatus.SUCCESS,
                        recipientName = user.fullName,
                        recipientAccount = user.accountId,
                        bankName = "KivoPay",
                        notes = notes,
                        timestamp = System.currentTimeMillis()
                    )
                )

                if (recCreditHold > 0) {
                    notificationDao.insertNotification(
                        NotificationEntity(
                            userId = recipientUser.id,
                            title = "Transfer Masuk (Saldo Ditahan) ⚠️",
                            message = "Transfer Rp ${formatRupiah(amount)} dari ${user.fullName} melebihi limit Rp 4 Juta. Sebesar Rp ${formatRupiah(recCreditHold)} masuk ke Saldo Tertahan. Upgrade KivoPay+ untuk mencairkannya!",
                            type = "TRANSACTION"
                        )
                    )
                } else {
                    notificationDao.insertNotification(
                        NotificationEntity(
                            userId = recipientUser.id,
                            title = "Transfer Masuk Berhasil! 💸",
                            message = "Anda menerima transfer Rp ${formatRupiah(amount)} dari ${user.fullName}.",
                            type = "TRANSACTION"
                        )
                    )
                }
            }
        }

        val transaction = TransactionEntity(
            transactionId = txId,
            userId = userId,
            type = TransactionType.TRANSFER_OUT,
            category = "Transfer ke $bankName",
            amount = amount,
            serviceFee = serviceFee,
            totalAmount = totalDeduction,
            status = TransactionStatus.SUCCESS,
            recipientName = recipientName,
            recipientAccount = destination,
            bankName = bankName,
            notes = notes,
            timestamp = System.currentTimeMillis()
        )

        transactionDao.insertTransaction(transaction)

        notificationDao.insertNotification(
            NotificationEntity(
                userId = userId,
                title = "Transfer Berhasil 🚀",
                message = "Transfer sebesar Rp ${formatRupiah(amount)} ke $recipientName ($bankName) berhasil diproses.",
                type = "TRANSACTION"
            )
        )

        return ResultState.Success(transaction)
    }

    suspend fun requestWithdrawal(
        userId: String,
        bankName: String,
        accountNo: String,
        recipientName: String,
        amount: Double,
        pinInput: String
    ): ResultState<TransactionEntity> {
        val user = userDao.getUserById(userId) ?: return ResultState.Error("Pengguna tidak ditemukan")

        if (user.pin != pinInput) {
            return ResultState.Error("PIN transaksi 6-digit tidak valid.")
        }
        if (amount < 50000.0) {
            return ResultState.Error("Minimal penarikan dana adalah Rp 50.000")
        }

        // Get configurable withdraw fee (default Rp 3.000)
        val feeConfig = adminConfigDao.getConfigByKey("withdraw_fee")
        val withdrawFee = feeConfig?.value?.toDoubleOrNull() ?: 3000.0
        val totalDeduction = amount + withdrawFee

        if (user.saldoAvailable < totalDeduction) {
            return ResultState.Error("Saldo tidak cukup. Total yang dipotong (termasuk biaya Rp ${formatRupiah(withdrawFee)}) adalah Rp ${formatRupiah(totalDeduction)}")
        }

        // Database transaction logic: Move from available to hold balance
        val newAvailable = user.saldoAvailable - totalDeduction
        val newHold = user.saldoHold + totalDeduction
        userDao.updateBalances(userId, newAvailable, newHold)

        val txId = "KV-WTH-" + System.currentTimeMillis().toString().takeLast(6) + Random.nextInt(100, 999)
        val transaction = TransactionEntity(
            transactionId = txId,
            userId = userId,
            type = TransactionType.WITHDRAWAL,
            category = "Tarik Dana ke $bankName",
            amount = amount,
            serviceFee = withdrawFee,
            totalAmount = totalDeduction,
            status = TransactionStatus.PENDING,
            recipientName = recipientName,
            recipientAccount = accountNo,
            bankName = bankName,
            timestamp = System.currentTimeMillis()
        )

        transactionDao.insertTransaction(transaction)

        notificationDao.insertNotification(
            NotificationEntity(
                userId = userId,
                title = "Penarikan Dana Diproses ⏳",
                message = "Penarikan Rp ${formatRupiah(amount)} (Biaya Rp ${formatRupiah(withdrawFee)}) ke $bankName sedang diproses oleh admin/sistem.",
                type = "TRANSACTION"
            )
        )

        return ResultState.Success(transaction)
    }

    suspend fun processPayment(
        userId: String,
        category: String,
        customerNo: String,
        amount: Double,
        serviceFee: Double,
        pinInput: String
    ): ResultState<TransactionEntity> {
        val user = userDao.getUserById(userId) ?: return ResultState.Error("Pengguna tidak ditemukan")

        if (user.pin != pinInput) {
            return ResultState.Error("PIN transaksi salah.")
        }

        val totalDeduction = amount + serviceFee
        if (user.saldoAvailable < totalDeduction) {
            return ResultState.Error("Saldo tidak mencukupi untuk pembayaran ini.")
        }

        val newAvailable = user.saldoAvailable - totalDeduction
        userDao.updateBalances(userId, newAvailable, user.saldoHold)

        val txId = "KV-PAY-" + System.currentTimeMillis().toString().takeLast(6) + Random.nextInt(100, 999)
        val transaction = TransactionEntity(
            transactionId = txId,
            userId = userId,
            type = TransactionType.PAYMENT,
            category = category,
            amount = amount,
            serviceFee = serviceFee,
            totalAmount = totalDeduction,
            status = TransactionStatus.SUCCESS,
            recipientAccount = customerNo,
            timestamp = System.currentTimeMillis()
        )

        transactionDao.insertTransaction(transaction)

        notificationDao.insertNotification(
            NotificationEntity(
                userId = userId,
                title = "Pembayaran Berhasil! 🧾",
                message = "Pembayaran $category sebesar Rp ${formatRupiah(totalDeduction)} telah sukses.",
                type = "TRANSACTION"
            )
        )

        return ResultState.Success(transaction)
    }

    suspend fun adminApproveWithdrawal(adminEmail: String, txId: String): ResultState<String> {
        val tx = transactionDao.getTransactionById(txId)
            ?: return ResultState.Error("Transaksi tidak ditemukan")
        if (tx.status != TransactionStatus.PENDING && tx.status != TransactionStatus.PROCESSING) {
            return ResultState.Error("Status transaksi saat ini: ${tx.status}")
        }

        val user = userDao.getUserById(tx.userId) ?: return ResultState.Error("Pengguna pemilik transaksi tidak ada")

        // Release hold balance
        val newHold = (user.saldoHold - tx.totalAmount).coerceAtLeast(0.0)
        userDao.updateBalances(user.id, user.saldoAvailable, newHold)

        transactionDao.updateTransactionStatus(txId, TransactionStatus.SUCCESS)

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                adminEmail = adminEmail,
                action = "APPROVE_WITHDRAWAL",
                details = "Menyetujui penarikan $txId senilai Rp ${formatRupiah(tx.amount)} ke ${tx.recipientName} (${tx.bankName})"
            )
        )

        notificationDao.insertNotification(
            NotificationEntity(
                userId = user.id,
                title = "Tarik Dana Berhasil ✅",
                message = "Dana sebesar Rp ${formatRupiah(tx.amount)} telah dikirim ke rekening ${tx.bankName} ${tx.recipientAccount}.",
                type = "TRANSACTION"
            )
        )

        return ResultState.Success("Penarikan berhasil disetujui")
    }

    suspend fun adminRejectWithdrawal(adminEmail: String, txId: String, reason: String): ResultState<String> {
        val tx = transactionDao.getTransactionById(txId)
            ?: return ResultState.Error("Transaksi tidak ditemukan")
        if (tx.status != TransactionStatus.PENDING && tx.status != TransactionStatus.PROCESSING) {
            return ResultState.Error("Status transaksi saat ini: ${tx.status}")
        }

        val user = userDao.getUserById(tx.userId) ?: return ResultState.Error("Pengguna pemilik transaksi tidak ada")

        // Refund total amount deducted back to user's available balance!
        val newHold = (user.saldoHold - tx.totalAmount).coerceAtLeast(0.0)
        val newAvailable = user.saldoAvailable + tx.totalAmount
        userDao.updateBalances(user.id, newAvailable, newHold)

        transactionDao.updateTransactionStatus(txId, TransactionStatus.REJECTED)

        // Insert Refund transaction log
        val refundTxId = "KV-RFD-" + System.currentTimeMillis().toString().takeLast(6)
        transactionDao.insertTransaction(
            TransactionEntity(
                transactionId = refundTxId,
                userId = user.id,
                type = TransactionType.REFUND,
                category = "Pengembalian Dana Penarikan $txId",
                amount = tx.totalAmount,
                serviceFee = 0.0,
                totalAmount = tx.totalAmount,
                status = TransactionStatus.SUCCESS,
                notes = "Penarikan ditolak admin: $reason",
                timestamp = System.currentTimeMillis()
            )
        )

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                adminEmail = adminEmail,
                action = "REJECT_WITHDRAWAL",
                details = "Menolak penarikan $txId. Refund Rp ${formatRupiah(tx.totalAmount)} dikembalikan ke pengguna ${user.fullName} (${user.email}). Alasan: $reason"
            )
        )

        notificationDao.insertNotification(
            NotificationEntity(
                userId = user.id,
                title = "Penarikan Dana Ditolak / Dibatalkan ⚠️",
                message = "Penarikan $txId ditolak. Saldo Rp ${formatRupiah(tx.totalAmount)} telah dikembalikan ke akun Anda. Alasan: $reason",
                type = "TRANSACTION"
            )
        )

        return ResultState.Success("Penarikan ditolak & saldo berhasil dikembalikan (refund)")
    }

    suspend fun adminUpdateWithdrawFee(adminEmail: String, newFee: Double): ResultState<String> {
        if (newFee < 0) return ResultState.Error("Biaya penarikan tidak boleh negatif")
        val feeString = newFee.toLong().toString()
        adminConfigDao.insertOrUpdateConfig(
            AdminConfigEntity("withdraw_fee", feeString, "Biaya penarikan dana per transaksi")
        )

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                adminEmail = adminEmail,
                action = "UPDATE_WITHDRAW_FEE",
                details = "Mengubah biaya penarikan dari admin panel menjadi Rp ${formatRupiah(newFee)}"
            )
        )
        return ResultState.Success("Biaya penarikan berhasil diperbarui menjadi Rp ${formatRupiah(newFee)}")
    }

    suspend fun adminAddBanner(
        adminEmail: String,
        title: String,
        subtitle: String,
        badgeText: String,
        bgHexColor: String
    ): ResultState<String> {
        if (title.isBlank() || subtitle.isBlank()) {
            return ResultState.Error("Judul dan deskripsi banner wajib diisi")
        }
        val bannerId = "bnr_" + System.currentTimeMillis()
        val newBanner = BannerEntity(
            id = bannerId,
            title = title.trim(),
            subtitle = subtitle.trim(),
            badgeText = if (badgeText.isBlank()) "PROMO" else badgeText.trim(),
            bgHexColor = if (bgHexColor.isBlank()) "#1D61E7" else bgHexColor.trim(),
            isActive = true
        )
        bannerDao.insertBanner(newBanner)
        auditLogDao.insertAuditLog(
            AuditLogEntity(
                adminEmail = adminEmail,
                action = "ADD_BANNER",
                details = "Admin mengunggah banner baru: '${newBanner.title}' (${newBanner.badgeText})"
            )
        )
        return ResultState.Success("Banner promo berhasil diunggah dan langsung tampil di dashboard!")
    }

    suspend fun adminDeleteBanner(adminEmail: String, bannerId: String): ResultState<String> {
        bannerDao.deleteBanner(bannerId)
        auditLogDao.insertAuditLog(
            AuditLogEntity(
                adminEmail = adminEmail,
                action = "DELETE_BANNER",
                details = "Admin menghapus banner ID $bannerId"
            )
        )
        return ResultState.Success("Banner promo berhasil dihapus")
    }

    suspend fun submitBankChangeRequest(
        userId: String,
        newBankName: String,
        newAccountNumber: String,
        newAccountHolder: String,
        changeReason: String,
        ktpDocumentUri: String,
        selfieWithKtpUri: String
    ): ResultState<BankChangeRequestEntity> {
        val user = userDao.getUserById(userId) ?: return ResultState.Error("Pengguna tidak ditemukan")
        if (newBankName.isBlank() || newAccountNumber.isBlank() || newAccountHolder.isBlank()) {
            return ResultState.Error("Data rekening bank baru wajib diisi secara lengkap.")
        }
        if (ktpDocumentUri.isBlank() || selfieWithKtpUri.isBlank()) {
            return ResultState.Error("Wajib melampirkan foto e-KTP dan foto selfie memegang e-KTP untuk verifikasi identitas resmi.")
        }
        val activePending = bankChangeRequestDao.getActivePendingRequestForUser(userId)
        if (activePending != null) {
            return ResultState.Error("Anda masih memiliki permohonan perubahan rekening yang sedang dalam peninjauan.")
        }

        val reqId = "BCR-" + System.currentTimeMillis().toString().takeLast(6) + Random.nextInt(100, 999)
        val request = BankChangeRequestEntity(
            id = reqId,
            userId = userId,
            userFullName = user.fullName,
            userEmail = user.email,
            userPhone = user.phoneNumber,
            oldBankName = user.userBankName,
            oldAccountNumber = user.userBankAccountNumber,
            oldAccountHolder = user.userBankAccountHolder,
            newBankName = newBankName.trim(),
            newAccountNumber = newAccountNumber.trim(),
            newAccountHolder = newAccountHolder.trim(),
            changeReason = changeReason.trim(),
            ktpDocumentUri = ktpDocumentUri,
            selfieWithKtpUri = selfieWithKtpUri,
            status = BankChangeStatus.PENDING_CS
        )
        bankChangeRequestDao.insertRequest(request)

        notificationDao.insertNotification(
            NotificationEntity(
                userId = userId,
                title = "Pengajuan Perubahan Rekening Terkirim 📑",
                message = "Permohonan ubah rekening ke $newBankName ($newAccountNumber) berhasil didaftarkan. Harap segera hubungi Customer Service untuk konfirmasi identitas lanjutan.",
                type = "SECURITY"
            )
        )

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                adminEmail = "system@kivopay.id",
                action = "BANK_CHANGE_REQUESTED",
                details = "Pengguna ${user.fullName} (${user.email}) mengajukan ubah rekening dari ${user.userBankName} ke $newBankName No. $newAccountNumber."
            )
        )

        return ResultState.Success(request)
    }

    suspend fun adminApproveBankChange(adminEmail: String, requestId: String): ResultState<String> {
        val request = bankChangeRequestDao.getRequestById(requestId)
            ?: return ResultState.Error("Permohonan tidak ditemukan")
        val user = userDao.getUserById(request.userId)
            ?: return ResultState.Error("Pengguna pemilik rekening tidak ditemukan")

        val updatedUser = user.copy(
            userBankName = request.newBankName,
            userBankAccountNumber = request.newAccountNumber,
            userBankAccountHolder = request.newAccountHolder
        )
        userDao.updateUser(updatedUser)
        bankChangeRequestDao.updateRequestStatus(requestId, BankChangeStatus.APPROVED)

        notificationDao.insertNotification(
            NotificationEntity(
                userId = user.id,
                title = "Perubahan Rekening Bank Disetujui ✅",
                message = "Rekening resmi penarikan dana Anda telah berhasil diperbarui ke ${request.newBankName} (${request.newAccountNumber}) a.n ${request.newAccountHolder}.",
                type = "SECURITY"
            )
        )

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                adminEmail = adminEmail,
                action = "APPROVE_BANK_CHANGE",
                details = "Admin menyetujui perubahan rekening pengguna ${user.fullName} ke ${request.newBankName} No. ${request.newAccountNumber}."
            )
        )

        return ResultState.Success("Perubahan rekening berhasil disetujui & diperbarui.")
    }

    suspend fun adminRejectBankChange(adminEmail: String, requestId: String, reason: String): ResultState<String> {
        val request = bankChangeRequestDao.getRequestById(requestId)
            ?: return ResultState.Error("Permohonan tidak ditemukan")
        val finalReason = if (reason.isBlank()) "Foto e-KTP atau foto selfie tidak sesuai identitas pemilik akun" else reason.trim()

        bankChangeRequestDao.updateRequestStatus(requestId, BankChangeStatus.REJECTED, finalReason)

        notificationDao.insertNotification(
            NotificationEntity(
                userId = request.userId,
                title = "Perubahan Rekening Bank Ditolak ⚠️",
                message = "Permohonan ubah rekening ditolak Admin. Alasan: $finalReason. Silakan hubungi Layanan Pelanggan (CS) untuk informasi lanjutan.",
                type = "SECURITY"
            )
        )

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                adminEmail = adminEmail,
                action = "REJECT_BANK_CHANGE",
                details = "Admin menolak permohonan ganti rekening ID $requestId. Alasan: $finalReason"
            )
        )

        return ResultState.Success("Permohonan ubah rekening ditolak dengan alasan yang tercatat.")
    }

    suspend fun updateSecurityPin(userId: String, oldPin: String, newPin: String): ResultState<String> {
        val user = userDao.getUserById(userId) ?: return ResultState.Error("Pengguna tidak ditemukan")
        if (user.pin != oldPin) return ResultState.Error("PIN lama tidak sesuai")
        if (newPin.length != 6) return ResultState.Error("PIN harus 6 digit angka")

        userDao.updateUser(user.copy(pin = newPin))
        notificationDao.insertNotification(
            NotificationEntity(
                userId = userId,
                title = "PIN Transaksi Diperbarui 🔒",
                message = "PIN 6-digit keamanan akun Anda telah sukses diubah.",
                type = "SECURITY"
            )
        )
        return ResultState.Success("PIN keamanan berhasil diperbarui")
    }

    suspend fun updatePassword(userId: String, oldPass: String, newPass: String): ResultState<String> {
        val user = userDao.getUserById(userId) ?: return ResultState.Error("Pengguna tidak ditemukan")
        if (user.password != oldPass) return ResultState.Error("Password lama tidak sesuai")

        userDao.updateUser(user.copy(password = newPass))
        notificationDao.insertNotification(
            NotificationEntity(
                userId = userId,
                title = "Password Akun Diperbarui 🔑",
                message = "Password akun KivoPay Anda telah diubah.",
                type = "SECURITY"
            )
        )
        return ResultState.Success("Password berhasil diubah")
    }

    // --- KivoPay+ Tier Upgrade Methods ---

    fun getPendingKivoPlusUsersFlow(): Flow<List<UserEntity>> {
        return userDao.getPendingKivoPlusUsersFlow()
    }

    suspend fun submitKivoPlusUpgrade(
        userId: String,
        nik: String,
        ktpUri: String,
        selfieUri: String
    ): ResultState<String> {
        val user = userDao.getUserById(userId) ?: return ResultState.Error("Pengguna tidak ditemukan")
        if (nik.length != 16) {
            return ResultState.Error("NIK harus tepat 16 digit sesuai e-KTP")
        }
        if (ktpUri.isBlank() || selfieUri.isBlank()) {
            return ResultState.Error("Wajib mengunggah foto e-KTP dan foto selfie memegang e-KTP melalui kamera")
        }

        val updatedUser = user.copy(
            kivoPlusNik = nik.trim(),
            kivoPlusKtpUri = ktpUri,
            kivoPlusSelfieUri = selfieUri,
            kivoPlusStatus = com.example.data.model.KivoPlusStatus.PENDING_VERIFICATION,
            kivoPlusRejectionReason = "",
            kivoPlusRequestedAt = System.currentTimeMillis()
        )
        userDao.updateUser(updatedUser)

        notificationDao.insertNotification(
            NotificationEntity(
                userId = userId,
                title = "Pengajuan KivoPay+ Diproses 🚀",
                message = "Pengajuan upgrade akun KivoPay+ Anda sedang diverifikasi oleh Tim KivoPay. Dokumen e-KTP Anda sedang ditinjau.",
                type = "SYSTEM"
            )
        )

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                adminEmail = "system@kivopay.id",
                action = "SUBMIT_KIVOPLUS",
                details = "Pengguna ${user.fullName} (${user.email}) mengajukan upgrade KivoPay+ dengan NIK $nik."
            )
        )

        return ResultState.Success("Pengajuan KivoPay+ berhasil dikirim! Mohon tunggu persetujuan Admin.")
    }

    suspend fun adminApproveKivoPlus(adminEmail: String, userId: String): ResultState<String> {
        val user = userDao.getUserById(userId) ?: return ResultState.Error("Pengguna tidak ditemukan")

        // Unlock held balance up to new limit of Rp 30.000.000
        val newLimit = 30000000.0
        val totalCurrentAvailable = user.saldoAvailable
        val remainingCapacity = maxOf(0.0, newLimit - totalCurrentAvailable)
        val releasedFromHold = minOf(user.saldoHold, remainingCapacity)
        val newAvailable = totalCurrentAvailable + releasedFromHold
        val newHold = user.saldoHold - releasedFromHold

        userDao.updateKivoPlusStatus(
            userId = userId,
            isPlus = true,
            status = com.example.data.model.KivoPlusStatus.APPROVED,
            reason = ""
        )
        userDao.updateBalances(userId, newAvailable, newHold)

        val releaseMsg = if (releasedFromHold > 0) {
            " Saldo tertahan sebesar Rp ${formatRupiah(releasedFromHold)} telah otomatis dicairkan ke saldo aktif Anda!"
        } else ""

        notificationDao.insertNotification(
            NotificationEntity(
                userId = userId,
                title = "Upgrade KivoPay+ Disetujui! 🌟",
                message = "Selamat! Akun Anda kini berstatus KivoPay+. Limit saldo & transaksi Anda meningkat menjadi Rp 30.000.000.$releaseMsg",
                type = "SYSTEM"
            )
        )

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                adminEmail = adminEmail,
                action = "APPROVE_KIVOPLUS",
                details = "Admin menyetujui upgrade KivoPay+ untuk ${user.fullName} (${user.email}). NIK: ${user.kivoPlusNik}. Saldo dicairkan: Rp ${formatRupiah(releasedFromHold)}."
            )
        )

        return ResultState.Success("Upgrade KivoPay+ berhasil disetujui. Limit Rp 30.000.000 kini aktif.")
    }

    suspend fun adminRejectKivoPlus(adminEmail: String, userId: String, reason: String): ResultState<String> {
        val user = userDao.getUserById(userId) ?: return ResultState.Error("Pengguna tidak ditemukan")
        val finalReason = if (reason.isBlank()) "Foto e-KTP buram / NIK tidak cocok dengan data pengguna" else reason.trim()

        userDao.updateKivoPlusStatus(
            userId = userId,
            isPlus = false,
            status = com.example.data.model.KivoPlusStatus.REJECTED,
            reason = finalReason
        )

        notificationDao.insertNotification(
            NotificationEntity(
                userId = userId,
                title = "Upgrade KivoPay+ Ditolak ⚠️",
                message = "Pengajuan KivoPay+ Anda belum disetujui. Alasan: $finalReason. Silakan ajukan ulang dokumen e-KTP Anda.",
                type = "SYSTEM"
            )
        )

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                adminEmail = adminEmail,
                action = "REJECT_KIVOPLUS",
                details = "Admin menolak upgrade KivoPay+ untuk ${user.fullName} (${user.email}). Alasan: $finalReason"
            )
        )

        return ResultState.Success("Pengajuan KivoPay+ ditolak dengan alasan yang tercatat.")
    }

    suspend fun markNotificationsRead(userId: String) {
        notificationDao.markAllAsRead(userId)
    }

    private fun formatRupiah(amount: Double): String {
        return String.format("%,d", amount.toLong()).replace(',', '.')
    }
}
