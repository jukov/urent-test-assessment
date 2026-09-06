import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Validates local release prerequisites without producing outputs")
abstract class ValidateReleaseTask : DefaultTask() {
    @get:Input
    abstract val hasVersion: Property<Boolean>

    @get:Input
    abstract val hasSigning: Property<Boolean>

    @TaskAction
    fun validate() {
        check(hasVersion.get()) {
            "Release requires a vMAJOR.MINOR.PATCH tag on HEAD. " +
                "For a local preflight only, use -PcandidateReleaseTag=v1.0.0."
        }
        check(hasSigning.get()) {
            "Release signing requires a complete keys/release.properties (or -PsigningProperties=...)."
        }
    }
}
