package com.github.encryptsl.lite.eco.common.extensions

import com.github.encryptsl.lite.eco.common.database.SqlPluginLogger
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.transactionManager

fun <T> loggedTransaction(db: Database? = null, statement: Transaction.() -> T): T {
    return transaction(db.transactionManager.defaultIsolationLevel, db.transactionManager.defaultReadOnly, db
    ) {
        addLogger(SqlPluginLogger())
        statement.invoke(this)
    }
}