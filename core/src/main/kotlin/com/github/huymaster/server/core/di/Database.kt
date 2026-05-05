package com.github.huymaster.server.core.di

import com.github.huymaster.server.core.database.table.BaseTable
import com.github.huymaster.server.core.utils.EnvironmentVariables
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import io.minio.MinioClient
import org.koin.dsl.module
import org.ktorm.database.Database
import org.ktorm.logging.Slf4jLoggerAdapter
import org.postgresql.Driver
import java.time.Duration
import javax.sql.DataSource

object Database {
    val PSQL = module {
        val host by EnvironmentVariables.POSTGRES_HOST
        val port by EnvironmentVariables.POSTGRES_PORT
        val db by EnvironmentVariables.POSTGRES_DB
        val user by EnvironmentVariables.POSTGRES_USER
        val password by EnvironmentVariables.POSTGRES_PASSWORD

        single<DataSource> {
            val config = HikariConfig().apply {
                jdbcUrl = "jdbc:postgresql://$host:$port/$db"
                username = user
                this.password = password
                driverClassName = Driver::class.java.name

                maximumPoolSize = 16
                minimumIdle = 1

                idleTimeout = 300000
                connectionTimeout = 5000
                keepaliveTime = 30000
                maxLifetime = 600000

                addDataSourceProperty("cachePrepStmts", "true")
                addDataSourceProperty("prepStmtCacheSize", "250")
                addDataSourceProperty("prepStmtCacheSqlLimit", "2048")

                addDataSourceProperty("tcpKeepAlive", "true")
            }
            HikariDataSource(config)
        }

        single {
            Database.connect(
                dataSource = get(),
                logger = Slf4jLoggerAdapter("Database")
            )
        }

        factory { params ->
            val table = params.get<BaseTable<*>>()
            table.getRepository()
        }
    }

    val REDIS = module {
        val host by EnvironmentVariables.REDIS_HOST
        val port by EnvironmentVariables.REDIS_PORT
        val password by EnvironmentVariables.REDIS_PASSWORD

        single {
            val uri = RedisURI.builder()
                .withHost(host)
                .withPort(port)
                .withPassword(password)
                .withTimeout(Duration.ofSeconds(10))
                .build()
            RedisClient.create(uri)
        }

        single<StatefulRedisPubSubConnection<String, String>> {
            get<RedisClient>().connectPubSub()
        }
    }

    val MINIO = module {
        val host by EnvironmentVariables.MINIO_HOST
        val port by EnvironmentVariables.MINIO_PORT
        val user by EnvironmentVariables.MINIO_USER
        val password by EnvironmentVariables.MINIO_PASSWORD

        single {
            val builder = MinioClient.builder()
            builder.endpoint(host, port, false)
            builder.credentials(user, password)
            builder.build()
        }
    }
}