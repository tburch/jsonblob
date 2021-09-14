package jsonblob.core

import jsonblob.config.SnowflakeConfig
import mu.KotlinLogging
import java.net.NetworkInterface
import java.security.SecureRandom
import java.time.Instant


private val log = KotlinLogging.logger {}

/**
 * Distributed Sequence Generator.
 * Inspired by Twitter snowflake: https://github.com/twitter/snowflake/tree/snowflake-2010
 *
 * This class should be used as a Singleton.
 * Make sure that you create and reuse a Single instance of Snowflake per node in your distributed system cluster.
 */
class Snowflake {
    constructor() {
        nodeId = createNodeId().also { log.info { "Using nodeId=$it" } }
        customEpoch = DEFAULT_CUSTOM_EPOCH
    }

    constructor(config: SnowflakeConfig) {
        nodeId = config.nodeId
        customEpoch = DEFAULT_CUSTOM_EPOCH
    }

    constructor(nodeId: Long, customEpoch: Long = DEFAULT_CUSTOM_EPOCH) {
        require(!(nodeId < 0 || nodeId > maxNodeId)) { String.format("NodeId must be between %d and %d", 0, maxNodeId) }
        this.nodeId = nodeId
        this.customEpoch = customEpoch
    }

    private val nodeId: Long
    private val customEpoch: Long

    @Volatile
    private var lastTimestamp = -1L

    @Volatile
    private var sequence = 0L

    @Synchronized
    fun nextId(): Long {
        var currentTimestamp = timestamp()
        check(currentTimestamp >= lastTimestamp) { "Invalid System Clock!" }
        if (currentTimestamp == lastTimestamp) {
            sequence = sequence + 1 and maxSequence
            if (sequence == 0L) {
                // Sequence Exhausted, wait till next millisecond.
                currentTimestamp = waitNextMillis(currentTimestamp)
            }
        } else {
            // reset sequence to start with zero for the next millisecond
            sequence = 0
        }
        lastTimestamp = currentTimestamp
        return (currentTimestamp shl NODE_ID_BITS + SEQUENCE_BITS or (nodeId shl SEQUENCE_BITS)
                or sequence)
    }

    // Get current timestamp in milliseconds, adjust for the custom epoch.
    private fun timestamp(): Long {
        return Instant.now().toEpochMilli() - customEpoch
    }

    // Block and wait till next millisecond
    private fun waitNextMillis(currentTimestamp: Long): Long {
        var ct = currentTimestamp
        while (currentTimestamp == lastTimestamp) {
            ct = timestamp()
        }
        return ct
    }

    private fun createNodeId(): Long {
        var nodeId: Long = try {
            val sb = StringBuilder()
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()
            while (networkInterfaces.hasMoreElements()) {
                val networkInterface = networkInterfaces.nextElement()
                val mac = networkInterface.hardwareAddress
                if (mac != null) {
                    for (macPort in mac) {
                        sb.append(String.format("%02X", macPort))
                    }
                }
            }
            sb.toString().hashCode().toLong()
        } catch (ex: Exception) {
            SecureRandom().nextInt().toLong()
        }
        nodeId = nodeId and maxNodeId
        return nodeId
    }

    fun parse(id: Long): LongArray {
        val maskNodeId = (1L shl NODE_ID_BITS) - 1 shl SEQUENCE_BITS
        val maskSequence = (1L shl SEQUENCE_BITS) - 1
        val timestamp = (id shr NODE_ID_BITS + SEQUENCE_BITS) + customEpoch
        val nodeId = id and maskNodeId shr SEQUENCE_BITS
        val sequence = id and maskSequence
        return longArrayOf(timestamp, nodeId, sequence)
    }

    override fun toString(): String {
        return ("Snowflake Settings [EPOCH_BITS=" + EPOCH_BITS + ", NODE_ID_BITS=" + NODE_ID_BITS
                + ", SEQUENCE_BITS=" + SEQUENCE_BITS + ", CUSTOM_EPOCH=" + customEpoch
                + ", NodeId=" + nodeId + "]")
    }

    companion object {
        private const val UNUSED_BITS = 1 // Sign bit, Unused (always set to 0)
        private const val EPOCH_BITS = 41
        private const val NODE_ID_BITS = 10
        private const val SEQUENCE_BITS = 12
        const val maxNodeId = (1L shl NODE_ID_BITS) - 1
        private const val maxSequence = (1L shl SEQUENCE_BITS) - 1

        // Custom Epoch (January 1, 2015 Midnight UTC = 2015-01-01T00:00:00Z)
        private const val DEFAULT_CUSTOM_EPOCH = 1420070400000L
    }
}