# JAADec
Forked from here https://github.com/dieterstueken/JAADec/tree/PS

The original project was licensed under Public Domain and as such this fork is also licensed under Public Domain. Use as you like!

JAAD is an AAC decoder and MP4 demultiplexer library written completely in Java. It uses no native libraries, is platform-independent and portable. It can read MP4 container from almost every input-stream (files, network sockets etc.) and decode AAC-LC (Low Complexity) and HE-AAC (High Efficiency/AAC+).

Current version: [![](https://jitpack.io/v/walkyst/JAADec-fork.svg)](https://jitpack.io/#walkyst/JAADec-fork)

For Gradle:
```groovy
repositories {
  maven {
    url 'https://jitpack.io'
  }
}

dependencies {
  compile 'com.github.walkyst:JAADec-fork:x.y.z'
}
```
For Maven:
```xml
<repositories>
  <repository>
    <id>jitpack</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.github.walkyst</groupId>
    <artifactId>JAADec-fork</artifactId>
    <version>x.y.z</version>
  </dependency>
</dependencies>
```
