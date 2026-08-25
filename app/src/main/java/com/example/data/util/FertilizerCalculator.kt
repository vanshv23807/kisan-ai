package com.example.data.util

import com.example.data.model.FertilizerDosageRecommendation

object FertilizerCalculator {

    data class CropNutritionProfile(
        val cropName: String,
        val season: String,
        val nKgPerHa: Double,
        val pKgPerHa: Double,
        val kKgPerHa: Double,
        val zincKgPerHa: Double = 25.0,
        val compostTonnesPerHa: Double = 5.0,
        val basalRatio: String = "50% N + 100% P + 100% K",
        val topDress1Ratio: String = "25% N at 1st Irrigation / Tillering (21-25 DAS)",
        val topDress2Ratio: String = "25% N at Booting / Panicle Initiation (45-50 DAS)",
        val foliarSprayAdvice: String = "1% Nano Urea or NPK 19:19:19 spray during flowering"
    )

    private val cropProfiles = mapOf(
        "Wheat" to CropNutritionProfile(
            cropName = "Wheat",
            season = "Rabi",
            nKgPerHa = 120.0,
            pKgPerHa = 60.0,
            kKgPerHa = 40.0,
            zincKgPerHa = 25.0,
            compostTonnesPerHa = 6.0,
            basalRatio = "Full P & K + 1/3 N at Sowing",
            topDress1Ratio = "1/3 N at 1st CRI Irrigation (21 DAS)",
            topDress2Ratio = "1/3 N at Jointing / Tillering stage (45 DAS)",
            foliarSprayAdvice = "Foliar 13:0:45 (Potassium Nitrate) 1.5kg/acre at grain filling to maximize grain weight."
        ),
        "Paddy / Rice" to CropNutritionProfile(
            cropName = "Paddy / Rice",
            season = "Kharif",
            nKgPerHa = 120.0,
            pKgPerHa = 60.0,
            kKgPerHa = 60.0,
            zincKgPerHa = 25.0,
            compostTonnesPerHa = 8.0,
            basalRatio = "Full P + 50% K + 1/3 N at Puddling",
            topDress1Ratio = "1/3 N at Active Tillering (25-30 DAT)",
            topDress2Ratio = "1/3 N + 50% K at Panicle Initiation (50-55 DAT)",
            foliarSprayAdvice = "Apply 25 kg Zinc Sulphate (21%) or 12 kg (33%) per ha to prevent Khaira disease."
        ),
        "Basmati Rice" to CropNutritionProfile(
            cropName = "Basmati Rice",
            season = "Kharif",
            nKgPerHa = 90.0,
            pKgPerHa = 40.0,
            kKgPerHa = 40.0,
            zincKgPerHa = 20.0,
            compostTonnesPerHa = 7.0,
            basalRatio = "Full P + 50% K + 1/3 N at Transplanting",
            topDress1Ratio = "1/3 N at Tillering (21-25 DAT)",
            topDress2Ratio = "1/3 N at Panicle Emergence (45-50 DAT)",
            foliarSprayAdvice = "Avoid excess nitrogen to prevent lodging and maintain aromatic grain elongation."
        ),
        "Mustard" to CropNutritionProfile(
            cropName = "Mustard",
            season = "Rabi",
            nKgPerHa = 80.0,
            pKgPerHa = 40.0,
            kKgPerHa = 40.0,
            zincKgPerHa = 20.0,
            compostTonnesPerHa = 5.0,
            basalRatio = "Full P, K & Sulphur (20kg/ha) + 50% N at Sowing",
            topDress1Ratio = "50% N at 1st Irrigation / Rosette stage (30 DAS)",
            topDress2Ratio = "Not required (Foliar spray only)",
            foliarSprayAdvice = "Elemental Sulphur 20-25 kg/ha essential for high oil content and seed shine."
        ),
        "Cotton" to CropNutritionProfile(
            cropName = "Cotton",
            season = "Kharif",
            nKgPerHa = 150.0,
            pKgPerHa = 60.0,
            kKgPerHa = 60.0,
            zincKgPerHa = 25.0,
            compostTonnesPerHa = 8.0,
            basalRatio = "Full P & K + 20% N at Sowing",
            topDress1Ratio = "40% N at Square Formation (45-50 DAS)",
            topDress2Ratio = "40% N at Peak Boll Development (75-80 DAS)",
            foliarSprayAdvice = "Spray 2% DAP or 1% Potassium Nitrate 4 times at 10-day intervals during boll burst."
        ),
        "Maize" to CropNutritionProfile(
            cropName = "Maize",
            season = "Kharif / Rabi",
            nKgPerHa = 120.0,
            pKgPerHa = 60.0,
            kKgPerHa = 40.0,
            zincKgPerHa = 25.0,
            compostTonnesPerHa = 6.0,
            basalRatio = "Full P, K, Zinc + 1/3 N at Sowing",
            topDress1Ratio = "1/3 N at Knee-High Stage (30-35 DAS)",
            topDress2Ratio = "1/3 N at Tasseling / Silking Stage (55-60 DAS)",
            foliarSprayAdvice = "Maintain adequate soil moisture during silking for complete grain setting."
        ),
        "Sugarcane" to CropNutritionProfile(
            cropName = "Sugarcane",
            season = "Perennial",
            nKgPerHa = 250.0,
            pKgPerHa = 115.0,
            kKgPerHa = 115.0,
            zincKgPerHa = 35.0,
            compostTonnesPerHa = 15.0,
            basalRatio = "Full P + 1/3 K + 1/4 N in furrows at Planting",
            topDress1Ratio = "1/4 N at 45 DAP (Tillering) + 1/4 N at 90 DAP",
            topDress2Ratio = "1/4 N + remaining K at Grand Growth (120-150 DAP)",
            foliarSprayAdvice = "Apply trash mulching to conserve moisture and suppress broadleaf weeds."
        ),
        "Potato" to CropNutritionProfile(
            cropName = "Potato",
            season = "Rabi",
            nKgPerHa = 180.0,
            pKgPerHa = 100.0,
            kKgPerHa = 150.0,
            zincKgPerHa = 25.0,
            compostTonnesPerHa = 10.0,
            basalRatio = "Full P + 50% K + 50% N at Planting",
            topDress1Ratio = "50% N + 50% K at Earthing Up (30-35 DAP)",
            topDress2Ratio = "Foliar 00:00:50 (SOP) at tuber bulking (60 DAP)",
            foliarSprayAdvice = "SOP (Sulphate of Potash) is preferred over MOP for improved starch quality and chip processing."
        ),
        "Tomato" to CropNutritionProfile(
            cropName = "Tomato",
            season = "All Season",
            nKgPerHa = 100.0,
            pKgPerHa = 60.0,
            kKgPerHa = 60.0,
            zincKgPerHa = 15.0,
            compostTonnesPerHa = 10.0,
            basalRatio = "Full P + 50% K + 1/3 N at Bed Preparation",
            topDress1Ratio = "1/3 N at 30 DAT (Vegetative growth)",
            topDress2Ratio = "1/3 N + 50% K at Flowering & Fruit Set (50-60 DAT)",
            foliarSprayAdvice = "Calcium Nitrate 0.5% spray prevents Blossom End Rot (BER) in high heat."
        ),
        "Soybean" to CropNutritionProfile(
            cropName = "Soybean",
            season = "Kharif",
            nKgPerHa = 30.0, // Legume fixes own N
            pKgPerHa = 60.0,
            kKgPerHa = 40.0,
            zincKgPerHa = 20.0,
            compostTonnesPerHa = 4.0,
            basalRatio = "Full N, P, K & Sulphur (20kg/ha) as starter basal dose",
            topDress1Ratio = "Inoculate seeds with Rhizobium culture before sowing",
            topDress2Ratio = "Spray 2% DAP or 19:19:19 at Pod Formation stage",
            foliarSprayAdvice = "Starter dose of 30kg N helps root nodule establishment in first 15 days."
        ),
        "Gram / Chickpea" to CropNutritionProfile(
            cropName = "Gram / Chickpea",
            season = "Rabi",
            nKgPerHa = 20.0,
            pKgPerHa = 40.0,
            kKgPerHa = 20.0,
            zincKgPerHa = 15.0,
            compostTonnesPerHa = 3.0,
            basalRatio = "Full N, P, K at Sowing (Plough sole depth)",
            topDress1Ratio = "Treat seed with Rhizobium and PSB (Phosphate Solubilizing Bacteria)",
            topDress2Ratio = "Foliar spray 2% Urea at flowering if foliage looks pale",
            foliarSprayAdvice = "Avoid heavy nitrogen to prevent excessive vegetative growth and flower drop."
        )
    )

