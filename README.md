# RingOfRings Core Library


## Introduction
The RingOfRings Core Library


## Installation


### Step 1: Configure your project
Add the following lines to the `dependencyResolutionManagement` section of your root `build.gradle` file to include the necessary repositories:


```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```


### Step 2: Add the library dependency
Include the HyperRing Core Library in your module's `build.gradle` file:


```groovy
dependencies {
    implementation 'com.github.RingOfRings:RingOfRingsSDK:TAG'
}
```