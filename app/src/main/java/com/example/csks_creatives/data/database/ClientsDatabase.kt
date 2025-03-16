package com.example.csks_creatives.data.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.csks_creatives.data.utils.Converter

@Entity(tableName = "ClientsList")
data class ClientItem(
    @PrimaryKey val clientId: String,
    val clientName: String,
    var clientTasks: List<String>
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