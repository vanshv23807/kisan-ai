package com.example.data.util

import com.example.data.model.FarmTask

data class FarmHealthAssessment(
    val overallScore: Int,
    val status: String,
    val soilScore: Int,
    val waterScore: Int,
    val pestScore: Int,
    val vitalityScore: Int,
    val summary: String,
    val issuesIdentified: List<String>,
    val generatedActionTasks: List<DynamicTaskTemplate>
)

data class DynamicTaskTemplate(
    val title: String,
    val description: String,
    val priority: String,
    val priorityLabel: String,
    val category: String,
    val whyReason: String
)

object FarmHealthCalculator {

    fun calculateHealth(
        farmType: String,
        primaryCropOrAnimal: String,
        soilType: String,
        soilTestingStatus: String,
        fertilizerPractice: String,
        drainageCondition: String,
        waterSource: String,
        pestPressure: String,
        diseaseSigns: String,
        seedQuality: String,
        boundaryFencing: String
    ): FarmHealthAssessment {
        val issues = mutableListOf<String>()
        val tasks = mutableListOf<DynamicTaskTemplate>()

        // 1. SOIL SCORE CALCULATION (Max 100)
        var soilPts = 0
        when {
            soilTestingStatus.contains("Recent", true) -> {
                soilPts += 35
            }
            soilTestingStatus.contains("1-2", true) -> {
                soilPts += 22
                issues.add("Soil testing is over 1 year old")
                tasks.add(
                    DynamicTaskTemplate(
                        title = "Schedule Soil NPK & pH Test",
                        description = "Collect 5 composite soil core samples from root zones to re-test micronutrient and organic carbon levels.",
                        priority = "Medium",
                        priorityLabel = "RECOMMENDED",
                        category = "Soil Health",
                        whyReason = "Soil testing renewal prevents nutrient over-application and balances fertilizer costs."
                    )
                )
            }
            else -> {
                soilPts += 10
                issues.add("Soil has never been lab-tested")
                tasks.add(
                    DynamicTaskTemplate(
                        title = "Collect Soil Sample for Lab Test",
                        description = "Send topsoil sample to nearest Krishi Vigyan Kendra (KVK) for comprehensive NPK, pH, and organic matter lab testing.",
                        priority = "High",
                        priorityLabel = "DO FIRST",
                        category = "Soil Health",
                        whyReason = "Knowing exact soil fertility prevents crop stunting and saves fertilizer expenditure."
                    )
                )
            }
        }

        when {
            soilType.contains("Loamy", true) || soilType.contains("Alluvial", true) || soilType.contains("Black", true) -> {
                soilPts += 35
            }
            soilType.contains("Red", true) || soilType.contains("Clay", true) || soilType.contains("Silt", true) -> {
                soilPts += 25
            }
            else -> {
                soilPts += 15
                issues.add("Soil texture is sandy or degraded")
            }
        }

        when {
            fertilizerPractice.contains("Balanced", true) -> {
                soilPts += 30
            }
            fertilizerPractice.contains("Organic", true) -> {
                soilPts += 30
            }
            fertilizerPractice.contains("Chemical", true) || fertilizerPractice.contains("Urea", true) -> {
                soilPts += 18
                issues.add("High reliance on chemical urea reduces soil microbial activity")
                tasks.add(
                    DynamicTaskTemplate(
                        title = "Apply Organic Compost / Vermicompost",
                        description = "Incorporate 2 tonnes/acre well-decomposed Farm Yard Manure (FYM) or vermicompost to rebuild beneficial soil microbes.",
                        priority = "Medium",
                        priorityLabel = "NUTRITION",
                        category = "Fertilizer",
                        whyReason = "Organic carbon buffer protects soil moisture and enhances fertilizer uptake efficiency."
                    )
                )
            }
            else -> {
                soilPts += 12
                issues.add("Low or deficient soil fertilization practice")
            }
        }
        val soilScore = soilPts.coerceIn(10, 100)

        // 2. WATER & DRAINAGE SCORE (Max 100)
        var waterPts = 0
        when {
            waterSource.contains("Drip", true) || waterSource.contains("Micro", true) -> {
                waterPts += 55
                tasks.add(
                    DynamicTaskTemplate(
                        title = "When to Irrigate: Evening Drip Run (45 min)",
                        description = "Run drip lines between 4:30 PM - 6:00 PM (45 mins) to deliver 25-30 mm targeted moisture to the root zones.",
                        priority = "High",
                        priorityLabel = "IRRIGATION",
                        category = "Irrigation",
                        whyReason = "Evening drip watering prevents evaporation losses and keeps root-zone moisture at optimal 55-65%."
                    )
                )
            }
            waterSource.contains("Borewell", true) || waterSource.contains("Solar", true) -> {
                waterPts += 48
                tasks.add(
                    DynamicTaskTemplate(
                        title = "When to Irrigate: Morning Solar Pump Cycle",
                        description = "Run solar/borewell pump for 2 hours during peak sunlight (9:00 AM - 11:30 AM) applying 40 mm water to field furrows.",
                        priority = "High",
                        priorityLabel = "IRRIGATION",
                        category = "Irrigation",
                        whyReason = "Utilizes free solar generation hours and provides deep soil infiltration before midday heat."
                    )
                )
            }
            waterSource.contains("Canal", true) -> {
                waterPts += 40
                tasks.add(
                    DynamicTaskTemplate(
                        title = "When to Irrigate: Canal Rotational Inflow Window",
                        description = "Open inlet gates for 3.5 hours during scheduled canal water turn to saturate field beds to 45 mm depth.",
                        priority = "High",
                        priorityLabel = "IRRIGATION",
                        category = "Irrigation",
                        whyReason = "Timely canal distribution capture ensures vital crop stages (CRI & tillering) receive adequate moisture."
                    )
                )
            }
            waterSource.contains("Rainfed", true) -> {
                waterPts += 25
                issues.add("100% rainfed dependency increases drought vulnerability")
                tasks.add(
                    DynamicTaskTemplate(
                        title = "When to Irrigate: Moisture Conservation & Spot Mulching",
                        description = "Inspect topsoil moisture depth; spread crop residue/biomass mulch along crop rows to conserve root moisture.",
                        priority = "High",
                        priorityLabel = "WATER CARE",
                        category = "Irrigation",
                        whyReason = "Mulching preserves 30-40% more soil moisture in rainfed farm parcels."
                    )
                )
            }
            else -> {
                waterPts += 12
                issues.add("Water is scarce or supplied via external tankers")
                tasks.add(
                    DynamicTaskTemplate(
                        title = "When to Irrigate: Root-Zone Moisture Check & Watering",
                        description = "Perform manual soil moisture test at 10cm depth; apply 35 mm supplemental watering if soil ball crumbles easily.",
                        priority = "High",
                        priorityLabel = "IRRIGATION",
                        category = "Irrigation",
                        whyReason = "Prevents moisture stress during vegetative and flowering growth phases."
                    )
                )
            }
        }

        when {
            drainageCondition.contains("Excellent", true) || drainageCondition.contains("No Waterlogging", true) -> {
                waterPts += 45
            }
            drainageCondition.contains("Moderate", true) -> {
                waterPts += 30
            }
            else -> {
                waterPts += 10
                issues.add("Poor drainage causing stagnant water accumulation")
                tasks.add(
                    DynamicTaskTemplate(
                        title = "Dig Peripheral Drainage Trenches",
                        description = "Excavate 1.5-foot deep drainage furrows along field perimeter to channel excess surface runoff away from crop roots.",
                        priority = "High",
                        priorityLabel = "URGENT",
                        category = "Field Work",
                        whyReason = "Standing water deprives root zones of oxygen and induces lethal fungal root rot."
                    )
                )
            }
        }
        val waterScore = waterPts.coerceIn(10, 100)

        // 3. PEST & DISEASE PRESSURE SCORE (Max 100)
        var pestPts = 0
        when {
            pestPressure.contains("Zero", true) || pestPressure.contains("None", true) -> {
                pestPts += 50
            }
            pestPressure.contains("Mild", true) -> {
                pestPts += 35
                tasks.add(
                    DynamicTaskTemplate(
                        title = "Install Yellow Sticky Traps for Sucking Pests",
                        description = "Place 6-8 yellow sticky cards per acre to monitor aphid and whitefly population densities.",
                        priority = "Medium",
                        priorityLabel = "SCOUTING",
                        category = "Pest",
                        whyReason = "Early trap monitoring detects pest population surges before crop damage occurs."
                    )
                )
            }
            pestPressure.contains("Moderate", true) -> {
                pestPts += 18
                issues.add("Visible insect damage observed on leaves/shoots")
                tasks.add(
                    DynamicTaskTemplate(
                        title = "Spray Neem Bio-Pesticide (10,000 PPM)",
                        description = "Spray cold-pressed Azadirachtin neem formulation (3ml/L) during early evening hours across affected canopy.",
                        priority = "High",
                        priorityLabel = "DO NOW",
                        category = "Pest",
                        whyReason = "Neem acts as an anti-feedant repellent and interrupts the pest reproductive cycle."
                    )
                )
            }
            else -> {
                pestPts += 5
                issues.add("Active severe pest infestation reported")
                tasks.add(
                    DynamicTaskTemplate(
                        title = "Urgent Targeted Pest Control Round",
                        description = "Scout affected crop rows immediately and apply recommended targeted pesticide under expert guidance.",
                        priority = "High",
                        priorityLabel = "CRITICAL",
                        category = "Pest",
                        whyReason = "Preventing catastrophic yield loss due to uncontrolled insect multiplication."
                    )
                )
            }
        }

        when {
            diseaseSigns.contains("No Disease", true) || diseaseSigns.contains("None", true) -> {
                pestPts += 50
            }
            diseaseSigns.contains("Minor", true) -> {
                pestPts += 30
                issues.add("Minor foliar discoloration or leaf tip burn")
            }
            else -> {
                pestPts += 10
                issues.add("Active fungal/bacterial blight or rust lesions detected")
                tasks.add(
                    DynamicTaskTemplate(
                        title = "Inspect & Apply Preventative Fungicide",
                        description = "Check lower leaf canopies for fungal spore patches and apply Mancozeb / Copper Oxychloride spray.",
                        priority = "High",
                        priorityLabel = "DO TODAY",
                        category = "Crop Health",
                        whyReason = "Halts fungal mycelium spread before the disease reaches upper flowering panicles."
                    )
                )
            }
        }
        val pestScore = pestPts.coerceIn(10, 100)

        // 4. VITALITY & SECURITY (Max 100)
        var vitalityPts = 0
        when {
            seedQuality.contains("Certified", true) || seedQuality.contains("Hybrid", true) -> vitalityPts += 50
            seedQuality.contains("Reputed", true) -> vitalityPts += 35
            else -> {
                vitalityPts += 18
                issues.add("Uncertified seed stock used")
            }
        }

        when {
            boundaryFencing.contains("Solar", true) || boundaryFencing.contains("Wire", true) || boundaryFencing.contains("Fenced", true) -> vitalityPts += 50
            boundaryFencing.contains("Partial", true) -> vitalityPts += 30
            else -> {
                vitalityPts += 15
                issues.add("Unfenced open perimeter exposed to stray cattle/wild boars")
                tasks.add(
                    DynamicTaskTemplate(
                        title = "Reinforce Field Boundary Bio-Fencing",
                        description = "Plant dense thorny live bio-hedging or wire boundary along perimeter to deter nocturnal wild animals.",
                        priority = "Medium",
                        priorityLabel = "SECURITY",
                        category = "Field Work",
                        whyReason = "Boundary fencing eliminates sudden midnight crop destruction by grazing livestock."
                    )
                )
            }
        }
        val vitalityScore = vitalityPts.coerceIn(10, 100)

        // OVERALL HEALTH CALCULATION (Weighted average)
        val overallScore = ((soilScore * 0.35) + (waterScore * 0.30) + (pestScore * 0.25) + (vitalityScore * 0.10)).toInt().coerceIn(10, 100)

        val status = when {
            overallScore >= 85 -> "Optimal Condition"
            overallScore >= 70 -> "Good Health"
            overallScore >= 50 -> "Needs Attention"
            else -> "Critical Health Risk"
        }

        val summary = when {
            overallScore >= 85 -> "Excellent farm setup with balanced soil fertility, reliable irrigation, and healthy vegetation."
            overallScore >= 70 -> "Strong foundational farm health with minor optimization potential in soil nutrition or scouting."
            overallScore >= 50 -> "Moderate health. Specific issues detected in pest control, water drainage, or soil testing need attention."
            else -> "Farm health is critically strained. Urgent intervention required for waterlogging, severe pests, or soil rehabilitation."
        }

        // Add a routine inspection task if tasks list is small
        if (tasks.isEmpty()) {
            tasks.add(
                DynamicTaskTemplate(
                    title = "Daily Morning Canopy & Moisture Scouting",
                    description = "Perform a quick walk-through of $primaryCropOrAnimal to check morning dew, soil surface moisture, and leaf color.",
                    priority = "Low",
                    priorityLabel = "ROUTINE",
                    category = "Scouting",
                    whyReason = "Routine scouting ensures early detection of any subtle micro-climate stress."
                )
            )
        }

        return FarmHealthAssessment(
            overallScore = overallScore,
            status = status,
            soilScore = soilScore,
            waterScore = waterScore,
            pestScore = pestScore,
            vitalityScore = vitalityScore,
            summary = summary,
            issuesIdentified = issues,
            generatedActionTasks = tasks
        )
    }
}
