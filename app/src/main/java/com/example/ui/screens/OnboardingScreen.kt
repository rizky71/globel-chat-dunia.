package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.utils.AppStrings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

data class CountryOption(val name: String, val code: String, val flag: String)
data class LanguageOption(val name: String, val code: String)

val countryOptions = listOf(
    CountryOption("Indonesia", "ID", "🇮🇩"),
    CountryOption("Jepang", "JP", "🇯🇵"),
    CountryOption("Brazil", "BR", "🇧🇷"),
    CountryOption("Prancis", "FR", "🇫🇷"),
    CountryOption("Mesir", "EG", "🇪🇬"),
    CountryOption("Jerman", "DE", "🇩🇪"),
    CountryOption("Amerika Serikat", "US", "🇺🇸")
)

val languageOptions = listOf(
    LanguageOption("Bahasa Indonesia", "id"),
    LanguageOption("Inggris", "en"),
    LanguageOption("Jepang", "ja"),
    LanguageOption("Portugis", "pt"),
    LanguageOption("Prancis", "fr"),
    LanguageOption("Arab", "ar"),
    LanguageOption("Jerman", "de")
)

val avatarPresets = listOf(
    "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&h=150&q=80", // Traveler 1
    "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=150&h=150&q=80", // Traveler 2
    "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=150&h=150&q=80", // Explorer 1
    "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=150&h=150&q=80", // Explorer 2
    "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?auto=format&fit=crop&w=150&h=150&q=80", // Backpacker 1
    "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=150&h=150&q=80", // Backpacker 2
    "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=150&h=150&q=80"  // Photographer 1
)

@Composable
fun OnboardingScreen(
    onRegisterSuccess: (String, String, String, String, String, String, String, String) -> Unit
) {
    // Automatically detect country and language from user's system locale
    val systemLocale = java.util.Locale.getDefault()
    val systemLang = when (systemLocale.language.lowercase()) {
        "in" -> "id"
        else -> systemLocale.language.lowercase()
    }
    val detectedCountry = countryOptions.find { it.code.equals(systemLocale.country, ignoreCase = true) } ?: countryOptions[0]
    val detectedLanguage = languageOptions.find { it.code.equals(systemLang, ignoreCase = true) } ?: languageOptions[0]

    var nameInput by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf(detectedCountry) }
    var selectedLanguage by remember { mutableStateOf(detectedLanguage) }
    var selectedAvatarUrl by remember { mutableStateOf(avatarPresets[0]) }
    var bioInput by remember { mutableStateOf("Halo! Saya suka bertualang, belajar budaya baru, dan mengobrol dengan orang-orang dari seluruh dunia! 🌎") }
    var isError by remember { mutableStateOf(false) }
    val lang = selectedLanguage.code

    val context = LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val savedPath = com.example.ui.utils.FileUtil.saveUriToInternalStorage(context, it)
            if (savedPath != null) {
                selectedAvatarUrl = savedPath
            }
        }
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = AppStrings.get("app_title", lang),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (lang == "id") "Temukan teman perjalanan budaya dan mengobrol tanpa batas bahasa" else "Find cultural travel companions and chat without language barriers",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 6.dp)
                )
            }

            // Small dynamic preview card of the Passport
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .padding(end = 12.dp)
                    ) {
                        AsyncImage(
                            model = selectedAvatarUrl,
                            contentDescription = "Preview Foto Profil",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = selectedCountry.flag, fontSize = 12.sp)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (nameInput.isBlank()) (if (lang == "id") "Nama Penumpang" else "Passenger Name") else nameInput,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = AppStrings.get("origin", lang) + ": ${selectedCountry.name} (${selectedCountry.code})",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = AppStrings.get("language", lang) + ": ${selectedLanguage.name}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        if (bioInput.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "\"$bioInput\"",
                                fontSize = 11.sp,
                                style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Input Form
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name
                Text(
                    text = if (lang == "id") "1. Siapa nama panggilanmu?" else "1. What is your nickname?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = {
                        nameInput = it
                        if (it.isNotBlank()) isError = false
                    },
                    placeholder = { Text(if (lang == "id") "Masukkan nama panggilanmu..." else "Enter your nickname...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text(
                                text = AppStrings.get("error_name", lang),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                    )
                )

                // Country Picker with Flag Indicators
                Column {
                    Text(
                        text = if (lang == "id") "2. Pilih Negara Asal Anda 🗺️" else "2. Select Your Home Country 🗺️",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (lang == "id") "• Terdeteksi otomatis dari sistem: ${detectedCountry.name}" else "• Auto-detected from system: ${detectedCountry.name}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(countryOptions) { option ->
                        val isSelected = option == selectedCountry
                        Card(
                            onClick = {
                                selectedCountry = option
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
                                    selectedLanguage = it
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .width(120.dp)
                                .height(80.dp)
                                .testTag("country_option_${option.code}")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(text = option.flag, fontSize = 24.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = option.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Language Preference Picker
                Column {
                    Text(
                        text = if (lang == "id") "3. Pilih Bahasa Utama Anda 🗣️" else "3. Select Your Main Language 🗣️",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (lang == "id") "• Terdeteksi otomatis dari sistem: ${detectedLanguage.name}" else "• Auto-detected from system: ${detectedLanguage.name}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(languageOptions) { option ->
                        val isSelected = option == selectedLanguage
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedLanguage = option },
                            label = { Text(option.name, fontSize = 12.sp) },
                            modifier = Modifier.testTag("language_option_${option.code}")
                        )
                    }
                }

                // 4. Pilih Foto Profil Perjalanan Anda
                Text(
                    text = if (lang == "id") "4. Pilih Foto Profil Perjalanan Anda 📸" else "4. Choose Your Travel Profile Photo 📸",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                // LazyRow for preset choices
                LazyRow(
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
                                .size(64.dp)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                                .testTag("onboarding_gallery_pick_card")
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🖼️", fontSize = 24.sp)
                            }
                        }
                    }

                    items(avatarPresets) { presetUrl ->
                        val isSelected = presetUrl == selectedAvatarUrl
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .clickable { selectedAvatarUrl = presetUrl }
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                )
                        ) {
                            AsyncImage(
                                model = presetUrl,
                                contentDescription = "Pilihan Avatar",
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
                        .testTag("onboarding_gallery_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(AppStrings.get("gallery_btn", lang), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                


                // 5. Tulis Bio Singkat Anda
                Text(
                    text = if (lang == "id") "5. Tulis Bio Singkat Anda 📝" else "5. Write Your Short Bio 📝",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                OutlinedTextField(
                    value = bioInput,
                    onValueChange = { bioInput = it },
                    placeholder = { Text(AppStrings.get("bio_hint", lang)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("onboarding_bio_input"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Submit Button
            Button(
                onClick = {
                    if (nameInput.trim().isNotBlank()) {
                        onRegisterSuccess(
                            nameInput.trim(),
                            selectedLanguage.name,
                            selectedLanguage.code,
                            selectedCountry.name,
                            selectedCountry.code,
                            selectedCountry.flag,
                            selectedAvatarUrl.trim(),
                            bioInput.trim()
                        )
                    } else {
                        isError = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("onboarding_submit_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = if (lang == "id") "Ambil Boarding Pass Anda 🛫" else "Get Your Boarding Pass 🛫",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
