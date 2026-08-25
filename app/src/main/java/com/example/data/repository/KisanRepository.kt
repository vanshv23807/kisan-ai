package com.example.data.repository

import com.example.data.local.KisanDao
import com.example.data.local.populateInitialData
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class KisanRepository(private val dao: KisanDao) {

    val userProfile: Flow<UserProfile?> = dao.getUserProfile()
    val allFarms: Flow<List<Farm>> = dao.getAllFarms()
    val allLandParcels: Flow<List<LandParcel>> = dao.getAllLandParcels()
    val allCrops: Flow<List<CropRecord>> = dao.getAllCrops()
    val allIrrigationZones: Flow<List<IrrigationZone>> = dao.getAllIrrigationZones()
    val allLivestock: Flow<List<Livestock>> = dao.getAllLivestock()
    val allPoultryBatches: Flow<List<PoultryBatch>> = dao.getAllPoultryBatches()
    val allMarketPrices: Flow<List<MarketPrice>> = dao.getAllMarketPrices()
    val allTransactions: Flow<List<FinancialTransaction>> = dao.getAllTransactions()
    val allTasks: Flow<List<FarmTask>> = dao.getAllTasks()
    val allChatMessages: Flow<List<ChatMessage>> = dao.getAllChatMessages()
    val allNotifications: Flow<List<AppNotification>> = dao.getAllNotifications()
    val allSoilRecords: Flow<List<SoilHealthRecord>> = dao.getAllSoilRecords()
    val allCropTasks: Flow<List<CropTask>> = dao.getAllCropTasks()

    fun getCropTasksByFarm(farmId: String): Flow<List<CropTask>> = dao.getCropTasksByFarm(farmId)
    fun getCropTasksByCrop(cropName: String): Flow<List<CropTask>> = dao.getCropTasksByCrop(cropName)

    suspend fun checkAndPrepopulateInitialData() {
        val existing = dao.getUserProfileOnce()
        if (existing == null) {
            populateInitialData(dao)
        }
    }

    suspend fun getUserProfileOnce() = dao.getUserProfileOnce()
    suspend fun saveUserProfile(profile: UserProfile) = dao.insertUserProfile(profile)

    suspend fun addFarm(farm: Farm) = dao.insertFarm(farm)
    suspend fun saveFarms(farms: List<Farm>) = dao.insertFarms(farms)
    suspend fun clearFarmsAndParcels() {
        dao.deleteAllFarms()
        dao.deleteAllLandParcels()
        dao.deleteAllCrops()
        dao.deleteAllLivestock()
        dao.deleteAllPoultryBatches()
        dao.deleteAllTransactions()
    }
    suspend fun updateParcel(parcel: LandParcel) = dao.updateLandParcel(parcel)
    suspend fun addIrrigationZone(zone: IrrigationZone) = dao.insertIrrigationZones(listOf(zone))
    suspend fun updateIrrigationZone(zone: IrrigationZone) = dao.updateIrrigationZone(zone)

    suspend fun addCrop(crop: CropRecord) = dao.insertCrop(crop)
    suspend fun addAnimal(animal: Livestock) = dao.insertSingleAnimal(animal)
    suspend fun updateAnimal(animal: Livestock) = dao.updateLivestock(animal)

    suspend fun addTransaction(transaction: FinancialTransaction) = dao.insertTransaction(transaction)
    suspend fun deleteTransaction(transaction: FinancialTransaction) = dao.deleteTransaction(transaction)

    suspend fun addTask(task: FarmTask) = dao.insertTask(task)
    suspend fun updateTask(task: FarmTask) = dao.updateTask(task)
    suspend fun deleteTask(task: FarmTask) = dao.deleteTask(task)

    suspend fun addChatMessage(message: ChatMessage) = dao.insertChatMessage(message)

    suspend fun addNotification(notification: AppNotification) = dao.insertNotification(notification)
    suspend fun markNotificationRead(id: String) = dao.markNotificationRead(id)
    suspend fun markAllNotificationsRead() = dao.markAllNotificationsRead()
    suspend fun deleteNotification(id: String) = dao.deleteNotification(id)

    // Soil Health
    suspend fun addSoilRecord(record: SoilHealthRecord) = dao.insertSoilRecord(record)
    suspend fun deleteSoilRecord(id: String) = dao.deleteSoilRecord(id)

    // Crop Tasks & Seasonal Schedules
    suspend fun addCropTask(task: CropTask) = dao.insertCropTask(task)
    suspend fun addCropTasks(tasks: List<CropTask>) = dao.insertCropTasks(tasks)
    suspend fun updateCropTask(task: CropTask) = dao.updateCropTask(task)
    suspend fun deleteCropTask(task: CropTask) = dao.deleteCropTask(task)
    suspend fun setCropTaskCompleted(id: String, completed: Boolean) = dao.setCropTaskCompleted(id, completed)
    suspend fun deleteCropTasksByFarm(farmId: String) = dao.deleteCropTasksByFarm(farmId)
}
