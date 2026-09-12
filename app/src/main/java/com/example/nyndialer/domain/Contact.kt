package com.example.nyndialer.domain

data class Contact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val email: String? = null,
    val sourceAccount: String = "Google (nayanmridha24@gmail.com)",
    val isGoogleContact: Boolean = true,
    val avatarColorHex: String = "#00E676",
    val isSipUri: Boolean = false
)

data class T9MatchResult(
    val contact: Contact,
    val matchedNameRange: IntRange? = null,
    val matchedNumberRange: IntRange? = null,
    val score: Int = 0
)

object T9Engine {
    private val charToDigitMap = mapOf(
        'a' to '2', 'b' to '2', 'c' to '2',
        'd' to '3', 'e' to '3', 'f' to '3',
        'g' to '4', 'h' to '4', 'i' to '4',
        'j' to '5', 'k' to '5', 'l' to '5',
        'm' to '6', 'n' to '6', 'o' to '6',
        'p' to '7', 'q' to '7', 'r' to '7', 's' to '7',
        't' to '8', 'u' to '8', 'v' to '8',
        'w' to '9', 'x' to '9', 'y' to '9', 'z' to '9'
    )

    fun nameToT9Digits(name: String): String {
        return name.lowercase().map { charToDigitMap[it] ?: it }.joinToString("")
    }

    fun cleanNumber(number: String): String {
        return number.filter { it.isDigit() || it == '+' }
    }

    /**
     * Smart contact search: matches dialed digits against contact names using T9 and against numbers.
     */
    fun search(contacts: List<Contact>, query: String): List<T9MatchResult> {
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) return emptyList()

        val results = mutableListOf<T9MatchResult>()

        for (contact in contacts) {
            // 1. Direct phone number matching
            val cleanedPhone = cleanNumber(contact.phoneNumber)
            val numIdx = cleanedPhone.indexOf(cleanQuery)
            if (numIdx >= 0) {
                results.add(
                    T9MatchResult(
                        contact = contact,
                        matchedNumberRange = numIdx until (numIdx + cleanQuery.length),
                        score = 100 - numIdx // prefix match scores higher
                    )
                )
                continue
            }

            // 2. T9 name matching
            val nameWords = contact.name.split(" ")
            var matched = false
            var bestScore = -1
            var bestRange: IntRange? = null

            // Check full name T9
            val fullT9 = nameToT9Digits(contact.name)
            val fullIdx = fullT9.indexOf(cleanQuery)
            if (fullIdx >= 0) {
                matched = true
                bestScore = 80 - fullIdx
                bestRange = fullIdx until (fullIdx + cleanQuery.length)
            }

            // Check initials / word starts (e.g. "NM" for Nayon Mridha = "66")
            if (!matched && nameWords.size > 1) {
                val initialsT9 = nameWords.map { nameToT9Digits(it.take(1)) }.joinToString("")
                if (initialsT9.startsWith(cleanQuery)) {
                    matched = true
                    bestScore = 90
                }
            }

            // Check individual words in name
            if (!matched) {
                var currentOffset = 0
                for (word in nameWords) {
                    val wordT9 = nameToT9Digits(word)
                    val wIdx = wordT9.indexOf(cleanQuery)
                    if (wIdx >= 0) {
                        matched = true
                        val globalStart = currentOffset + wIdx
                        bestScore = 75 - wIdx
                        bestRange = globalStart until (globalStart + cleanQuery.length)
                        break
                    }
                    currentOffset += word.length + 1
                }
            }

            // Also check text query in contact name directly (e.g. if letters typed)
            if (!matched && contact.name.contains(cleanQuery, ignoreCase = true)) {
                val start = contact.name.indexOf(cleanQuery, ignoreCase = true)
                matched = true
                bestScore = 70 - start
                bestRange = start until (start + cleanQuery.length)
            }

            if (matched) {
                results.add(
                    T9MatchResult(
                        contact = contact,
                        matchedNameRange = bestRange,
                        score = bestScore
                    )
                )
            }
        }

        return results.sortedByDescending { it.score }
    }
}
