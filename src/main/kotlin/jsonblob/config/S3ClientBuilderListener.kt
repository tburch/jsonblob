package jsonblob.config

import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener
import mu.KotlinLogging
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration
import software.amazon.awssdk.core.retry.RetryMode
import software.amazon.awssdk.services.s3.S3ClientBuilder
import java.net.URI
import javax.inject.Singleton


private val log = KotlinLogging.logger {}

@Singleton
@Requires(beans = [S3ClientBuilder::class])
class S3ClientBuilderListener(
    @Property(name = endpointProp) private val endpoint: String?
) : BeanCreatedEventListener<S3ClientBuilder> {
    companion object {
        const val endpointProp = "aws.s3.endpoint"
    }

    override fun onCreated(event: BeanCreatedEvent<S3ClientBuilder>): S3ClientBuilder {
        return event.bean
            .overrideConfiguration(ClientOverrideConfiguration.builder().retryPolicy(RetryMode.LEGACY).build())
            .apply {
                endpoint?.let {
                    endpointOverride(URI.create(it))
                }
            }
    }
}