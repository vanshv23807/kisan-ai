package com.example.data.util

object LocationCoordinatesHelper {

    data class GeoCoordinates(val latitude: Double, val longitude: Double)

    // Lookup table for Indian States & Districts to exact GPS coordinates
    private val districtCoordinates = mapOf(
        // Punjab
        "ludhiana" to GeoCoordinates(30.9010, 75.8573),
        "amritsar" to GeoCoordinates(31.6340, 74.8723),
        "jalandhar" to GeoCoordinates(31.3260, 75.5762),
        "patiala" to GeoCoordinates(30.3398, 76.3869),
        "bathinda" to GeoCoordinates(30.2110, 74.9455),
        "samrala" to GeoCoordinates(30.8354, 76.1914),
        "khanna" to GeoCoordinates(30.7042, 76.2201),
        "ferozepur" to GeoCoordinates(30.9237, 74.6065),
        "hoshiarpur" to GeoCoordinates(31.5273, 75.9149),
        "sangrur" to GeoCoordinates(30.2458, 75.8421),

        // Haryana
        "karnal" to GeoCoordinates(29.6857, 76.9905),
        "hisar" to GeoCoordinates(29.1492, 75.7217),
        "kurukshetra" to GeoCoordinates(29.9695, 76.8783),
        "ambala" to GeoCoordinates(30.3782, 76.7767),
        "rohtak" to GeoCoordinates(28.8955, 76.6066),
        "panipat" to GeoCoordinates(29.3909, 76.9635),
        "sirsa" to GeoCoordinates(29.5349, 75.0298),

        // Uttar Pradesh
        "meerut" to GeoCoordinates(28.9845, 77.7064),
        "varanasi" to GeoCoordinates(25.3176, 82.9739),
        "lucknow" to GeoCoordinates(26.8467, 80.9462),
        "kanpur" to GeoCoordinates(26.4499, 80.3319),
        "agra" to GeoCoordinates(27.1767, 78.0081),
        "gorakhpur" to GeoCoordinates(26.7606, 83.3732),
        "bareilly" to GeoCoordinates(28.3670, 79.4304),
        "aligarh" to GeoCoordinates(27.8974, 78.0880),
        "moradabad" to GeoCoordinates(28.8386, 78.7733),
        "muzaffarnagar" to GeoCoordinates(29.4727, 77.7085),

        // Rajasthan
        "jaipur" to GeoCoordinates(26.9124, 75.7873),
        "jodhpur" to GeoCoordinates(26.2389, 73.0243),
        "kota" to GeoCoordinates(25.2138, 75.8648),
        "bikaner" to GeoCoordinates(28.0229, 73.3119),
        "ganganagar" to GeoCoordinates(29.9038, 73.8772),
        "alwar" to GeoCoordinates(27.5530, 76.6346),
        "udaipur" to GeoCoordinates(24.5854, 73.7125),

        // Madhya Pradesh
        "indore" to GeoCoordinates(22.7196, 75.8577),
        "bhopal" to GeoCoordinates(23.2599, 77.4126),
        "jabalpur" to GeoCoordinates(23.1815, 79.9864),
        "gwalior" to GeoCoordinates(26.2183, 78.1828),
        "ujjain" to GeoCoordinates(23.1765, 75.7885),

        // Maharashtra
        "pune" to GeoCoordinates(18.5204, 73.8567),
        "nagpur" to GeoCoordinates(21.1458, 79.0882),
        "nashik" to GeoCoordinates(19.9975, 73.7898),
        "aurangabad" to GeoCoordinates(19.8762, 75.3433),
        "kolhapur" to GeoCoordinates(16.7050, 74.2433),
        "solapur" to GeoCoordinates(17.6599, 75.9064),

        // Gujarat
        "ahmedabad" to GeoCoordinates(23.0225, 72.5714),
        "surat" to GeoCoordinates(21.1702, 72.8311),
        "vadodara" to GeoCoordinates(22.3072, 73.1812),
        "rajkot" to GeoCoordinates(22.3039, 70.8022),
        "bhavnagar" to GeoCoordinates(21.7645, 72.1519),

        // Bihar
        "patna" to GeoCoordinates(25.5941, 85.1376),
        "gaya" to GeoCoordinates(24.7914, 85.0002),
        "muzaffarpur" to GeoCoordinates(26.1209, 85.3647),
        "bhagalpur" to GeoCoordinates(25.2425, 86.9842),

        // Karnataka
        "bengaluru" to GeoCoordinates(12.9716, 77.5946),
        "mysuru" to GeoCoordinates(12.2958, 76.6394),
        "hubballi" to GeoCoordinates(15.3647, 75.1240),
        "belagavi" to GeoCoordinates(15.8497, 74.4977),

        // Andhra Pradesh & Telangana
        "hyderabad" to GeoCoordinates(17.3850, 78.4867),
        "vijayawada" to GeoCoordinates(16.5062, 80.6480),
        "guntur" to GeoCoordinates(16.3067, 80.4365),
        "visakhapatnam" to GeoCoordinates(17.6868, 83.2185)
    )

