package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ChatPartner
import com.example.data.UserProfile
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import com.example.ui.utils.AppStrings
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userProfile: UserProfile,
    chatPartners: List<ChatPartner>,
    isApiKeyConfigured: Boolean,
    onStartMatching: () -> Unit,
    onPartnerClick: (String) -> Unit,
    onDeletePartner: (String) -> Unit,
    onUpdateProfile: (String, String, String, String, String, String, String, String) -> Unit,
    onResetApp: () -> Unit
) {
    val lang = userProfile.nativeLanguageCode
    var showResetDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (pagerState.currentPage == 0) Icons.Default.Public else Icons.Default.People,
                            contentDescription = "Menu Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = if (pagerState.currentPage == 0) {
                                AppStrings.get("app_title", lang)
                            } else {
                                AppStrings.get("nav_companions", lang)
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    if (pagerState.currentPage == 0) {
                        TextButton(onClick = { showResetDialog = true }) {
                            Text(AppStrings.get("reset_hub", lang), color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = pagerState.currentPage == 0,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = AppStrings.get("nav_home", lang)
                        )
                    },
                    label = {
                        Text(
                            text = AppStrings.get("nav_home", lang),
                            fontWeight = if (pagerState.currentPage == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("nav_item_home")
                )

                NavigationBarItem(
                    selected = pagerState.currentPage == 1,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = AppStrings.get("nav_companions", lang)
                        )
                    },
                    label = {
                        Text(
                            text = AppStrings.get("nav_companions", lang),
                            fontWeight = if (pagerState.currentPage == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("nav_item_companions")
                )
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { page ->
            when (page) {
                0 -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Section 1: Welcome & Profile Header
                        item {
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(
                                    text = AppStrings.get("hello_user", lang, "name" to userProfile.name),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = AppStrings.get("ready_to_takeoff", lang),
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        // Section 2: Virtual Boarding Pass / Ticket
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("boarding_pass_card"),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp)
                                ) {
                                    // Ticket Header
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = AppStrings.get("boarding_pass_title", lang),
                                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                        Icon(
                                            imageVector = Icons.Default.FlightTakeoff,
                                            contentDescription = "Takeoff",
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Flight Locations
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = when (userProfile.homeCountryCode) {
                                                    "ID" -> "CGK"
                                                    "JP" -> "HND"
                                                    "BR" -> "GIG"
                                                    "FR" -> "CDG"
                                                    "EG" -> "CAI"
                                                    "DE" -> "FRA"
                                                    "US" -> "JFK"
                                                    else -> "CGK"
                                                },
                                                fontSize = 32.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Monospace,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                            Text(
                                                text = "${userProfile.homeCountry} ${userProfile.homeFlagEmoji}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                            )
                                        }

                                        // Flight Line
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .height(1.dp)
                                                    .weight(1f)
                                                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f))
                                            )
                                            Text(
                                                text = "✈️",
                                                fontSize = 16.sp,
                                                modifier = Modifier.padding(horizontal = 4.dp)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .height(1.dp)
                                                    .weight(1f)
                                                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f))
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "GLB",
                                                fontSize = 32.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Monospace,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                            Text(
                                                text = "Seluruh Dunia",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                            )
                                        }
                                    }

                                    // Divider dotted-style
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Passenger Details
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = AppStrings.get("nickname", lang).uppercase(),
                                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = userProfile.name,
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = AppStrings.get("miles_travelled", lang),
                                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = AppStrings.get("miles_val", lang, "miles" to userProfile.milesTravelled.toString()),
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // Match Launch Button
                                    Button(
                                        onClick = onStartMatching,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .testTag("start_flight_button"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.onPrimary,
                                            contentColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.FlightTakeoff,
                                                contentDescription = "Takeoff Icon",
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                            Text(
                                                text = AppStrings.get("takeoff_btn", lang),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Section 2.5: Passport Saya (User Profile Card Component)
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("user_passport_card"),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp)
                                ) {
                                    // Header row of Passport card
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "PASPOR VIRTUAL SAYA",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = "🛂", fontSize = 14.sp)
                                        }
                                        
                                        IconButton(
                                            onClick = { showEditProfileDialog = true },
                                            modifier = Modifier.size(28.dp).testTag("edit_profile_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Profil",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Bio layout
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Avatar on left with flag badge overlay
                                        val avatarBg = when (userProfile.name.hashCode() % 6) {
                                            0 -> Color(0xFFE0F2FE)
                                            1 -> Color(0xFFFEE2E2)
                                            2 -> Color(0xFFDCFCE7)
                                            3 -> Color(0xFFFEF9C3)
                                            4 -> Color(0xFFF3E8FF)
                                            else -> Color(0xFFFCE7F3)
                                        }
                                        val avatarTextColor = when (userProfile.name.hashCode() % 6) {
                                            0 -> Color(0xFF0369A1)
                                            1 -> Color(0xFFB91C1C)
                                            2 -> Color(0xFF15803D)
                                            3 -> Color(0xFFA16207)
                                            4 -> Color(0xFF6D28D9)
                                            else -> Color(0xFFBE185D)
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(68.dp)
                                        ) {
                                            if (userProfile.avatarUrl.isNotEmpty()) {
                                                AsyncImage(
                                                    model = userProfile.avatarUrl,
                                                    contentDescription = "Foto Profil Saya",
                                                    modifier = Modifier
                                                        .size(60.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(60.dp)
                                                        .clip(CircleShape)
                                                        .background(avatarBg),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = userProfile.name.take(2).uppercase(),
                                                        color = avatarTextColor,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 20.sp
                                                    )
                                                }
                                            }
                                            // Flag overlay badge
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White)
                                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                                    .padding(2.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = userProfile.homeFlagEmoji, fontSize = 12.sp)
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(16.dp))

                                        // Details on right
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = userProfile.name,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = AppStrings.get("origin", lang) + ": ",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                )
                                                Text(
                                                    text = "${userProfile.homeCountry} ${userProfile.homeFlagEmoji}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = AppStrings.get("language", lang) + ": ",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                )
                                                Text(
                                                    text = userProfile.nativeLanguage,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    // Short bio rendering
                                    if (userProfile.bio.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(14.dp))
                                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = AppStrings.get("bio_travel", lang),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = userProfile.bio,
                                            fontSize = 13.sp,
                                            style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                                        )
                                    }
                                }
                            }
                        }

                        // Section 3: API Warning Banner (Simulated Mode Indicator)
                        if (!isApiKeyConfigured) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "Warning",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.padding(end = 12.dp)
                                        )
                                        Column {
                                            Text(
                                                text = "Aplikasi Berjalan dalam Mode Simulasi",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                            Text(
                                                text = "Untuk menikmati terjemahan dinamis cerdas AI dan percakapan interaktif menggunakan Gemini, harap konfigurasikan GEMINI_API_KEY Anda di Secrets Panel AI Studio.",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Bottom space
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }

                1 -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Section 4: Chat Partners (Teman Perjalanan)
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = AppStrings.get("recent_companions", lang) + " (${chatPartners.size})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }

                        if (chatPartners.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Map,
                                            contentDescription = "Empty Map",
                                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = if (lang == "id") "Koper Anda Masih Kosong!" else "Your suitcase is empty!",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = AppStrings.get("no_companions", lang),
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            items(chatPartners, key = { it.id }) { partner ->
                                PartnerListItem(
                                    partner = partner,
                                    onClick = { onPartnerClick(partner.id) },
                                    onDelete = { onDeletePartner(partner.id) }
                                )
                            }
                        }

                        // Bottom space
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }

    // Reset confirmation dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(if (lang == "id") "Reset Semua Data?" else "Reset All Data?") },
            text = { Text(if (lang == "id") "Tindakan ini akan menghapus semua riwayat percakapan, semua teman perjalanan global, dan mengembalikan total Mil terbang Anda menjadi 0." else "This action will delete all conversation histories, all global travel companions, and reset your total flight miles to 0.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetApp()
                        showResetDialog = false
                    }
                ) {
                    Text(if (lang == "id") "Ya, Reset" else "Yes, Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(AppStrings.get("cancel", lang))
                }
            }
        )
    }

    // Edit profile dialog
    if (showEditProfileDialog) {
        var tempName by remember { mutableStateOf(userProfile.name) }
        var tempCountry by remember { mutableStateOf(countryOptions.find { it.code == userProfile.homeCountryCode } ?: countryOptions[0]) }
        var tempLanguage by remember { mutableStateOf(languageOptions.find { it.code == userProfile.nativeLanguageCode } ?: languageOptions[0]) }
        var tempAvatarUrl by remember { mutableStateOf(userProfile.avatarUrl) }
        var tempBio by remember { mutableStateOf(userProfile.bio) }
        var nameError by remember { mutableStateOf(false) }

        val context = LocalContext.current
        val galleryLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                val savedPath = com.example.ui.utils.FileUtil.saveUriToInternalStorage(context, it)
                if (savedPath != null) {
                    tempAvatarUrl = savedPath
                }
            }
        }

        val avatarPresets = listOf(
            "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&h=150&q=80",
            "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=150&h=150&q=80",
            "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=150&h=150&q=80",
            "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=150&h=150&q=80",
            "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?auto=format&fit=crop&w=150&h=150&q=80",
            "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=150&h=150&q=80",
            "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=150&h=150&q=80"
        )

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Text(
                    text = AppStrings.get("edit_profile_title", lang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Name Input
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = {
                            tempName = it
                            if (it.isNotBlank()) nameError = false
                        },
                        label = { Text(AppStrings.get("nickname", lang)) },
                        isError = nameError,
                        modifier = Modifier.fillMaxWidth().testTag("edit_name_input"),
                        singleLine = true,
                        supportingText = {
                            if (nameError) {
                                Text(AppStrings.get("error_name", lang), color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )

                    // Country Selection
                    Text(
                        text = "Negara Asal",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(countryOptions) { option ->
                            val isSelected = option == tempCountry
                            Card(
                                onClick = {
                                    tempCountry = option
                                    val matchingLangCode = when (option.code) {
                                        "ID" -> "id"
                                        "JP" -> "ja"
                                        "BR" -> "pt"
                                        "FR" -> "fr"
                                        "EG" -> "ar"
                                        "DE" -> "de"
                                        "US" -> "en"
                                        else -> "id"
                                    }
                                    languageOptions.find { it.code == matchingLangCode }?.let {
                                        tempLanguage = it
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(70.dp)
                                    .testTag("edit_country_${option.code}")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(text = option.flag, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = option.name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // Language Selection
                    Text(
                        text = AppStrings.get("language", lang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(languageOptions) { option ->
                            val isSelected = option == tempLanguage
                            FilterChip(
                                selected = isSelected,
                                onClick = { tempLanguage = option },
                                label = { Text(option.name, fontSize = 11.sp) },
                                modifier = Modifier.testTag("edit_language_${option.code}")
                            )
                        }
                    }

                    // Avatar Selection presets
                    Text(
                        text = AppStrings.get("profile_pic", lang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Gallery pick item
                        item {
                            Card(
                                onClick = { galleryLauncher.launch("image/*") },
                                shape = CircleShape,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier
                                    .size(56.dp)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape
                                    )
                                    .testTag("edit_gallery_pick_card")
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "🖼️", fontSize = 20.sp)
                                }
                            }
                        }

                        items(avatarPresets) { presetUrl ->
                            val isSelected = presetUrl == tempAvatarUrl
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .clickable { tempAvatarUrl = presetUrl }
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = CircleShape
                                    )
                            ) {
                                AsyncImage(
                                    model = presetUrl,
                                    contentDescription = "Pilihan Avatar Edit",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    // Gallery Button
                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_gallery_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(AppStrings.get("gallery_btn", lang), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }



                    // Bio Input field
                    Text(
                        text = AppStrings.get("bio_title", lang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(
                        value = tempBio,
                        onValueChange = { tempBio = it },
                        placeholder = { Text(AppStrings.get("bio_hint", lang)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("edit_bio_input"),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempName.trim().isNotBlank()) {
                            onUpdateProfile(
                                tempName.trim(),
                                tempLanguage.name,
                                tempLanguage.code,
                                tempCountry.name,
                                tempCountry.code,
                                tempCountry.flag,
                                tempAvatarUrl.trim(),
                                tempBio.trim()
                            )
                            showEditProfileDialog = false
                        } else {
                            nameError = true
                        }
                    },
                    modifier = Modifier.testTag("save_profile_button")
                ) {
                    Text(AppStrings.get("save_changes", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text(AppStrings.get("cancel", lang))
                }
            }
        )
    }
}

@Composable
fun PartnerListItem(
    partner: ChatPartner,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    // Elegant Avatar Background Color mapping based on ColorSeed
    val avatarBg = when (partner.avatarColorSeed % 6) {
        0 -> Color(0xFFE0F2FE) // Sky Blue
        1 -> Color(0xFFFEE2E2) // Rose
        2 -> Color(0xFFDCFCE7) // Mint
        3 -> Color(0xFFFEF9C3) // Light Gold
        4 -> Color(0xFFF3E8FF) // Purple
        else -> Color(0xFFFCE7F3) // Pink
    }
    
    val avatarTextColor = when (partner.avatarColorSeed % 6) {
        0 -> Color(0xFF0369A1)
        1 -> Color(0xFFB91C1C)
        2 -> Color(0xFF15803D)
        3 -> Color(0xFFA16207)
        4 -> Color(0xFF6D28D9)
        else -> Color(0xFFBE185D)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("partner_item_${partner.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Circle
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(avatarBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = partner.name.take(2).uppercase(),
                    color = avatarTextColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = partner.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = partner.flagEmoji,
                        fontSize = 16.sp
                    )
                }
                
                Text(
                    text = "${partner.city}, ${partner.country}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = partner.bio,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Action: Delete Match Button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Chat",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                )
            }
        }
    }
}
