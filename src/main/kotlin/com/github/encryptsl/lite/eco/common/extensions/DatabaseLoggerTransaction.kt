package com.github.encryptsl.lite.eco.common.extensions

import com.github.encryptsl.lite.eco.common.database.SqlPluginLogger
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.transactions.transactionManager

fun <T> loggedTransaction(db: Database? = null, statement: Transaction.() -> T): T {
    val isolationLevel = db?.transactionManager?.defaultIsolationLevel
    val readOnly = db?.transactionManager?.defaultReadOnly
    return transaction(db, isolationLevel, readOnly) {
        addLogger(SqlPluginLogger())
        statement.invoke(this)
    }
}