package io.toolbox.passwdmanager.utils

import androidx.annotation.ColorInt
import kotlin.math.log2
import kotlin.math.pow

object PasswordStrength {
    private const val GUESSES_PER_SECOND = 1_000_000_000.0 // 1 billion guesses per second
    private const val MIN_PASSWORD_LENGTH = 8
    private const val MIN_ENTROPY = 20.0 // Minimum entropy floor

    // Time constants matching the JavaScript reference
    private const val MINUTE = 60.0
    private const val HOUR = 60.0 * MINUTE
    private const val DAY = 24.0 * HOUR
    private const val MONTH = 31.0 * DAY
    private const val YEAR = 355.0 * DAY
    private const val CENTURY = 100.0 * YEAR
    private const val BY = 1_000_000.0 * YEAR

    // Common words that reduce password strength
    private val commonWords = setOf(
        "admin", "error", "home", "testing", "computer",
        "password", "qwerty", "123456", "letmein", "welcome",
        "monkey", "dragon", "baseball", "football", "shadow",
        "master", "hello", "freedom", "whatever", "qazwsx",
        "trustno1", "sunshine", "iloveyou", "starwars", "princess",
        "admin123", "admin101", "admin2546", "chemistry", "macbook",
        "air", "m4", "123", "test"
    )

    // Common patterns that reduce password strength
    private val commonPatterns = listOf(
        Regex("qwerty|asdfgh|zxcvbn"),
        Regex("123456|12345|1234"),
        Regex("password|passwd|pass"),
        Regex("admin|root|user"),
        Regex("\\d{4,}"), // 4 or more consecutive digits
        Regex("[a-z]{4,}"), // 4 or more consecutive lowercase letters
        Regex("[A-Z]{4,}"), // 4 or more consecutive uppercase letters
        Regex("(.)\\1{2,}") // 3 or more repeated characters
    )

    data class StrengthResult(
        val score: Int, // 0-4 score
        val crackTime: Double, // Estimated crack time in seconds
        val feedback: List<String>, // List of feedback messages
        @ColorInt val color: Int // Color for UI representation
    )

    fun calculateStrength(password: String): StrengthResult {
        if (password.isEmpty()) {
            return StrengthResult(
                score = 0,
                crackTime = 0.0,
                feedback = listOf("Enter a password"),
                color = 0x00EC2939
            )
        }

        val crackTime = calculateCrackTime(password)
        val score = calculateScore(crackTime)
        val feedback = generateFeedback(password, score)
        val color = getColorForScore(score)

        return StrengthResult(score, crackTime, feedback, color)
    }

    private fun calculateCrackTime(password: String): Double {
        // Handle very short passwords
        if (password.length < 4) {
            return when (password.length) {
                1 -> 1.0
                2 -> 2.0
                3 -> 11.0
                else -> 0.0
            }
        }

        // Calculate character set size based on character types
        var charSetSize = 0
        val hasLowercase = password.any { it.isLowerCase() }
        val hasUppercase = password.any { it.isUpperCase() }
        val hasDigits = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }

        if (hasLowercase) charSetSize += 26
        if (hasUppercase) charSetSize += 26
        if (hasDigits) charSetSize += 10
        if (hasSpecial) charSetSize += 33

        // Calculate base entropy
        var entropy = password.length * log2(charSetSize.toDouble())

        // Apply penalties for common patterns and words
        var patternPenalty = 1.0
        var wordPenalty = 1.0

        // Check for common patterns
        for (pattern in commonPatterns) {
            if (pattern.containsMatchIn(password)) {
                patternPenalty *= 0.8 // Less aggressive pattern penalty
            }
        }

        // Check for common words
        var foundCommonWord = false
        for (word in commonWords) {
            if (password.contains(word, ignoreCase = true)) {
                foundCommonWord = true
                // Less aggressive word penalty, and only apply once
                wordPenalty = 0.7
                break
            }
        }

        // Apply penalties
        entropy *= patternPenalty * wordPenalty

