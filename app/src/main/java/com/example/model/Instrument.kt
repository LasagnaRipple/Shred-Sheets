package com.example.model

/**
 * Supported instruments for Shred Sheets Tuner
 */
enum class InstrumentType(val id: String, val displayName: String, val iconName: String) {
    GUITAR("guitar", "Guitar", "🎸"),
    BASS("bass", "Bass", "🎸"),
    UKULELE("ukulele", "Ukulele", "🌴"),
    BANJO("banjo", "Banjo", "🪕")
}

data class InstrumentString(
    val stringNumber: Int,    // 1 to N (1 = thinnest/highest pitch)
    val noteName: String,     // e.g. "E4", "B3", "G3", "D3", "A2", "E2"
    val noteLetter: String,   // e.g. "E", "B", "G", "D", "A", "E"
    val targetFrequency: Double, // Hz
    val octave: Int,
    val defaultWord: String   // Default anagram word
)

data class TuningMode(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val strings: List<InstrumentString>,
    val anagrams: List<String> = emptyList()
) {
    val shortName: String get() = name.substringBefore("(").trim()
    val notesDisplay: String get() = strings.joinToString(" ") { it.noteLetter }
    val defaultSentence: String get() = anagrams.firstOrNull() ?: strings.joinToString(" ") { it.defaultWord }
}

data class InstrumentConfig(
    val type: InstrumentType,
    val tunings: List<TuningMode>,
    val defaultTuningId: String,
    val defaultAnagrams: List<String>
)

object InstrumentRepository {

    val GUITAR_STANDARD = TuningMode(
        id = "guitar_standard",
        name = "Standard (E A D G B E)",
        category = "Standard",
        description = "The most popular tuning for rock, pop, and blues.",
        strings = listOf(
            InstrumentString(6, "E2", "E", 82.41, 2, "Every"),
            InstrumentString(5, "A2", "A", 110.00, 2, "Angry"),
            InstrumentString(4, "D3", "D", 146.83, 3, "Dad"),
            InstrumentString(3, "G3", "G", 196.00, 3, "Gets"),
            InstrumentString(2, "B3", "B", 246.94, 3, "Bad"),
            InstrumentString(1, "E4", "E", 329.63, 4, "Eggs")
        ),
        anagrams = listOf(
            "Eddie Ate Dynamite Good Bye Eddie",
            "Every Angry Dad Gets Bad Eggs",
            "Elephants And Donkeys Grow Big Ears",
            "Eat All Day Get Big Energy",
            "Every Alien Dreams Giant Blue Eyes",
            "Elvis Always Did Get By Easy",
            "Even Astronauts Dance Groovy Beat Energy",
            "Every Animal Deserves Great Big Enclosures",
            "Eat Apples Daily Grow Big Eagles",
            "Electric Ants Dance Groovy Beat Everyday",
            "Every Awesome Drummer Grooves Best Early",
            "Excited Astronauts Dodge Giant Bouncing Eggs",
            "Eagles And Ducks Glide By Easily"
        )
    )

    val GUITAR_DROP_D = TuningMode(
        id = "guitar_drop_d",
        name = "Drop D (D A D G B E)",
        category = "Rock / Metal",
        description = "Lower 6th string for heavy rock and metal power riffs.",
        strings = listOf(
            InstrumentString(6, "D2", "D", 73.42, 2, "Drop"),
            InstrumentString(5, "A2", "A", 110.00, 2, "And"),
            InstrumentString(4, "D3", "D", 146.83, 3, "Dance"),
            InstrumentString(3, "G3", "G", 196.00, 3, "Groovy"),
            InstrumentString(2, "B3", "B", 246.94, 3, "Big"),
            InstrumentString(1, "E4", "E", 329.63, 4, "Energy")
        ),
        anagrams = listOf(
            "Drop And Dance Groovy Big Energy",
            "Daring Astronauts Dodge Giant Bouncing Eggs",
            "Dogs And Ducks Give Big Encouragement",
            "Dinosaurs Always Dance Groovy Beats Everywhere",
            "Dude Ate Delicious Giant Blueberry Eggos",
            "Daring Aliens Do Great Backflips Easily",
            "Dancing Alligators Do Gymnastics Before Eating",
            "Dragons Always Defend Giant Boulder Empires",
            "Do Awesome Deeds Get Big Energy",
            "Double Apples Delicious Gooey Berry Eating",
            "Dolphins And Ducks Glide By Easily",
            "Daring Animals Dance Groovy Beats Easily",
            "Donkeys And Dogs Get Big Ears"
        )
    )

