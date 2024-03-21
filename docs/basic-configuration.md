## Basic configuration

#### Init library by default
You may use Logger.default() with basic config. In that case library will not catch exceptions only write logs.

```kotlin
class App : Application() {
    override fun onCreate() {
        Logger.default(applicationContext)
        super.onCreate()
    }
}
```

> You should init the library in Application.attachBaseContext() or in Application.onCreate() methods before super.onCreate()

#### Init library using builder
If you want more flexibility you should use Logger.builder() method and customize configuration.
```kotlin
class App : Application() {
    override fun onCreate() {
        Logger.builder(applicationContext)
            .setLogWriter(LogTextFileWriter(Logger.logFileConfig, true))
            .setLogPublisher(ShareIntentLogPublisher(applicationContext))
            .addLogTransformer(TimeTransformer())
            .addLogTransformer(DebugLevelTransformer())
            .addLogTransformer(MessageTransformer())
            .setUncaughtExceptionHooker(
                DefaultUncaughtExceptionHooker(
                    applicationContext,
                    UncaughtExceptionAction.PublishAction
                )
            )
            .build()
        super.onCreate()
    }
}
```
**Trackeroo** has some default classes to working with log data:
* LogTextFileWriter writes logs to the .txt file
* ShareIntentLogPublisher allows you to export log files via system sharing pop-up
* TimeTransformer, DebugLevelTransformer, MessageTransformer modifies log strings with additional data
* DefaultUncaughtExceptionHooker handles crashes and react on exceptions with one of predefined actions like auto-publishing logs

Read the docs for detailed explanation (_under construction for now_).

The order of transformer classes is important. The working in FIFO order (first in, first out):
```kotlin
addLogTransformer(TimeTransformer())
addLogTransformer(DebugLevelTransformer())
addLogTransformer(MessageTransformer())
```
In this case TimeTransformer will modify log string first, then DebugLevelTransformer works next after him and last will make changes MessageTransformer. In example we will get something like this:
>[19.03.2024 12:10:02:30] [ASSERT] Test string: Lorem ipsum dolor sit amet

If order of transformers will be changed:
```kotlin
addLogTransformer(DebugLevelTransformer())
addLogTransformer(MessageTransformer())
addLogTransformer(TimeTransformer())
```
We getting this result:
>[ASSERT] Test string: Lorem ipsum dolor sit amet [19.03.2024 12:10:02:30]
 
All other methods in Builder class are order insensitive
