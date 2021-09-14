package jsonblob.util

import com.fasterxml.jackson.core.JsonEncoding
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonParser.NumberType.BIG_DECIMAL
import com.fasterxml.jackson.core.JsonParser.NumberType.BIG_INTEGER
import com.fasterxml.jackson.core.JsonParser.NumberType.DOUBLE
import com.fasterxml.jackson.core.JsonParser.NumberType.FLOAT
import com.fasterxml.jackson.core.JsonParser.NumberType.INT
import com.fasterxml.jackson.core.JsonParser.NumberType.LONG
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.JsonToken.END_ARRAY
import com.fasterxml.jackson.core.JsonToken.END_OBJECT
import com.fasterxml.jackson.core.JsonToken.FIELD_NAME
import com.fasterxml.jackson.core.JsonToken.NOT_AVAILABLE
import com.fasterxml.jackson.core.JsonToken.START_ARRAY
import com.fasterxml.jackson.core.JsonToken.START_OBJECT
import com.fasterxml.jackson.core.JsonToken.VALUE_EMBEDDED_OBJECT
import com.fasterxml.jackson.core.JsonToken.VALUE_FALSE
import com.fasterxml.jackson.core.JsonToken.VALUE_NULL
import com.fasterxml.jackson.core.JsonToken.VALUE_NUMBER_FLOAT
import com.fasterxml.jackson.core.JsonToken.VALUE_NUMBER_INT
import com.fasterxml.jackson.core.JsonToken.VALUE_STRING
import com.fasterxml.jackson.core.JsonToken.VALUE_TRUE
import java.io.ByteArrayOutputStream

class JsonCleaner {
    companion object {
        fun validJson(json: String) = kotlin
            .runCatching {
                removeWhiteSpace(json)
                true
            }.getOrElse { false }

        fun removeWhiteSpace(json: String): String {
            val outputStream = ByteArrayOutputStream()
            val jsonFactory = JsonFactory()
            jsonFactory.createParser(json).use { jp ->
                jsonFactory.createGenerator(outputStream, JsonEncoding.UTF8).use { jg ->
                    var next: JsonToken?
                    do {
                        next = jp.nextToken().also {
                            when (it) {
                                NOT_AVAILABLE -> {
                                    // no-op for non-blocking
                                }
                                START_OBJECT -> jg.writeStartObject()
                                START_ARRAY -> jg.writeStartArray()
                                END_OBJECT -> jg.writeEndObject()
                                END_ARRAY -> jg.writeEndArray()
                                FIELD_NAME -> jg.writeFieldName(jp.text)
                                VALUE_EMBEDDED_OBJECT -> jg.writeEmbeddedObject(jp.embeddedObject)
                                VALUE_STRING -> jg.writeString(jp.valueAsString)
                                VALUE_NUMBER_INT -> writeNumber(jp, jg)
                                VALUE_NUMBER_FLOAT -> writeNumber(jp, jg)
                                VALUE_TRUE -> jg.writeBoolean(true)
                                VALUE_FALSE -> jg.writeBoolean(false)
                                VALUE_NULL -> jg.writeNull()
                                else -> {
                                }
                            }
                        }
                    } while (next != null)

                }
                return String(outputStream.toByteArray())
            }
        }

        private fun writeNumber(jp: JsonParser, jg: JsonGenerator) {
            when (jp.numberType) {
                INT -> jg.writeNumber(jp.intValue)
                LONG -> jg.writeNumber(jp.longValue)
                BIG_INTEGER -> jg.writeNumber(jp.bigIntegerValue)
                FLOAT -> jg.writeNumber(jp.floatValue)
                DOUBLE -> jg.writeNumber(jp.doubleValue)
                BIG_DECIMAL -> jg.writeNumber(jp.decimalValue)
                else -> {
                }
            }

        }
    }
}