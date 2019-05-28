package LoggerPlugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class LoggerPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.tasks.create(name: "applyTransform", type: TransformTask)
    }
}
