package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.AbloDatabase
import com.example.data.ChatPartner
import com.example.data.UserMessage
import com.example.data.UserProfile
import com.example.repository.AbloRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AbloViewModel(
    application: Application,
    private val repository: AbloRepository
) : AndroidViewModel(application) {

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val chatPartners: StateFlow<List<ChatPartner>> = repository.chatPartners
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentConversation = MutableStateFlow<List<UserMessage>>(emptyList())
    val currentConversation: StateFlow<List<UserMessage>> = _currentConversation.asStateFlow()

    private val _isMatching = MutableStateFlow(false)
    val isMatching: StateFlow<Boolean> = _isMatching.asStateFlow()

    private val _matchingPartner = MutableStateFlow<ChatPartner?>(null)
    val matchingPartner: StateFlow<ChatPartner?> = _matchingPartner.asStateFlow()

    private val _isSendingMessage = MutableStateFlow(false)
    val isSendingMessage: StateFlow<Boolean> = _isSendingMessage.asStateFlow()

    private val _matchingStatusText = MutableStateFlow("")
    val matchingStatusText: StateFlow<String> = _matchingStatusText.asStateFlow()

    val isApiKeyConfigured: Boolean
        get() {
            val key = BuildConfig.GEMINI_API_KEY
            return key.isNotEmpty() && key != "MY_GEMINI_API_KEY"
        }

    fun registerUser(
        name: String,
        nativeLanguage: String,
        nativeLanguageCode: String,
        homeCountry: String,
        homeCountryCode: String,
        homeFlagEmoji: String,
        avatarUrl: String = "",
        bio: String = "Halo! Saya suka bertualang, belajar budaya baru, dan mengobrol dengan orang-orang dari seluruh dunia! 🌎"
    ) {
        viewModelScope.launch {
            val profile = UserProfile(
                name = name,
                nativeLanguage = nativeLanguage,
                nativeLanguageCode = nativeLanguageCode,
                homeCountry = homeCountry,
                homeCountryCode = homeCountryCode,
                homeFlagEmoji = homeFlagEmoji,
                avatarUrl = avatarUrl,
                bio = bio,
                milesTravelled = 0
            )
            repository.insertUserProfile(profile)
        }
    }

    fun updateUserProfile(
        name: String,
        nativeLanguage: String,
        nativeLanguageCode: String,
        homeCountry: String,
        homeCountryCode: String,
        homeFlagEmoji: String,
        avatarUrl: String,
        bio: String
    ) {
        viewModelScope.launch {
            val current = userProfile.value ?: return@launch
            val updated = current.copy(
                name = name,
                nativeLanguage = nativeLanguage,
                nativeLanguageCode = nativeLanguageCode,
                homeCountry = homeCountry,
                homeCountryCode = homeCountryCode,
                homeFlagEmoji = homeFlagEmoji,
                avatarUrl = avatarUrl,
                bio = bio
            )
            repository.insertUserProfile(updated)
        }
    }

    fun startMatching() {
        val currentProfile = userProfile.value ?: return
        viewModelScope.launch {
            _isMatching.value = true
            _matchingPartner.value = null
            
            // Simulating flight takeoff & travel steps for Ablo immersion
            val flightSteps = listOf(
                "Menyiapkan tiket pesawat Anda... 🎫",
                "Boarding... Silakan kenakan sabuk pengaman Anda. 🛫",
                "Lepas landas! Meninggalkan Indonesia... ☁️",
                "Terbang di ketinggian 11.000 meter... ✈️",
                "Melintasi batas samudra dan benua... 🗺️",
                "Mempersiapkan pendaratan di destinasi baru... 🛬",
                "Mendarat dengan selamat! Menyinkronkan waktu lokal... 🌍"
            )

            for (step in flightSteps) {
                _matchingStatusText.value = step
                delay(800) // Beautiful progression
            }

            // Perform real matching
            val partner = repository.matchNewPartner(currentProfile)
            _matchingPartner.value = partner
            _isMatching.value = false
        }
    }

    fun clearMatchingPartner() {
        _matchingPartner.value = null
    }

    fun observeConversation(partnerId: String) {
        viewModelScope.launch {
            repository.getMessagesForPartner(partnerId).collect {
                _currentConversation.value = it
            }
        }
    }

    fun sendMessage(partnerId: String, text: String, autoTranslate: Boolean, imageUrl: String? = null) {
        if (text.isBlank() && imageUrl == null) return
        viewModelScope.launch {
            val partner = chatPartners.value.find { it.id == partnerId } ?: return@launch
            
            // User message model
            // Original: typed text (Indonesian)
            // Translated: text in partner's language
            var translated: String? = null
            if (autoTranslate && text.isNotBlank()) {
                translated = repository.translateText(text, "id", partner.nativeLanguageCode)
            }

            val userMsg = UserMessage(
                senderId = "me",
                partnerId = partnerId,
                messageText = text,
                translatedText = translated,
                timestamp = System.currentTimeMillis(),
                imageUrl = imageUrl
            )

            // Save user message
            repository.saveMessage(userMsg)

            // Trigger AI Typing status
            _isSendingMessage.value = true
            
            // Wait slightly for natural response flow
            delay(1500)

            // Generate reply
            val promptText = if (text.isNotBlank()) text else if (imageUrl != null) "[Mengirimkan Foto]" else ""
            repository.generatePartnerResponse(partnerId, promptText)
            _isSendingMessage.value = false
        }
    }

    fun deleteChat(partnerId: String) {
        viewModelScope.launch {
            repository.deletePartner(partnerId)
        }
    }

    fun resetApp() {
        viewModelScope.launch {
            repository.resetAllData()
        }
    }
}

class AbloViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AbloViewModel::class.java)) {
            val database = AbloDatabase.getDatabase(application)
            val repository = AbloRepository(database.chatDao())
            @Suppress("UNCHECKED_CAST")
            return AbloViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
