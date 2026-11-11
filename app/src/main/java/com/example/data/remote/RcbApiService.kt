package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

// --- Models for RCB Create Gateway Payment (QRIS & VA & USDT) ---

@JsonClass(generateAdapter = true)
data class RcbCreatePaymentRequest(
    @Json(name = "amount") val amount: Long,
    @Json(name = "item_name") val itemName: String? = null,
    @Json(name = "customer_name") val customerName: String? = null,
    @Json(name = "customer_email") val customerEmail: String? = null,
    @Json(name = "external_id") val externalId: String? = null,
    @Json(name = "qris_mode") val qrisMode: String? = "mdr",
    @Json(name = "payment_type") val paymentType: String? = "QRIS_MDR_DYNAMIC",
    @Json(name = "allowed_methods") val allowedMethods: String? = null,
    @Json(name = "callback_url") val callbackUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class RcbCreatePaymentData(
    @Json(name = "order_id") val orderId: String? = null,
    @Json(name = "payment_url") val paymentUrl: String? = null,
    @Json(name = "qris_url") val qrisUrl: String? = null,
    @Json(name = "qris_string") val qrisString: String? = null,
    @Json(name = "amount") val amount: Long? = null,
    @Json(name = "total_amount") val totalAmount: Long? = null,
    @Json(name = "va_number") val vaNumber: String? = null,
    @Json(name = "account_number") val accountNumber: String? = null,
    @Json(name = "expiry_time") val expiryTime: String? = null
)

// --- Official RCB Payment Channels Specification ---
data class RcbChannelInfo(
    val code: String,
    val name: String,
    val category: String, // "QRIS & E-Wallet", "Virtual Account Bank", "Kripto & Pulsa"
    val description: String,
    val isRecommended: Boolean = false,
    val iconType: String = "VA" // "QRIS", "VA", "CRYPTO", "PULSA"
)

object RcbChannels {
    val allChannels: List<RcbChannelInfo> = listOf(
        // QRIS Channels
        RcbChannelInfo(
            code = "qris_mdr",
            name = "QRIS Direct MDR (Bank BNI Direct)",
            category = "QRIS & E-Wallet",
            description = "🔥 Rekomendasi - Jaringan Direct BNI Ultra-Stabil 99.9% uptime, bebas kendala server, settlement instan",
            isRecommended = true,
            iconType = "QRIS"
        ),
        RcbChannelInfo(
            code = "qris",
            name = "QRIS Dinamis Multi-Bank",
            category = "QRIS & E-Wallet",
            description = "Scan instan via BCA, Mandiri, BRI, BNI, GoPay, OVO, Dana, ShopeePay",
            iconType = "QRIS"
        ),

        // Virtual Account Bank
        RcbChannelInfo(
            code = "bni_va",
            name = "BNI Virtual Account",
            category = "Virtual Account Bank",
            description = "Transfer instan via BNI Mobile Banking, ATM BNI, atau SMS Banking",
            iconType = "VA"
        ),
        RcbChannelInfo(
            code = "bri_va",
            name = "BRI Virtual Account (BRIVA)",
            category = "Virtual Account Bank",
            description = "Transfer instan via BRImo atau ATM BRI seluruh Indonesia",
            iconType = "VA"
        ),
        RcbChannelInfo(
            code = "cimb_niaga_va",
            name = "CIMB Niaga VA",
            category = "Virtual Account Bank",
            description = "Transfer via OCTO Mobile, OCTO Clicks, atau ATM CIMB Niaga",
            iconType = "VA"
        ),
        RcbChannelInfo(
            code = "permata_va",
            name = "Permata Bank VA",
            category = "Virtual Account Bank",
            description = "Transfer via PermataMobile X atau ATM Permata",
            iconType = "VA"
        ),
        RcbChannelInfo(
            code = "bnc_va",
            name = "BNC Bank Neo Commerce VA",
            category = "Virtual Account Bank",
            description = "Transfer instan via Neobank atau ATM",
            iconType = "VA"
        ),
        RcbChannelInfo(
            code = "maybank_va",
            name = "Maybank VA",
            category = "Virtual Account Bank",
            description = "Transfer via M2U ID App atau ATM Maybank",
            iconType = "VA"
        ),
        RcbChannelInfo(
            code = "sampoerna_va",
            name = "Sahabat Sampoerna VA",
            category = "Virtual Account Bank",
            description = "Transfer via Bank Sahabat Sampoerna atau jaringan ATM",
            iconType = "VA"
        ),
        RcbChannelInfo(
            code = "artha_graha_va",
            name = "Artha Graha VA",
            category = "Virtual Account Bank",
            description = "Transfer via agi Mobile atau ATM Artha Graha",
            iconType = "VA"
        ),
        RcbChannelInfo(
            code = "atm_bersama_va",
            name = "ATM Bersama VA",
            category = "Virtual Account Bank",
            description = "Mendukung transfer dari 80+ bank anggota ATM Bersama",
            iconType = "VA"
        ),
        RcbChannelInfo(
            code = "va",
            name = "Semua Virtual Account Terintegrasi",
            category = "Virtual Account Bank",
            description = "Virtual Account otomatis terpadu multi-bank partner",
            iconType = "VA"
        ),

        // Kripto (Crypto) & Pulsa
        RcbChannelInfo(
            code = "usdt",
            name = "Kripto USDT (Stablecoin)",
            category = "Kripto & Pulsa",
            description = "Kripto USDT: TRC20, ERC20, BEP20, TON, SOL, NEAR",
            iconType = "CRYPTO"
        ),
        RcbChannelInfo(
            code = "pulsa",
            name = "Pulsa Seluler",
            category = "Kripto & Pulsa",
            description = "Transfer pulsa resmi Telkomsel, XL, Axis, Smartfren, Tri",
            iconType = "PULSA"
        )
    )
}

@JsonClass(generateAdapter = true)
data class RcbCreatePaymentResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String? = null,
    @Json(name = "data") val data: RcbCreatePaymentData? = null
)

