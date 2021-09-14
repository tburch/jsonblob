package jsonblob.core.store.file

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.Instant
import javax.inject.Inject

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class BlobMetadataTest {
    val json = """
        {
          "lastAccessedByBlobId": {
            "8c29f41b-925f-11eb-b419-f39ee7606dbe": 1617235736215,
            "0ab03830-9228-11eb-b419-954641a7ca78": 1629623201374,
            "b2a8fb31-9236-11eb-b419-0b887f420eea": 1617276513144,
            "f74bdaae-91df-11eb-b419-cfab8fd54eaf": 1617249707172,
            "57ff5cc6-9234-11eb-b419-938336b9ef7b": 1617246105863,
            "2a19b5b1-920d-11eb-b419-139a0e6ef658": 1628674282282,
            "969411d3-91e0-11eb-b419-47bc9e462c82": 1617304664661,
            "f9fa8a05-91e0-11eb-b419-1701fff14c6d": 1617327422593
          }
        }
    """.trimIndent()

    @Inject
    lateinit var objectMapper: ObjectMapper

    @Test
    fun test() {
        val blobMetadata = objectMapper.readValue<BlobMetadata>(json)
        assertThat(blobMetadata).isNotNull
        assertThat(blobMetadata.lastAccessedByBlobId.size).isEqualTo(8)
        assertThat(blobMetadata.lastAccessedByBlobId["f9fa8a05-91e0-11eb-b419-1701fff14c6d"]).isInstanceOf(Instant::class.java)
    }
}