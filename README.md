## Trackeroo

This is an All-in-One tracking and logging library for Android projects. You only need to add
library in your project and initialize it with builder. Trackeroo affects not only your code but
also third-party libraries too (like Glide, Timber, Cicerone and so on)

#### Warning!! Project currently in progress and available in early alpha and may not work in many cases

### Key features

#### Currently available:

* `android.util.Log` interception and storing - all logs in the app would be proxying and written in
  file includes the logging data that creates by another logging libraries
* Hooking of `android.view.View.OnClickListener` wherever in your code `View.setOnClickListener` calls
  and logging it automatically
* Transforming log strings and adding meta data like timestamp, debug level or any specific info you want
* Publish the file to the apps on your device (Mail client, Telegram, Google Drive etc.)
* Customize library by adding your own transformers, publishers and file writers via interfaces

#### Planning features:

* Hooking scrolls, double clicks, long clicks
* Hooking fragment stack (also for Compose Navigation)
* Log file size limit and auto-removing old data
* Publish modules for cloud storages and  file sharing open APIs
* Custom rules for auto-publishing
* More configs for customize