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
    @Json(name = "total_amount") val totalAmount: Long? = null
)

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
            upper.contains("ALOBANK") -> "ALOBANK"
            else -> "OTHER"
        }
    }
}