// --- Models for RCB Payment Status Check ---

@JsonClass(generateAdapter = true)
data class RcbPaymentStatusData(
    @Json(name = "order_id") val orderId: String? = null,
    @Json(name = "external_id") val externalId: String? = null,
    @Json(name = "status") val status: String? = null, // "PAID", "PENDING", etc.
    @Json(name = "harga") val harga: Long? = null,
    @Json(name = "total_amount") val totalAmount: Long? = null,
    @Json(name = "payment_type") val paymentType: String? = null,
    @Json(name = "paid_at") val paidAt: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class RcbPaymentStatusResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String? = null,
    @Json(name = "data") val data: RcbPaymentStatusData? = null
)

// --- Models for RCB Disbursement / Payout API ---

@JsonClass(generateAdapter = true)
data class RcbPayoutRequest(
    @Json(name = "amount") val amount: Long,
    @Json(name = "bank_code") val bankCode: String, // e.g. "BCA", "MANDIRI", "BRI", "BNI"
    @Json(name = "account_number") val accountNumber: String,
    @Json(name = "account_name") val accountName: String,
    @Json(name = "external_id") val externalId: String,
    @Json(name = "notes") val notes: String? = "Tarik Dana KivoPay",
    @Json(name = "callback_url") val callbackUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class RcbPayoutData(
    @Json(name = "payout_id") val payoutId: String? = null,
    @Json(name = "order_id") val orderId: String? = null,
    @Json(name = "external_id") val externalId: String? = null,
    @Json(name = "status") val status: String? = null, // "SUCCESS", "PENDING", "PROCESSING", "FAILED"
    @Json(name = "amount") val amount: Long? = null,
    @Json(name = "total_deducted") val totalDeducted: Long? = null,
    @Json(name = "source_account") val sourceAccount: String? = null,
    @Json(name = "remark") val remark: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "completed_at") val completedAt: String? = null,
    @Json(name = "signature") val signature: String? = null
)

@JsonClass(generateAdapter = true)
data class RcbPayoutResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String? = null,
    @Json(name = "data") val data: RcbPayoutData? = null
)

// --- Retrofit Service Interface ---

interface RcbApiService {
    @POST("gateway/create")
    suspend fun createPayment(
        @Header("x-api-key") apiKey: String,
        @Body request: RcbCreatePaymentRequest
    ): Response<RcbCreatePaymentResponse>

    @GET("payment-status/{order_id}")
    suspend fun getPaymentStatus(
        @Path("order_id") orderId: String,
        @Header("x-api-key") apiKey: String
    ): Response<RcbPaymentStatusResponse>

    @POST("payout/create")
    suspend fun createPayout(
        @Header("x-api-key") apiKey: String,
        @Body request: RcbPayoutRequest
    ): Response<RcbPayoutResponse>

    @POST("disbursement/create")
    suspend fun createDisbursement(
        @Header("x-api-key") apiKey: String,
        @Body request: RcbPayoutRequest
    ): Response<RcbPayoutResponse>
}

// --- Client Singleton ---

object RcbApiClient {
    const val BASE_URL = "https://api.ragaciptabersama.web.id/api/"
    // Live API Key provided by user for PT. Raga Cipta Bersama
    const val DEFAULT_API_KEY = "rcb_live_usr_ffbffccebf4e"

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val service: RcbApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(RcbApiService::class.java)
    }

    fun mapBankToRcbCode(bankName: String): String {
        val upper = bankName.uppercase()
        return when {
            // E-Wallets
            upper.contains("OVO") -> "OVO"
            upper.contains("DANA") -> "DANA"
            upper.contains("GOPAY") || upper.contains("GO-PAY") -> "GOPAY"
            upper.contains("SHOPEEPAY") || upper.contains("SHOPEE") -> "SHOPEEPAY"
            upper.contains("LINKAJA") || upper.contains("LINK AJA") -> "LINKAJA"
            // Banks
            upper.contains("BCA") -> "BCA"
            upper.contains("MANDIRI") -> "MANDIRI"
            upper.contains("BRI") -> "BRI"
            upper.contains("BNI") -> "BNI"
            upper.contains("CIMB") -> "CIMB"
            upper.contains("PERMATA") -> "PERMATA"
            upper.contains("DANAMON") -> "DANAMON"
            upper.contains("BSI") || upper.contains("SYARIAH INDONESIA") -> "BSI"
            upper.contains("JAGO") -> "JAGO"
            upper.contains("BNC") || upper.contains("NEO") -> "BNC"
            upper.contains("SEABANK") -> "SEABANK"
            upper.contains("ALOBANK") || upper.contains("ALLO") -> "ALOBANK"
            upper.contains("SAMPOERNA") -> "SAMPOERNA"
            upper.contains("MAYBANK") -> "MAYBANK"
            upper.contains("NOBU") -> "NOBU"
            upper.contains("BTN") -> "BTN"
            else -> "OTHER"
        }
    }
}
