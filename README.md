# RageMode [![GitHub Pre-Release](https://img.shields.io/github/release-pre/montlikadani/RageMode.svg)](https://github.com/montlikadani/RageMode/releases) [![Github All Releases](https://img.shields.io/github/downloads/montlikadani/RageMode/total.svg)](https://github.com/montlikadani/RageMode/releases) [![GitHub issues](https://img.shields.io/github/issues/montlikadani/RageMode.svg)](https://github.com/montlikadani/RageMode/issues)
RageMode is an open source and free plugin, that contains a lot of configurable settings, compared to the old one.
***

Old plugin page: [spigot](https://www.spigotmc.org/resources/12690/), [github](https://github.com/KWStudios/RageMode)<br/>
New plugin page: [spigot](https://www.spigotmc.org/resources/69169/)

# RageMode API
You can manually add the jar file to your build path or you can use jitpack repository site.

For javadoc:

**Manually included into build path**
- Download the RageMode jar file and add this jar to Javadoc Location

**Using one of build environment**
- During the building of the project the javadoc will be included automatically to the dependencies, or you need to download javadoc manually.

## Maven
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.montlikadani</groupId>
        <artifactId>RageMode</artifactId>
        <version>LATEST</version> <!-- Change the LATEST to the current version of this plugin -->
        <scope>provided</scope>
    </dependency>
</dependencies>
```

## Gradle
```gradle
repositories {
    maven { 
        url 'https://jitpack.io' 
    }
}

dependencies {
    implementation 'com.github.montlikadani:RageMode:LATEST' // Change the LATEST to the current version of this plugin
}
```

For API events: https://github.com/montlikadani/RageMode/wiki/API
