package com.akrishna87.budgettracker.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Database(
    entities = [
        CategoryEntity::class,
        TransactionEntity::class,
        RecurringExpenseEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun recurringExpenseDao(): RecurringExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: build(context).also { INSTANCE = it }
            }
        }

        private fun build(context: Context): AppDatabase {
            val seedScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "budget-tracker.db"
            )
                // This rebuild cuts over to a simpler schema (debts/goals/old
                // bill-reminder fields dropped); a destructive fallback is fine
                // since this is an unpublished debug app with no real installs
                // to preserve data for.
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // onCreate only fires on first real table access, which happens
                        // after getInstance() has already assigned INSTANCE below, so
                        // this coroutine (scheduled to run later, not inline) always
                        // sees a non-null INSTANCE by the time it actually executes.
                        seedScope.launch {
                            INSTANCE?.categoryDao()?.insertAll(DefaultCategories.seed)
                        }
                    }
                })
                .build()
        }
    }
}
