package com.github.encryptsl.lite.eco.common.extensions

import com.github.encryptsl.lite.eco.common.database.SqlPluginLogger
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.transactions.transactionManager
import java.sql.Connection
import java.sql.PreparedStatement

fun <T> loggedTransaction(db: Database? = null, statement: Transaction.() -> T): T {
    val isolationLevel = db?.transactionManager?.defaultIsolationLevel
    val readOnly = db?.transactionManager?.defaultReadOnly
    return transaction(db, isolationLevel, readOnly) {
        addLogger(SqlPluginLogger())
        maxAttempts = 3
        statement.invoke(this)
    }
}

fun <T> Table.batchUpdate(data: Iterable<T>, sql: String, body: PreparedStatement.(T) -> Unit) {
    val connection = TransactionManager.current().connection

    val javaConnection = connection.connection as Connection
    val statement = javaConnection.prepareStatement(sql)

    statement.use { statement ->
        for (item in data) {
            body.invoke(statement, item)
            statement.addBatch()
        }
        statement.executeBatch()
    }
}