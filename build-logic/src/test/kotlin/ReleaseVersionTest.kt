import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseVersionTest {
    @Test
    fun `missing tag produces a test build that can be upgraded to the first release`() {
        val untagged = ReleaseVersion.fromTag(null)

        assertEquals("0.0.0-release-test", untagged.name)
        assertTrue(untagged.code > 0)
        assertTrue(untagged.code < ReleaseVersion.fromTag("v1.0.0").code)
    }

    @Test
    fun `parses tag into Android release identity`() {
        assertEquals(ReleaseVersion("1.0.0", 1_000_000), ReleaseVersion.fromTag("v1.0.0"))
        assertEquals(ReleaseVersion("12.34.56", 12_034_056), ReleaseVersion.fromTag("v12.34.56"))
        assertEquals(ReleaseVersion("0.0.1", 1), ReleaseVersion.fromTag("v0.0.1"))
    }

    @Test
    fun `minor and major increments sort after full lower component ranges`() {
        val tags = listOf("v1.0.998", "v1.0.999", "v1.1.0", "v1.999.999", "v2.0.0")
        val codes = tags.map { ReleaseVersion.fromTag(it).code }
        assertTrue(codes.zipWithNext().all { (previous, next) -> previous < next })
    }

    @Test
    fun `accepts Android upper bound and rejects overflow`() {
        assertEquals(2_100_000_000, ReleaseVersion.fromTag("v2100.0.0").code)
        listOf("v2100.0.1", "v2101.0.0", "v999999999999999999999.0.0").forEach {
            assertThrows(IllegalArgumentException::class.java) { ReleaseVersion.fromTag(it) }
        }
    }

    @Test
    fun `rejects malformed ambiguous and colliding versions`() {
        listOf(
            "", "1.0.0", "v1.0", "v01.0.0", "v1.00.0", "v1.0.00", "v-1.0.0",
            "v1.0.0-rc.1", "v1.0.0+build", "v1.1000.0", "v1.0.1000", "v0.0.0", "v1.0.0\n"
        ).forEach {
            assertThrows("Must reject $it", IllegalArgumentException::class.java) { ReleaseVersion.fromTag(it) }
        }
    }
}
