![ThePerfectApp](logo.png)

TPA is the world leading tool for app distribution to mobile devices. Watch live statistics and collect both detailed crash reports and feedback with JIRA integration. TPA helps your team to quickly respond to actual app usage.

## Installation

```
allprojects {
    repositories {
        ...
        maven {
            url 'https://dl.bintray.com/tpa/TPA-Android'
        }
    }
}


dependencies {
    // Core library
    implementation 'io.tpa.tpalib:tpalib:<version>'
    
    // Distribution library
    implementation 'io.tpa.tpalib:tpalib-distribution:<version>'
}
```


## License

Licensed under the MIT license