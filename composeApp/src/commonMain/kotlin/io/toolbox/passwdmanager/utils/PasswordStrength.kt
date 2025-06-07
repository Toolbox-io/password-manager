package io.toolbox.passwdmanager.utils

import kotlin.math.pow

object PasswordStrength {
    private val commonWords = setOf(
        "password", "admin", "qwerty", "123456", "welcome", "login", "abc123",
        "letmein", "monkey", "dragon", "baseball", "football", "superman",
        "trustno1", "iloveyou", "sunshine", "master", "shadow", "princess",
        "admin123", "password123", "qwerty123", "welcome123", "login123"
    )

    private val commonPatterns = listOf(
        "1234567890", "qwertyuiop", "asdfghjkl", "zxcvbnm",
        "0987654321", "poiuytrewq", "lkjhgfdsa", "mnbvcxz"
    )

    private val datePatterns = listOf(
        "\\d{4}", // 4-digit year
        "\\d{2}[01]\\d[0123]\\d", // YYMMDD or MMDDYY
        "\\d{2}[01]\\d[0123]\\d\\d{4}", // YYMMDDYYYY or MMDDYYYY
        "\\d{4}[01]\\d[0123]\\d" // YYYYMMDD
    )

    private val dictionaryService = DictionaryService

    private suspend fun containsDictionaryWords(password: String): Int {
        val words = password.lowercase().split(Regex("[^a-z]"))
        var wordCount = 0
        
        for (word in words) {
            if (word.length >= 3 && dictionaryService.isWord(word)) {
                wordCount++
            }
        }
        
        return wordCount
    }

    suspend fun calculateStrength(password: String): Double {
        if (password.isEmpty()) return 0.0
        
        // Get AI-based strength assessment (weighted at 40%)
        val aiStrength = PasswordStrengthAI.assessStrength(password) * 40
        
        // Get rule-based strength assessment (weighted at 60%)
        var ruleBasedScore = 0.0
        
        // Length contribution (up to 20% of rule-based score)
        ruleBasedScore += (password.length.coerceAtMost(20) / 20.0) * 20
        
        // Character variety contribution (up to 20% of rule-based score)
        val hasLowercase = password.any { it.isLowerCase() }
        val hasUppercase = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        
        val varietyScore = (if (hasLowercase) 1 else 0) +
                (if (hasUppercase) 1 else 0) +
                (if (hasDigit) 1 else 0) +
                (if (hasSpecial) 1 else 0)
        
        ruleBasedScore += (varietyScore / 4.0) * 20

        // Entropy contribution (up to 10% of rule-based score)
        val entropy = calculateEntropy(password)
        ruleBasedScore += (entropy / 8.0) * 10

        // Penalties (up to -20% of rule-based score)
        var penalty = 0.0

        // Check for common words
        if (commonWords.any { password.lowercase().contains(it) }) {
            penalty += 10
        }

        // Check for dictionary words
        val dictionaryWordCount = containsDictionaryWords(password)
        penalty += (dictionaryWordCount * 5.0).coerceAtMost(10.0)

        // Check for common patterns
        if (commonPatterns.any { password.lowercase().contains(it) }) {
            penalty += 5
        }

        // Check for dates
        if (datePatterns.any { password.matches(Regex(it)) }) {
            penalty += 5
        }

        // Check for repeated characters
        var repeatedChars = 0
        var currentChar = ' '
        var currentCount = 0
        password.forEach { char ->
            if (char == currentChar) {
                currentCount++
                if (currentCount > 2) repeatedChars++
            } else {
                currentChar = char
                currentCount = 1
            }
        }
        penalty += (repeatedChars * 2.5).coerceAtMost(5.0)

        // Check for sequential numbers
        if (password.matches(Regex(".*(123|234|345|456|567|678|789|890).*"))) {
            penalty += 5
        }

        // Apply penalty
        ruleBasedScore -= penalty.coerceAtMost(20.0)
        
        // Combine AI and rule-based scores
        return (aiStrength + ruleBasedScore).coerceIn(0.0, 100.0)
    }

    private fun calculateEntropy(password: String): Double {
        var entropy = 0.0
        val charCount = mutableMapOf<Char, Int>()
        
        // Count character frequencies
        password.forEach { char ->
            charCount[char] = (charCount[char] ?: 0) + 1
        }
        
        // Calculate entropy using Shannon's formula
        val length = password.length.toDouble()
        charCount.values.forEach { count ->
            val probability = count / length
            entropy -= probability * kotlin.math.log(probability, 2.0)
        }
        
        return entropy
    }
    
    suspend fun estimateCrackTime(password: String): String {
        if (password.isEmpty()) return "instantly"
        
        // Calculate character set size
        val hasLowercase = password.any { it.isLowerCase() }
        val hasUppercase = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        
        var charsetSize = 0
        if (hasLowercase) charsetSize += 26
        if (hasUppercase) charsetSize += 26
        if (hasDigit) charsetSize += 10
        if (hasSpecial) charsetSize += 32
        
        // Adjust for common patterns and words
        var adjustedCombinations = charsetSize.toDouble().pow(password.length)
        
        // Reduce combinations if common patterns are found
        if (commonWords.any { password.lowercase().contains(it) }) {
            adjustedCombinations *= 0.1 // 90% reduction
        }
        
        // Reduce combinations for dictionary words
        val dictionaryWordCount = containsDictionaryWords(password)
        if (dictionaryWordCount > 0) {
            adjustedCombinations *= 0.2.pow(dictionaryWordCount) // 80% reduction per word
        }
        
        if (commonPatterns.any { password.lowercase().contains(it) }) {
            adjustedCombinations *= 0.2 // 80% reduction
        }
        if (datePatterns.any { password.matches(Regex(it)) }) {
            adjustedCombinations *= 0.3 // 70% reduction
        }
        
        // Assume 1 billion guesses per second (typical for a home computer)
        val guessesPerSecond = 1_000_000_000.0
        val secondsToCrack = adjustedCombinations / guessesPerSecond
        
        return when {
            secondsToCrack < 1 -> "instantly"
            secondsToCrack < 60 -> "${secondsToCrack.toInt()} seconds"
            secondsToCrack < 3600 -> "${(secondsToCrack / 60).toInt()} minutes"
            secondsToCrack < 86400 -> "${(secondsToCrack / 3600).toInt()} hours"
            secondsToCrack < 31536000 -> "${(secondsToCrack / 86400).toInt()} days"
            else -> "${(secondsToCrack / 31536000).toInt()} years"
        }
    }
}