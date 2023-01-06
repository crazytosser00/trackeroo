## Trackeroo

This is an All-in-One tracking and logging library for Android projects. You only need to add
library in your project and initialize it with builder. Trackeroo affects not only your code but
also third-party libraries too (like Glide, Timber, Cicerone and so on)

#### Warning!! Project currently in progress and available in early alpha and may not work in many cases

### Key features

#### Currently available:

* `android.util.Log` interception and storing - all logs in the app would be proxying and written in
  file includes the logging data that creates by another logging libraries
* Hooking of `android.view.View.OnClickListener` wherever in your code `View.setOnClickListener`
  calls and logging it automatically
* Transforming log strings and adding meta data like timestamp, debug level or any specific info you
  want
* Publish the file to the apps on your device (Mail client, Telegram, Google Drive etc.)
* Customize library by adding your own transformers, publishers and file writers via interfaces

#### Planning features:

* Hooking scrolls, double clicks, long clicks
* Hooking fragment stack (also for Compose Navigation)
* Log file size limit and auto-removing old data
* Publish modules for cloud storages and file sharing open APIs
* Custom rules for auto-publishing
* More configs for customize

### How to use

Add Jitpack to your buildscript repos and also add Trackeroo-plugin dependency in app module.
Current latest version of plugin is `0.1.1-alpha`

```groovy
buildscript {
    repositories {
        maven { url "https://jitpack.io" }
    }

    dependencies {
        classpath("com.github.crazytosser00:trackeroo-plugin:$plugin_version")
    }
}
```

Next, add Trackeroo-core library dependency

```groovy
dependencies {
    implementation "com.github.crazytosser00:trackeroo:$library_version"
}
```

Then you need to init Trackeroo. In your Application class you should create `Logger.Builder` and
create config

```kotlin
override fun onCreate() {
    super.onCreate()
    Logger
        .init()
        .setLogFormatter(LogFormatter())
        .addLogTransformer(DebugLevelTransformer())
        .addLogTransformer(TimeTransformer())
        .addLogTransformer(MessageTransformer())
        .setLogPublisher(ExportLogPublisher(applicationContext))
}
```

Or you may just use default config

```kotlin
override fun onCreate() {
    super.onCreate()
    Logger.default(applicationContext)
}
```

And that's it! Trackeroo will handle `android.util.Log` and view clicks.

Also you may manually add data to your log file. `Logger` class duplicates all logging methods
from `android.util.Log`.

To export log file you should call `Logger.publish()`. By default this method will open system
dialog with apps list to share. But you may change behavior by implementing `ILogPublisher` and
changing ExportLogPublisher in library config.



