package com.realestate.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.realestate.app.data.dealassistant.QuickNote
import com.realestate.app.data.dealassistant.QuickNoteDao
import com.realestate.app.data.property.Note
import com.realestate.app.data.property.NoteDao
import com.realestate.app.data.property.TimelineDao
import com.realestate.app.data.property.TimelineEvent
import com.realestate.app.data.wallet.WalletDao
import com.realestate.app.data.wallet.WalletTransaction

@Database(
    entities = [Property::class, WalletTransaction::class, Note::class, TimelineEvent::class, QuickNote::class],
    version = 10,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun propertyDao(): PropertyDao
    abstract fun walletDao(): WalletDao
    abstract fun noteDao(): NoteDao
    abstract fun timelineDao(): TimelineDao
    abstract fun quickNoteDao(): QuickNoteDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE properties ADD COLUMN lastViewedAt INTEGER")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `wallet_transactions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `amount` INTEGER NOT NULL,
                        `type` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `referenceId` TEXT,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE properties ADD COLUMN ownerName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE properties ADD COLUMN status TEXT NOT NULL DEFAULT 'NEW'")
                db.execSQL("ALTER TABLE properties ADD COLUMN tags TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE properties ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE properties ADD COLUMN favoriteFolder TEXT")
                db.execSQL("ALTER TABLE properties ADD COLUMN lastModifiedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE properties ADD COLUMN lastSharedAt INTEGER")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `notes` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `propertyId` INTEGER NOT NULL,
                        `content` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `timeline_events` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `propertyId` INTEGER NOT NULL,
                        `type` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE properties ADD COLUMN viewCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE properties ADD COLUMN followUpAt INTEGER")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `quick_notes` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `content` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        // Adds every field the Case redesign needs (see data/Property.kt). Every existing row
        // becomes a well-formed OWNER case: caseType defaults to 'OWNER', every other new column
        // is either nullable or defaults to its "nothing set yet" value, so nothing existing is
        // reinterpreted or lost.
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE properties ADD COLUMN caseType TEXT NOT NULL DEFAULT 'OWNER'")
                db.execSQL("ALTER TABLE properties ADD COLUMN transactionType TEXT")
                db.execSQL("ALTER TABLE properties ADD COLUMN caseFlags TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE properties ADD COLUMN priority TEXT NOT NULL DEFAULT 'NORMAL'")
                db.execSQL("ALTER TABLE properties ADD COLUMN expiryType TEXT NOT NULL DEFAULT 'NO_EXPIRATION'")
                db.execSQL("ALTER TABLE properties ADD COLUMN customExpiryAt INTEGER")
                db.execSQL("ALTER TABLE properties ADD COLUMN mortgageStatus TEXT NOT NULL DEFAULT 'NONE'")
                db.execSQL("ALTER TABLE properties ADD COLUMN titleDeedReady INTEGER")
                db.execSQL("ALTER TABLE properties ADD COLUMN reasonForSelling TEXT")
                db.execSQL("ALTER TABLE properties ADD COLUMN viewingHours TEXT")
                db.execSQL("ALTER TABLE properties ADD COLUMN keyHolder TEXT")
                db.execSQL("ALTER TABLE properties ADD COLUMN paymentConditions TEXT")
                db.execSQL("ALTER TABLE properties ADD COLUMN constructionAge INTEGER")
                db.execSQL("ALTER TABLE properties ADD COLUMN legalStatus TEXT")
                db.execSQL("ALTER TABLE properties ADD COLUMN hiddenNotes TEXT")
                db.execSQL("ALTER TABLE properties ADD COLUMN floor INTEGER")
                db.execSQL("ALTER TABLE properties ADD COLUMN totalFloors INTEGER")
                db.execSQL("ALTER TABLE properties ADD COLUMN landZoning TEXT")
                db.execSQL("ALTER TABLE properties ADD COLUMN hasBusinessLicense INTEGER")
                db.execSQL("ALTER TABLE properties ADD COLUMN budgetMin INTEGER")
                db.execSQL("ALTER TABLE properties ADD COLUMN budgetMax INTEGER")
                db.execSQL("ALTER TABLE properties ADD COLUMN desiredMinArea REAL")
                db.execSQL("ALTER TABLE properties ADD COLUMN desiredMaxArea REAL")
                db.execSQL("ALTER TABLE properties ADD COLUMN desiredBedrooms INTEGER")
                db.execSQL("ALTER TABLE properties ADD COLUMN preferredAreas TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE properties ADD COLUMN floorPreference TEXT")
                db.execSQL("ALTER TABLE properties ADD COLUMN viewPreference TEXT")
                db.execSQL("ALTER TABLE properties ADD COLUMN cashAvailable INTEGER")
                db.execSQL("ALTER TABLE properties ADD COLUMN maxDeposit INTEGER")
                db.execSQL("ALTER TABLE properties ADD COLUMN maxMonthlyRent INTEGER")
            }
        }

        // Pure index addition — no column or row is touched, so it is safe on any existing install.
        // Every name here has to match Room's own convention (`index_<table>_<column>`) exactly,
        // because Room validates the live schema against its generated one when it opens the
        // database and throws if an index it expects is missing or named differently.
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_properties_dateAdded` ON `properties` (`dateAdded`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_properties_isFavorite` ON `properties` (`isFavorite`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_properties_lastViewedAt` ON `properties` (`lastViewedAt`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_propertyId` ON `notes` (`propertyId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_timeline_events_propertyId` ON `timeline_events` (`propertyId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_timeline_events_createdAt` ON `timeline_events` (`createdAt`)")
            }
        }

        // Adds the portable uid Property.kt needs for cross-device export/import merging (see
        // data/exportimport/). Every existing row gets a fresh random id here — SQLite's
        // randomblob()/hex() are built-in, so one UPDATE backfills the whole table without a
        // per-row Kotlin loop. The empty-string default only ever exists transiently between the
        // ADD COLUMN and the UPDATE in the same migration; no row is ever left with it.
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE properties ADD COLUMN uid TEXT NOT NULL DEFAULT ''")
                db.execSQL("UPDATE properties SET uid = lower(hex(randomblob(16))) WHERE uid = ''")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_properties_uid` ON `properties` (`uid`)")
            }
        }

        // Create Case redesign, round 2 (see data/Property.kt / the field-redesign doc it
        // implements). Every column here is new and nullable/defaulted — none of the columns this
        // migration touches from earlier versions (price, mortgageStatus, caseFlags, legalStatus,
        // desiredBedrooms, floorPreference, viewPreference, ownerName/ownerPhone, imageUri) are
        // altered or dropped, so every existing row keeps reading exactly as it did before this
        // migration. New per-case-type fields default to their "nothing set yet" value.
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE properties ADD COLUMN district TEXT")
                db.execSQL("ALTER TABLE properties ADD COLUMN latitude REAL")
                db.execSQL("ALTER TABLE properties ADD COLUMN longitude REAL")
                db.execSQL("ALTER TABLE properties ADD COLUMN legalDocumentType TEXT")
                db.execSQL("ALTER TABLE properties ADD COLUMN ownershipType TEXT")
                db.execSQL("ALTER TABLE properties ADD COLUMN depositAmount INTEGER")
                db.execSQL("ALTER TABLE properties ADD COLUMN nextViewingAt INTEGER")
                db.execSQL("ALTER TABLE properties ADD COLUMN developerName TEXT")
                db.execSQL("ALTER TABLE properties ADD COLUMN expectedDeliveryDate INTEGER")
                db.execSQL("ALTER TABLE properties ADD COLUMN constructionProgressPercent INTEGER")
                db.execSQL("ALTER TABLE properties ADD COLUMN waterSource TEXT")
                db.execSQL("ALTER TABLE properties ADD COLUMN hasWellPermit INTEGER")
                db.execSQL("ALTER TABLE properties ADD COLUMN frontageWidth REAL")
                db.execSQL("ALTER TABLE properties ADD COLUMN leadSource TEXT")
                db.execSQL("ALTER TABLE properties ADD COLUMN responsibleAgent TEXT")
                db.execSQL("ALTER TABLE properties ADD COLUMN lastContactAt INTEGER")
                db.execSQL("ALTER TABLE properties ADD COLUMN visitStatus TEXT")
                db.execSQL("ALTER TABLE properties ADD COLUMN isConfidential INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE properties ADD COLUMN floorPreferenceOptions TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE properties ADD COLUMN viewPreferenceOptions TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE properties ADD COLUMN needsLoanFinancing INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_properties_district` ON `properties` (`district`)")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "real_estate.db"
                ).addMigrations(
                    MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
                    MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10
                ).build().also { INSTANCE = it }
            }
        }
    }
}
