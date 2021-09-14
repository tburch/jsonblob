package jsonblob.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert.assertEquals


internal class JsonCleanerTest {
    @Test
    fun test() {
        val json = """
            {
                "name": "Tristan",
                "age": 21
            }
        """.trimIndent()
        val cleaned = JsonCleaner.removeWhiteSpace(json)
        assertThat(json).isNotEqualTo(cleaned)
        assertEquals(json, cleaned, true)

    }
}