    private val stateCoordinates = mapOf(
        "punjab" to GeoCoordinates(31.1471, 75.3412),
        "haryana" to GeoCoordinates(29.0588, 76.0856),
        "uttar pradesh" to GeoCoordinates(26.8467, 80.9462),
        "rajasthan" to GeoCoordinates(27.0238, 74.2179),
        "madhya pradesh" to GeoCoordinates(22.9734, 78.6569),
        "maharashtra" to GeoCoordinates(19.7515, 75.7139),
        "gujarat" to GeoCoordinates(22.2587, 71.1924),
        "bihar" to GeoCoordinates(25.0961, 85.3131),
        "karnataka" to GeoCoordinates(15.3173, 75.7139),
        "telangana" to GeoCoordinates(18.1124, 79.0193),
        "andhra pradesh" to GeoCoordinates(15.9129, 79.7400),
        "tamil nadu" to GeoCoordinates(11.1271, 78.6569),
        "west bengal" to GeoCoordinates(22.9868, 87.8550),
        "odisha" to GeoCoordinates(20.9517, 85.0985),
        "kerala" to GeoCoordinates(10.8505, 76.2711),
        "himachal pradesh" to GeoCoordinates(31.1048, 77.1734),
        "uttarakhand" to GeoCoordinates(30.0668, 79.0193),
        "assam" to GeoCoordinates(26.2006, 92.9376),
        "chhattisgarh" to GeoCoordinates(21.2787, 81.8661),
        "jharkhand" to GeoCoordinates(23.6102, 85.2799)
    )

    fun resolveCoordinates(
        village: String,
        district: String,
        state: String,
        pinCode: String,
        fallbackLat: Double = 30.8987,
        fallbackLng: Double = 75.8573
    ): GeoCoordinates {
        val cleanVillage = village.trim().lowercase()
        val cleanDistrict = district.trim().lowercase()
        val cleanState = state.trim().lowercase()

        // 1. Check Village Match
        districtCoordinates[cleanVillage]?.let { return it }

        // 2. Check District Match
        districtCoordinates[cleanDistrict]?.let { return it }

        // 3. Check if any known district is contained within the district or village string
        for ((key, coord) in districtCoordinates) {
            if (cleanDistrict.contains(key) || cleanVillage.contains(key)) {
                return coord
            }
        }

        // 4. Check State Match
        stateCoordinates[cleanState]?.let { return it }
        for ((key, coord) in stateCoordinates) {
            if (cleanState.contains(key)) {
                return coord
            }
        }

        // 5. PinCode heuristic for Northern & Western India
        val pin = pinCode.trim()
        if (pin.length >= 2) {
            when (pin.substring(0, 2)) {
                "14" -> return GeoCoordinates(30.9010, 75.8573) // Punjab
                "15" -> return GeoCoordinates(30.2110, 74.9455) // Bathinda Punjab
                "16" -> return GeoCoordinates(30.7333, 76.7794) // Chandigarh
                "12", "13" -> return GeoCoordinates(29.6857, 76.9905) // Haryana
                "11" -> return GeoCoordinates(28.6139, 77.2090) // Delhi
                "20", "24", "25", "26", "27", "28" -> return GeoCoordinates(26.8467, 80.9462) // UP
                "30", "31", "32", "33", "34" -> return GeoCoordinates(26.9124, 75.7873) // Rajasthan
                "36", "37", "38", "39" -> return GeoCoordinates(23.0225, 72.5714) // Gujarat
                "40", "41", "42", "43", "44" -> return GeoCoordinates(18.5204, 73.8567) // Maharashtra
                "45", "46", "47", "48" -> return GeoCoordinates(22.7196, 75.8577) // MP
                "50", "51", "52", "53" -> return GeoCoordinates(16.5062, 80.6480) // AP/Telangana
                "56", "57", "58", "59" -> return GeoCoordinates(12.9716, 77.5946) // Karnataka
                "60", "61", "62", "63", "64" -> return GeoCoordinates(11.1271, 78.6569) // Tamil Nadu
                "67", "68", "69" -> return GeoCoordinates(10.8505, 76.2711) // Kerala
                "70", "71", "72", "73", "74" -> return GeoCoordinates(22.9868, 87.8550) // West Bengal
                "80", "81", "82", "83", "84", "85" -> return GeoCoordinates(25.5941, 85.1376) // Bihar
            }
        }

        return GeoCoordinates(fallbackLat, fallbackLng)
    }
}
