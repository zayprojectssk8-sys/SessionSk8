package com.zaysk8.core.model


enum class TypeStanceTrick(
    var typeStanceName: String
) {
    NORMAL("Normal"),
    FAKIE("Fakie"),
    SWITCH("Switch"),
    NOLLIE("Nollie")
}

enum class TypeTrick(
    var typeTrickName: String,
    val trickList: List<TFMTrick>
) {
    FLOOR(
        typeTrickName = "Floor",
        trickList = arrayListOf(
            TFMTrick("Ollie", "Ollie", 1.0f, "Floor"),
            TFMTrick("Ollie North", "Ollie North", 1.5f, "Floor"),
            TFMTrick("Ollie South", "Ollie South", 1.5f, "Floor"),
            TFMTrick("Ollie Sex Change", "Ollie Sex Change", 2.0f, "Floor"),

            /** BS Shove-it **/
            TFMTrick("180 BS Shove-it", "BS Shove-it", 2.0f, "Floor"),
            TFMTrick("180 BS Shove-it Sex Change", "BS Shove-it Sex Change", 3.0f, "Floor"),
            TFMTrick("360 BS Shove-it", "BS 360 Shove-it", 3.0f, "Floor"),
            TFMTrick("360 BS Shove-it Sex Change", "BS 360 Shove-it Sex Change", 4.0f, "Floor"),
            TFMTrick("360 BS Shove-it Bs 180 Body Varial", "BS Bigspin", 3.0f, "Floor"),
            TFMTrick("540 BS Shove-it", "BS 540 Shove-it", 4.0f, "Floor"),
            TFMTrick("540 BS Shove-it Sex Change", "BS 540 Shove-it Sex Change", 5.0f, "Floor"),
            TFMTrick("540 BS Shove-it Bs 180 Body Varial", "BS Bigger Spin", 3.0f, "Floor"),
            TFMTrick("540 BS Shove-it Bs 360 Body Varial", "BS Gazzele Spin", 3.0f, "Floor"),

            TFMTrick("180 BS Shove-it Kickflip", "Varial Flip", 3.0f, "Floor"),
            TFMTrick(
                "180 BS Shove-it Kickflip Sex Change",
                "Varial Flip Sex Change",
                4.0f,
                "Floor"
            ),
            TFMTrick("180 BS Shove-it Double Kickflip", "Nightmare Flip", 4.0f, "Floor"),
            TFMTrick(
                "180 BS Shove-it Double Kickflip Sex Change",
                "Nightmare Flip Sex Change",
                5.0f
            ),
            TFMTrick("360 BS Shove-it Kickflip", "Tre Flip", 4.0f, "Floor"),
            TFMTrick("360 BS Shove-it Kickflip Sex Change", "Tre Flip Sex Change", 5.0f, "Floor"),
            TFMTrick(
                "360 BS Shove-it Bs 180 Body Varial Kickflip",
                "BS Bigspin Flip",
                3.0f,
                "Floor"
            ),
            TFMTrick("540 BS Shove-it Kickflip", "540 Flip", 5.0f, "Floor"),
            TFMTrick("540 BS Shove-it Kickflip Sex Change", "540 Flip Sex Change", 6.0f, "Floor"),
            TFMTrick(
                "540 BS Shove-it Bs 180 Body Varial Kickflip",
                "BS Bigger Spin Flip",
                3.0f,
                "Floor"
            ),
            TFMTrick(
                "540 BS Shove-it Bs 360 Body Varial Kickflip",
                "BS Gazzele Spin Flip",
                3.0f,
                "Floor"
            ),

            TFMTrick("180 BS Shove-it Heelflip", "Inward Heelflip", 3.0f, "Floor"),
            TFMTrick(
                "180 BS Shove-it Heelflip Sex Change",
                "Inward Heelflip Sex Change",
                4.0f,
                "Floor"
            ),
            TFMTrick("180 BS Shove-it Double Heelflip", "Inward Double Heelflip", 4.0f, "Floor"),
            TFMTrick(
                "180 BS Shove-it Double Heelflip Sex Change",
                "Inward Double Heelflip Sex Change",
                5.0f
            ),
            TFMTrick("360 BS Shove-it Heelflip", "360 Inward Heelflip", 4.0f, "Floor"),
            TFMTrick(
                "360 BS Shove-it Heelflip Sex Change",
                "360 Inward Heelflip Sex Change",
                5.0f,
                "Floor"
            ),
            TFMTrick(
                "360 BS Shove-it Bs 180 Body Varial Heelflip",
                "BS Bigspin Heelflip",
                3.0f,
                "Floor"
            ),
            TFMTrick("540 BS Shove-it Heelflip", "540 Inward Heelflip", 5.0f, "Floor"),
            TFMTrick(
                "540 BS Shove-it Heelflip Sex Change",
                "540 Inward Heelflip Sex Change",
                6.0f,
                "Floor"
            ),
            TFMTrick(
                "540 BS Shove-it Bs 180 Body Varial Heelflip",
                "BS Bigger Spin Heelflip",
                3.0f
            ),
            TFMTrick(
                "540 BS Shove-it Bs 360 Body Varial Heelflip",
                "BS Gazzele Spin Heelflip",
                3.0f
            ),

            /** FS Shove-it **/
            TFMTrick("180 FS Shove-it", "FS Shove-it", 2.0f, "Floor"),
            TFMTrick("180 FS Shove-it Sex Change", "FS Shove-it Sex Change", 3.0f, "Floor"),
            TFMTrick("360 FS Shove-it", "FS 360 Shove-it", 3.0f, "Floor"),
            TFMTrick("360 FS Shove-it Sex Change", "FS 360 Shove-it Sex Change", 4.0f, "Floor"),
            TFMTrick("360 FS Shove-it FS 180 Body Varial", "FS Bigspin", 3.0f, "Floor"),
            TFMTrick("540 FS Shove-it", "FS 540 Shove-it", 4.0f, "Floor"),
            TFMTrick("540 FS Shove-it Sex Change", "FS 540 Shove-it Sex Change", 5.0f, "Floor"),
            TFMTrick("540 FS Shove-it FS 180 Body Varial", "FS Bigger Spin", 3.0f, "Floor"),
            TFMTrick("540 FS Shove-it Bs 360 Body Varial", "FS Gazzele Spin", 3.0f, "Floor"),

            TFMTrick("180 FS Shove-it Kickflip", "Hardflip", 3.0f, "Floor"),
            TFMTrick("180 FS Shove-it Kickflip Sex Change", "Hardflip Sex Change", 4.0f, "Floor"),
            TFMTrick("180 FS Shove-it Double Kickflip", "Hard Double Kickflip", 4.0f, "Floor"),
            TFMTrick(
                "180 FS Shove-it Double Kickflip Sex Change",
                "Hard Double Kickflip Sex Change",
                5.0f
            ),
            TFMTrick("180 FS Shove-it Kickflip FS 360 Body varial", "Grape Flip", 3.0f, "Floor"),
            TFMTrick("360 FS Shove-it Kickflip", "360 Hardflip", 4.0f, "Floor"),
            TFMTrick(
                "360 FS Shove-it Kickflip Sex Change",
                "360 Hardflip Sex Change",
                5.0f,
                "Floor"
            ),
            TFMTrick(
                "360 FS Shove-it FS 180 Body Varial Kickflip",
                "FS Bigspin Flip",
                3.0f,
                "Floor"
            ),
            TFMTrick("540 FS Shove-it Kickflip", "540 Hardflip", 5.0f, "Floor"),
            TFMTrick(
                "540 FS Shove-it Kickflip Sex Change",
                "540 Hardflip Sex Change",
                6.0f,
                "Floor"
            ),
            TFMTrick(
                "540 FS Shove-it FS 180 Body Varial Kickflip",
                "FS Bigger Spin Flip",
                3.0f,
                "Floor"
            ),
            TFMTrick(
                "540 BS Shove-it FS 360 Body Varial Kickflip",
                "FS Gazzele Spin Flip",
                3.0f,
                "Floor"
            ),

            TFMTrick("180 FS Shove-it Heelflip", "Varial Heelflip", 3.0f, "Floor"),
            TFMTrick(
                "180 FS Shove-it Heelflip Sex Change",
                "Varial Heelflip Sex Change",
                4.0f,
                "Floor"
            ),
            TFMTrick("180 FS Shove-it Double Heelflip", "Daydream Flip", 4.0f, "Floor"),
            TFMTrick(
                "180 FS Shove-it Double Heelflip Sex Change",
                "Daydream Flip Sex Change",
                5.0f
            ),
            TFMTrick("360 FS Shove-it Heelflip", "Laser Flip", 4.0f, "Floor"),
            TFMTrick("360 FS Shove-it Heelflip Sex Change", "Laser Flip Sex Change", 5.0f, "Floor"),
            TFMTrick(
                "360 FS Shove-it FS 180 Body Varial Heelflip",
                "FS Bigspin Heelflip",
                3.0f,
                "Floor"
            ),
            TFMTrick("540 FS Shove-it Heelflip", "540 Laser Flip", 5.0f, "Floor"),
            TFMTrick(
                "540 FS Shove-it Heelflip Sex Change",
                "540 Laser Flip Sex Change",
                6.0f,
                "Floor"
            ),
            TFMTrick(
                "540 FS Shove-it FS 180 Body Varial Heelflip",
                "FS Bigger Spin Heelflip",
                3.0f
            ),
            TFMTrick(
                "540 BS Shove-it FS 360 Body Varial Heelflip",
                "FS Gazzele Spin Heelflip",
                3.0f
            ),

            /** Kickflip **/
            TFMTrick("Kickflip", "Flip", 2.0f, "Floor"),
            TFMTrick("Kickflip Sex Change", "Flip Sex Change", 3.0f, "Floor"),
            TFMTrick("Double Kickflip", "Double Flip", 3.0f, "Floor"),
            TFMTrick("Double Kickflip Sex Change", "Double Flip", 3.0f, "Floor"),
            TFMTrick("Triple Kickflip", "Triple Flip", 4.0f, "Floor"),

            /** Heelflip **/
            TFMTrick("Heelflip", "Heelflip", 2.0f, "Floor"),
            TFMTrick("Double Heelflip", "Double Heelflip", 3.0f, "Floor"),
            TFMTrick("Triple Heelflip", "Triple Heelflip", 4.0f, "Floor"),

            /** 180 BS **/
            TFMTrick("180 BS", "Backside", 2.0f, "Floor"),
            TFMTrick("360 BS", "360 BS", 3.0f, "Floor"),

            TFMTrick("180 BS Kickflip", "Backside Flip", 3.0f, "Floor"),
            TFMTrick("360 BS Kickflip", "360 BS Flip", 4.0f, "Floor"),

            TFMTrick("180 BS Heelflip", "Backside Heelflip", 3.0f, "Floor"),
            TFMTrick("360 BS Heelflip", "360 BS Heelflip", 4.0f, "Floor"),

            /** 180 FS **/
            TFMTrick("180 FS", "Frontside", 2.0f, "Floor"),
            TFMTrick("360 FS", "360 FS", 3.0f, "Floor"),

            TFMTrick("180 FS Kickflip", "Frontside Flip", 3.0f, "Floor"),
            TFMTrick("360 FS Kickflip", "360 FS Flip", 4.0f, "Floor"),

            TFMTrick("180 FS Heelflip", "Frontside Heelflip", 3.0f, "Floor"),
            TFMTrick("360 FS Heelflip", "360 FS Heelflip", 4.0f, "Floor"),

            /** Otros **/
            TFMTrick("180 BS Shove-it Heelflip BS 360 Body varial", "Orange Flip", 3.0f, "Floor"),

            TFMTrick("Hospital Flip", "Hospital Flip", 3.0f, "Floor"),
            TFMTrick("BS Bigspin Hospital Flip", "BS Bigspin Hospital Flip", 3.0f, "Floor"),

            TFMTrick("Hospital Heelflip", "Hospital Heelflip", 3.0f, "Floor"),
            TFMTrick("FS Bigspin Hospital Heelflip", "FS Bigspin Hospital Heelflip", 3.0f, "Floor"),

            TFMTrick("Dolphin Flip", "Dolphin Flip", 3.5f, "Floor"),
            TFMTrick("360 Dolphin flip", "Dragon Flip", 4.5f, "Floor"),
            TFMTrick("Dolphin Heelflip", "Dolphin Heelflip", 3.5f, "Floor"),
            TFMTrick("360 Dolphin Heelflip", "Dragon Heelflip", 4.5f, "Floor"),

            TFMTrick("Impossible", "Impossible", 5.0f, "Floor"),
            TFMTrick("Front Foot Impossible", "Front Foot Impossible", 5.5f, "Floor"),

            TFMTrick("Feather Flip", "Feather Flip", 3.5f, "Floor"),
            TFMTrick("Fingerflip", "Fingerflip", 3.0f, "Floor"),
            TFMTrick("Varial Half Flip Late Flip", "Haslam Flip", 4.0f, "Floor"),
            TFMTrick("Underflip", "Underflip", 4.0f, "Floor"),
            TFMTrick("Plasma Spin", "Plasma Spin", 4.5f, "Floor"),
            TFMTrick("Semi Flip", "Semi Flip", 3.5f, "Floor"),
            TFMTrick("Sex Change", "Sex Change", 2.0f, "Floor"),
            TFMTrick("Sigma Flip", "Sigma Flip", 4.5f, "Floor")
        )
    ),
    GRIND(
        typeTrickName = "Grind",
        trickList = arrayListOf(
            TFMTrick("BS 5-0", "BS 5-0", 2.5f, "Grind"),
            TFMTrick("FS 5-0", "FS 5-0", 2.5f, "Grind"),
            TFMTrick("BS 50-50", "BS 50-50", 2.0f, "Grind"),
            TFMTrick("FS 50-50", "FS 50-50", 2.0f, "Grind"),
            TFMTrick("BS Crooked", "BS Crooked", 3.0f, "Grind"),
            TFMTrick("FS Crooked", "FS Crooked", 3.0f, "Grind"),
            TFMTrick("BS Feeble", "BS Feeble", 3.5f, "Grind"),
            TFMTrick("FS Feeble", "FS Feeble", 3.5f, "Grind"),
            TFMTrick("BS Nose Grind", "BS Nose Grind", 2.5f, "Grind"),
            TFMTrick("FS Nose Grind", "FS Nose Grind", 2.5f, "Grind"),
            TFMTrick("BS Over Crook", "BS Over Crook", 3.5f, "Grind"),
            TFMTrick("FS Over Crook", "FS Over Crook", 3.5f, "Grind"),
            TFMTrick("BS Salad", "BS Salad", 3.0f, "Grind"),
            TFMTrick("FS Salad", "FS Salad", 3.0f, "Grind"),
            TFMTrick("BS Smith", "BS Smith", 3.5f, "Grind"),
            TFMTrick("FS Smith", "FS Smith", 3.5f, "Grind"),
            TFMTrick("BS Suski", "BS Suski", 3.0f, "Grind"),
            TFMTrick("FS Suski", "FS Suski", 3.0f, "Grind"),
            TFMTrick("BS Hurricane Grind", "BS Hurricane Grind", 4.5f, "Grind"),
            TFMTrick("FS Hurricane Grind", "FS Hurricane Grind", 4.5f, "Grind"),
            TFMTrick("BS Layback Grind", "BS Layback Grind", 2.5f, "Grind"),
            TFMTrick("FS Layback Grind", "FS Layback Grind", 2.5f, "Grind"),
            TFMTrick("BS Slash Grind", "BS Slash Grind", 2.0f, "Grind"),
            TFMTrick("FS Slash Grind", "FS Slash Grind", 2.0f, "Grind"),
            TFMTrick("BS Willy Grind", "BS Willy Grind", 3.0f, "Grind"),
            TFMTrick("FS Willy Grind", "FS Willy Grind", 3.0f, "Grind"),
            TFMTrick("BS Losi Grind", "BS Losi Grind", 3.0f, "Grind"),
            TFMTrick("FS Losi Grind", "FS Losi Grind", 3.0f, "Grind"),
        )
    ),
    SLIDE(
        typeTrickName = "Slide",
        trickList = arrayListOf(
            TFMTrick("BS Blunt Slide", "BS Blunt Slide", 4.0f, "Slide"),
            TFMTrick("FS Blunt Slide", "FS Blunt Slide", 4.0f, "Slide"),
            TFMTrick("BS Boardslide", "BS Boardslide", 2.0f, "Slide"),
            TFMTrick("FS Boardslide", "FS Boardslide", 2.0f, "Slide"),
            TFMTrick("BS Lipslide", "BS Lipslide", 2.5f, "Slide"),
            TFMTrick("FS Lipslide", "FS Lipslide", 2.5f, "Slide"),
            TFMTrick("BS Nose Blunt Slide", "BS Nose Blunt Slide", 4.5f, "Slide"),
            TFMTrick("FS Nose Blunt Slide", "FS Nose Blunt Slide", 4.5f, "Slide"),
            TFMTrick("BS Nose Slide", "BS Nose Slide", 2.0f, "Slide"),
            TFMTrick("FS Nose Slide", "FS Nose Slide", 2.0f, "Slide"),
            TFMTrick("BS Tail Slide", "BS Tail Slide", 2.5f, "Slide"),
            TFMTrick("FS Tail Slide", "FS Tail Slide", 2.5f, "Slide"),
            TFMTrick("BS Casper Slide", "BS Casper Slide", 3.0f, "Slide"),
            TFMTrick("FS Casper Slide", "FS Casper Slide", 3.0f, "Slide"),
            TFMTrick("BS Darkslide", "BS Darkslide", 4.0f, "Slide"),
            TFMTrick("FS Darkslide", "FS Darkslide", 4.0f, "Slide"),
            TFMTrick("BS Nail Slide", "BS Nail Slide", 5.0f, "Slide"),
            TFMTrick("FS Nail Slide", "FS Nail Slide", 5.0f, "Slide"),
        )
    ),
    GRABS(
        typeTrickName = "Grabs",
        trickList = arrayListOf(
            TFMTrick("Airwalk", "Airwalk", 2.5f, "Grabs"),
            TFMTrick("Benihana", "Benihana", 3.0f, "Grabs"),
            TFMTrick("Body Jar", "Body Jar", 2.5f, "Grabs"),
            TFMTrick("Cannonball", "Cannonball", 3.0f, "Grabs"),
            TFMTrick("Christ Air", "Christ Air", 2.0f, "Grabs"),
            TFMTrick("Crail Grab", "Crail", 3.5f, "Grabs"),
            TFMTrick("Creeper", "Creeper", 3.5f, "Grabs"),
            TFMTrick("Crossbone", "Crossbone", 3.5f, "Grabs"),
            TFMTrick("Del Mar Indy", "Del Mar Indy", 3.0f, "Grabs"),
            TFMTrick("Double Grab", "Double Grab", 3.0f, "Grabs"),
            TFMTrick("Grosman Grab", "Grosman Grab", 2.0f, "Grabs"),
            TFMTrick("Indy Grab", "Indy Grab", 2.0f, "Grabs"),
            TFMTrick("Japan Air", "Japan Air", 1.5f, "Grabs"),
            TFMTrick("Invert", "Invert", 4.0f, "Grabs"),
            TFMTrick("Judo Air", "Judo Air", 2.0f, "Grabs"),
            TFMTrick("Frontside Air / Lien Air Grab", "Frontside Air / Lien Air", 3.0f, "Grabs"),
            TFMTrick("Madonna", "Madonna", 2.5f, "Grabs"),
            TFMTrick("McTwist / 540", "McTwist / 540", 4.5f, "Grabs"),
            TFMTrick("Melon Grab", "Melon Grab", 2.0f, "Grabs"),
            TFMTrick("Method Air", "Method Air", 3.0f, "Grabs"),
            TFMTrick("Mute Air", "Mute Air", 2.0f, "Grabs"),
            TFMTrick("Nose Grab", "Nose Grab", 2.0f, "Grabs"),
            TFMTrick("Nosebone", "Nosebone", 2.5f, "Grabs"),
            TFMTrick("Nuclear Grab", "Nuclear Grab", 2.0f, "Grabs"),
            TFMTrick("Roastbeef Grab", "Roastbeef Grab", 2.0f, "Grabs"),
            TFMTrick("Rocket Air", "Rocket Air", 3.5f, "Grabs"),
            TFMTrick("Sacktap", "Sacktap", 3.5f, "Grabs"),
            TFMTrick("Sal Flip", "Sal Flip", 4.0f, "Grabs"),
            TFMTrick("Seatbelt", "Seatbelt Grab", 2.5f, "Grabs"),
            TFMTrick("Slob Air", "Slob Air", 2.5f, "Grabs"),
            TFMTrick("Stalefish Grab", "Stalefish Grab", 3.5f, "Grabs"),
            TFMTrick("Stiffy Grab", "Stiffy Grab", 3.0f, "Grabs"),
            TFMTrick("Tail Grab", "Tail Grab", 2.0f, "Grabs"),
            TFMTrick("Tailbone", "Tailbone", 2.5f, "Grabs"),
            TFMTrick("Superman Grab", "Superman Grab", 4.5f, "Grabs"),
        )
    ),
    BALANCE(
        typeTrickName = "Balance",
        trickList = arrayListOf(
            TFMTrick("Manual", "Manual", 1.0f, "Balance"),
            TFMTrick("Nose Manual", "Nose Manual", 1.5f, "Balance"),
            TFMTrick("One Foot Manual", "One Foot Manual", 2.0f, "Balance"),
            TFMTrick("One Foot Nose Manual", "One Foot Nose Manual", 2.5f, "Balance"),
            TFMTrick("One Wheel Manual", "One Wheel Manual", 3.0f, "Balance"),
            TFMTrick("One Wheel Nose Manual", "One Wheel Nose Manual", 3.5f, "Balance"),
        )
    ),

}

data class TFMTrick(
    val realName: String = "",
    val akaName: String = "",
    val difficulty: Float = 0.0f,
    val typeTrick: String = ""
) {
    fun getRealDifficulty(): Float {
        return when {
            realName.contains("Fakie") -> difficulty.plus(0.5).toFloat()
            realName.contains("Switch") -> difficulty.plus(1.0).toFloat()
            realName.contains("Nollie") -> difficulty.plus(1.5).toFloat()
            else -> difficulty
        }
    }
}