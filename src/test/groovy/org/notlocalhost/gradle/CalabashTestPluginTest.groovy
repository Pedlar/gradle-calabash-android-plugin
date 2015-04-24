package org.notlocalhost.gradle

import org.fest.assertions.api.Assertions
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class CalabashTestPluginTest {
  @Test public void pluginDetectsAppPlugin() {
    Project project = ProjectBuilder.builder().build();
    project.apply plugin: 'android'
    project.apply plugin: 'calabash-test'
  }

  @Test public void pluginDetectsLibraryPlugin() {
    Project project = ProjectBuilder.builder().build();
    project.apply plugin: 'android-library'
    project.apply plugin: 'calabash-test'
  }

  @Test public void pluginFailsWithoutAndroidPlugin() {
    Project project = ProjectBuilder.builder().build();
    try {
      project.apply plugin: 'calabash-test'
    } catch (IllegalStateException e) {
      Assertions.assertThat(e).hasMessage("The 'android' or 'android-library' plugin is required.");
    }
  }

  @Test public void pluginGetsFeaturesPathFromGradleBuildFileWhenAvailable() {
      CalabashTestPlugin plugin = new CalabashTestPlugin();

      String apkFile = "TestApkFile";
      File outFile = new File("/File/Path");
      Project project = ProjectBuilder.builder().build();

      project.extensions.create("calabashTest", CalabashTestPluginExtension)

      project.calabashTest.featuresPath = "features-path";

      Iterable commandArguments = plugin.constructCommandLineArguments(project, apkFile, outFile);

      Assertions.assertThat(commandArguments.contains("features-path")).isTrue();
  }

  @Test public void pluginGetsProfileFromGradleBuildFileWhenAvailable() {
      CalabashTestPlugin plugin = new CalabashTestPlugin();

      String apkFile = "TestApkFile";
      File outFile = new File("/File/Path");
      Project project = ProjectBuilder.builder().build();

      project.extensions.create("calabashTest", CalabashTestPluginExtension)

      project.calabashTest.profile = "expectedProfile";

      Iterable commandArguments = plugin.constructCommandLineArguments(project, apkFile, outFile);

      Assertions.assertThat(commandArguments.contains("expectedProfile")).isTrue();
  }

  @Test public void pluginGetsFormatsFromGradleBuildFileWhenAvailable() {
      CalabashTestPlugin plugin = new CalabashTestPlugin();

      String apkFile = "TestApkFile";
      File outFile = new File("/File/Path");
      Project project = ProjectBuilder.builder().build();

      project.extensions.create("calabashTest", CalabashTestPluginExtension)

      project.calabashTest.formats = ["html", "json"]

      Iterable commandArguments = plugin.constructCommandLineArguments(project, apkFile, outFile);

      Assertions.assertThat(commandArguments.contains("--format")).isTrue();
      Assertions.assertThat(commandArguments.contains("html")).isTrue();
      Assertions.assertThat(commandArguments.contains("json")).isTrue();
  }
}
