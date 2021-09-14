package jsonblob.core

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import javax.inject.Inject

@MicronautTest
internal class SnowflakeTest {

    @Inject
    lateinit var snowflake: Snowflake

    @Test
    fun testNoArgs() {
        assertNotNull(snowflake)
    }
}