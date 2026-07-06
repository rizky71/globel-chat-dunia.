package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.GTranslate
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.widget.Toast
import coil.compose.AsyncImage
import com.example.data.ChatPartner
import com.example.data.UserMessage
import com.example.ui.utils.AppStrings
import java.text.SimpleDateFormat
import java.util.*

fun createImageUri(context: android.content.Context): Uri {
    val directory = java.io.File(context.cacheDir, "camera_photos")
    if (!directory.exists()) {
        directory.mkdirs()
    }
    val file = java.io.File.createTempFile(
        "photo_${System.currentTimeMillis()}",
        ".jpg",
        directory
    )
    val authority = "com.example.fileprovider"
    return androidx.core.content.FileProvider.getUriForFile(context, authority, file)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    partner: ChatPartner,
    messages: List<UserMessage>,
    isSendingMessage: Boolean,
    langCode: String = "id",
    onSendMessage: (String, Boolean, String?) -> Unit,
    onBackClick: () -> Unit,
    onPartnerHeaderClick: () -> Unit
) {
    val context = LocalContext.current
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempCameraUri?.let { uri ->
                onSendMessage("", true, uri.toString())
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            onSendMessage("", true, it.toString())
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val uri = createImageUri(context)
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                Toast.makeText(context, "Error creating image file: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(
                context,
                if (langCode == "id") "Izin kamera ditolak. Silakan aktifkan di pengaturan." else "Camera permission denied. Please enable in settings.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    var textInput by remember { mutableStateOf("") }
    var autoTranslate by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()

    // Automatically scroll to the bottom when new messages arrive
    LaunchedEffect(messages.size, isSendingMessage) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Calculate dynamic local time of the partner based on their timezone offset
    val partnerLocalTime = remember(partner.localTimeOffset, messages.size) {
        try {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.add(Calendar.HOUR_OF_DAY, partner.localTimeOffset)
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            timeFormat.format(calendar.time)
        } catch (e: Exception) {
            "--:--"
        }
    }

    // Icebreaker prompts to boost engagement
    val icebreakers = if (langCode == "id") {
        listOf(
            "Kirim Salam Hangat 👋",
            "Tanya Makanan Khas 🍜",
            "Tanya Cuaca di Sana ☀️",
            "Tanya Destinasi Keren 🗺️",
            "Kirim Rekomendasi Lagu 🎵"
        )
    } else {
        listOf(
            "Send Warm Greeting 👋",
            "Ask About Local Food 🍜",
            "Ask About Weather ☀️",
            "Ask About Places 🗺️",
            "Ask for Song Recommendation 🎵"
        )
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = AppStrings.get("back_to_hub", langCode)
                            )
                        }
                    },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .testTag("partner_header_row")
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onPartnerHeaderClick() }
                                .padding(horizontal = 4.dp, vertical = 4.dp)
                        ) {
                            // Mini dynamic avatar
                            val avatarBg = when (partner.avatarColorSeed % 6) {
                                0 -> Color(0xFFE0F2FE)
                                1 -> Color(0xFFFEE2E2)
                                2 -> Color(0xFFDCFCE7)
                                3 -> Color(0xFFFEF9C3)
                                4 -> Color(0xFFF3E8FF)
                                else -> Color(0xFFFCE7F3)
                            }
                            val avatarTextColor = when (partner.avatarColorSeed % 6) {
                                0 -> Color(0xFF0369A1)
                                1 -> Color(0xFFB91C1C)
                                2 -> Color(0xFF15803D)
                                3 -> Color(0xFFA16207)
                                4 -> Color(0xFF6D28D9)
                                else -> Color(0xFFBE185D)
                            }

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(avatarBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = partner.name.take(2).uppercase(),
                                    color = avatarTextColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = partner.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = partner.flagEmoji,
                                        fontSize = 16.sp
                                    )
                                }
                                Text(
                                    text = if (langCode == "id") "Waktu Lokal: $partnerLocalTime • ${partner.distanceKm} km jauhnya" else "Local Time: $partnerLocalTime • ${partner.distanceKm} km away",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Translation toggle bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GTranslate,
                            contentDescription = "Translate",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (langCode == "id") "Terjemahan Otomatis Real-time" else "Real-time Auto-Translation",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = autoTranslate,
                        onCheckedChange = { autoTranslate = it },
                        modifier = Modifier
                            .scale(0.7f)
                            .height(24.dp)
                            .testTag("translate_switch")
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Message List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
            ) {
                items(messages) { msg ->
                    val isMe = msg.senderId == "me"
                    MessageBubble(
                        message = msg,
                        isMe = isMe,
                        partnerLanguage = partner.nativeLanguage,
                        langCode = langCode
                    )
                }

                if (isSendingMessage) {
                    item {
                        TypingIndicatorBubble(partnerName = partner.name, langCode = langCode)
                    }
                }
            }

            // Icebreaker Prompts Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(MaterialTheme.colorScheme.background),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                androidx.compose.foundation.lazy.LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(icebreakers) { icebreaker ->
                        AssistChip(
                            onClick = {
                                // Strip emoji when sending text if desired, or send as is
                                val textToSend = if (langCode == "id") {
                                    when (icebreaker) {
                                        "Kirim Salam Hangat 👋" -> "Halo! Saya mengirimkan salam hangat dari Indonesia. Bagaimana harimu di sana?"
                                        "Tanya Makanan Khas 🍜" -> "Wah, saya penasaran sekali! Apa makanan khas paling lezat yang wajib dicoba di kotamu?"
                                        "Tanya Cuaca di Sana ☀️" -> "Bagaimana cuaca di kotamu hari ini? Di Indonesia saat ini sangat hangat!"
                                        "Tanya Destinasi Keren 🗺️" -> "Jika suatu saat saya berkunjung ke kotamu, tempat wisata mana yang paling pertama harus saya kunjungi?"
                                        else -> "Tolong rekomendasikan satu lagu lokal favoritmu yang sering kamu dengarkan!"
                                    }
                                } else {
                                    when (icebreaker) {
                                        "Send Warm Greeting 👋" -> "Hello! I'm sending warm greetings to you. How is your day going?"
                                        "Ask About Local Food 🍜" -> "Wow, I'm so curious! What is the most delicious local food that I must try in your city?"
                                        "Ask About Weather ☀️" -> "How is the weather in your city today? It's very warm here!"
                                        "Ask About Places 🗺️" -> "If I ever visit your city, which tourist destination should I visit first?"
                                        else -> "Please recommend one of your favorite local songs that you listen to often!"
                                    }
                                }
                                onSendMessage(textToSend, autoTranslate, null)
                            },
                            label = { Text(icebreaker, fontSize = 11.sp) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(100.dp)
                        )
                    }
                }
            }

            // Input Row
            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding()
                        .imePadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val permissionCheckResult = androidx.core.content.ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.CAMERA
                            )
                            if (permissionCheckResult == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                try {
                                    val uri = createImageUri(context)
                                    tempCameraUri = uri
                                    cameraLauncher.launch(uri)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error creating image file: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                permissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = if (langCode == "id") "Ambil Foto" else "Take Photo",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = {
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = if (langCode == "id") "Pilih dari Galeri" else "Choose from Gallery",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text(if (langCode == "id") "Ketik pesan..." else "Type a message...", fontSize = 14.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_text_input"),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    FloatingActionButton(
                        onClick = {
                            if (textInput.trim().isNotBlank()) {
                                onSendMessage(textInput.trim(), autoTranslate, null)
                                textInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("chat_send_button"),
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Message",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: UserMessage,
    isMe: Boolean,
    partnerLanguage: String,
    langCode: String = "id"
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        val bubbleBg = if (isMe) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }

        val textColor = if (isMe) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }

        val bubbleShape = if (isMe) {
            RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 0.dp)
        } else {
            RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 16.dp)
        }

        Surface(
            color = bubbleBg,
            shape = bubbleShape,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Render image if present
                if (!message.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = message.imageUrl,
                        contentDescription = "Sent photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .padding(bottom = if (message.messageText.isNotBlank()) 8.dp else 0.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                // Show message text if present
                if (message.messageText.isNotBlank()) {
                    Text(
                        text = message.messageText,
                        color = textColor,
                        fontSize = 14.sp
                    )
                }

                // Render dynamic translation in bubble
                if (isMe && !message.translatedText.isNullOrBlank()) {
                    // Show our outgoing message translated into partner's native language
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "🌐 $partnerLanguage:\n${message.translatedText}",
                            color = textColor.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic
                        )
                    }
                } else if (!isMe && !message.translatedText.isNullOrBlank()) {
                    // Show incoming foreign message translated into user's language
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = if (langCode == "id") "🇮🇩 Terjemahan:\n${message.translatedText}" else "🌐 Translation:\n${message.translatedText}",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Format and render time
        val timeStr = remember(message.timestamp) {
            try {
                val format = SimpleDateFormat("HH:mm", Locale.getDefault())
                format.format(Date(message.timestamp))
            } catch (e: Exception) {
                ""
            }
        }
        
        Text(
            text = timeStr,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
        )
    }
}

@Composable
fun TypingIndicatorBubble(partnerName: String, langCode: String = "id") {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 16.dp),
            modifier = Modifier.widthIn(max = 240.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (langCode == "id") "$partnerName sedang menerjemahkan... ✍️" else "$partnerName is translating... ✍️",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

