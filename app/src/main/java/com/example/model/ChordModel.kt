package com.example.model

data class ChordPosition(
    val stringIndex: Int, // 0-based from low string (left) to high string (right)
    val fret: Int,        // 0 = open, -1 = muted/x, 1..5 = fret number
    val finger: Int = 0   // 0 = none, 1 = index, 2 = middle, 3 = ring, 4 = pinky, -1 = thumb
)

data class ChordItem(
    val id: String,
    val name: String,
    val instrumentType: InstrumentType,
    val difficulty: String, // "Beginner", "Intermediate", "Advanced"
    val category: String,   // "Major", "Minor", "7th"
    val baseFret: Int = 1,
    val positions: List<ChordPosition>, // 6 for guitar, 4 for uke/bass
    val audioFrequencies: List<Double>, // frequencies for strumming
    val tip: String,
    val fullName: String = ""
)

object ChordRepository {

    val GUITAR_CHORDS = listOf(
        // Row 1: E, A, D, C
        ChordItem(
            id = "g_e_major",
            name = "E",
            fullName = "E Major",
            instrumentType = InstrumentType.GUITAR,
            difficulty = "Beginner",
            category = "Major",
            positions = listOf(
                ChordPosition(0, 0),    // 6th open
                ChordPosition(1, 2, 2), // 5th 2nd fret middle
                ChordPosition(2, 2, 3), // 4th 2nd fret ring
                ChordPosition(3, 1, 1), // 3rd 1st fret index
                ChordPosition(4, 0),    // 2nd open
                ChordPosition(5, 0)     // 1st open
            ),
            audioFrequencies = listOf(82.41, 123.47, 164.81, 207.65, 246.94, 329.63),
            tip = "Big powerful chord, great for electric rock riffs."
        ),
        ChordItem(
            id = "g_a_major",
            name = "A",
            fullName = "A Major",
            instrumentType = InstrumentType.GUITAR,
            difficulty = "Beginner",
            category = "Major",
            positions = listOf(
                ChordPosition(0, -1),   // 6th mute
                ChordPosition(1, 0),    // 5th open
                ChordPosition(2, 2, 1), // 4th 2nd fret index
                ChordPosition(3, 2, 2), // 3rd 2nd fret middle
                ChordPosition(4, 2, 3), // 2nd 2nd fret ring
                ChordPosition(5, 0)     // 1st open
            ),
            audioFrequencies = listOf(110.00, 164.81, 220.00, 277.18, 329.63),
            tip = "Tuck fingers 1, 2, 3 close together in the 2nd fret."
        ),
        ChordItem(
            id = "g_d_major",
            name = "D",
            fullName = "D Major",
            instrumentType = InstrumentType.GUITAR,
            difficulty = "Beginner",
            category = "Major",
            positions = listOf(
                ChordPosition(0, -1),   // 6th mute
                ChordPosition(1, -1),   // 5th mute
                ChordPosition(2, 0),    // 4th open
                ChordPosition(3, 2, 1), // 3rd 2nd fret index
                ChordPosition(4, 3, 3), // 2nd 3rd fret ring
                ChordPosition(5, 2, 2)  // 1st 2nd fret middle
            ),
            audioFrequencies = listOf(146.83, 220.00, 293.66, 369.99),
            tip = "Form a small triangle with fingers 1, 2, and 3 on the top 3 strings."
        ),
        ChordItem(
            id = "g_c_major",
            name = "C",
            fullName = "C Major",
            instrumentType = InstrumentType.GUITAR,
            difficulty = "Beginner",
            category = "Major",
            positions = listOf(
                ChordPosition(0, -1),   // 6th mute
                ChordPosition(1, 3, 3), // 5th 3rd fret ring
                ChordPosition(2, 2, 2), // 4th 2nd fret middle
                ChordPosition(3, 0),    // 3rd open
                ChordPosition(4, 1, 1), // 2nd 1st fret index
                ChordPosition(5, 0)     // 1st open
            ),
            audioFrequencies = listOf(130.81, 164.81, 196.00, 261.63, 329.63),
            tip = "Keep your thumb behind the neck and arch your fingers so strings ring clear!"
        ),

        // Row 2: G, Em, Am, Dm
        ChordItem(
            id = "g_g_major",
            name = "G",
            fullName = "G Major",
            instrumentType = InstrumentType.GUITAR,
            difficulty = "Beginner",
            category = "Major",
            positions = listOf(
                ChordPosition(0, 3, 2), // 6th 3rd fret middle
                ChordPosition(1, 2, 1), // 5th 2nd fret index
                ChordPosition(2, 0),    // 4th open
                ChordPosition(3, 0),    // 3rd open
                ChordPosition(4, 0),    // 2nd open
                ChordPosition(5, 3, 3)  // 1st 3rd fret ring
            ),
            audioFrequencies = listOf(98.00, 123.47, 146.83, 196.00, 246.94, 392.00),
            tip = "The staple of 1000s of rock & pop songs! Strum all 6 strings."
        ),
        ChordItem(
            id = "g_e_minor",
            name = "Em",
            fullName = "E Minor",
            instrumentType = InstrumentType.GUITAR,
            difficulty = "Beginner",
            category = "Minor",
            positions = listOf(
                ChordPosition(0, 0),    // 6th open
                ChordPosition(1, 2, 2), // 5th 2nd fret middle
                ChordPosition(2, 2, 3), // 4th 2nd fret ring
                ChordPosition(3, 0),    // 3rd open
                ChordPosition(4, 0),    // 2nd open
                ChordPosition(5, 0)     // 1st open
            ),
            audioFrequencies = listOf(82.41, 123.47, 164.81, 196.00, 246.94, 329.63),
            tip = "Only two fingers needed! Strum with full rock energy."
        ),
        ChordItem(
            id = "g_a_minor",
            name = "Am",
            fullName = "A Minor",
            instrumentType = InstrumentType.GUITAR,
            difficulty = "Beginner",
            category = "Minor",
            positions = listOf(
                ChordPosition(0, -1),   // 6th mute
                ChordPosition(1, 0),    // 5th open
                ChordPosition(2, 2, 2), // 4th 2nd fret middle
                ChordPosition(3, 2, 3), // 3rd 2nd fret ring
                ChordPosition(4, 1, 1), // 2nd 1st fret index
                ChordPosition(5, 0)     // 1st open
            ),
            audioFrequencies = listOf(110.00, 164.81, 220.00, 261.63, 329.63),
            tip = "Same shape as E Major, just moved down one string."
        ),
        ChordItem(
            id = "g_d_minor",
            name = "Dm",
            fullName = "D Minor",
            instrumentType = InstrumentType.GUITAR,
            difficulty = "Beginner",
            category = "Minor",
            positions = listOf(
                ChordPosition(0, -1),   // 6th mute
                ChordPosition(1, -1),   // 5th mute
                ChordPosition(2, 0),    // 4th open
                ChordPosition(3, 2, 2), // 3rd 2nd fret middle
                ChordPosition(4, 3, 3), // 2nd 3rd fret ring
                ChordPosition(5, 1, 1)  // 1st 1st fret index
            ),
            audioFrequencies = listOf(146.83, 220.00, 293.66, 349.23),
            tip = "Melancholic and dramatic chord used in classical and acoustic rock."
        ),

        // Row 3: E7, A7, G7, Cadd9
        ChordItem(
            id = "g_e7",
            name = "E7",
            fullName = "E7",
            instrumentType = InstrumentType.GUITAR,
            difficulty = "Beginner",
            category = "7th",
            positions = listOf(
                ChordPosition(0, 0),    // 6th open
                ChordPosition(1, 2, 2), // 5th 2nd fret middle
                ChordPosition(2, 0),    // 4th open
                ChordPosition(3, 1, 1), // 3rd 1st fret index
                ChordPosition(4, 0),    // 2nd open
                ChordPosition(5, 0)     // 1st open
            ),
            audioFrequencies = listOf(82.41, 123.47, 146.83, 207.65, 246.94, 329.63),
            tip = "Bluesy and bright! Just lift your ring finger from standard E major."
        ),
        ChordItem(
            id = "g_a7",
            name = "A7",
            fullName = "A7",
            instrumentType = InstrumentType.GUITAR,
            difficulty = "Beginner",
            category = "7th",
            positions = listOf(
                ChordPosition(0, -1),   // 6th mute
                ChordPosition(1, 0),    // 5th open
                ChordPosition(2, 2, 1), // 4th 2nd fret index
                ChordPosition(3, 0),    // 3rd open
                ChordPosition(4, 2, 2), // 2nd 2nd fret middle
                ChordPosition(5, 0)     // 1st open
            ),
            audioFrequencies = listOf(110.00, 164.81, 196.00, 277.18, 329.63),
            tip = "Essential 12-bar blues chord with an open G string ringing through the center."
        ),
        ChordItem(
            id = "g_g7",
            name = "G7",
            fullName = "G7",
            instrumentType = InstrumentType.GUITAR,
            difficulty = "Beginner",
            category = "7th",
            positions = listOf(
                ChordPosition(0, 3, 3), // 6th 3rd fret ring
                ChordPosition(1, 2, 2), // 5th 2nd fret middle
                ChordPosition(2, 0),    // 4th open
                ChordPosition(3, 0),    // 3rd open
                ChordPosition(4, 0),    // 2nd open
                ChordPosition(5, 1, 1)  // 1st 1st fret index
            ),
            audioFrequencies = listOf(98.00, 123.47, 146.83, 196.00, 246.94, 349.23),
            tip = "The classic blues turnaround chord that resolves naturally to C Major."
        ),
        ChordItem(
            id = "g_cadd9",
            name = "Cadd9",
            fullName = "Cadd9",
            instrumentType = InstrumentType.GUITAR,
            difficulty = "Beginner",
            category = "Major",
            positions = listOf(
                ChordPosition(0, -1),   // 6th mute
                ChordPosition(1, 3, 2), // 5th 3rd fret middle
                ChordPosition(2, 2, 1), // 4th 2nd fret index
                ChordPosition(3, 0),    // 3rd open
                ChordPosition(4, 3, 3), // 2nd 3rd fret ring
                ChordPosition(5, 3, 4)  // 1st 3rd fret pinky
            ),
            audioFrequencies = listOf(130.81, 164.81, 196.00, 293.66, 392.00),
            tip = "The acoustic anthem chord used by Pink Floyd, Green Day, and Oasis."
        ),

        // Row 4: B7, D7, Am7, F
        ChordItem(
            id = "g_b7",
            name = "B7",
            fullName = "B7",
            instrumentType = InstrumentType.GUITAR,
            difficulty = "Intermediate",
            category = "7th",
            positions = listOf(
                ChordPosition(0, -1),   // 6th mute
                ChordPosition(1, 2, 2), // 5th 2nd fret middle
                ChordPosition(2, 1, 1), // 4th 1st fret index
                ChordPosition(3, 2, 3), // 3rd 2nd fret ring
                ChordPosition(4, 0),    // 2nd open
                ChordPosition(5, 2, 4)  // 1st 2nd fret pinky
            ),
            audioFrequencies = listOf(123.47, 155.56, 220.00, 246.94, 369.99),
            tip = "Key transition chord in blues and folk. Leave the B string open to ring."
        ),
        ChordItem(
            id = "g_d7",
            name = "D7",
            fullName = "D7",
            instrumentType = InstrumentType.GUITAR,
            difficulty = "Beginner",
            category = "7th",
            positions = listOf(
                ChordPosition(0, -1),   // 6th mute
                ChordPosition(1, -1),   // 5th mute
                ChordPosition(2, 0),    // 4th open
                ChordPosition(3, 2, 2), // 3rd 2nd fret middle
                ChordPosition(4, 1, 1), // 2nd 1st fret index
                ChordPosition(5, 2, 3)  // 1st 2nd fret ring
            ),
            audioFrequencies = listOf(146.83, 220.00, 261.63, 369.99),
            tip = "Inverted triangle shape from standard D major. Lead-in chord to G."
        ),
        ChordItem(
            id = "g_am7",
            name = "Am7",
            fullName = "Am7",
            instrumentType = InstrumentType.GUITAR,
            difficulty = "Beginner",
            category = "Minor",
            positions = listOf(
                ChordPosition(0, -1),   // 6th mute
                ChordPosition(1, 0),    // 5th open
                ChordPosition(2, 2, 2), // 4th 2nd fret middle
                ChordPosition(3, 0),    // 3rd open
                ChordPosition(4, 1, 1), // 2nd 1st fret index
                ChordPosition(5, 0)     // 1st open
            ),
            audioFrequencies = listOf(110.00, 164.81, 196.00, 261.63, 329.63),
            tip = "Lush, jazzy and easy! Just 2 fingers: 2nd on D string, 1st on B string."
        ),
        ChordItem(
            id = "g_f_major",
            name = "F",
            fullName = "F Major",
            instrumentType = InstrumentType.GUITAR,
            difficulty = "Beginner",
            category = "Major",
            positions = listOf(
                ChordPosition(0, -1),   // 6th mute
                ChordPosition(1, -1),   // 5th mute
                ChordPosition(2, 3, 3), // 4th 3rd fret ring
                ChordPosition(3, 2, 2), // 3rd 2nd fret middle
                ChordPosition(4, 1, 1), // 2nd 1st fret index
                ChordPosition(5, 1, 1)  // 1st 1st fret index
            ),
            audioFrequencies = listOf(174.61, 220.00, 261.63, 349.23),
            tip = "The easy 4-string mini-barre F chord! Flatten index across the top two strings."
        )
    )

