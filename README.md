InjectLogTagPlugin
==================

Inject Log Tag Plugin is a plugin for Android Studio.  

When you want to output log by logcat, you write like this.

```
Log.d(LOG_TAG, "somthing");
```

Usually I declare LOG_TAG variable below the class declaration, 
and use it is Log class.

## Usage

### Install the plugin

Download plugin from JetBrains repository.

```
Preferences > Plugins > Install JetBrains Plugins... > Inject LOG_TAG Plugin
```

### Call the plugin while coding

```
Code > Generate...
```

```
Select Inject LOG-TAG
```

LOG_TAG field will be appeared.