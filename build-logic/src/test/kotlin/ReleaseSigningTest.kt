import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ReleaseSigningTest {
    @get:Rule
    val directory = TemporaryFolder()

    @Test
    fun `missing properties select debug fallback`() {
        assertNull(ReleaseSigning.load(directory.root.resolve("absent.properties")))
    }

    @Test
    fun `incomplete credentials select debug fallback even when keystore exists`() {
        directory.newFile("release.jks")
        val properties = directory.newFile("release.properties")
        properties.writeText("storeFile=release.jks\nstorePassword=test\nkeyAlias=urent\n")

        assertNull(ReleaseSigning.load(properties))
    }

    @Test
    fun `missing keystore selects debug fallback`() {
        val properties = directory.newFile("release.properties")
        properties.writeText(CREDENTIALS)

        assertNull(ReleaseSigning.load(properties))
    }

    @Test
    fun `complete credentials resolve keystore relative to properties and select release signing`() {
        val keys = directory.newFolder("keys")
        val keystore = keys.resolve("release.jks").apply { writeBytes(byteArrayOf()) }
        val properties = keys.resolve("release.properties").apply { writeText(CREDENTIALS) }

        val signing = ReleaseSigning.load(properties)

        assertNotNull(signing)
        assertEquals(keystore, signing?.storeFile)
        assertEquals("urent", signing?.keyAlias)
        assertEquals("store-test", signing?.storePassword)
        assertEquals("key-test", signing?.keyPassword)
    }

    private companion object {
        const val CREDENTIALS =
            "storeFile=release.jks\nstorePassword=store-test\nkeyAlias=urent\nkeyPassword=key-test\n"
    }
}
