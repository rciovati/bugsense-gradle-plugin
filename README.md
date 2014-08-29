# BugSense Gradle Plugin

This plugin allows you to upload your Proguard mapping files to BugSense as part of the build process.

## Usage

```groovy
// 1. Add the plugin dependency
buildscript {
  repositories {
    jcenter()
  }

  dependencies {
    classpath 'io.github.rciovati.tools:bugsense-plugin:1.0.0'
  }
}

// 2. Apply the plugin
apply plugin: 'bugsense'

// 3. Declare the upload tasks, for each variant you run Proguard.
android.applicationVariants.all { variant ->
  if (variant.buildType.runProguard) {
    def variantName = variant.name
    project.tasks.create(name: "uploadProguardMapping${variantName.capitalize()}",
            type: io.github.rciovati.bugsense.UploadMappingTask) {
      apiKey = 'api-key'
      authToken = 'auth-token'
      appVersion = project.android.defaultConfig.versionName as String
      mappingFile = new File(buildDir.getPath(), "/outputs/proguard/$variantName/mapping.txt")
    }
  }
}
```

## License

```
Copyright 2014 Riccardo Ciovati

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