    val GUITAR_DADGAD = TuningMode(
        id = "guitar_dadgad",
        name = "DADGAD (D A D G A D)",
        category = "Celtic / Acoustic",
        description = "Atmospheric open sound popular in acoustic & folk music.",
        strings = listOf(
            InstrumentString(6, "D2", "D", 73.42, 2, "Dark"),
            InstrumentString(5, "A2", "A", 110.00, 2, "Aces"),
            InstrumentString(4, "D3", "D", 146.83, 3, "Drive"),
            InstrumentString(3, "G3", "G", 196.00, 3, "Great"),
            InstrumentString(2, "A3", "A", 220.00, 3, "Acoustic"),
            InstrumentString(1, "D4", "D", 293.66, 4, "Drums")
        ),
        anagrams = listOf(
            "Dark Aces Drive Great Acoustic Drums",
            "Daring Astronauts Dodge Giant Asteroid Debris",
            "Dogs And Ducks Go Around Dancing",
            "Dinosaurs Always Dance Groovy Awesome Dances",
            "Dudes Always Do Good And Dance",
            "Do All Dogs Get Apples Daily",
            "Dragons And Dolphins Glide Across Dunes",
            "Daring Athletes Dash Gracefully Across Dunes"
        )
    )

    val GUITAR_OPEN_D = TuningMode(
        id = "guitar_open_d",
        name = "Open D (D A D F# A D)",
        category = "Open Tunings",
        description = "Strum all open strings to play a major D chord.",
        strings = listOf(
            InstrumentString(6, "D2", "D", 73.42, 2, "Down"),
            InstrumentString(5, "A2", "A", 110.00, 2, "At"),
            InstrumentString(4, "D3", "D", 146.83, 3, "Dawn"),
            InstrumentString(3, "F#3", "F#", 185.00, 3, "Fast"),
            InstrumentString(2, "A3", "A", 220.00, 3, "Air"),
            InstrumentString(1, "D4", "D", 293.66, 4, "Drop")
        ),
        anagrams = listOf(
            "Down At Dawn Fast Air Drops",
            "Dinosaurs Always Dance Fast And Dynamically",
            "Dogs And Ducks Find Awesome Donuts",
            "Daring Astronauts Discover Far Away Dimensions",
            "Dolphins Always Do Flips And Dives",
            "Dragons Always Defend Fierce Ancient Dwellings",
            "Dude Always Does Fun Awesome Deeds"
        )
    )

    val GUITAR_HALF_STEP = TuningMode(
        id = "guitar_half_step",
        name = "Half Step Down (Eb Ab Db Gb Bb Eb)",
        category = "Rock Classics",
        description = "Jimi Hendrix, Guns N' Roses, and Nirvana tuning.",
        strings = listOf(
            InstrumentString(6, "Eb2", "Eb", 77.78, 2, "Electric"),
            InstrumentString(5, "Ab2", "Ab", 103.83, 2, "Awesome"),
            InstrumentString(4, "Db3", "Db", 138.59, 3, "Daring"),
            InstrumentString(3, "Gb3", "Gb", 185.00, 3, "Giant"),
            InstrumentString(2, "Bb3", "Bb", 233.08, 3, "Bold"),
            InstrumentString(1, "Eb4", "Eb", 311.13, 4, "Echo")
        ),
        anagrams = listOf(
            "Electric Awesome Daring Giant Bold Echo",
            "Every Alien Dreams Giant Bright Earth",
            "Energetic Astronauts Discover Great Big Empires",
            "Epic Artists Draw Giant Bright Elephants",
            "Extra Awesome Dudes Groove Best Ever",
            "Every Awesome Drummer Grooves Best Early"
        )
    )

