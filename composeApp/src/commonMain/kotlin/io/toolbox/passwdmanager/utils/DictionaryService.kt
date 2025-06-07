package io.toolbox.passwdmanager.utils

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

data class WordDetails(
    val word: String,
    val phonetic: String? = null,
    val meanings: List<Meaning> = emptyList()
)

data class Meaning(
    val partOfSpeech: String,
    val definitions: List<Definition>
)

data class Definition(
    val definition: String,
    val example: String? = null
)

object DictionaryService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private val cache = mutableMapOf<String, Boolean>()

    suspend fun isWord(word: String): Boolean = withContext(Dispatchers.IO) {
        // Check cache first
        cache[word]?.let { return@withContext it }

        try {
            // Make API request
            val response = client.get("https://api.dictionaryapi.dev/api/v2/entries/en/$word")
            
            // If we get a 200 response, it's a word
            val isWord = response.status.value == 200
            
            // Cache the result
            cache[word] = isWord
            
            isWord
        } catch (e: Exception) {
            // If we get a 404, it's not a word
            if (e.message?.contains("404") == true) {
                cache[word] = false
                false
            } else {
                // For other errors, assume it's not a word to be safe
                cache[word] = false
                false
            }
        }
    }

    suspend fun getWordDetails(word: String): WordDetails? = withContext(Dispatchers.IO) {
        try {
            client.get("https://api.dictionaryapi.dev/api/v2/entries/en/$word").body()
        } catch (_: Exception) {
            null
        }
    }
} 