    fun getAvailableCrops(): List<String> = cropProfiles.keys.toList()

    /**
     * Calculate exact fertilizer recommendation in pure nutrients and commercial bags (Urea, DAP, MOP).
     * Area is converted to Hectares (1 Acre = 0.404686 Ha).
     */
    fun calculate(
        cropName: String,
        fieldArea: Double,
        areaUnit: String = "Acres"
    ): FertilizerDosageRecommendation {
        val normalizedCrop = cropProfiles.keys.find { it.contains(cropName, ignoreCase = true) } ?: "Wheat"
        val profile = cropProfiles[normalizedCrop] ?: cropProfiles["Wheat"]!!

        // Convert input area to Hectares
        val areaInHa = when (areaUnit.lowercase()) {
            "acres", "acre" -> fieldArea * 0.404686
            "hectares", "hectare", "ha" -> fieldArea
            "bigha" -> fieldArea * 0.16187 // approx 1 Bigha = 0.4 Acre
            "guntha" -> fieldArea * 0.010117
            else -> fieldArea * 0.404686
        }

        // Total pure nutrient requirement for the field area
        val pureN = profile.nKgPerHa * areaInHa
        val pureP = profile.pKgPerHa * areaInHa
        val pureK = profile.kKgPerHa * areaInHa

        // Commercial fertilizer calculations:
        // 1 bag DAP (50kg) contains: 18% N (9kg N) and 46% P2O5 (23kg P)
        val dapBags = (pureP / 23.0).coerceAtLeast(0.0)
        val nSuppliedByDap = dapBags * 9.0

        // Remaining N supplied by Urea (45kg bag contains 46% N = 20.7kg N per bag)
        val remainingN = (pureN - nSuppliedByDap).coerceAtLeast(0.0)
        val ureaBags = (remainingN / 20.7).coerceAtLeast(0.0)

        // Potassium supplied by MOP (50kg bag contains 60% K2O = 30kg K per bag)
        val mopBags = (pureK / 30.0).coerceAtLeast(0.0)

        // Micronutrient & organic supplements
        val zincKg = profile.zincKgPerHa * areaInHa
        val nanoUreaBottles = kotlin.math.ceil(ureaBags * 0.35).toInt().coerceAtLeast(1)
        val compostTonnes = profile.compostTonnesPerHa * areaInHa

        // Estimated cost (Govt Subsidized MRP: Urea ~₹267/45kg, DAP ~₹1350/50kg, MOP ~₹1700/50kg, Zinc ~₹85/kg)
        val costUrea = ureaBags * 267.0
        val costDap = dapBags * 1350.0
        val costMop = mopBags * 1700.0
        val costZinc = zincKg * 85.0
        val totalCost = (costUrea + costDap + costMop + costZinc).toInt()

        val npkRatioDisplay = "${profile.nKgPerHa.toInt()} : ${profile.pKgPerHa.toInt()} : ${profile.kKgPerHa.toInt()} NPK (kg/ha)"

        return FertilizerDosageRecommendation(
            cropName = profile.cropName,
            fieldArea = fieldArea,
            areaUnit = areaUnit,
            recommendedNpkRatio = npkRatioDisplay,
            pureNitrogenKg = pureN,
            purePhosphorusKg = pureP,
            purePotassiumKg = pureK,
            ureaBags45Kg = kotlin.math.round(ureaBags * 10) / 10.0,
            dapBags50Kg = kotlin.math.round(dapBags * 10) / 10.0,
            mopBags50Kg = kotlin.math.round(mopBags * 10) / 10.0,
            zincSulphateKg = kotlin.math.round(zincKg * 10) / 10.0,
            nanoUreaBottles = nanoUreaBottles,
            basalDoseSummary = profile.basalRatio,
            topDressing1Summary = profile.topDress1Ratio,
            topDressing2Summary = profile.topDress2Ratio,
            organicCompostRecommendedTonnes = kotlin.math.round(compostTonnes * 10) / 10.0,
            estimatedCostInr = totalCost,
            applicationAdvice = profile.foliarSprayAdvice
        )
    }
}
