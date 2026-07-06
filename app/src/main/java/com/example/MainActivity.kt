package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AbloViewModel
import com.example.ui.AbloViewModelFactory
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MatchingScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PartnerProfileScreen
import com.example.ui.theme.MyApplicationTheme

sealed class Screen {
    object Home : Screen()
    data class Chat(val partnerId: String) : Screen()
    data class PartnerProfile(val partnerId: String) : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val viewModel: AbloViewModel by viewModels {
                    AbloViewModelFactory(application)
                }

                val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
                val chatPartners by viewModel.chatPartners.collectAsStateWithLifecycle()
                val isMatching by viewModel.isMatching.collectAsStateWithLifecycle()
                val matchingPartner by viewModel.matchingPartner.collectAsStateWithLifecycle()
                val isSendingMessage by viewModel.isSendingMessage.collectAsStateWithLifecycle()
                val currentConversation by viewModel.currentConversation.collectAsStateWithLifecycle()
                val matchingStatusText by viewModel.matchingStatusText.collectAsStateWithLifecycle()

                var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

                // Dynamic UI Router
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when {
                        // User Profile is not created yet -> Go to Onboarding
                        userProfile == null -> {
                            OnboardingScreen(
                                onRegisterSuccess = { name, nativeLanguage, nativeLanguageCode, homeCountry, homeCountryCode, homeFlagEmoji, avatarUrl, bio ->
                                    viewModel.registerUser(
                                        name = name,
                                        nativeLanguage = nativeLanguage,
                                        nativeLanguageCode = nativeLanguageCode,
                                        homeCountry = homeCountry,
                                        homeCountryCode = homeCountryCode,
                                        homeFlagEmoji = homeFlagEmoji,
                                        avatarUrl = avatarUrl,
                                        bio = bio
                                    )
                                }
                            )
                        }

                        // Flight Matching in progress (loading flight state)
                        isMatching -> {
                            MatchingScreen(
                                statusText = matchingStatusText,
                                partner = null,
                                langCode = userProfile?.nativeLanguageCode ?: "id",
                                onChatClick = {},
                                onBackClick = {}
                            )
                        }

                        // Matching succeeded -> Reveal new dynamic partner card!
                        matchingPartner != null -> {
                            MatchingScreen(
                                statusText = matchingStatusText,
                                partner = matchingPartner,
                                langCode = userProfile?.nativeLanguageCode ?: "id",
                                onChatClick = { partnerId ->
                                    viewModel.observeConversation(partnerId)
                                    viewModel.clearMatchingPartner()
                                    currentScreen = Screen.Chat(partnerId)
                                },
                                onBackClick = {
                                    viewModel.clearMatchingPartner()
                                    currentScreen = Screen.Home
                                }
                            )
                        }

                        // Chat Screen active
                        currentScreen is Screen.Chat -> {
                            val partnerId = (currentScreen as Screen.Chat).partnerId
                            val partner = chatPartners.find { it.id == partnerId }

                            if (partner != null) {
                                // Dynamic back press interceptor
                                BackHandler {
                                    currentScreen = Screen.Home
                                }

                                ChatScreen(
                                    partner = partner,
                                    messages = currentConversation,
                                    isSendingMessage = isSendingMessage,
                                    langCode = userProfile?.nativeLanguageCode ?: "id",
                                    onSendMessage = { text, autoTranslate, imageUrl ->
                                        viewModel.sendMessage(partnerId, text, autoTranslate, imageUrl)
                                    },
                                    onBackClick = {
                                        currentScreen = Screen.Home
                                    },
                                    onPartnerHeaderClick = {
                                        currentScreen = Screen.PartnerProfile(partnerId)
                                    }
                                )
                            } else {
                                // Fallback to Home if partner deleted or missing
                                currentScreen = Screen.Home
                            }
                        }

                        // Partner Profile Detail Screen active
                        currentScreen is Screen.PartnerProfile -> {
                            val partnerId = (currentScreen as Screen.PartnerProfile).partnerId
                            val partner = chatPartners.find { it.id == partnerId }

                            if (partner != null) {
                                BackHandler {
                                    currentScreen = Screen.Chat(partnerId)
                                }

                                PartnerProfileScreen(
                                    partner = partner,
                                    langCode = userProfile?.nativeLanguageCode ?: "id",
                                    onBackClick = {
                                        currentScreen = Screen.Chat(partnerId)
                                    }
                                )
                            } else {
                                currentScreen = Screen.Home
                            }
                        }

                        // Default: Home Virtual Travel Hub
                        else -> {
                            HomeScreen(
                                userProfile = userProfile!!,
                                chatPartners = chatPartners,
                                isApiKeyConfigured = viewModel.isApiKeyConfigured,
                                onStartMatching = {
                                    viewModel.startMatching()
                                },
                                onPartnerClick = { partnerId ->
                                    viewModel.observeConversation(partnerId)
                                    currentScreen = Screen.Chat(partnerId)
                                },
                                onDeletePartner = { partnerId ->
                                    viewModel.deleteChat(partnerId)
                                },
                                onUpdateProfile = { name, nativeLanguage, nativeLanguageCode, homeCountry, homeCountryCode, homeFlagEmoji, avatarUrl, bio ->
                                    viewModel.updateUserProfile(name, nativeLanguage, nativeLanguageCode, homeCountry, homeCountryCode, homeFlagEmoji, avatarUrl, bio)
                                },
                                onResetApp = {
                                    viewModel.resetApp()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
