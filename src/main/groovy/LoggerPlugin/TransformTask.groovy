package LoggerPlugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import LoggerPlugin.LoggerTransform;

class TransformTask extends DefaultTask {
    String group = "loggerplugin"
    String description = "Applies transform"

    @TaskAction
    def applyTransform() {
        project.android.registerTransform(new LoggerTransform())
        println "apply Transform called and transform was registered"
    }
}
