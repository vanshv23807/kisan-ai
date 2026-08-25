package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface KisanDao {

    // User Profile
    @Query("SELECT * FROM user_profiles WHERE id = 'primary_user' LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles WHERE id = 'primary_user' LIMIT 1")
    suspend fun getUserProfileOnce(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(userProfile: UserProfile)

    // Farms
    @Query("SELECT * FROM farms")
    fun getAllFarms(): Flow<List<Farm>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarm(farm: Farm)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarms(farms: List<Farm>)

    @Query("DELETE FROM farms")
    suspend fun deleteAllFarms()

    // Land Parcels
    @Query("SELECT * FROM land_parcels")
    fun getAllLandParcels(): Flow<List<LandParcel>>

    @Query("SELECT * FROM land_parcels WHERE id = :parcelId LIMIT 1")
    fun getLandParcelById(parcelId: String): Flow<LandParcel?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLandParcels(parcels: List<LandParcel>)

    @Query("DELETE FROM land_parcels")
    suspend fun deleteAllLandParcels()

    @Update
    suspend fun updateLandParcel(parcel: LandParcel)

    // Crops
    @Query("SELECT * FROM crops")
    fun getAllCrops(): Flow<List<CropRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrops(crops: List<CropRecord>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrop(crop: CropRecord)

    @Query("DELETE FROM crops")
    suspend fun deleteAllCrops()

    // Irrigation
    @Query("SELECT * FROM irrigation_zones")
    fun getAllIrrigationZones(): Flow<List<IrrigationZone>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIrrigationZones(zones: List<IrrigationZone>)

    @Update
    suspend fun updateIrrigationZone(zone: IrrigationZone)

    // Livestock
    @Query("SELECT * FROM livestock")
    fun getAllLivestock(): Flow<List<Livestock>>

    @Query("SELECT * FROM livestock WHERE id = :id LIMIT 1")
    fun getLivestockById(id: String): Flow<Livestock?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLivestock(livestockList: List<Livestock>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingleAnimal(animal: Livestock)

    @Update
    suspend fun updateLivestock(animal: Livestock)

    @Query("DELETE FROM livestock")
    suspend fun deleteAllLivestock()

    // Poultry
    @Query("SELECT * FROM poultry_batches")
    fun getAllPoultryBatches(): Flow<List<PoultryBatch>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoultryBatches(batches: List<PoultryBatch>)

    @Query("DELETE FROM poultry_batches")
    suspend fun deleteAllPoultryBatches()

    // Market Prices
    @Query("SELECT * FROM market_prices")
    fun getAllMarketPrices(): Flow<List<MarketPrice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketPrices(prices: List<MarketPrice>)

    // Financials
    @Query("SELECT * FROM financial_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<FinancialTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: FinancialTransaction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<FinancialTransaction>)

    @Delete
    suspend fun deleteTransaction(transaction: FinancialTransaction)

    @Query("DELETE FROM financial_transactions")
    suspend fun deleteAllTransactions()

    // Farm Tasks
    @Query("SELECT * FROM farm_tasks")
    fun getAllTasks(): Flow<List<FarmTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<FarmTask>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: FarmTask)

    @Update
    suspend fun updateTask(task: FarmTask)

    @Delete
    suspend fun deleteTask(task: FarmTask)

    // Chat
    @Query("SELECT * FROM chat_messages ORDER BY id ASC")
    fun getAllChatMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessage)

    // App Notifications
    @Query("SELECT * FROM app_notifications ORDER BY dateMillis DESC")
    fun getAllNotifications(): Flow<List<AppNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotification)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<AppNotification>)

    @Query("UPDATE app_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationRead(id: String)

    @Query("UPDATE app_notifications SET isRead = 1")
    suspend fun markAllNotificationsRead()

    @Query("DELETE FROM app_notifications WHERE id = :id")
    suspend fun deleteNotification(id: String)

    // Soil Health Records
    @Query("SELECT * FROM soil_health_records ORDER BY timestamp DESC")
    fun getAllSoilRecords(): Flow<List<SoilHealthRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSoilRecord(record: SoilHealthRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSoilRecords(records: List<SoilHealthRecord>)

    @Query("DELETE FROM soil_health_records WHERE id = :id")
    suspend fun deleteSoilRecord(id: String)

    // Crop Tasks (Seasonal Planting Schedules & Irrigation Reminders)
    @Query("SELECT * FROM crop_tasks ORDER BY dueTimestamp ASC")
    fun getAllCropTasks(): Flow<List<CropTask>>

    @Query("SELECT * FROM crop_tasks WHERE farmId = :farmId ORDER BY daysFromSowing ASC, dueTimestamp ASC")
    fun getCropTasksByFarm(farmId: String): Flow<List<CropTask>>

    @Query("SELECT * FROM crop_tasks WHERE cropName = :cropName ORDER BY daysFromSowing ASC")
    fun getCropTasksByCrop(cropName: String): Flow<List<CropTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCropTask(task: CropTask)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCropTasks(tasks: List<CropTask>)

    @Update
    suspend fun updateCropTask(task: CropTask)

    @Delete
    suspend fun deleteCropTask(task: CropTask)

    @Query("UPDATE crop_tasks SET isCompleted = :completed WHERE id = :id")
    suspend fun setCropTaskCompleted(id: String, completed: Boolean)

    @Query("DELETE FROM crop_tasks WHERE farmId = :farmId")
    suspend fun deleteCropTasksByFarm(farmId: String)
}
