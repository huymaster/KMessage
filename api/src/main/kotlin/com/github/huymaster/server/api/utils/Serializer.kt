package com.github.huymaster.server.api.utils

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.cbor.CborBuilder
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.modules.SerializersModule
import java.util.*

private val defaultSerializersModule = SerializersModule {
    contextual(UUID::class, UUIDSerializer)
    contextual(ByteArray::class, CompactByteArraySerializer)
}

val DefaultJson = Json {
    serializersModule = defaultSerializersModule
    allowTrailingComma = true
    classDiscriminator = "__type__"
    coerceInputValues = true
    encodeDefaults = true
    explicitNulls = false
    ignoreUnknownKeys = true
    isLenient = true
    prettyPrint = true
}

@OptIn(ExperimentalSerializationApi::class)
val DefaultCbor = Cbor {
    serializersModule = defaultSerializersModule
    alwaysUseByteString = true
    encodeDefaults = true
    ignoreUnknownKeys = true
}

fun buildJson(from: Json = DefaultJson, builder: JsonBuilder.() -> Unit) = Json(from) { builder() }

@OptIn(ExperimentalSerializationApi::class)
fun buildCbor(from: Cbor = DefaultCbor, builder: CborBuilder.() -> Unit) = Cbor(from) { builder() }

private object UUIDSerializer : KSerializer<UUID> {
    private const val BUFFER_SIZE = Long.SIZE_BYTES * 2
    private val encoder = Base64.getUrlEncoder().withoutPadding()
    private val decoder = Base64.getUrlDecoder()

    override val descriptor = PrimitiveSerialDescriptor(
        UUID::class.java.name,
        PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: UUID) {
        val msb = value.mostSignificantBits
        val lsb = value.leastSignificantBits
        val bytes = ByteArray(16)
        for (i in 0..7) {
            bytes[i] = (msb shr (56 - i * 8)).toByte()
            bytes[i + 8] = (lsb shr (56 - i * 8)).toByte()
        }
        encoder.encodeString(UUIDSerializer.encoder.encodeToString(bytes))
    }

    override fun deserialize(decoder: Decoder): UUID {
        val str = decoder.decodeString()
        val bytes = UUIDSerializer.decoder.decode(str)
        require(bytes.size == BUFFER_SIZE) { "Invalid UUID" }

        var msb = 0L
        var lsb = 0L

        for (i in 0..7)
            msb = (msb shl 8) or (bytes[i].toLong() and 0xFFL)

        for (i in 8..15)
            lsb = (lsb shl 8) or (bytes[i].toLong() and 0xFFL)

        return UUID(msb, lsb)
    }
}

private object CompactByteArraySerializer : KSerializer<ByteArray> {
    private val encoder = Base64.getUrlEncoder().withoutPadding()
    private val decoder = Base64.getUrlDecoder()

    override val descriptor = PrimitiveSerialDescriptor(
        "CompactByteArray",
        PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: ByteArray) {
        if (encoder is JsonEncoder) {
            val bytes = CompactByteArraySerializer.encoder.encodeToString(value)
            encoder.encodeString(bytes)
        } else {
            encoder.encodeSerializableValue(ByteArraySerializer(), value)
        }
    }

    override fun deserialize(decoder: Decoder): ByteArray {
        return if (decoder is JsonDecoder) {
            val string = decoder.decodeString()
            CompactByteArraySerializer.decoder.decode(string)
        } else {
            decoder.decodeSerializableValue(ByteArraySerializer())
        }
    }
}