![ThePerfectApp](logo.png)

TPA is the world leading tool for app distribution to mobile devices. Watch live statistics and collect both detailed crash reports and feedback with JIRA integration. TPA helps your team to quickly respond to actual app usage.

## Installation

```
allprojects {
    repositories {
        ...
        jcenter() // This is being deprecated, but can possibly still be used
        maven { url "https://nexus3.trifork.com/repository/the-perfect-app/" } // This can be used instead of jcenter
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