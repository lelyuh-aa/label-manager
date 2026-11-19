package com.leliukh.labelmanager.core.session.data.serializer

import androidx.datastore.core.CorruptionException
import com.leliukh.labelmanager.core.session.data.models.SessionFileNO
import com.leliukh.labelmanager.core.session.data.models.SessionNO
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.time.Instant

class SessionFileSerializerTest {

    @Test
    fun `a stored session reads back unchanged`() = runTest {
        val output = ByteArrayOutputStream()

        SessionFileSerializer.writeTo(file, output)
        val read = SessionFileSerializer.readFrom(ByteArrayInputStream(output.toByteArray()))

        assertEquals(file, read)
    }

    @Test
    fun `an empty file is a signed-out session`() = runTest {
        val read = SessionFileSerializer.readFrom(ByteArrayInputStream(ByteArray(0)))

        assertEquals(SessionFileNO(), read)
    }

    @Test(expected = CorruptionException::class)
    fun `garbage is reported as corruption so the handler can reset the file`() = runTest {
        SessionFileSerializer.readFrom(ByteArrayInputStream("not json".encodeToByteArray()))
    }

    private val file = SessionFileNO(
        session = SessionNO(
            userId = "user-1",
            email = "producer@example.com",
            labelId = "label-1",
            accessToken = "access",
            accessTokenExpiresAt = Instant.parse("2026-10-25T12:00:00Z"),
            refreshToken = "refresh",
        ),
    )
}
