package com.example.repository

import android.util.Log
import com.example.BuildConfig
import com.example.api.*
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.random.Random

class AbloRepository(private val chatDao: ChatDao) {

    val userProfile: Flow<UserProfile?> = chatDao.getUserProfile()
    val chatPartners: Flow<List<ChatPartner>> = chatDao.getChatPartners()

    fun getMessagesForPartner(partnerId: String): Flow<List<UserMessage>> {
        return chatDao.getMessagesForPartner(partnerId)
    }

    suspend fun insertUserProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        chatDao.insertUserProfile(profile)
    }

    suspend fun saveMessage(message: UserMessage) = withContext(Dispatchers.IO) {
        chatDao.insertMessage(message)
    }

    suspend fun deletePartner(partnerId: String) = withContext(Dispatchers.IO) {
        chatDao.deletePartner(partnerId)
        chatDao.deleteMessagesForPartner(partnerId)
    }

    suspend fun resetAllData() = withContext(Dispatchers.IO) {
        chatDao.deleteAllPartners()
        chatDao.deleteAllMessages()
        // Keep profile but reset miles
        chatDao.getUserProfile().collect { profile ->
            if (profile != null) {
                chatDao.insertUserProfile(profile.copy(milesTravelled = 0, currentCountry = null))
            }
        }
    }

    // List of static mock countries and partners for matching
    private val availablePartners = listOf(
        ChatPartner(
            id = "partner_1",
            name = "Yuki Tanaka",
            country = "Jepang",
            countryCode = "JP",
            flagEmoji = "🇯🇵",
            bio = "Pencinta anime, kuliner ramen, dan fotografer amatir di Tokyo. Mari bertukar budaya!",
            nativeLanguage = "Jepang (日本語)",
            nativeLanguageCode = "ja",
            city = "Tokyo",
            localTimeOffset = 9,
            distanceKm = 5780,
            avatarColorSeed = 1
        ),
        ChatPartner(
            id = "partner_2",
            name = "Mateo Silva",
            country = "Brazil",
            countryCode = "BR",
            flagEmoji = "🇧🇷",
            bio = "Suka bermain sepak bola di pantai Copacabana, menari samba, dan minum kopi!",
            nativeLanguage = "Portugis (Português)",
            nativeLanguageCode = "pt",
            city = "Rio de Janeiro",
            localTimeOffset = -3,
            distanceKm = 17500,
            avatarColorSeed = 2
        ),
        ChatPartner(
            id = "partner_3",
            name = "Chloé Dubois",
            country = "Prancis",
            countryCode = "FR",
            flagEmoji = "🇫🇷",
            bio = "Pencinta seni di museum Louvre, musik akordeon, dan croissant hangat di pagi hari.",
            nativeLanguage = "Prancis (Français)",
            nativeLanguageCode = "fr",
            city = "Paris",
            localTimeOffset = 1,
            distanceKm = 11500,
            avatarColorSeed = 3
        ),
        ChatPartner(
            id = "partner_4",
            name = "Youssef Ali",
            country = "Mesir",
            countryCode = "EG",
            flagEmoji = "🇪🇬",
            bio = "Pemandu wisata sejarah di Giza, suka kopi rempah dan musik tradisional Arab.",
            nativeLanguage = "Arab (العربية)",
            nativeLanguageCode = "ar",
            city = "Kairo",
            localTimeOffset = 2,
            distanceKm = 9400,
            avatarColorSeed = 4
        ),
        ChatPartner(
            id = "partner_5",
            name = "Min-jun Kim",
            country = "Korea Selatan",
            countryCode = "KR",
            flagEmoji = "🇰🇷",
            bio = "Penggemar K-Pop, gamer, dan pencinta kuliner pedas tteokbokki di Hongdae, Seoul.",
            nativeLanguage = "Korea (한국어)",
            nativeLanguageCode = "ko",
            city = "Seoul",
            localTimeOffset = 9,
            distanceKm = 5200,
            avatarColorSeed = 5
        ),
        ChatPartner(
            id = "partner_6",
            name = "Giulia Rossi",
            country = "Italia",
            countryCode = "IT",
            flagEmoji = "🇮🇹",
            bio = "Suka memasak pasta tradisional, kopi espresso, dan jalan-jalan sore di Colosseum.",
            nativeLanguage = "Italia (Italiano)",
            nativeLanguageCode = "it",
            city = "Roma",
            localTimeOffset = 1,
            distanceKm = 11000,
            avatarColorSeed = 6
        ),
        ChatPartner(
            id = "partner_7",
            name = "Aarav Patel",
            country = "India",
            countryCode = "IN",
            flagEmoji = "🇮🇳",
            bio = "Insinyur perangkat lunak di Mumbai, pencinta kuliner pedas kari dan kriket.",
            nativeLanguage = "Hindi (हिन्दी)",
            nativeLanguageCode = "hi",
            city = "Mumbai",
            localTimeOffset = 5, // UTC +5.5
            distanceKm = 4600,
            avatarColorSeed = 7
        )
    )

    // Fallback dictionary for mock offline translation and conversation
    private val fallbackResponses = mapOf(
        "ja" to listOf(
            Pair("こんにちは！はじめまして。お元気ですか？", "Halo! Salam kenal. Apa kabar?"),
            Pair("日本の文化について話しましょう。寿司は好きですか？", "Mari bicara tentang budaya Jepang. Apakah kamu suka sushi?"),
            Pair("東京は今とても賑やかです！そちらはどうですか？", "Tokyo sekarang sangat ramai! Bagaimana dengan di sana?"),
            Pair("素晴らしいですね！いつか日本に来てください。", "Luar biasa! Silakan datang ke Jepang suatu hari nanti."),
            Pair("おやすみなさい！良い一日を。", "Selamat tidur! Semoga harimu menyenangkan.")
        ),
        "pt" to listOf(
            Pair("Olá! Muito prazer em te conhecer. Como você está?", "Halo! Senang sekali bertemu denganmu. Bagaimana kabarmu?"),
            Pair("O Brasil é muito caloroso e alegre! Você gosta de futebol?", "Brazil sangat hangat dan ceria! Apakah kamu suka sepak bola?"),
            Pair("Estou na praia bebendo água de coco agora mesmo!", "Saya sedang di pantai minum air kelapa saat ini juga!"),
            Pair("Que ótimo ouvir isso! Um abraço forte do Brasil.", "Senang mendengarnya! Pelukan hangat dari Brazil."),
            Pair("Até logo! Tenha um excelente dia.", "Sampai jumpa! Semoga harimu luar biasa.")
        ),
        "fr" to listOf(
            Pair("Bonjour ! Ravi de faire votre connaissance. Comment ça va ?", "Halo! Senang bertemu denganmu. Bagaimana kabarmu?"),
            Pair("La France est magnifique. Connaissez-vous la Tour Eiffel ?", "Prancis sangat indah. Apakah kamu tahu Menara Eiffel?"),
            Pair("Je mange un délicieux croissant à Paris en ce moment.", "Saya sedang makan croissant lezat di Paris saat ini."),
            Pair("C'est fantastique ! J'aimerais beaucoup visiter votre pays aussi.", "Itu fantastis! Saya juga ingin sekali mengunjungi negaramu."),
            Pair("Bonne journée à vous ! À bientôt.", "Semoga harimu menyenangkan! Sampai jumpa segera.")
        ),
        "ar" to listOf(
            Pair("مرحباً! سعيد بلقائك. كيف حالك؟", "Halo! Senang bertemu denganmu. Bagaimana kabarmu?"),
            Pair("مصر بلد الأهرامات والتاريخ العظيم! هل زرتها من قبل؟", "Mesir adalah negara piramida dan sejarah yang agung! Apakah kamu pernah berkunjung sebelumnya?"),
            Pair("الطقس هنا دافئ وجميل اليوم في القاهرة.", "Cuaca di sini hangat dan indah hari ini di Kairo."),
            Pair("هذا رائع حقاً! أتمنى لك كل التوفيق.", "Ini sungguh luar biasa! Semoga kamu sukses selalu."),
            Pair("مع السلامة! طاب يومك.", "Selamat tinggal! Semoga harimu menyenangkan.")
        ),
        "ko" to listOf(
            Pair("안녕하세요! 만나서 반가워요. 어떻게 지내세요?", "Halo! Senang bertemu denganmu. Bagaimana kabarmu?"),
            Pair("한국 요리는 아주 맛있어요. 김치를 좋아하시나요?", "Masakan Korea sangat lezat. Apakah kamu suka kimchi?"),
            Pair("지금 서울의 밤거리는 정말 아름다워요!", "Jalanan malam di Seoul sekarang sungguh indah!"),
            Pair("정말 멋지네요! 언제든지 편하게 이야기해요.", "Sungguh keren! Kapan saja santai saja mengobrol."),
            Pair("안녕히 계세요! 좋은 하루 보내세요.", "Sampai jumpa! Semoga harimu menyenangkan.")
        ),
        "it" to listOf(
            Pair("Ciao! Piacere di conoscerti. Come stai?", "Halo! Senang berkenalan denganmu. Bagaimana kabarmu?"),
            Pair("L'Italia è famosa per la pizza e l'arte. Ti piace la pasta?", "Italia terkenal dengan pizza dan seninya. Apakah kamu suka pasta?"),
            Pair("Sto bevendo un ottimo caffè espresso in piazza.", "Saya sedang minum kopi espresso lezat di alun-alun."),
            Pair("Che meraviglia! Spero che ci incontreremo un giorno.", "Sungguh indah! Saya harap kita bisa bertemu suatu hari nanti."),
            Pair("Arrivederci! Buona giornata.", "Sampai jumpa! Semoga harimu menyenangkan.")
        ),
        "hi" to listOf(
            Pair("नमस्ते! आपसे मिलकर बहुत खुशी हुई। आप कैसे हैं?", "Namaste! Senang sekali bertemu denganmu. Bagaimana kabarmu?"),
            Pair("भारत में कई त्योहार हैं। क्या आपने कभी दिवाली के बारे में सुना है?", "India memiliki banyak festival. Apakah kamu pernah mendengar tentang Diwali?"),
            Pair("आज मुंबई में हल्की बारिश हो रही है और मौसम सुहाना है।", "Hari ini di Mumbai sedang hujan gerimis dan cuacanya menyenangkan."),
            Pair("यह बहुत बढ़िया है! मुझे आपसे बात करके अच्छा लगा।", "Ini sangat luar biasa! Saya senang berbicara denganmu."),
            Pair("अलविदा! आपका दिन शुभ हो।", "Selamat tinggal! Semoga harimu menyenangkan.")
        )
    )

    // Translate text using Gemini or fallback
    suspend fun translateText(text: String, sourceLang: String, targetLang: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w("AbloRepo", "Gemini API Key missing, skipping real translation.")
            return@withContext "[Terjemahan Simulasi] $text"
        }

        val prompt = "Terjemahkan teks berikut dari bahasa $sourceLang ke bahasa $targetLang secara akurat. Hanya berikan hasil terjemahannya, tanpa penjelasan tambahan atau tanda kutip pembungkus:\n\nTeks: $text"
        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(temperature = 0.3f, maxOutputTokens = 500)
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim() ?: "[Error terjemahan]"
        } catch (e: Exception) {
            Log.e("AbloRepo", "Translation failed", e)
            "[Gagal menerjemahkan: ${e.localizedMessage}]"
        }
    }

    // Match with a new partner. Adds new partner to db and updates user's profile miles.
    suspend fun matchNewPartner(userProfile: UserProfile): ChatPartner = withContext(Dispatchers.IO) {
        val currentPartnerIds = chatDao.getChatPartners().first().map { it.id }.toSet()

        val unmatched = availablePartners.filter { it.id !in currentPartnerIds }
        val partner = if (unmatched.isNotEmpty()) {
            unmatched.random()
        } else {
            // Re-generate ID if all matched
            val base = availablePartners.random()
            base.copy(id = "partner_${UUID.randomUUID().toString().take(6)}")
        }

        // Save to DB
        chatDao.insertChatPartner(partner)

        // Update UserProfile miles
        val updatedProfile = userProfile.copy(
            milesTravelled = userProfile.milesTravelled + partner.distanceKm,
            currentCountry = partner.country
        )
        chatDao.insertUserProfile(updatedProfile)

        // Generate initial greeting message
        val defaultGreeting = fallbackResponses[partner.nativeLanguageCode]?.get(0)
            ?: Pair("Hello!", "Halo!")

        val initialMessage = UserMessage(
            senderId = partner.id,
            partnerId = partner.id,
            messageText = defaultGreeting.first,
            translatedText = defaultGreeting.second,
            timestamp = System.currentTimeMillis()
        )
        chatDao.insertMessage(initialMessage)

        partner
    }

    // Generate dynamic chat response using Gemini or offline dictionary
    suspend fun generatePartnerResponse(partnerId: String, userMessageText: String): UserMessage = withContext(Dispatchers.IO) {
        val partner = chatDao.getChatPartnerById(partnerId) ?: throw IllegalArgumentException("Partner not found")
        val apiKey = BuildConfig.GEMINI_API_KEY

        val nativeLang = partner.nativeLanguage
        val nativeCode = partner.nativeLanguageCode

        // Determine if we should use offline fallback
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Pick a responses pair based on conversation length or random
            val responses = fallbackResponses[nativeCode] ?: listOf(Pair("Hello!", "Halo!"))
            val randomPair = responses.random()

            val msg = UserMessage(
                senderId = partner.id,
                partnerId = partner.id,
                messageText = randomPair.first,
                translatedText = randomPair.second,
                timestamp = System.currentTimeMillis()
            )
            chatDao.insertMessage(msg)
            return@withContext msg
        }

        // If Gemini is available, generate dynamically!
        val systemPrompt = """
            Kamu adalah ${partner.name}, seseorang berusia 24 tahun yang tinggal di ${partner.city}, ${partner.country}. 
            Bahasa ibumu adalah ${partner.nativeLanguage}. Bahasa pertamamu adalah ${partner.nativeLanguageCode}.
            Kamu sedang mengobrol dengan pengguna asal Indonesia di aplikasi penjelajah budaya bernama Globel.
            Karaktermu: ${partner.bio}.
            Berbicaralah dan jawablah dalam bahasa ibumu (${partner.nativeLanguageCode}) saja. 
            Buat balasan percakapan yang sangat ramah, singkat (cukup 1-2 kalimat), santai, dan mencerminkan latar belakang atau budaya kotamu (${partner.city}).
            JANGAN pernah menyertakan terjemahan bahasa Indonesia di dalam teks jawabanmu. Jawab HANYA dalam bahasa ibumu!
        """.trimIndent()

        val conversationList = chatDao.getMessagesForPartner(partnerId).first()

        val chatContext = conversationList.takeLast(6).joinToString("\n") { msg ->
            if (msg.senderId == "me") "User: ${msg.messageText}" else "${partner.name}: ${msg.messageText}"
        }

        val prompt = "$chatContext\nUser baru saja mengirim: $userMessageText\nBalas dalam bahasa ibumu (${partner.nativeLanguageCode}):"

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
            generationConfig = GenerationConfig(temperature = 0.8f, maxOutputTokens = 300)
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val replyNative = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim() ?: ""

            if (replyNative.isNotEmpty()) {
                // Now translate the generated reply into Indonesian so the app has it pre-translated!
                val replyIndo = translateText(replyNative, partner.nativeLanguageCode, "id")

                val msg = UserMessage(
                    senderId = partner.id,
                    partnerId = partner.id,
                    messageText = replyNative,
                    translatedText = replyIndo,
                    timestamp = System.currentTimeMillis()
                )
                chatDao.insertMessage(msg)
                return@withContext msg
            }
        } catch (e: Exception) {
            Log.e("AbloRepo", "Gemini response generation failed, falling back", e)
        }

        // Fallback inside failure
        val responses = fallbackResponses[nativeCode] ?: listOf(Pair("Hello!", "Halo!"))
        val randomPair = responses.random()
        val msg = UserMessage(
            senderId = partner.id,
            partnerId = partner.id,
            messageText = randomPair.first,
            translatedText = randomPair.second,
            timestamp = System.currentTimeMillis()
        )
        chatDao.insertMessage(msg)
        msg
    }
}
