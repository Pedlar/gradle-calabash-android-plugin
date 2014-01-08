package org.notlocalhost.gradle;

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.Exec

class CalabashTestPlugin implements Plugin<Project> {
    private static final String TEST_TASK_NAME = 'calabash'

    void apply(Project project) {
        def hasAppPlugin = project.plugins.hasPlugin AppPlugin
        def hasLibraryPlugin = project.plugins.hasPlugin LibraryPlugin

        // Ensure the Android plugin has been added in app or library form, but not both.
        if (!hasAppPlugin && !hasLibraryPlugin) {
            throw new IllegalStateException("The 'android' or 'android-library' plugin is required.")
        } else if (hasAppPlugin && hasLibraryPlugin) {
            throw new IllegalStateException(
                    "Having both 'android' and 'android-library' plugin is not supported.")
        }

        def variants = hasAppPlugin ? project.android.applicationVariants :
                project.android.libraryVariants

        def apkFilePath = "$project.buildDir/apk"

        variants.all { variant ->
            def buildTypeName = variant.buildType.name.capitalize()
            def projectFlavorNames = [""]
            if (hasAppPlugin) {
                projectFlavorNames = variant.productFlavors.collect { it.name.capitalize() }
                if (projectFlavorNames.isEmpty()) {
                    projectFlavorNames = [""]
                }
            }
            def projectFlavorName = projectFlavorNames.join()
            def variationName = "$projectFlavorName$buildTypeName"

            def apkName = ""
            if(projectFlavorName != "") {
                apkName = "${project.name}-${projectFlavorName.toLowerCase()}-${buildTypeName.toLowerCase()}-unaligned.apk"
            } else {
                apkName = "${project.name}-${buildTypeName.toLowerCase()}-unaligned.apk"
            }

            project.logger.debug "==========================="
            project.logger.debug "$apkFilePath/$apkName"
            project.logger.debug "${project.getPath()}"
            project.logger.debug "==========================="

            def outFile = new File(project.file("build/reports/calabash/${variationName}"), "report.html")
            def outFileDir = outFile.parentFile


            def taskRunName = "$TEST_TASK_NAME$variationName"
            def testRunTask = project.tasks.create(taskRunName, Exec)
            testRunTask.dependsOn project["assemble${variationName}"]
            testRunTask.description = "Run Calabash Tests for '$variationName'."
            testRunTask.group = JavaBasePlugin.VERIFICATION_GROUP

            def apkFile = "$apkFilePath/$apkName"
            testRunTask.workingDir "${project.rootDir}/"
            def os = System.getProperty("os.name").toLowerCase()
            if (os.contains("windows")) {
                // you start commands in Windows by kicking off a cmd shell
                testRunTask.commandLine "cmd", "/c", "calabash-android", "run", "${apkFile}", "--format", "html", "--out", outFile.canonicalPath
            }  else { // assume Linux 
                testRunTask.commandLine "calabash-android", "run", "${apkFile}", "--format", "html", "--out", outFile.canonicalPath
            }

            testRunTask.doFirst {
                if(!outFileDir.exists()) {
                    project.logger.debug "Making dir path $outFileDir.canonicalPath"
                    if(!outFileDir.mkdirs()) {
                        throw new IllegalStateException("Could not create reporting directories")
                    }
                }
            }
            
            testRunTask.doLast {
                println "\r\nCalabash HTML Report: file://$outFile.canonicalPath"
            }
        }
    }
}