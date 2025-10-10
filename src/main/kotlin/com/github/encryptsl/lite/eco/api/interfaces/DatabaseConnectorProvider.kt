package com.github.encryptsl.lite.eco.api.interfaces

/**
 * Interface defining the contract for a database connection provider.
 *
 * This provider handles the lifecycle of the database connection,
 * including initialization, loading (pre-connection setup), and shutdown.
 */
interface DatabaseConnectorProvider {

    /**
     * Executes any necessary setup or loading tasks before the database connection is initialized.
     *
     * This method typically handles configuration loading or resource preparation.
     */
    fun onLoad()

    /**
     * Initializes and establishes the connection to the database using the provided credentials.
     *
     * @param driver The fully qualified class name of the JDBC driver (e.g., 'com.mysql.cj.jdbc.Driver').
     * @param jdbcHost The JDBC connection string/URL for the database (e.g., 'jdbc:mysql://localhost:3306/db_name').
     * @param user The username for database authentication.
     * @param pass The password for database authentication.
     */
    fun initConnect(driver: String, jdbcHost: String, user: String, pass: String)

    /**
     * Executes cleanup tasks and closes the database connection gracefully.
     *
     * This method is called when the application or module is shutting down.
     */
    fun onDisable()
}