    val GUITAR_A_STANDARD = TuningMode(
        id = "guitar_a_standard",
        name = "A Standard (A D G C E A)",
        category = "Baritone / Low",
        description = "Baritone / down-tuned 6-string guitar tuning tuned down to A.",
        strings = listOf(
            InstrumentString(6, "A1", "A", 55.00, 1, "All"),
            InstrumentString(5, "D2", "D", 73.42, 2, "Dogs"),
            InstrumentString(4, "G2", "G", 98.00, 2, "Get"),
            InstrumentString(3, "C3", "C", 130.81, 3, "Cool"),
            InstrumentString(2, "E3", "E", 164.81, 3, "Extra"),
            InstrumentString(1, "A3", "A", 220.00, 3, "Apples")
        ),
        anagrams = listOf(
            "All Dogs Get Cool Extra Apples",
            "Astronauts Discover Giant Cosmic Energy Always",
            "Awesome Dinosaurs Groove Clean Electric Anthems",
            "Always Do Good Cause Everyone Appreciates",
            "Apples Ducks Geese Cows Eat Alfalfa",
            "All Daring Gymnasts Can Easily Acrobat",
            "Animals Dance Groovy Circles Every Afternoon",
            "Aliens Do Giant Cosmic Experiments Always",
            "Awesome Drummer Grooves Catch Every Audience",
            "Artistic Dads Give Cool Energy Always"
        )
    )

    val BASS_STANDARD = TuningMode(
        id = "bass_standard",
        name = "Standard (E A D G)",
        category = "Standard",
        description = "Standard 4-string electric & acoustic bass tuning.",
        strings = listOf(
            InstrumentString(4, "E1", "E", 41.20, 1, "Every"),
            InstrumentString(3, "A1", "A", 55.00, 1, "Angry"),
            InstrumentString(2, "D2", "D", 73.42, 2, "Dad"),
            InstrumentString(1, "G2", "G", 98.00, 2, "Grows")
        ),
        anagrams = listOf(
            "Every Angry Dad Grows",
            "Elephants And Donkeys Grow",
            "Eat All Day Good",
            "Electric Animals Dance Great",
            "Even Aliens Do Gymnastics",
            "Earth Always Dreams Green",
            "Energetic Alligators Dive Gracefully",
            "Epic Astronauts Dodge Gravity",
            "Eat Apples Daily Guys"
        )
    )

    val BASS_DROP_D = TuningMode(
        id = "bass_drop_d",
        name = "Drop D (D A D G)",
        category = "Rock / Metal",
        description = "Deep low end for heavy bass grooves.",
        strings = listOf(
            InstrumentString(4, "D1", "D", 36.71, 1, "Deep"),
            InstrumentString(3, "A1", "A", 55.00, 1, "Awesome"),
            InstrumentString(2, "D2", "D", 73.42, 2, "Dynamic"),
            InstrumentString(1, "G2", "G", 98.00, 2, "Groove")
        ),
        anagrams = listOf(
            "Deep Awesome Dynamic Groove",
            "Dogs And Ducks Groove",
            "Dinosaurs Always Dance Great",
            "Daring Astronauts Dodge Gravity",
            "Do Awesome Deeds Guys",
            "Dragons Always Destroy Goblins",
            "Dolphins And Ducks Giggle",
            "Dude Ate Delicious Grapes"
        )
    )

