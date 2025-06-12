package io.toolbox.passwdmanager.utils

import kotlin.random.Random

object PasswordUtils {
    private const val LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
    private const val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val NUMBERS = "0123456789"
    private const val SPECIAL = "!@#$%^&*()_+-=[]{}|;:,.<>?"
    private const val ALL_CHARS = LOWERCASE + UPPERCASE + NUMBERS + SPECIAL

    // Common word patterns for memorable passwords
    private val ADJECTIVES = listOf(
        "Happy", "Brave", "Swift", "Clever", "Bright", "Calm", "Eager", "Fierce",
        "Gentle", "Jolly", "Kind", "Lively", "Merry", "Noble", "Proud", "Quick"
    )
    private val NOUNS = listOf(
        "Tiger", "Eagle", "Dolphin", "Phoenix", "Dragon", "Lion", "Wolf", "Bear",
        "Hawk", "Shark", "Falcon", "Puma", "Lynx", "Fox", "Hawk", "Stag"
    )
    private val SPECIAL_CHARS = listOf("!", "@", "#", "$", "%", "&", "*", "+", "=")
    private val NUMBER_PATTERNS = listOf("123", "456", "789", "2023", "2024", "99", "88", "77")

    fun generateStrongPassword() = buildMemorablePassword()

    fun improve(password: String): String {
        if (password.isEmpty()) return generateStrongPassword()

        val random = Random.Default
        val improved = StringBuilder(password)

        // Remove spaces if any
        if (improved.contains(" ")) {
            improved.replace(" ".toRegex(), "")
        }

        // Add missing character types in a way that preserves the original password
        if (!password.any { it.isUpperCase() }) {
            // Add uppercase at a random position
            val pos = random.nextInt(improved.length + 1)
            improved.insert(pos, UPPERCASE[random.nextInt(UPPERCASE.length)])
        }

        if (!password.any { it.isDigit() }) {
            // Add a number at a random position
            val pos = random.nextInt(improved.length + 1)
            improved.insert(pos, NUMBERS[random.nextInt(NUMBERS.length)])
        }

        if (!password.any { it in SPECIAL }) {
            // Add a special character at a random position
            val pos = random.nextInt(improved.length + 1)
            improved.insert(pos, SPECIAL_CHARS[random.nextInt(SPECIAL_CHARS.size)])
        }

        // Ensure minimum length of 8 characters
        while (improved.length < 8) {
            val pos = random.nextInt(improved.length + 1)
            improved.insert(pos, ALL_CHARS[random.nextInt(ALL_CHARS.length)])
        }

        // If the password is still too weak, add a few more characters
        if (improved.length < 12) {
            repeat(2) {
                val pos = random.nextInt(improved.length + 1)
                improved.insert(pos, ALL_CHARS[random.nextInt(ALL_CHARS.length)])
            }
        }

        // If the password contains a common word, try to make it less obvious
        for (word in listOf("test", "pass", "admin", "123", "qwerty")) {
            if (improved.toString().contains(word, ignoreCase = true)) {
                // Replace some characters in the common word with similar looking characters
                val wordStart = improved.toString().indexOf(word, ignoreCase = true)
                if (wordStart >= 0) {
                    when (word.lowercase()) {
                        "test" -> improved[wordStart + 1] = '3' // e -> 3
                        "pass" -> improved[wordStart + 1] = '@' // a -> @
                        "admin" -> improved[wordStart + 2] = '1' // i -> 1
                        "123" -> improved[wordStart] = 'I' // 1 -> I
                        "qwerty" -> improved[wordStart + 2] = '3' // e -> 3
                    }
                }
            }
        }

        return improved.toString()
    }

    private fun buildMemorablePassword(): String {
        val random = Random.Default
        val pattern = random.nextInt(3) // Choose one of three patterns

        return when (pattern) {
            0 -> {
                // Pattern: Adjective + Noun + Number + Special
                val adj = ADJECTIVES[random.nextInt(ADJECTIVES.size)]
                val noun = NOUNS[random.nextInt(NOUNS.size)]
                val number = NUMBER_PATTERNS[random.nextInt(NUMBER_PATTERNS.size)]
                val special = SPECIAL_CHARS[random.nextInt(SPECIAL_CHARS.size)]
                "$adj$noun$number$special"
            }
            1 -> {
                // Pattern: Noun + Special + Adjective + Number
                val noun = NOUNS[random.nextInt(NOUNS.size)]
                val special = SPECIAL_CHARS[random.nextInt(SPECIAL_CHARS.size)]
                val adj = ADJECTIVES[random.nextInt(ADJECTIVES.size)]
                val number = NUMBER_PATTERNS[random.nextInt(NUMBER_PATTERNS.size)]
                "$noun$special$adj$number"
            }
            else -> {
                // Pattern: Number + Adjective + Special + Noun
                val number = NUMBER_PATTERNS[random.nextInt(NUMBER_PATTERNS.size)]
                val adj = ADJECTIVES[random.nextInt(ADJECTIVES.size)]
                val special = SPECIAL_CHARS[random.nextInt(SPECIAL_CHARS.size)]
                val noun = NOUNS[random.nextInt(NOUNS.size)]
                "$number$adj$special$noun"
            }
        }
    }
}