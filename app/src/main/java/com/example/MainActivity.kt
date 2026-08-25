package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.localization.LocalAppLanguage
import com.example.data.model.*
import com.example.ui.components.KisanBottomBar
import com.example.ui.components.KisanTopAppBar
import com.example.ui.screens.*
import com.example.ui.theme.KisanAITheme
import com.example.ui.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: KisanViewModel = viewModel()
            KisanApp(viewModel = viewModel)
        }
    }
}

@Composable
fun KisanApp(viewModel: KisanViewModel) {
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val appState by viewModel.appState.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val currentSubScreen by viewModel.currentSubScreen.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()

    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val farms by viewModel.allFarms.collectAsStateWithLifecycle()
    val selectedFarmId by viewModel.selectedFarmId.collectAsStateWithLifecycle()
    val parcels by viewModel.allParcels.collectAsStateWithLifecycle()
    val crops by viewModel.allCrops.collectAsStateWithLifecycle()
    val irrigationZones by viewModel.allIrrigationZones.collectAsStateWithLifecycle()
    val livestockList by viewModel.allLivestock.collectAsStateWithLifecycle()
    val poultryBatches by viewModel.allPoultryBatches.collectAsStateWithLifecycle()
    val marketPrices by viewModel.allMarketPrices.collectAsStateWithLifecycle()
    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val chatMessages by viewModel.allChatMessages.collectAsStateWithLifecycle()
    val notifications by viewModel.allNotifications.collectAsStateWithLifecycle()
    val unreadNotificationCount by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()
    val isThinking by viewModel.isAgentThinking.collectAsStateWithLifecycle()

    val agriProducts by viewModel.allAgriProducts.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val agriShops by viewModel.allAgriShops.collectAsStateWithLifecycle()
    val cropListings by viewModel.allCropListings.collectAsStateWithLifecycle()
    val buyerBids by viewModel.allBuyerBids.collectAsStateWithLifecycle()
    val physicalMandis by viewModel.allPhysicalMandis.collectAsStateWithLifecycle()
    val soilPackages by viewModel.allSoilTestingPackages.collectAsStateWithLifecycle()
    val soilHealthRecords by viewModel.allSoilHealthRecords.collectAsStateWithLifecycle()
    val cropTasks by viewModel.allCropTasks.collectAsStateWithLifecycle()

    val selectedAnimalId by viewModel.selectedAnimalId.collectAsStateWithLifecycle()
    val selectedAnimal = livestockList.find { it.id == selectedAnimalId } ?: livestockList.firstOrNull()
    val selectedParcel = parcels.find { it.farmId == selectedFarmId } ?: parcels.firstOrNull()

    val onboardingStep by viewModel.onboardingStep.collectAsStateWithLifecycle()
    val draftName by viewModel.draftName.collectAsStateWithLifecycle()
    val draftMobile by viewModel.draftMobile.collectAsStateWithLifecycle()
    val draftFarmerId by viewModel.draftFarmerId.collectAsStateWithLifecycle()
    val draftAadhaar by viewModel.draftAadhaar.collectAsStateWithLifecycle()
    val isOtpSent by viewModel.isOtpSent.collectAsStateWithLifecycle()
    val draftOtp by viewModel.draftOtp.collectAsStateWithLifecycle()
    val isOtpVerified by viewModel.isOtpVerified.collectAsStateWithLifecycle()
    val otpHelperMessage by viewModel.otpHelperMessage.collectAsStateWithLifecycle()
    val draftAge by viewModel.draftAge.collectAsStateWithLifecycle()
    val draftGender by viewModel.draftGender.collectAsStateWithLifecycle()
    val draftFarmingType by viewModel.draftFarmingType.collectAsStateWithLifecycle()
    val draftHouseNo by viewModel.draftHouseNo.collectAsStateWithLifecycle()
    val draftLandmark by viewModel.draftLandmark.collectAsStateWithLifecycle()
    val draftState by viewModel.draftState.collectAsStateWithLifecycle()
    val draftDistrict by viewModel.draftDistrict.collectAsStateWithLifecycle()
    val draftVillage by viewModel.draftVillage.collectAsStateWithLifecycle()
    val draftPinCode by viewModel.draftPinCode.collectAsStateWithLifecycle()
    val draftLatitude by viewModel.draftLatitude.collectAsStateWithLifecycle()
    val draftLongitude by viewModel.draftLongitude.collectAsStateWithLifecycle()
    val draftFarmsList by viewModel.draftFarmsList.collectAsStateWithLifecycle()
    val draftBiggestProblem by viewModel.draftBiggestProblem.collectAsStateWithLifecycle()
    val draftFinancialLossCause by viewModel.draftFinancialLossCause.collectAsStateWithLifecycle()
    val draftAiMonitorPriorities by viewModel.draftAiMonitorPriorities.collectAsStateWithLifecycle()

    val isDoctorAnalyzing by viewModel.isDoctorAnalyzing.collectAsStateWithLifecycle()
    val doctorDiagnosis by viewModel.doctorDiagnosis.collectAsStateWithLifecycle()
    val doctorPathologyResult by viewModel.doctorPathologyResult.collectAsStateWithLifecycle()

    KisanAITheme(darkTheme = isDarkTheme) {
        CompositionLocalProvider(LocalAppLanguage provides currentLanguage) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
            when (appState) {
                AppNavState.SPLASH, AppNavState.LANGUAGE_SELECT -> {
                    LanguageSelectScreen(
                        selectedLanguage = currentLanguage,
                        onLanguageSelected = { viewModel.setLanguage(it) },
                        onContinue = { viewModel.setAppState(AppNavState.AUTH_LANDING) }
                    )
                }

                AppNavState.AUTH_LANDING -> {
                    AuthLandingScreen(
                        currentLanguage = currentLanguage,
                        isDarkTheme = isDarkTheme,
                        onLanguageChange = { viewModel.setLanguage(it) },
                        onThemeToggle = { viewModel.toggleTheme() },
                        onCreateAccount = {
                            viewModel.setOnboardingStep(1)
                            viewModel.setAppState(AppNavState.ONBOARDING_WIZARD)
                        },
                        onLogin = {
                            viewModel.setAppState(AppNavState.MAIN_APP)
                        }
                    )
                }

                AppNavState.ONBOARDING_WIZARD -> {
                    OnboardingWizardScreen(
                        currentStep = onboardingStep,
                        name = draftName,
                        onNameChange = { viewModel.draftName.value = it },
                        mobile = draftMobile,
                        onMobileChange = { viewModel.draftMobile.value = it },
                        farmerId = draftFarmerId,
                        onFarmerIdChange = { viewModel.draftFarmerId.value = it },
                        aadhaar = draftAadhaar,
                        onAadhaarChange = { viewModel.draftAadhaar.value = it },
                        isOtpSent = isOtpSent,
                        onSendOtp = { viewModel.sendOtp() },
                        otpCode = draftOtp,
                        onOtpCodeChange = { viewModel.draftOtp.value = it },
                        isOtpVerified = isOtpVerified,
                        onVerifyOtp = { viewModel.verifyOtp(it) },
                        otpHelperMessage = otpHelperMessage,
                        age = draftAge,
                        onAgeChange = { viewModel.draftAge.value = it },
                        gender = draftGender,
                        onGenderChange = { viewModel.draftGender.value = it },
                        farmingType = draftFarmingType,
                        onFarmingTypeChange = { viewModel.draftFarmingType.value = it },
                        houseNo = draftHouseNo,
                        onHouseNoChange = { viewModel.draftHouseNo.value = it },
                        landmark = draftLandmark,
                        onLandmarkChange = { viewModel.draftLandmark.value = it },
                        state = draftState,
                        onStateChange = { viewModel.draftState.value = it },
                        district = draftDistrict,
                        onDistrictChange = { viewModel.draftDistrict.value = it },
                        village = draftVillage,
                        onVillageChange = { viewModel.draftVillage.value = it },
                        pinCode = draftPinCode,
                        onPinCodeChange = { viewModel.draftPinCode.value = it },
                        latitude = draftLatitude,
                        longitude = draftLongitude,
                        onLocationPicked = { pState, pDist, pVill, pPin, lat, lng ->
                            viewModel.draftState.value = pState
                            viewModel.draftDistrict.value = pDist
                            viewModel.draftVillage.value = pVill
                            viewModel.draftPinCode.value = pPin
                            viewModel.draftLatitude.value = lat
                            viewModel.draftLongitude.value = lng
                        },
                        farmsList = draftFarmsList,
                        onAddFarmDraft = { type -> viewModel.addFarmDraft(type) },
                        onRemoveFarmDraft = { id -> viewModel.removeFarmDraft(id) },
                        onUpdateFarmDraft = { updated -> viewModel.updateFarmDraft(updated) },
                        biggestProblem = draftBiggestProblem,
                        onBiggestProblemChange = { viewModel.draftBiggestProblem.value = it },
                        financialLossCause = draftFinancialLossCause,
                        onFinancialLossCauseChange = { viewModel.draftFinancialLossCause.value = it },
                        aiPriorities = draftAiMonitorPriorities,
                        onToggleAiPriority = { viewModel.toggleAiPriority(it) },
                        onNextStep = { viewModel.setOnboardingStep(onboardingStep + 1) },
                        onPrevStep = {
                            if (onboardingStep > 1) viewModel.setOnboardingStep(onboardingStep - 1)
                            else viewModel.setAppState(AppNavState.AUTH_LANDING)
                        },
                        onFinish = { viewModel.completeOnboarding() }
                    )
                }

                AppNavState.MAIN_APP -> {
                    // Check SubScreens
                    when (currentSubScreen) {
                        SubScreen.SOIL_REPORT -> {
                            SoilReportScreen(
                                parcel = selectedParcel,
                                onBack = { viewModel.navigateBack() }
                            )
                        }
                        SubScreen.IRRIGATION_DETAIL -> {
                            IrrigationScreen(
                                zones = irrigationZones,
                                onToggleZone = { viewModel.toggleIrrigationZone(it) },
                                onOpenSchedule = { viewModel.navigateToSubScreen(SubScreen.SET_SCHEDULE) },
                                onBack = { viewModel.navigateBack() }
                            )
                        }
                        SubScreen.SET_SCHEDULE -> {
                            SetScheduleScreen(
                                onSaveSchedule = { startTime, duration, days, smart ->
                                    val firstZoneId = irrigationZones.firstOrNull()?.id ?: "zone_1"
                                    viewModel.updateIrrigationSchedule(firstZoneId, startTime, duration, days, smart)
                                },
                                onClose = { viewModel.navigateBack() }
                            )
                        }
                        SubScreen.LIVESTOCK_LIST -> {
                            LivestockScreen(
                                cropsList = crops,
                                livestockList = livestockList,
                                poultryBatches = poultryBatches,
                                onSelectAnimal = { viewModel.selectAnimal(it) },
                                onAddAnimalClick = { viewModel.navigateToSubScreen(SubScreen.ADD_ANIMAL) },
                                onAddCropClick = { name, variety, stage ->
                                    viewModel.addNewCrop(name, variety, stage)
                                },
                                onBack = { viewModel.navigateBack() }
                            )
                        }
                        SubScreen.ANIMAL_DETAIL -> {
                            AnimalDetailScreen(
                                animal = selectedAnimal,
                                onLogYield = { yield ->
                                    selectedAnimal?.let { viewModel.logMilkYield(it.id, yield) }
                                },
                                onBack = { viewModel.navigateBack() }
                            )
                        }
                        SubScreen.ADD_ANIMAL -> {
                            AddAnimalScreen(
                                onSaveAnimal = { name, type, breed, age, tag, hasBle, bleName ->
                                    viewModel.addNewAnimal(name, type, breed, age, tag, hasBle, bleName)
                                },
                                onBack = { viewModel.navigateBack() }
                            )
                        }
                        SubScreen.FINANCE_DASHBOARD -> {
                            FinanceScreen(
                                transactions = transactions,
                                onAddTransaction = { title, category, amount, isIncome ->
                                    viewModel.addFinancialTransaction(title, category, amount, isIncome)
                                },
                                onDeleteTransaction = { viewModel.deleteFinancialTransaction(it) },
                                onOpenSoilReport = { viewModel.navigateToSubScreen(SubScreen.SOIL_REPORT) },
                                onBack = { viewModel.navigateBack() }
                            )
                        }
                        SubScreen.CROP_DOCTOR -> {
                            CropDoctorScreen(
                                isAnalyzing = isDoctorAnalyzing,
                                diagnosisResult = doctorDiagnosis,
                                pathologyResult = doctorPathologyResult,
                                onScanPhoto = { bitmap, cropName -> viewModel.runCropDoctorScan(bitmap, cropName) },
                                onBack = { viewModel.navigateBack() }
                            )
                        }
                        SubScreen.TASKS_LIST -> {
                            FarmActionPlanScreen(
                                farms = farms,
                                tasks = tasks,
                                onToggleTask = { viewModel.toggleTaskComplete(it) },
                                onDeleteTask = { viewModel.deleteTask(it) },
                                onAddTask = { farmId, title, desc, cat, prio, area, why ->
                                    viewModel.addFarmTask(farmId, title, desc, cat, prio, area, why)
                                },
                                onNavigateToAddFarm = {
                                    viewModel.setTab(MainTab.FARMS)
                                    viewModel.navigateBack()
                                },
                                onBack = { viewModel.navigateBack() }
                            )
                        }
                        SubScreen.NOTIFICATIONS -> {
                            NotificationsScreen(
                                notifications = notifications,
                                onMarkNotificationRead = { viewModel.markNotificationAsRead(it) },
                                onMarkAllNotificationsRead = { viewModel.markAllNotificationsAsRead() },
                                onDeleteNotification = { viewModel.deleteNotification(it) },
                                onBack = { viewModel.navigateBack() }
                            )
                        }
                        SubScreen.WEATHER_REPORT -> {
                            FarmWeatherScreen(
                                farms = farms,
                                selectedFarmId = selectedFarmId,
                                onSelectFarm = { viewModel.selectFarm(it) },
                                onSendAlertNotification = { farmName, title, msg ->
                                    viewModel.triggerSevereWeatherAlert(farmName, title, msg)
                                },
                                onBack = { viewModel.navigateBack() }
                            )
                        }
                        SubScreen.GOVT_SCHEMES -> {
                            GovtSchemesScreen(
                                onBack = { viewModel.navigateBack() }
                            )
                        }
                        SubScreen.CROP_GROWTH_TRACKER -> {
                            CropGrowthTrackerScreen(
                                crops = crops,
                                farms = farms,
                                onAddCropClick = { name, variety, stage, isNewCrop, daysElapsed ->
                                    viewModel.addNewCrop(name, variety, stage, isNewCrop, daysElapsed)
                                },
                                onBack = { viewModel.navigateBack() }
                            )
                        }
                        SubScreen.SETTINGS -> {
                            SettingsScreen(
                                userProfile = userProfile,
                                currentLanguage = currentLanguage,
                                isDarkTheme = isDarkTheme,
                                onLanguageChange = { viewModel.setLanguage(it) },
                                onToggleTheme = { viewModel.toggleTheme() },
                                onBack = { viewModel.navigateBack() }
                            )
                        }
                        SubScreen.USER_PROFILE -> {
                            FarmerProfileScreen(
                                userProfile = userProfile,
                                onUpdateProfile = { viewModel.updateUserProfile(it) },
                                onOpenSettings = { viewModel.navigateToSubScreen(SubScreen.SETTINGS) },
                                onBack = { viewModel.navigateBack() }
                            )
                        }
                        SubScreen.NONE -> {
                            // Main Tabs
                            Scaffold(
                                topBar = {
                                    KisanTopAppBar(
                                        title = "KisanAI",
                                        showFarmerAvatar = true,
                                        unreadNotificationCount = unreadNotificationCount,
                                        onAvatarClick = {
                                            viewModel.navigateToSubScreen(SubScreen.USER_PROFILE)
                                        },
                                        onNotificationClick = {
                                            viewModel.navigateToSubScreen(SubScreen.NOTIFICATIONS)
                                        }
                                    )
                                },
                                bottomBar = {
                                    KisanBottomBar(
                                        currentTab = currentTab,
                                        onTabSelected = { viewModel.setTab(it) }
                                    )
                                }
                            ) { paddingValues ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(paddingValues)
                                        .consumeWindowInsets(paddingValues)
                                        .imePadding()
                                ) {
                                    when (currentTab) {
                                        MainTab.HOME -> {
                                            HomeScreen(
                                                userProfile = userProfile,
                                                farms = farms,
                                                selectedFarmId = selectedFarmId,
                                                onSelectFarm = { viewModel.selectFarm(it) },
                                                parcels = parcels,
                                                crops = crops,
                                                marketPrices = marketPrices,
                                                tasks = tasks,
                                                onToggleTask = { viewModel.toggleTaskComplete(it) },
                                                onNavigateTab = { viewModel.setTab(it) },
                                                onNavigateSubScreen = { viewModel.navigateToSubScreen(it) },
                                                onAskDetails = { viewModel.setTab(MainTab.AI_AGENT) }
                                            )
                                        }
                                        MainTab.FARMS -> {
                                            FarmsScreen(
                                                farms = farms,
                                                selectedFarmId = selectedFarmId,
                                                onSelectFarm = { viewModel.selectFarm(it) },
                                                parcels = parcels,
                                                tasks = tasks,
                                                irrigationZones = irrigationZones,
                                                onToggleTask = { viewModel.toggleTaskComplete(it) },
                                                onToggleIrrigationZone = { viewModel.toggleIrrigationZone(it) },
                                                onAdvanceFarmDay = { viewModel.advanceFarmDay(it) },
                                                soilRecords = soilHealthRecords,
                                                onLogSoilHealthRecord = { ph, moisture, src, notes, date, temp, ec, batt, dev ->
                                                    viewModel.logSoilHealthRecord(
                                                        farmId = selectedFarmId,
                                                        phLevel = ph,
                                                        moisturePercent = moisture,
                                                        source = src,
                                                        notes = notes,
                                                        dateLabel = date,
                                                        temp = temp,
                                                        ec = ec,
                                                        battery = batt,
                                                        deviceName = dev
                                                    )
                                                },
                                                onDeleteSoilHealthRecord = { viewModel.deleteSoilHealthRecord(it) },
                                                cropTasks = cropTasks,
                                                onToggleCropTask = { viewModel.toggleCropTask(it) },
                                                onAddFertilizerTask = { title, desc, cat ->
                                                    viewModel.addTask(
                                                        FarmTask(
                                                            id = "ft_${System.currentTimeMillis()}",
                                                            farmId = selectedFarmId,
                                                            title = title,
                                                            description = desc,
                                                            priority = "High",
                                                            priorityLabel = "FERTILIZER DOSE",
                                                            targetArea = "Active Plot",
                                                            category = cat,
                                                            whyReason = "Precision NPK nutrition for optimal vegetative & grain growth",
                                                            isCompleted = false
                                                        )
                                                    )
                                                },
                                                onAddCropTask = { viewModel.addCropTask(it) },
                                                onAddNewFarmDetails = { name, type, area, unit, primaryCrop, ownership, houseNo, landmark, village, district, state, pinCode, soilType, soilTestingStatus, fertilizerPractice, drainageCondition, waterSource, pestPressure, diseaseSigns, seedQuality, boundaryFencing ->
                                                    viewModel.addNewActiveFarm(
                                                        type = type,
                                                        name = name,
                                                        area = area,
                                                        unit = unit,
                                                        primaryCropOrAnimal = primaryCrop,
                                                        ownership = ownership,
                                                        houseNo = houseNo,
                                                        landmark = landmark,
                                                        village = village,
                                                        district = district,
                                                        state = state,
                                                        pinCode = pinCode,
                                                        soilType = soilType,
                                                        soilTestingStatus = soilTestingStatus,
                                                        fertilizerPractice = fertilizerPractice,
                                                        drainageCondition = drainageCondition,
                                                        waterSource = waterSource,
                                                        pestPressure = pestPressure,
                                                        diseaseSigns = diseaseSigns,
                                                        seedQuality = seedQuality,
                                                        boundaryFencing = boundaryFencing
                                                    )
                                                }
                                            )
                                        }

                                        MainTab.AI_AGENT -> {
                                            AIAgentScreen(
                                                messages = chatMessages,
                                                isThinking = isThinking,
                                                onSendMessage = { text, image ->
                                                    viewModel.sendChatMessage(text, image)
                                                },
                                                onOpenCropDoctor = { viewModel.navigateToSubScreen(SubScreen.CROP_DOCTOR) }
                                            )
                                        }
                                        MainTab.MARKET -> {
                                            MarketScreen(
                                                prices = marketPrices,
                                                userProfile = userProfile,
                                                parcels = parcels,
                                                products = agriProducts,
                                                cartItems = cartItems,
                                                shops = agriShops,
                                                cropListings = cropListings,
                                                buyerBids = buyerBids,
                                                physicalMandis = physicalMandis,
                                                soilPackages = soilPackages,
                                                onAddToCart = { id, qty -> viewModel.addToCart(id, qty) },
                                                onUpdateCartQty = { id, qty -> viewModel.updateCartQty(id, qty) },
                                                onPlaceOrder = { product, qty, pack, address, payment ->
                                                    viewModel.placeAgriOrder(product, qty, pack, address, payment)
                                                },
                                                onListCropForSale = { crop, variety, qty, price, moisture, loc ->
                                                    viewModel.listCropForSale(crop, variety, qty, price, moisture, loc)
                                                },
                                                onAcceptBid = { listing, bid ->
                                                    viewModel.acceptBuyerBid(listing, bid)
                                                },
                                                onBookSoilTest = { pkg, parcel, date, slot, address, notes ->
                                                    viewModel.bookSoilTest(pkg, parcel, date, slot, address, notes)
                                                },
                                                onOpenNotifications = {
                                                    viewModel.navigateToSubScreen(SubScreen.TASKS_LIST)
                                                },
                                                onSelectPrice = { /* Mandi detail modal */ }
                                            )
                                        }
                                        MainTab.MORE -> {
                                            MoreHubScreen(
                                                onNavigateSubScreen = { viewModel.navigateToSubScreen(it) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}
