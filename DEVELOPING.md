### Software Requirements
You should have the following things installed:
* Git
* Java 25 - should be as unmodified as possible (Recommended: [Eclipse Adoptium](https://adoptium.net/temurin/releases/))
* Gradle (Note that the [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) is shipped with the repo)

### Recommended setup
* Install `IntelliJ`
  * Recommended setup actions
    * Disable not needed plugins
    * Disable [telemetry](https://www.jetbrains.com/help/idea/settings-usage-statistics.html)
    * Configure the available memory
  * Import the project
  * You will get prompted to install the required plugins
  * Ensure that everything is encoded in ``UTF-8``
  * Ensure that the JDK/Java-Version is correct


## Releasing 

Before releasing:
* Consider doing a `test-deployment` before actually releasing.
* Check the [changelog](CHANGELOG.md)

If `dev` is ready for release, create a pull request to the `master`-Branch and merge the changes

When the release is finished do the following:
* Merge the auto-generated PR (with the incremented version number) back into `dev`