    val UKULELE_STANDARD = TuningMode(
        id = "ukulele_standard",
        name = "Standard High-G (G C E A)",
        category = "Standard",
        description = "Standard soprano, concert, and tenor ukulele tuning.",
        strings = listOf(
            InstrumentString(4, "G4", "G", 392.00, 4, "Goats"),
            InstrumentString(3, "C4", "C", 261.63, 4, "Can"),
            InstrumentString(2, "E4", "E", 329.63, 4, "Eat"),
            InstrumentString(1, "A4", "A", 440.00, 4, "Anything")
        ),
        anagrams = listOf(
            "Goats Can Eat Anything",
            "Good Chefs Eat Always",
            "Giant Cats Eat Apples",
            "Green Cows Enjoy Alfalfa",
            "Great Children Enjoy Adventure",
            "Gentle Clouds Embrace Afternoon",
            "Goldfish Can Easily Acrobat",
            "Gigantic Chimps Eat Avocados",
            "Groovy Campers Enjoy Astronomy",
            "Guitars Can Emit Audio"
        )
    )

    val UKULELE_D_TUNING = TuningMode(
        id = "ukulele_d",
        name = "D-Tuning (A D F# B)",
        category = "Traditional",
        description = "Brighter English & Hawaiian traditional tuning.",
        strings = listOf(
            InstrumentString(4, "A4", "A", 440.00, 4, "Always"),
            InstrumentString(3, "D4", "D", 293.66, 4, "Do"),
            InstrumentString(2, "F#4", "F#", 369.99, 4, "Fun"),
            InstrumentString(1, "B4", "B", 493.88, 4, "Beats")
        ),
        anagrams = listOf(
            "Always Do Fun Beats",
            "All Dogs Find Bones",
            "Awesome Dolphins Flip Beautifully",
            "Astronauts Discover Far Boundaries",
            "Ants Dance Fast Bananas",
            "Apples Do Feel Berry",
            "Always Dream Far Beyond"
        )
    )

    val BANJO_STANDARD = TuningMode(
        id = "banjo_standard",
        name = "Open G (g D G B D)",
        category = "Bluegrass",
        description = "Standard 5-string banjo bluegrass tuning.",
        strings = listOf(
            InstrumentString(5, "G4", "g", 392.00, 4, "Green"),
            InstrumentString(4, "D3", "D", 146.83, 3, "Ducks"),
            InstrumentString(3, "G3", "G", 196.00, 3, "Go"),
            InstrumentString(2, "B3", "B", 246.94, 3, "Briskly"),
            InstrumentString(1, "D4", "D", 293.66, 4, "Dancing")
        ),
        anagrams = listOf(
            "Green Ducks Go Briskly Dancing",
            "Great Dads Get Big Donuts",
            "Good Dogs Grow Big Daily",
            "Giant Dinosaurs Go Bounce Down",
            "Grandma Does Gardening Beside Daisies",
            "Green Dragons Glide Beyond Dunes",
            "Golden Dolphins Glisten By Daylight"
        )
    )

    val INSTRUMENTS = listOf(
        InstrumentConfig(
            type = InstrumentType.GUITAR,
            tunings = listOf(GUITAR_STANDARD, GUITAR_DROP_D, GUITAR_DADGAD, GUITAR_OPEN_D, GUITAR_HALF_STEP, GUITAR_A_STANDARD),
            defaultTuningId = "guitar_standard",
            defaultAnagrams = GUITAR_STANDARD.anagrams
        ),
        InstrumentConfig(
            type = InstrumentType.BASS,
            tunings = listOf(BASS_STANDARD, BASS_DROP_D),
            defaultTuningId = "bass_standard",
            defaultAnagrams = BASS_STANDARD.anagrams
        ),
        InstrumentConfig(
            type = InstrumentType.UKULELE,
            tunings = listOf(UKULELE_STANDARD, UKULELE_D_TUNING),
            defaultTuningId = "ukulele_standard",
            defaultAnagrams = UKULELE_STANDARD.anagrams
        ),
        InstrumentConfig(
            type = InstrumentType.BANJO,
            tunings = listOf(BANJO_STANDARD),
            defaultTuningId = "banjo_standard",
            defaultAnagrams = BANJO_STANDARD.anagrams
        )
    )

    fun getConfig(type: InstrumentType): InstrumentConfig {
        return INSTRUMENTS.firstOrNull { it.type == type } ?: INSTRUMENTS.first()
    }
}
