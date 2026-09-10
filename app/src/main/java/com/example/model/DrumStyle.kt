package com.example.model

enum class MetronomeSoundMode(val displayName: String) {
    CLICK("Click"),
    DRUMS("Drums")
}

enum class DrumStyle(val displayName: String) {
    ROCK("Rock"),
    POP("Pop"),
    FOLK("Folk"),
    LATIN("Latin"),
    SHUFFLE("Shuffle")
}

enum class DrumHit {
    KICK,
    SNARE,
    CLOSED_HIHAT,
    OPEN_HIHAT,
    RIM_CLICK
}

object DrumPatternEngine {

    /**
     * Returns the one-shot drum hits to fire on a given beat for the chosen style and time signature.
     * Beat is 1-indexed (1..timeSignatureBeats).
     */
    fun getHitsForBeat(
        style: DrumStyle,
        timeSignatureBeats: Int,
        beat: Int
    ): List<DrumHit> {
        return when (style) {
            DrumStyle.ROCK -> getRockPattern(timeSignatureBeats, beat)
            DrumStyle.POP -> getPopPattern(timeSignatureBeats, beat)
            DrumStyle.FOLK -> getFolkPattern(timeSignatureBeats, beat)
            DrumStyle.LATIN -> getLatinPattern(timeSignatureBeats, beat)
            DrumStyle.SHUFFLE -> getShufflePattern(timeSignatureBeats, beat)
        }
    }

    private fun getRockPattern(timeSignatureBeats: Int, beat: Int): List<DrumHit> {
        return when (timeSignatureBeats) {
            3 -> when (beat) {
                1 -> listOf(DrumHit.KICK, DrumHit.CLOSED_HIHAT)
                2 -> listOf(DrumHit.SNARE, DrumHit.CLOSED_HIHAT)
                3 -> listOf(DrumHit.SNARE, DrumHit.CLOSED_HIHAT)
                else -> listOf(DrumHit.CLOSED_HIHAT)
            }
            2 -> when (beat) {
                1 -> listOf(DrumHit.KICK, DrumHit.CLOSED_HIHAT)
                2 -> listOf(DrumHit.SNARE, DrumHit.CLOSED_HIHAT)
                else -> listOf(DrumHit.CLOSED_HIHAT)
            }
            6 -> when (beat) {
                1 -> listOf(DrumHit.KICK, DrumHit.CLOSED_HIHAT)
                4 -> listOf(DrumHit.SNARE, DrumHit.CLOSED_HIHAT)
                else -> listOf(DrumHit.CLOSED_HIHAT)
            }
            else -> { // Default 4/4
                when (beat) {
                    1, 3 -> listOf(DrumHit.KICK, DrumHit.CLOSED_HIHAT)
                    2, 4 -> listOf(DrumHit.SNARE, DrumHit.CLOSED_HIHAT)
                    else -> listOf(DrumHit.CLOSED_HIHAT)
                }
            }
        }
    }

    private fun getPopPattern(timeSignatureBeats: Int, beat: Int): List<DrumHit> {
        return when (timeSignatureBeats) {
            3 -> when (beat) {
                1 -> listOf(DrumHit.KICK, DrumHit.CLOSED_HIHAT)
                2 -> listOf(DrumHit.SNARE, DrumHit.CLOSED_HIHAT)
                3 -> listOf(DrumHit.OPEN_HIHAT)
                else -> listOf(DrumHit.CLOSED_HIHAT)
            }
            2 -> when (beat) {
                1 -> listOf(DrumHit.KICK, DrumHit.CLOSED_HIHAT)
                2 -> listOf(DrumHit.SNARE, DrumHit.OPEN_HIHAT)
                else -> listOf(DrumHit.CLOSED_HIHAT)
            }
            6 -> when (beat) {
                1 -> listOf(DrumHit.KICK, DrumHit.CLOSED_HIHAT)
                3 -> listOf(DrumHit.KICK)
                4 -> listOf(DrumHit.SNARE, DrumHit.CLOSED_HIHAT)
                else -> listOf(DrumHit.CLOSED_HIHAT)
            }
            else -> { // 4/4
                when (beat) {
                    1 -> listOf(DrumHit.KICK, DrumHit.CLOSED_HIHAT)
                    2 -> listOf(DrumHit.SNARE, DrumHit.CLOSED_HIHAT)
                    3 -> listOf(DrumHit.KICK, DrumHit.OPEN_HIHAT)
                    4 -> listOf(DrumHit.SNARE, DrumHit.CLOSED_HIHAT)
                    else -> listOf(DrumHit.CLOSED_HIHAT)
                }
            }
        }
    }

