package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.components.kivoInputTextStyle
import com.example.ui.components.kivoTextFieldColors
import com.example.ui.theme.CardBorderColor
import com.example.ui.theme.CleanWhite
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyBluePrimary
import com.example.ui.theme.PageBackground
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueDark
import com.example.ui.theme.SoftBlueBg
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryMuted
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ChatSender {
    ASSISTANT_BOT, // Asisten KivoPay (Karakter Wanita 3D)
    HUMAN_CS,      // CS Resmi KivoPay (Nadia - Agent Manusia)
    USER,          // Pengguna KivoPay
    SYSTEM         // Pesan Pengalihan Sistem
}

enum class ActiveAgentMode {
    VIRTUAL_ASSISTANT,
    HUMAN_AGENT
}

data class LiveChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: ChatSender,
    val text: String? = null,
    val imageUri: String? = null,
    val imageDrawableId: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveChatScreen(
    userFullName: String,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    var activeMode by remember { mutableStateOf(ActiveAgentMode.VIRTUAL_ASSISTANT) }
    var isAgentTyping by remember { mutableStateOf(false) }

    // Dialog for full-screen image inspection
    var previewImageUrl by remember { mutableStateOf<String?>(null) }
    var previewImageDrawable by remember { mutableStateOf<Int?>(null) }

    val messages = remember {
        mutableStateListOf(
            LiveChatMessage(
                sender = ChatSender.ASSISTANT_BOT,
                text = "Halo Kak $userFullName! 👋 Selamat datang di Pusat Bantuan Resmi KivoPay.\n\nSaya Asisten KivoPay siap membantu Anda 24 jam setiap hari terkait informasi akun, top up, atau transaksi digital."
            ),
            LiveChatMessage(
                sender = ChatSender.ASSISTANT_BOT,
                text = "Silakan pilih salah satu opsi bantuan cepat di bawah ini atau ketik langsung pesan Anda:"
            )
        )
    }

    val quickReplies = listOf(
        "Cara Top Up Saldo QRIS",
        "Kendala Penarikan Dana Bank",
        "Panduan KivoPay+ & Limit",
        "Ubah Rekening Bank",
        "Hubungi Customer Service (Live CS)"
    )

    // Photo picker for sending images
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val userMsg = LiveChatMessage(
                sender = ChatSender.USER,
                imageUri = it.toString()
            )
            messages.add(userMsg)
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
                delay(1200L)
                if (activeMode == ActiveAgentMode.HUMAN_AGENT) {
                    messages.add(
                        LiveChatMessage(
                            sender = ChatSender.HUMAN_CS,
                            text = "Terima kasih atas lampiran fotonya Kak. Sedang saya periksa di sistem perbankan KivoPay ya. Mohon ditunggu sebentar."
                        )
                    )
                } else {
                    messages.add(
                        LiveChatMessage(
                            sender = ChatSender.ASSISTANT_BOT,
                            text = "Gambar Anda telah diterima oleh sistem Asisten KivoPay. Jika Anda memerlukan pengecekan langsung oleh tim operasional, silakan pilih 'Hubungi Customer Service'."
                        )
                    )
                }
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    fun handleQuickReply(option: String) {
        // Add user selected option as message
        messages.add(LiveChatMessage(sender = ChatSender.USER, text = option))

        coroutineScope.launch {
            listState.animateScrollToItem(messages.size - 1)
            isAgentTyping = true
            delay(1000L)
            isAgentTyping = false

            when (option) {
                "Cara Top Up Saldo QRIS" -> {
                    messages.add(
                        LiveChatMessage(
                            sender = ChatSender.ASSISTANT_BOT,
                            text = "📌 Panduan Top Up Saldo KivoPay:\n1. Buka menu Top Up di halaman utama.\n2. Pilih metode QRIS Direct MDR (Bank BNI) atau QRIS Mandiri.\n3. Masukkan nominal top up (minimal Rp 10.000).\n4. Pindai kode QRIS menggunakan aplikasi perbankan atau e-wallet Anda.\n5. Begitu Anda membayar, sistem otomatis mendeteksi dan saldo masuk seketika (T+0)!"
                        )
                    )
                }

                "Kendala Penarikan Dana Bank" -> {
                    messages.add(
                        LiveChatMessage(
                            sender = ChatSender.ASSISTANT_BOT,
                            text = "💸 Informasi Penarikan Dana:\n• Penarikan dana diproses otomatis ke rekening bank Anda yang telah terverifikasi.\n• Biaya administrasi standar adalah Rp 3.000 per transaksi.\n• Khusus penarikan ke akun OVO adalah GRATIS Biaya Admin (Promo KivoPay).\n• Pastikan status KYC rekening bank Anda sudah berstatus 'Disetujui'."
                        )
                    )
                }

                "Panduan KivoPay+ & Limit" -> {
                    messages.add(
                        LiveChatMessage(
                            sender = ChatSender.ASSISTANT_BOT,
                            text = "⭐ Keuntungan KivoPay+:\n• Limit penerimaan uang hingga Rp 30.000.000 (Akun reguler Rp 4.000.000).\n• Saldo tertahan otomatis dicairkan ke saldo aktif begitu upgrade disetujui.\n• Cara daftar: Buka menu KivoPay+ di beranda atau profil, unggah foto KTP asli dan foto selfie bersama KTP."
                        )
                    )
                }

                "Ubah Rekening Bank" -> {
                    messages.add(
                        LiveChatMessage(
                            sender = ChatSender.ASSISTANT_BOT,
                            text = "🏦 Prosedur Penggantian Rekening Bank:\nDemi keamanan akun, perubahan rekening bank wajib mengisi formulir resmi di Profil -> Ubah Rekening Bank dengan mengunggah foto KTP dan selfie bersama KTP, kemudian menghubungi CS KivoPay untuk tahap verifikasi lanjutan."
                        )
                    )
                }

                "Hubungi Customer Service (Live CS)" -> {
                    // System handoff event
                    messages.add(
                        LiveChatMessage(
                            sender = ChatSender.SYSTEM,
                            text = "Chat kamu telah dialihkan ke CS KivoPay"
                        )
                    )
                    activeMode = ActiveAgentMode.HUMAN_AGENT

                    delay(800L)
                    messages.add(
                        LiveChatMessage(
                            sender = ChatSender.HUMAN_CS,
                            text = "Halo Kak $userFullName! Selamat datang di layanan CS Resmi KivoPay. Saya Nadia yang akan menangani kendala transaksi atau pertanyaan Anda hari ini. Ada yang bisa saya bantu?"
                        )
                    )
                }
            }
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun sendUserMessage(text: String) {
        if (text.isBlank()) return
        val trimmed = text.trim()
        inputText = ""
        messages.add(LiveChatMessage(sender = ChatSender.USER, text = trimmed))

        coroutineScope.launch {
            listState.animateScrollToItem(messages.size - 1)
            isAgentTyping = true
            delay(1200L)
            isAgentTyping = false

            val lower = trimmed.lowercase()

            // Trigger handoff to CS if user mentions agent/cs/human
            if (activeMode == ActiveAgentMode.VIRTUAL_ASSISTANT && (lower.contains("cs") || lower.contains("agent") || lower.contains("manusia") || lower.contains("operator") || lower.contains("bantuan"))) {
                messages.add(
                    LiveChatMessage(
                        sender = ChatSender.SYSTEM,
                        text = "Chat kamu telah dialihkan ke CS KivoPay"
                    )
                )
                activeMode = ActiveAgentMode.HUMAN_AGENT
                delay(600L)
                messages.add(
                    LiveChatMessage(
                        sender = ChatSender.HUMAN_CS,
                        text = "Halo Kak $userFullName! Saya Nadia dari Customer Service KivoPay telah tersambung. Silakan jelaskan detail kendala Anda, dan kirimkan tangkapan layar jika diperlukan ya."
                    )
                )
            } else if (activeMode == ActiveAgentMode.HUMAN_AGENT) {
                // Human agent response logic
                if (lower.contains("tarik") || lower.contains("saldo") || lower.contains("topup") || lower.contains("invoice")) {
                    messages.add(
                        LiveChatMessage(
                            sender = ChatSender.HUMAN_CS,
                            text = "Baik Kak, sistem gateway pembayaran kami sedang memvalidasi mutasi tersebut. Boleh diinfokan Order ID tagihan atau nomor rekening tujuannya?"
                        )
                    )
                } else if (lower.contains("rekening") || lower.contains("ktp") || lower.contains("kivopay+")) {
                    messages.add(
                        LiveChatMessage(
                            sender = ChatSender.HUMAN_CS,
                            text = "Untuk verifikasi dokumen, data Anda sudah kami terima dan sedang ditinjau oleh tim verifikator KivoPay. Mohon pastikan foto identitas terbaca jelas ya Kak."
                        )
                    )
                    delay(800L)
                    // CS sends guide photo example
                    messages.add(
                        LiveChatMessage(
                            sender = ChatSender.HUMAN_CS,
                            imageDrawableId = R.drawable.ic_qris_logo,
                            text = "Berikut format contoh dokumen resmi yang terverifikasi di sistem KivoPay."
                        )
                    )
                } else {
                    messages.add(
                        LiveChatMessage(
                            sender = ChatSender.HUMAN_CS,
                            text = "Pesan Anda sudah kami catat Kak. Tim CS KivoPay selalu siap memastikan transaksi digital Anda berjalan aman dan lancar."
                        )
                    )
                }
            } else {
                // Bot response
                messages.add(
                    LiveChatMessage(
                        sender = ChatSender.ASSISTANT_BOT,
                        text = "Terima kasih atas pertanyaannya. Untuk bantuan otomatis, silakan pilih salah satu opsi di bawah atau ketik 'Hubungi CS' untuk berbicara dengan customer service kami."
                    )
                )
            }
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Profile Avatar: Switches between 3D Female Character and Human CS Nadia
                        Box {
                            if (activeMode == ActiveAgentMode.VIRTUAL_ASSISTANT) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_asisten_kivo),
                                    contentDescription = "Asisten KivoPay",
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .border(1.5.dp, PrimaryBlue, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.img_cs_agent),
                                    contentDescription = "CS KivoPay",
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .border(1.5.dp, EmeraldSuccess, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // Online dot indicator
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(11.dp)
                                    .background(EmeraldSuccess, CircleShape)
                                    .border(1.5.dp, CleanWhite, CircleShape)
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (activeMode == ActiveAgentMode.VIRTUAL_ASSISTANT) "Asisten KivoPay" else "Nadia (CS KivoPay)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimaryDark
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Resmi",
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            Text(
                                text = if (activeMode == ActiveAgentMode.VIRTUAL_ASSISTANT)
                                    "Asisten Virtual Otomatis 24/7"
                                else
                                    "Customer Care Specialist • Online",
                                fontSize = 11.sp,
                                color = TextSecondaryMuted
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = TextPrimaryDark)
                    }
                },
                actions = {
                    // Option to Close/End CS Session if currently talking to human agent
                    if (activeMode == ActiveAgentMode.HUMAN_AGENT) {
                        Surface(
                            onClick = {
                                messages.add(
                                    LiveChatMessage(
                                        sender = ChatSender.SYSTEM,
                                        text = "Sesi dengan CS KivoPay telah diakhiri. Chat dialihkan kembali ke Asisten KivoPay"
                                    )
                                )
                                activeMode = ActiveAgentMode.VIRTUAL_ASSISTANT
                                coroutineScope.launch {
                                    delay(500L)
                                    messages.add(
                                        LiveChatMessage(
                                            sender = ChatSender.ASSISTANT_BOT,
                                            text = "Sesi dengan CS selesai. Ada hal lain yang bisa Asisten KivoPay bantu?"
                                        )
                                    )
                                    listState.animateScrollToItem(messages.size - 1)
                                }
                            },
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFFFEBEE),
                            border = BorderStroke(1.dp, Color(0xFFEF4444)),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "Akhiri Sesi",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
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
                .imePadding()
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            // Chat Message Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatBubbleItem(
                        message = msg,
                        onImageClick = { url, resId ->
                            previewImageUrl = url
                            previewImageDrawable = resId
                        }
                    )
                }

                if (isAgentTyping) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = PrimaryBlue,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (activeMode == ActiveAgentMode.VIRTUAL_ASSISTANT) "Asisten KivoPay sedang mengetik..." else "Nadia sedang mengetik...",
                                fontSize = 11.sp,
                                color = TextSecondaryMuted
                            )
                        }
                    }
                }
            }

            // Quick Reply Option Buttons (Horizontal Scrollable Chips)
            if (activeMode == ActiveAgentMode.VIRTUAL_ASSISTANT) {
                Surface(
                    color = CleanWhite,
                    shadowElevation = 2.dp,
                    border = BorderStroke(1.dp, CardBorderColor)
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(
                            text = "💡 Pilihan Bantuan Cepat:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 6.dp)
                        )

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(quickReplies) { reply ->
                                Surface(
                                    onClick = { handleQuickReply(reply) },
                                    shape = RoundedCornerShape(16.dp),
                                    color = SoftBlueBg,
                                    border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.35f))
                                ) {
                                    Text(
                                        text = reply,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PrimaryBlueDark,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Chat Input Bar with Image Attachment Button
            Surface(
                color = CleanWhite,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Attachment Icon Button (Image Picker)
                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .background(SoftBlueBg, CircleShape)
                            .testTag("chat_attachment_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Kirim Gambar",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Text Input Field
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = if (activeMode == ActiveAgentMode.VIRTUAL_ASSISTANT) "Tanya Asisten KivoPay..." else "Ketik pesan ke CS Nadia...",
                                fontSize = 13.sp,
                                color = TextSecondaryMuted
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 44.dp, max = 100.dp)
                            .testTag("chat_input_field"),
                        shape = RoundedCornerShape(22.dp),
                        singleLine = false,
                        maxLines = 3,
                        textStyle = kivoInputTextStyle(),
                        colors = kivoTextFieldColors()
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Send Button
                    IconButton(
                        onClick = { sendUserMessage(inputText) },
                        enabled = inputText.isNotBlank(),
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                if (inputText.isNotBlank()) PrimaryBlue else Color(0xFFCBD5E1),
                                CircleShape
                            )
                            .testTag("chat_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Kirim",
                            tint = CleanWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // Full screen image preview dialog
    if (previewImageUrl != null || previewImageDrawable != null) {
        Dialog(onDismissRequest = {
            previewImageUrl = null
            previewImageDrawable = null
        }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        previewImageUrl = null
                        previewImageDrawable = null
                    },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CleanWhite)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = {
                                previewImageUrl = null
                                previewImageDrawable = null
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Tutup")
                            }
                        }

                        if (previewImageUrl != null) {
                            AsyncImage(
                                model = previewImageUrl,
                                contentDescription = "Lampiran Gambar",
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .heightIn(max = 400.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else if (previewImageDrawable != null) {
                            Image(
                                painter = painterResource(id = previewImageDrawable!!),
                                contentDescription = "Lampiran Gambar",
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .heightIn(max = 400.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubbleItem(
    message: LiveChatMessage,
    onImageClick: (String?, Int?) -> Unit
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(message.timestamp))

    when (message.sender) {
        ChatSender.SYSTEM -> {
            // Centered System Handoff Banner Bubble
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFEFF6FF),
                    border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.4f)),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = message.text ?: "",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlueDark
                        )
                    }
                }
            }
        }

        ChatSender.USER -> {
            // User Chat Bubble (Right aligned, Primary Blue)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.widthIn(max = 300.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                        color = PrimaryBlue,
                        shadowElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // If has image
                            if (!message.imageUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = message.imageUri,
                                    contentDescription = "Foto Terkirim",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 220.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onImageClick(message.imageUri, null) },
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }

                            if (!message.text.isNullOrBlank()) {
                                Text(
                                    text = message.text,
                                    fontSize = 13.sp,
                                    color = CleanWhite,
                                    lineHeight = 18.sp
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .padding(top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formattedTime,
                                    fontSize = 10.sp,
                                    color = CleanWhite.copy(alpha = 0.75f)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Terkirim",
                                    tint = CleanWhite.copy(alpha = 0.85f),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        ChatSender.ASSISTANT_BOT -> {
            // Asisten KivoPay Bubble (Left aligned with 3D character avatar)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_asisten_kivo),
                    contentDescription = "Asisten KivoPay",
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .border(1.dp, PrimaryBlue, CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.widthIn(max = 290.dp)) {
                    Text(
                        text = "Asisten KivoPay",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
                    )

                    Surface(
                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                        color = CleanWhite,
                        border = BorderStroke(1.dp, CardBorderColor),
                        shadowElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = message.text ?: "",
                                fontSize = 13.sp,
                                color = TextPrimaryDark,
                                lineHeight = 18.sp
                            )

                            Text(
                                text = formattedTime,
                                fontSize = 10.sp,
                                color = TextSecondaryMuted,
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        ChatSender.HUMAN_CS -> {
            // Human CS Nadia Bubble (Left aligned with real CS portrait avatar)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_cs_agent),
                    contentDescription = "CS Nadia",
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, EmeraldSuccess, CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.widthIn(max = 290.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Nadia (CS KivoPay)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = EmeraldSuccess.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "AGENT CS",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Surface(
                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                        color = CleanWhite,
                        border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.4f)),
                        shadowElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // If CS attached image
                            if (message.imageDrawableId != null) {
                                Image(
                                    painter = painterResource(id = message.imageDrawableId),
                                    contentDescription = "Gambar CS",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 180.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onImageClick(null, message.imageDrawableId) },
                                    contentScale = ContentScale.Fit
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            } else if (!message.imageUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = message.imageUri,
                                    contentDescription = "Gambar CS",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 200.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onImageClick(message.imageUri, null) },
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }

                            if (!message.text.isNullOrBlank()) {
                                Text(
                                    text = message.text,
                                    fontSize = 13.sp,
                                    color = TextPrimaryDark,
                                    lineHeight = 18.sp
                                )
                            }

                            Text(
                                text = formattedTime,
                                fontSize = 10.sp,
                                color = TextSecondaryMuted,
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
