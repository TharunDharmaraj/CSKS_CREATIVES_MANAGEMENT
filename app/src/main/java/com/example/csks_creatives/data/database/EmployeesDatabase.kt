package com.example.csks_creatives.data.database

import androidx.room.*
import com.example.csks_creatives.data.utils.Converter

@Entity(tableName = "EmployeesList")
data class EmployeeItem(
    @PrimaryKey val employeeId: String,
    val employeeName: String,
    val employeePassword: String,
    val joinedTime: String,
    val tasksInProgress: List<String>,
    val tasksCompleted: List<String>,
    val numberOfTasksCompleted: String
)

@Dao
interface EmployeesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(clientItem: EmployeeItem)

    @Query("SELECT * from EmployeesList order by employeeName ASC")
    fun getAllEmployees(): List<EmployeeItem>

    @Query("SELECT * from EmployeesList WHERE employeeId = :employeeId")
    suspend fun getEmployeeById(employeeId: String): EmployeeItem?

    @Query("UPDATE EmployeesList SET numberOfTasksCompleted = :count WHERE employeeId = :employeeId")
    suspend fun updateCompletedTasksCount(employeeId: String, count: String)

    @Query("DELETE from EmployeesList")
    fun deleteAllClients()
}

@Database(entities = [EmployeeItem::class], version = 1, exportSchema = false)
@TypeConverters(Converter::class)
abstract class EmployeesDatabase : RoomDatabase() {
    abstract val employeesDao: EmployeesDao
}