    private fun getFolkPattern(timeSignatureBeats: Int, beat: Int): List<DrumHit> {
        return when (timeSignatureBeats) {
            3 -> when (beat) {
                1 -> listOf(DrumHit.KICK, DrumHit.RIM_CLICK)
                2, 3 -> listOf(DrumHit.RIM_CLICK)
                else -> listOf(DrumHit.RIM_CLICK)
            }
            2 -> when (beat) {
                1 -> listOf(DrumHit.KICK, DrumHit.RIM_CLICK)
                2 -> listOf(DrumHit.RIM_CLICK)
                else -> listOf(DrumHit.RIM_CLICK)
            }
            6 -> when (beat) {
                1 -> listOf(DrumHit.KICK, DrumHit.RIM_CLICK)
                4 -> listOf(DrumHit.RIM_CLICK)
                else -> listOf(DrumHit.CLOSED_HIHAT)
            }
            else -> { // 4/4
                when (beat) {
                    1 -> listOf(DrumHit.KICK, DrumHit.RIM_CLICK)
                    2 -> listOf(DrumHit.RIM_CLICK)
                    3 -> listOf(DrumHit.KICK, DrumHit.RIM_CLICK)
                    4 -> listOf(DrumHit.RIM_CLICK)
                    else -> listOf(DrumHit.RIM_CLICK)
                }
            }
        }
    }

    private fun getLatinPattern(timeSignatureBeats: Int, beat: Int): List<DrumHit> {
        return when (timeSignatureBeats) {
            3 -> when (beat) {
                1 -> listOf(DrumHit.KICK, DrumHit.CLOSED_HIHAT)
                2 -> listOf(DrumHit.RIM_CLICK)
                3 -> listOf(DrumHit.RIM_CLICK, DrumHit.CLOSED_HIHAT)
                else -> listOf(DrumHit.CLOSED_HIHAT)
            }
            2 -> when (beat) {
                1 -> listOf(DrumHit.KICK, DrumHit.CLOSED_HIHAT)
                2 -> listOf(DrumHit.RIM_CLICK)
                else -> listOf(DrumHit.CLOSED_HIHAT)
            }
            6 -> when (beat) {
                1 -> listOf(DrumHit.KICK, DrumHit.CLOSED_HIHAT)
                3 -> listOf(DrumHit.RIM_CLICK)
                4 -> listOf(DrumHit.KICK)
                6 -> listOf(DrumHit.RIM_CLICK)
                else -> listOf(DrumHit.CLOSED_HIHAT)
            }
            else -> { // 4/4
                when (beat) {
                    1 -> listOf(DrumHit.KICK, DrumHit.CLOSED_HIHAT)
                    2 -> listOf(DrumHit.RIM_CLICK)
                    3 -> listOf(DrumHit.KICK, DrumHit.CLOSED_HIHAT)
                    4 -> listOf(DrumHit.RIM_CLICK, DrumHit.OPEN_HIHAT)
                    else -> listOf(DrumHit.CLOSED_HIHAT)
                }
            }
        }
    }

    private fun getShufflePattern(timeSignatureBeats: Int, beat: Int): List<DrumHit> {
        return when (timeSignatureBeats) {
            3 -> when (beat) {
                1 -> listOf(DrumHit.KICK, DrumHit.CLOSED_HIHAT)
                2 -> listOf(DrumHit.SNARE, DrumHit.CLOSED_HIHAT)
                3 -> listOf(DrumHit.OPEN_HIHAT)
                else -> listOf(DrumHit.CLOSED_HIHAT)
            }
            2 -> when (beat) {
                1 -> listOf(DrumHit.KICK, DrumHit.CLOSED_HIHAT)
                2 -> listOf(DrumHit.SNARE, DrumHit.OPEN_HIHAT)
                else -> listOf(DrumHit.CLOSED_HIHAT)
            }
            6 -> when (beat) {
                1 -> listOf(DrumHit.KICK, DrumHit.CLOSED_HIHAT)
                4 -> listOf(DrumHit.SNARE, DrumHit.CLOSED_HIHAT)
                else -> listOf(DrumHit.CLOSED_HIHAT)
            }
            else -> { // 4/4
                when (beat) {
                    1, 3 -> listOf(DrumHit.KICK, DrumHit.CLOSED_HIHAT)
                    2 -> listOf(DrumHit.SNARE, DrumHit.CLOSED_HIHAT)
                    4 -> listOf(DrumHit.SNARE, DrumHit.OPEN_HIHAT)
                    else -> listOf(DrumHit.CLOSED_HIHAT)
                }
            }
        }
    }
}
