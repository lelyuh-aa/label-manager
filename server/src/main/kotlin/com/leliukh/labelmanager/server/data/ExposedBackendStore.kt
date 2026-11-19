package com.leliukh.labelmanager.server.data

import com.leliukh.labelmanager.backend.data.BackendStore
import com.leliukh.labelmanager.backend.data.StoreReader
import com.leliukh.labelmanager.backend.data.StoreWriter
import java.io.File
import java.sql.Connection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

/**
 * [BackendStore] over a relational database through Exposed; the server uses SQLite.
 *
 * Every read and write is one database transaction at the serializable isolation level, so a
 * throwing block rolls everything back. Writes are additionally serialised in process, because
 * SQLite allows one writer at a time and a queue is cheaper than retrying busy errors.
 *
 * @param database the database to use; its schema is created when missing
 */
class ExposedBackendStore(private val database: Database) : BackendStore {

    private val writeMutex = Mutex()

    init {
        transaction(database) { SchemaUtils.create(*allTables) }
    }

    override suspend fun <T> read(block: StoreReader.() -> T): T =
        withContext(Dispatchers.IO) { transaction(database) { ExposedAccess.block() } }

    override suspend fun <T> write(block: StoreWriter.() -> T): T = writeMutex.withLock {
        withContext(Dispatchers.IO) { transaction(database) { ExposedAccess.block() } }
    }

    companion object {

        private const val BUSY_TIMEOUT_MILLIS = 5_000

        /**
         * @param file the SQLite database file; created with its folders when missing
         * @return a store over that file
         */
        fun sqlite(file: File): ExposedBackendStore {
            file.absoluteFile.parentFile?.mkdirs()
            val database =
                Database.connect(
                    url = "jdbc:sqlite:${file.absolutePath}",
                    driver = "org.sqlite.JDBC",
                    setupConnection = { connection ->
                        connection.createStatement().use {
                            it.execute("PRAGMA journal_mode = WAL")
                            it.execute("PRAGMA busy_timeout = $BUSY_TIMEOUT_MILLIS")
                        }
                    },
                    databaseConfig =
                        DatabaseConfig {
                            defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
                        },
                )
            return ExposedBackendStore(database)
        }
    }
}
