package com.example.csks_creatives.data.database

import androidx.room.*
import com.example.csks_creatives.data.utils.Converter
import com.example.csks_creatives.domain.model.utills.sealed.UserRole

@Entity(tableName = "currentUser")
data class CurrentUser(
    @PrimaryKey val loginTime: String,
    val userRole: UserRole = UserRole.Employee,
    val adminName: String,
    val employeeId: String
)

@Dao
interface CurrentUserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(currentUser: CurrentUser)

    @Query("Delete from currentUser")
    suspend fun deleteCurrentUser()

    @Query("Select * from currentUser limit(1)")
    suspend fun getCurrentUser(): CurrentUser?
}

@Database(entities = [CurrentUser::class], version = 1, exportSchema = false)
@TypeConverters(Converter::class)
abstract class CurrentUserDatabase : RoomDatabase() {
    abstract val currentUserDao: CurrentUserDao
}