package jsonblob.config

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.context.annotation.Factory
import jsonblob.core.Snowflake
import javax.inject.Singleton
import javax.validation.constraints.Max
import javax.validation.constraints.Min

@ConfigurationProperties("snowflake")
class SnowflakeConfig {
    @get:Min(1)
    @get:Max(Snowflake.maxNodeId)
    var nodeId: Long = 1

    var autoConfigureNodeId = true
}

@Factory
class SnowFlakeFactory {
    @Singleton
    fun snowflake(config: SnowflakeConfig): Snowflake {
        return if (config.autoConfigureNodeId) {
            Snowflake()
        } else {
            Snowflake(config)
        }
    }
}
