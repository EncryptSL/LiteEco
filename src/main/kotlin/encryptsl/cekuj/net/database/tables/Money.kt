package encryptsl.cekuj.net.database.tables

import org.jetbrains.exposed.sql.Table

object Money : Table() {
    private val id = integer( "id").autoIncrement()
    val uuid = varchar("uuid", 36)
    val money = double("money")

    override val primaryKey: PrimaryKey = PrimaryKey(id)

    override val tableName: String
        get() = "lite_eco"
}