    val UKULELE_CHORDS = listOf(
        ChordItem(
            id = "u_c_major",
            name = "C Major",
            instrumentType = InstrumentType.UKULELE,
            difficulty = "Beginner",
            category = "Major",
            positions = listOf(
                ChordPosition(0, 0),
                ChordPosition(1, 0),
                ChordPosition(2, 0),
                ChordPosition(3, 3, 3) // 1st string 3rd fret ring
            ),
            audioFrequencies = listOf(392.00, 261.63, 329.63, 523.25),
            tip = "The easiest chord on Ukulele! 1 finger on the 3rd fret."
        ),
        ChordItem(
            id = "u_f_major",
            name = "F Major",
            instrumentType = InstrumentType.UKULELE,
            difficulty = "Beginner",
            category = "Major",
            positions = listOf(
                ChordPosition(0, 2, 2), // 4th string 2nd fret
                ChordPosition(1, 0),
                ChordPosition(2, 1, 1), // 2nd string 1st fret
                ChordPosition(3, 0)
            ),
            audioFrequencies = listOf(440.00, 261.63, 349.23, 440.00),
            tip = "Pair with C Major and G Major to play hundreds of songs."
        ),
        ChordItem(
            id = "u_g_major",
            name = "G Major",
            instrumentType = InstrumentType.UKULELE,
            difficulty = "Beginner",
            category = "Major",
            positions = listOf(
                ChordPosition(0, 0),
                ChordPosition(1, 2, 1),
                ChordPosition(2, 3, 3),
                ChordPosition(3, 2, 2)
            ),
            audioFrequencies = listOf(392.00, 293.66, 392.00, 493.88),
            tip = "Looks like a D chord on guitar. Sweet and upbeat!"
        ),
        ChordItem(
            id = "u_a_minor",
            name = "A Minor",
            instrumentType = InstrumentType.UKULELE,
            difficulty = "Beginner",
            category = "Minor",
            positions = listOf(
                ChordPosition(0, 2, 2),
                ChordPosition(1, 0),
                ChordPosition(2, 0),
                ChordPosition(3, 0)
            ),
            audioFrequencies = listOf(440.00, 261.63, 329.63, 440.00),
            tip = "Only 1 finger on top string 2nd fret. Moody & emotional."
        )
    )

