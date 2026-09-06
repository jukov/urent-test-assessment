/** Android identity from a canonical Git tag, or a clearly marked untagged test build. */
data class ReleaseVersion(val name: String, val code: Int) {
    companion object {
        private const val COMPONENT_BASE = 1000L
        private const val MAX_VERSION_CODE = 2_100_000_000L
        private val tagPattern = Regex("v(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)")

        fun fromTag(tag: String?): ReleaseVersion {
            if (tag == null) return ReleaseVersion("0.0.0-release-test", 1)

            val match = requireNotNull(tagPattern.matchEntire(tag)) {
                "Expected a release tag vMAJOR.MINOR.PATCH, got: $tag"
            }
            val (major, minor, patch) = match.destructured.toList().map {
                requireNotNull(it.toLongOrNull()) { "Version component is too large: $tag" }
            }
            require(major <= MAX_VERSION_CODE / COMPONENT_BASE / COMPONENT_BASE) {
                "Major version exceeds Android versionCode limit: $tag"
            }
            require(minor < COMPONENT_BASE && patch < COMPONENT_BASE) {
                "Minor and patch must each be in 0..999: $tag"
            }
            val code = major * COMPONENT_BASE * COMPONENT_BASE + minor * COMPONENT_BASE + patch
            require(code in 1..MAX_VERSION_CODE) { "Android versionCode must be in 1..$MAX_VERSION_CODE: $tag" }
            return ReleaseVersion(tag.removePrefix("v"), code.toInt())
        }
    }
}
