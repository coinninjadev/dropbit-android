# DropBit

Bitcoin client app

Currently, Coin Ninja does not support any non-Coin-Ninja build process. We are providing this software as open source for transparency purposes, but not to be externally buildable.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)

## Prerequisites

The guide assumes that you are using Unix for development.

- Install [Android Studio](https://developer.android.com/studio/index.html)
- Install [Java JDK v8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

## Getting Started

**Google Services**
The Application is dependent of integration with Firebase for push notifications.  This requires 
that there is a google-services.json file located at `{project-dir}/app/google-services.json`

This should have access to the following packages or `applicationIds`
```
com.coinninja.coinkeeper
com.coinninja.staging.coinkeeper
com.coinninja.coinkeeper.debug
com.coinninja.staging.coinkeeper.debug
```

**Libbitcoin**
  
**Fetching Libbitcoin dependency with gradle in Android Studio**

* Add `GIT_LAB_APIKEY_COIN_NINJA` global `gradle.properites` file:
 
```
touch ~/.gradle/gradle.properties
echo "GIT_LAB_APIKEY_COIN_NINJA={{API_KEY}}" >> ~/.gradle/gradle.properties

```

**Fetching Libbitcoin dependency with on command line gradlew**

**ZSH**

```
echo "export GIT_LAB_APIKEY_COIN_NINJA={{API_KEY}}" >> .zshrc

```

**Bash**

```
echo "export GIT_LAB_APIKEY_COIN_NINJA={{API_KEY}}" >> .bash_profile
```

## Coin Ninja API Base:

Define API base URI for app to use when syncing

The following properties are required to build the application successfully within Android Studio

`vi ~/.gradle/gradle.properties`

```gradle
COIN_NINJA_API_BASE__STAGING={{URL}}
COIN_NINJA_API_BASE={{URL}}
 
ANALYTICS_TOKEN={{TOKEN}}
MIX_PANEL_SECRET={{TOKEN}}
 
nexusUID={{USERNAME}}
nexusPWD={{PASSWORD}}
```

