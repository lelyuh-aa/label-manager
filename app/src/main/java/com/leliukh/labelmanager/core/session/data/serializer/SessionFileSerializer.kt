package com.leliukh.labelmanager.core.session.data.serializer

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.leliukh.labelmanager.core.coroutines.domain.runSuspendCatching
import com.leliukh.labelmanager.core.session.data.models.SessionFileNO
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/**
 * Plain JSON serializer of [SessionFileNO]. In the app it is wrapped by an AEAD serializer,
 * so only ciphertext reaches the disk.
 */
object SessionFileSerializer : Serializer<SessionFileNO> {

    private val json = Json { ignoreUnknownKeys = true }

    override val defaultValue: SessionFileNO = SessionFileNO()

    override suspend fun readFrom(input: InputStream): SessionFileNO {
        val text = input.readBytes().decodeToString()
        if (text.isEmpty()) return defaultValue
        return runSuspendCatching { json.decodeFromString(SessionFileNO.serializer(), text) }
            .getOrElse { error ->
                if (error is SerializationException) {
                    throw CorruptionException("Unreadable session file", error)
                }
                throw error
            }
    }

    override suspend fun writeTo(t: SessionFileNO, output: OutputStream) {
        output.write(json.encodeToString(SessionFileNO.serializer(), t).encodeToByteArray())
    }
}
