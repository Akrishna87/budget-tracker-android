package com.akrishna87.budgettracker.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Database(
    entities = [
        CategoryEntity::class,
        TransactionEntity::class,
        RecurringBillEntity::class,
        DebtAccountEntity::class,
        SavingsGoalEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun recurringBillDao(): RecurringBillDao
    abstract fun debtAccountDao(): DebtAccountDao
    abstract fun savingsGoalDao(): SavingsGoalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS recurring_bills (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        amount REAL NOT NULL,
                        categoryId TEXT,
                        dueDayOfMonth INTEGER NOT NULL,
                        reminderDaysBefore INTEGER NOT NULL,
                        isActive INTEGER NOT NULL,
                        lastPaidMonth TEXT,
                        lastNotifiedMonth TEXT,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS debt_accounts (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        type TEXT NOT NULL,
                        originalAmount REAL NOT NULL,
                        outstandingAmount REAL NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS savings_goals (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        emoji TEXT NOT NULL,
                        targetAmount REAL NOT NULL,
                        savedAmount REAL NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

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
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
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
