package de.achimonline.recent_projects_cleaner

import com.intellij.ide.RecentProjectListActionProvider
import com.intellij.ide.RecentProjectsManager
import com.intellij.ide.ReopenProjectAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import java.io.File

class RecentProjectsCleaner: ProjectActivity {
    override suspend fun execute(project: Project) {
        RecentProjectListActionProvider.getInstance().getActions().forEach {
            val recentProject: ReopenProjectAction = it as ReopenProjectAction

            if (recentProject.isRemoved || File(recentProject.projectPath).exists()) {
                return@forEach
            }

            RecentProjectsManager.getInstance().removePath(recentProject.projectPath)
        }
    }
}
