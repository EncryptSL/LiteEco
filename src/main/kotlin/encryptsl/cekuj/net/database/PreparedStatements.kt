package encryptsl.cekuj.net.database

import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.api.interfaces.DatabaseSQLProvider
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*

class PreparedStatements(private val liteEco: LiteEco) : DatabaseSQLProvider {

    override fun createTable() {
        var connection: Connection? = null
        var preparedStatement: PreparedStatement? = null
        try {
            connection = liteEco.databaseConnector.getDatabase()
            preparedStatement = connection
                ?.prepareStatement("CREATE TABLE IF NOT EXISTS lite_eco (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid VARCHAR(36) NOT NULL, money DOUBLE)")
            preparedStatement?.execute()
            liteEco.logger.info("Table lite_eco was successfully created.")
        } catch (exc: SQLException) {
            exc.printStackTrace()
        } finally {
            if (connection != null) {
                try {
                    connection.close()
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close()
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun createPlayerAccount(uuid: UUID, money: Double) {
        var connection: Connection? = null
        var preparedStatement: PreparedStatement? = null
        try {
            if (!getExistPlayerAccount(uuid)) {
                connection = liteEco.databaseConnector.getDatabase()
                preparedStatement = connection?.prepareStatement("INSERT INTO lite_eco (uuid, money) VALUES (?,?)")
                preparedStatement?.setString(1, uuid.toString())
                preparedStatement?.setDouble(2, money)
                preparedStatement?.execute()
                liteEco.logger.info("User $uuid was inserted into table lite_eco.")
            }
        } catch (exc: SQLException) {
            exc.printStackTrace()
        } finally {
            if (connection != null) {
                try {
                    connection.close()
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close()
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun deletePlayerAccount(uuid: UUID) {
        var connection: Connection? = null
        var preparedStatement: PreparedStatement? = null
        try {
            connection = liteEco.databaseConnector.getDatabase()
            preparedStatement = connection?.prepareStatement("DELETE FROM lite_eco WHERE uuid=?")
            preparedStatement?.setString(1, uuid.toString())
            preparedStatement?.execute()
            liteEco.logger.info("User $uuid was deleted from table lite_eco.")
        } catch (exc: SQLException) {
            exc.printStackTrace()
        } finally {
            if (connection != null) {
                try {
                    connection.close()
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close()
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun getExistPlayerAccount(uuid: UUID): Boolean {
        var connection: Connection? = null
        var preparedStatement: PreparedStatement? = null
        var resultSet: ResultSet? = null
        try {
            connection = liteEco.databaseConnector.getDatabase()
            preparedStatement = connection?.prepareStatement("SELECT * FROM lite_eco WHERE uuid=?")
            preparedStatement?.setString(1, uuid.toString())
            resultSet = preparedStatement?.executeQuery()
            resultSet?.next()
            if (resultSet?.getObject("uuid") != null) {
                return true
            }
        } catch (exc: SQLException) {
            exc.printStackTrace()
        } finally {
            if (connection != null) {
                try {
                    connection.close()
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close()
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
            if (resultSet != null) {
                try {
                    resultSet.close()
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
        }
        return false
    }

    override fun getBalance(uuid: UUID): Double {
        var connection: Connection? = null
        var preparedStatement: PreparedStatement? = null
        var resultSet: ResultSet? = null
        try {
            connection = liteEco.databaseConnector.getDatabase()
            preparedStatement = connection?.prepareStatement("SELECT money FROM lite_eco WHERE uuid=?")
            preparedStatement?.setString(1, uuid.toString())
            resultSet = preparedStatement?.executeQuery()
            if (resultSet?.next() == true) {
                return resultSet.getDouble("money")
            }
        } catch (exc: SQLException) {
            exc.printStackTrace()
        } finally {
            if (connection != null) {
                try {
                    connection.close()
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close()
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
            if (resultSet != null) {
                try {
                    resultSet.close()
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
        }
        return 0.0
    }

    override fun depositMoney(uuid: UUID, money: Double) {
        var connection: Connection? = null
        var preparedStatement: PreparedStatement? = null
        try {
            connection = liteEco.databaseConnector.getDatabase()
            preparedStatement = connection
                ?.prepareStatement("UPDATE lite_eco SET money=? WHERE uuid=?")
            preparedStatement?.setDouble(1, getBalance(uuid) + money)
            preparedStatement?.setString(2, uuid.toString())
            preparedStatement?.execute()
        } catch (exc: SQLException) {
            exc.printStackTrace()
        } finally {
            if (connection != null) {
                try {
                    connection.close()
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close()
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun withdrawMoney(uuid: UUID, money: Double) {
        var connection: Connection? = null
        var preparedStatement: PreparedStatement? = null
        try {
            connection = liteEco.databaseConnector.getDatabase()
            preparedStatement = connection
                ?.prepareStatement("UPDATE lite_eco SET money=? WHERE uuid=?")
            preparedStatement?.setDouble(1, getBalance(uuid) - money)
            preparedStatement?.setString(2, uuid.toString())
            preparedStatement?.execute()
        } catch (exc: SQLException) {
            exc.printStackTrace()
        } finally {
            if (connection != null) {
                try {
                    connection.close()
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close()
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun setMoney(uuid: UUID, money: Double) {
        var connection: Connection? = null
        var preparedStatement: PreparedStatement? = null
        try {
            connection = liteEco.databaseConnector.getDatabase()
            preparedStatement = connection
                ?.prepareStatement("UPDATE lite_eco SET money=? WHERE uuid=?")
            preparedStatement?.setDouble(1, money)
            preparedStatement?.setString(2, uuid.toString())
            preparedStatement?.execute()
        } catch (exc: SQLException) {
            exc.printStackTrace()
        } finally {
            if (connection != null) {
                try {
                    connection.close()
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close()
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
        }
    }
}