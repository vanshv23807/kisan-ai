package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.CropTask
import com.example.data.model.FertilizerDosageRecommendation
import com.example.data.repository.KisanRepository
import com.example.data.util.FertilizerCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel to manage seasonal planting schedules and irrigation reminders for different crops.
 */
class CropTaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: KisanRepository

    // Filter states
    val selectedCropFilter = MutableStateFlow("All Crops")
    val selectedSeasonFilter = MutableStateFlow("All Seasons")
    val selectedTab = MutableStateFlow(0) // 0: All Schedules, 1: Irrigation Reminders, 2: Planting Milestones, 3: Completed

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = KisanRepository(db.kisanDao())
    }

    val allCropTasks: StateFlow<List<CropTask>> = repository.allCropTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredCropTasks: StateFlow<List<CropTask>> = combine(
        allCropTasks,
        selectedCropFilter,
        selectedSeasonFilter,
        selectedTab
    ) { tasks, crop, season, tab ->
        tasks.filter { task ->
            val matchesCrop = (crop == "All Crops") || task.cropName.equals(crop, ignoreCase = true)
            val matchesSeason = (season == "All Seasons") || task.season.equals(season, ignoreCase = true)
            val matchesTab = when (tab) {
                1 -> task.isIrrigationReminder && !task.isCompleted
                2 -> task.isPlantingSchedule && !task.isCompleted
                3 -> task.isCompleted
                else -> true
            }
            matchesCrop && matchesSeason && matchesTab
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingIrrigationRemindersCount: StateFlow<Int> = allCropTasks.map { tasks ->
        tasks.count { it.isIrrigationReminder && !it.isCompleted }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val upcomingPlantingMilestonesCount: StateFlow<Int> = allCropTasks.map { tasks ->
        tasks.count { it.isPlantingSchedule && !it.isCompleted }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setCropFilter(crop: String) {
        selectedCropFilter.value = crop
    }

    fun setSeasonFilter(season: String) {
        selectedSeasonFilter.value = season
    }

    fun setSelectedTab(tab: Int) {
        selectedTab.value = tab
    }

    fun toggleTaskCompletion(task: CropTask) {
        viewModelScope.launch {
            repository.setCropTaskCompleted(task.id, !task.isCompleted)
        }
    }

    fun addCustomCropTask(
        farmId: String = "farm_1",
        cropName: String,
        season: String,
        stage: String,
        taskTitle: String,
        taskDescription: String,
        isIrrigationReminder: Boolean,
        isPlantingSchedule: Boolean,
        recommendedDateDisplay: String,
        daysFromSowing: Int,
        irrigationDurationMinutes: Int = 45,
        waterRequirementMm: Double = 35.0,
        fertilizerRecommendation: String = "",
        npkRatio: String = "120:60:40",
        priority: String = "High"
    ) {
        viewModelScope.launch {
            val task = CropTask(
                id = "ct_${System.currentTimeMillis()}",
                farmId = farmId,
                cropName = cropName,
                season = season,
                stage = stage,
                taskTitle = taskTitle,
                taskDescription = taskDescription,
                isIrrigationReminder = isIrrigationReminder,
                isPlantingSchedule = isPlantingSchedule,
                recommendedDateDisplay = recommendedDateDisplay,
                daysFromSowing = daysFromSowing,
                irrigationDurationMinutes = irrigationDurationMinutes,
                waterRequirementMm = waterRequirementMm,
                fertilizerRecommendation = fertilizerRecommendation,
                npkRatio = npkRatio,
                priority = priority,
                isCompleted = false
            )
            repository.addCropTask(task)
        }
    }

    fun deleteCropTask(task: CropTask) {
        viewModelScope.launch {
            repository.deleteCropTask(task)
        }
    }

    /**
     * Auto-generate full seasonal crop schedule and critical irrigation reminders for a crop.
     */
    fun generateSeasonalSchedule(farmId: String, cropName: String, season: String = "Rabi") {
        viewModelScope.launch {
            val defaultTasks = when (cropName.lowercase()) {
                "wheat" -> listOf(
                    CropTask(
                        id = "ct_gen_${System.currentTimeMillis()}_1",
                        farmId = farmId,
                        cropName = "Wheat",
                        season = "Rabi",
                        stage = "Land Preparation & Sowing",
                        taskTitle = "Seed Treatment & Basal DAP Application",
                        taskDescription = "Treat seeds with Trichoderma (4g/kg) and Azotobacter. Drill 1 bag DAP + 1/3 bag MOP per acre.",
                        isPlantingSchedule = true,
                        isIrrigationReminder = false,
                        recommendedDateDisplay = "Day 0",
                        daysFromSowing = 0,
                        fertilizerRecommendation = "DAP 50kg + MOP 20kg",
                        npkRatio = "120:60:40",
                        priority = "High"
                    ),
                    CropTask(
                        id = "ct_gen_${System.currentTimeMillis()}_2",
                        farmId = farmId,
                        cropName = "Wheat",
                        season = "Rabi",
                        stage = "Crown Root Initiation (CRI)",
                        taskTitle = "1st Critical Irrigation & Urea Top Dress",
                        taskDescription = "Water requirement: 50-60mm. Top dress with 22.5kg Urea + 5kg Zinc Sulphate per acre.",
                        isPlantingSchedule = false,
                        isIrrigationReminder = true,
                        recommendedDateDisplay = "Day 21",
                        daysFromSowing = 21,
                        irrigationDurationMinutes = 60,
                        waterRequirementMm = 55.0,
                        fertilizerRecommendation = "Urea 22.5kg + Zinc 5kg",
                        npkRatio = "120:60:40",
                        priority = "High"
                    ),
                    CropTask(
                        id = "ct_gen_${System.currentTimeMillis()}_3",
                        farmId = farmId,
                        cropName = "Wheat",
                        season = "Rabi",
                        stage = "Late Tillering / Jointing",
                        taskTitle = "2nd Irrigation & Broadleaf Weed Inspection",
                        taskDescription = "Apply second light irrigation. Spray Sulfosulfuron if Phalaris minor (Gulli Danda) is spotted.",
                        isPlantingSchedule = false,
                        isIrrigationReminder = true,
                        recommendedDateDisplay = "Day 42",
                        daysFromSowing = 42,
                        irrigationDurationMinutes = 45,
                        waterRequirementMm = 45.0,
                        fertilizerRecommendation = "Urea 22.5kg",
                        npkRatio = "120:60:40",
                        priority = "Medium"
                    ),
                    CropTask(
                        id = "ct_gen_${System.currentTimeMillis()}_4",
                        farmId = farmId,
                        cropName = "Wheat",
                        season = "Rabi",
                        stage = "Booting & Heading",
                        taskTitle = "3rd Irrigation & Foliar NPK 19:19:19",
                        taskDescription = "Critical flowering moisture window. Spray 1% Nano Urea or NPK 19:19:19 for higher grain count.",
                        isPlantingSchedule = false,
                        isIrrigationReminder = true,
                        recommendedDateDisplay = "Day 65",
                        daysFromSowing = 65,
                        irrigationDurationMinutes = 50,
                        waterRequirementMm = 50.0,
                        fertilizerRecommendation = "Foliar 19:19:19 (1kg/acre)",
                        npkRatio = "120:60:40",
                        priority = "High"
                    ),
                    CropTask(
                        id = "ct_gen_${System.currentTimeMillis()}_5",
                        farmId = farmId,
                        cropName = "Wheat",
                        season = "Rabi",
                        stage = "Grain Dough / Ripening",
                        taskTitle = "4th Light Irrigation (Avoid Strong Winds)",
                        taskDescription = "Final light irrigation to ensure plump grains. Stop irrigation 15 days before combine harvest.",
                        isPlantingSchedule = false,
                        isIrrigationReminder = true,
                        recommendedDateDisplay = "Day 88",
                        daysFromSowing = 88,
                        irrigationDurationMinutes = 35,
                        waterRequirementMm = 35.0,
                        fertilizerRecommendation = "00:00:50 Potash foliar spray",
                        npkRatio = "120:60:40",
                        priority = "Medium"
                    )
                )
                "mustard" -> listOf(
                    CropTask(
                        id = "ct_gen_${System.currentTimeMillis()}_1",
                        farmId = farmId,
                        cropName = "Mustard",
                        season = "Rabi",
                        stage = "Sowing & Basal Sulphur",
                        taskTitle = "Basal Fertilizer & Seed Furrowing",
                        taskDescription = "Apply 1 bag SSP (or 35kg DAP) + 15kg MOP + 10kg Bentonite Sulphur per acre at sowing.",
                        isPlantingSchedule = true,
                        isIrrigationReminder = false,
                        recommendedDateDisplay = "Day 0",
                        daysFromSowing = 0,
                        fertilizerRecommendation = "SSP 50kg + MOP 15kg + Sulphur 10kg",
                        npkRatio = "60:30:30",
                        priority = "High"
                    ),
                    CropTask(
                        id = "ct_gen_${System.currentTimeMillis()}_2",
                        farmId = farmId,
                        cropName = "Mustard",
                        season = "Rabi",
                        stage = "Rosette & Pre-Flowering",
                        taskTitle = "1st Irrigation & Urea Top Dress",
                        taskDescription = "Light irrigation at 30 days after sowing. Top dress with 25kg Urea per acre.",
                        isPlantingSchedule = false,
                        isIrrigationReminder = true,
                        recommendedDateDisplay = "Day 30",
                        daysFromSowing = 30,
                        irrigationDurationMinutes = 40,
                        waterRequirementMm = 30.0,
                        fertilizerRecommendation = "Urea 25kg / acre",
                        npkRatio = "60:30:30",
                        priority = "High"
                    ),
                    CropTask(
                        id = "ct_gen_${System.currentTimeMillis()}_3",
                        farmId = farmId,
                        cropName = "Mustard",
                        season = "Rabi",
                        stage = "Siliqua (Pod) Formation",
                        taskTitle = "2nd Irrigation at Pod Filling",
                        taskDescription = "Second critical irrigation during pod development (55-60 DAS) to enhance seed oil percentage.",
                        isPlantingSchedule = false,
                        isIrrigationReminder = true,
                        recommendedDateDisplay = "Day 58",
                        daysFromSowing = 58,
                        irrigationDurationMinutes = 40,
                        waterRequirementMm = 35.0,
                        fertilizerRecommendation = "Foliar 13:00:45 (1kg/acre)",
                        npkRatio = "60:30:30",
                        priority = "High"
                    )
                )
                else -> listOf(
                    CropTask(
                        id = "ct_gen_${System.currentTimeMillis()}_1",
                        farmId = farmId,
                        cropName = cropName,
                        season = season,
                        stage = "Land Preparation & Sowing",
                        taskTitle = "Seed Sowing & Basal Nutrition for $cropName",
                        taskDescription = "Prepare well-drained seedbed, apply balanced basal NPK nutrition and organic compost.",
                        isPlantingSchedule = true,
                        isIrrigationReminder = false,
                        recommendedDateDisplay = "Day 0",
                        daysFromSowing = 0,
                        fertilizerRecommendation = "Balanced NPK Basal",
                        npkRatio = "100:50:50",
                        priority = "High"
                    ),
                    CropTask(
                        id = "ct_gen_${System.currentTimeMillis()}_2",
                        farmId = farmId,
                        cropName = cropName,
                        season = season,
                        stage = "Vegetative Growth",
                        taskTitle = "1st Scheduled Irrigation for $cropName",
                        taskDescription = "Moisturize root zone and apply 1st nitrogen top dressing.",
                        isPlantingSchedule = false,
                        isIrrigationReminder = true,
                        recommendedDateDisplay = "Day 25",
                        daysFromSowing = 25,
                        irrigationDurationMinutes = 45,
                        waterRequirementMm = 40.0,
                        fertilizerRecommendation = "Top dressing Nitrogen",
                        npkRatio = "100:50:50",
                        priority = "High"
                    ),
                    CropTask(
                        id = "ct_gen_${System.currentTimeMillis()}_3",
                        farmId = farmId,
                        cropName = cropName,
                        season = season,
                        stage = "Flowering & Fruit/Grain Setting",
                        taskTitle = "2nd Irrigation & Micronutrient Foliar Spray",
                        taskDescription = "Maintain steady moisture during flowering window to prevent bloom drop.",
                        isPlantingSchedule = false,
                        isIrrigationReminder = true,
                        recommendedDateDisplay = "Day 50",
                        daysFromSowing = 50,
                        irrigationDurationMinutes = 50,
                        waterRequirementMm = 45.0,
                        fertilizerRecommendation = "Foliar NPK 19:19:19",
                        npkRatio = "100:50:50",
                        priority = "High"
                    )
                )
            }

            repository.addCropTasks(defaultTasks)
        }
    }

    /**
     * Compute fertilizer dosage recommendations using the FertilizerCalculator engine.
     */
    fun calculateFertilizer(cropName: String, fieldArea: Double, areaUnit: String = "Acres"): FertilizerDosageRecommendation {
        return FertilizerCalculator.calculate(cropName, fieldArea, areaUnit)
    }
}