    val BASS_PATTERNS = listOf(
        ChordItem(
            id = "b_e_root",
            name = "E Root",
            instrumentType = InstrumentType.BASS,
            difficulty = "Beginner",
            category = "Groove",
            positions = listOf(
                ChordPosition(0, 0),
                ChordPosition(1, 2, 1),
                ChordPosition(2, 2, 2),
                ChordPosition(3, 1, 1)
            ),
            audioFrequencies = listOf(41.20, 82.41, 110.00),
            tip = "Solid low foundation. Anchor with your thumb on the pickup."
        ),
        ChordItem(
            id = "b_a_root",
            name = "A Pentatonic",
            instrumentType = InstrumentType.BASS,
            difficulty = "Beginner",
            category = "Groove",
            positions = listOf(
                ChordPosition(0, 5, 1),
                ChordPosition(1, 3, 1),
                ChordPosition(2, 5, 3),
                ChordPosition(3, 0)
            ),
            audioFrequencies = listOf(55.00, 82.41, 110.00),
            tip = "The golden rock bass box pattern used in legendary riffs."
        )
    )

    fun getChordsForInstrument(type: InstrumentType): List<ChordItem> {
        return when (type) {
            InstrumentType.GUITAR -> GUITAR_CHORDS
            InstrumentType.UKULELE, InstrumentType.BANJO -> UKULELE_CHORDS
            InstrumentType.BASS -> BASS_PATTERNS
        }
    }
}