        // Additional penalties for specific cases
        when {
            // All letters penalty
            password.all { it.isLetter() } && password.length <= 8 -> entropy *= 0.8
            // All numbers penalty
            password.all { it.isDigit() } && password.length <= 6 -> entropy *= 0.6
            // Sequential characters penalty
            isSequential(password) -> entropy *= 0.7
        }

        // Ensure minimum entropy for passwords with mixed character types
        if (hasLowercase && hasDigits && password.length >= 6) {
            entropy = maxOf(entropy, MIN_ENTROPY)
        }

        // Calculate crack time based on entropy
        val crackTime = (2.0.pow(entropy) / GUESSES_PER_SECOND)

        // Ensure minimum crack time for passwords with certain characteristics
        return when {
            // Passwords with common words but good length and mixed types
            foundCommonWord && password.length >= 8 && (hasLowercase && hasDigits) -> 
                maxOf(crackTime, 3600.0) // At least 1 hour
            
            // Passwords with good length and mixed types
            password.length >= 8 && (hasLowercase && hasDigits) -> 
                maxOf(crackTime, 1800.0) // At least 30 minutes
            
            // Other passwords
            else -> crackTime
        }
    }

    private fun isSequential(password: String): Boolean {
        if (password.length < 3) return false

        // Check for sequential numbers
        if (password.all { it.isDigit() }) {
            val numbers = password.map { it.digitToInt() }
            var sequential = true
            for (i in 1 until numbers.size) {
                if (numbers[i] != numbers[i - 1] + 1) {
                    sequential = false
                    break
                }
            }
            if (sequential) return true
        }

        // Check for sequential letters
        if (password.all { it.isLetter() }) {
            val letters = password.lowercase()
            var sequential = true
            for (i in 1 until letters.length) {
                if (letters[i].code != letters[i - 1].code + 1) {
                    sequential = false
                    break
                }
            }
            if (sequential) return true
        }

        return false
    }

    private fun calculateScore(crackTime: Double): Int {
        return when {
            crackTime < MINUTE -> 0  // Less than a minute
            crackTime < HOUR -> 1    // Less than an hour
            crackTime < DAY -> 2     // Less than a day
            crackTime < MONTH -> 3   // Less than a month
            crackTime < YEAR -> 4    // Less than a year
            else -> 5                // More than a year
        }
    }

    private fun generateFeedback(password: String, score: Int): List<String> {
        val feedback = mutableListOf<String>()

        if (password.isEmpty()) {
            return listOf("Enter a password")
        }

        if (password.length < MIN_PASSWORD_LENGTH) {
            feedback.add("Password is too short")
        }

        if (score == 0) {
            feedback.add("Password is too weak")
        }

        if (!password.any { !it.isLetterOrDigit() }) {
            feedback.add("Add special characters")
        }

        if (!password.any { it.isUpperCase() }) {
            feedback.add("Add uppercase letters")
        }

        if (!password.any { it.isDigit() }) {
            feedback.add("Add numbers")
        }

        return feedback
    }

    @ColorInt
    private fun getColorForScore(score: Int): Int {
        return when (score) {
            0, 1 -> 0xFFEC2939.toInt() // Red
            2, 3 -> 0xFFEE9E26.toInt() // Orange
            4, 5 -> 0xFF006A52.toInt() // Green
            else -> 0xFFEC2939.toInt() // Red
        }
    }

    fun formatCrackTime(seconds: Double): String {
        val times = listOf(
            MINUTE to "second",
            HOUR to "minute",
            DAY to "hour",
            MONTH to "day",
            YEAR to "month",
            CENTURY to "century",
            BY to "century"
        )

        for (i in times.indices) {
            val (threshold, unit) = times[i]
            if (seconds < threshold) {
                val divisor = if (i > 0) times[i - 1].first else 1.0
                var number = (seconds / divisor).toInt()
                if (number == 0) number = 1 // avoid zeros
                val plural = when (unit) {
                    "century" -> if (number > 1) "centuries" else "century"
                    else -> if (number > 1) "${unit}s" else unit
                }
                return "$number $plural"
            }
        }

        return "10000+ centuries"
    }
}