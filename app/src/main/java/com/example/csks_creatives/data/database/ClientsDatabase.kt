package com.example.csks_creatives.data.database

import androidx.room.*
import com.example.csks_creatives.data.utils.Converter

@Entity(tableName = "ClientsList")
data class ClientItem(
    @PrimaryKey val clientId: String,
    val clientName: String
)

@Dao
interface ClientsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(clientItem: ClientItem)

    @Query("SELECT * from ClientsList order by clientName ASC")
    fun getAllClients(): List<ClientItem>

    @Query("DELETE from ClientsList")
    fun deleteAllClients()
}

@Database(entities = [ClientItem::class], version = 1, exportSchema = false)
@TypeConverters(Converter::class)
abstract class ClientsDataBase : RoomDatabase() {
    abstract val clientsDao: ClientsDao
}