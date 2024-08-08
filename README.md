# RingOfRings Core Library


## Introduction
The RingOfRings Core Library


## Installation


### Step 1: Configure your project
Add the following lines to the `dependencyResolutionManagement` section of your root `build.gradle` file to include the necessary repositories:

minSdk = 28

### groovy
```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}

subprojects {
    configurations.all {
        resolutionStrategy.eachDependency { details ->
            if (details.requested.group == 'org.bouncycastle') {
                details.useVersion '1.68'
                details.because 'Avoiding duplicate classes from different versions'
            }
        }
    }
}

```
### kts
```kts
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}


subprojects {
    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.bouncycastle") {
                useVersion("1.68")
                because("Avoiding duplicate classes from different versions")
            }
        }
    }
}

```



### Step 2: Add the library dependency
Include the HyperRing Core Library in your module's `build.gradle` file:

### groovy
```groovy
dependencies {
    implementation 'com.github.RingOfRings:RingOfRingsSDK:TAG'
}
```
### kts
```kts
dependencies {
    implementation("com.github.RingOfRings:RingOfRingsSDK:TAG")
}
```