package util

import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.config.DriverConfigLoader
import com.datastax.oss.driver.api.core.config.DefaultDriverOption.*
import com.datastax.oss.driver.api.core.session.Session

class Database(
    private val address: String,
    private val port: Int,
    private val userName: String,
    private val password: String,
    private val keyspace: String
) {
    private var session: Session? = null

    fun connection(): Session {
        val configLoaderBuilder = DriverConfigLoader.programmaticBuilder()
        configLoaderBuilder.withString(CONTACT_POINTS, "${address}:${port}")
        configLoaderBuilder.withString(AUTH_PROVIDER_USER_NAME, userName)
        configLoaderBuilder.withString(AUTH_PROVIDER_PASSWORD, password)
        session = CqlSession.builder()
            .withConfigLoader(configLoaderBuilder.build())
            .withKeyspace(keyspace)
            .build()
        return session!!
    }

    fun getSession(): Session? {
        return session
    }
}