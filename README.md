# Amazfit Internet Companion
Amazfit Internet Companion is a generic and open source internet companion app for the Amazfit Pace and Stratos. It allows apps developed with the [Amazfit Communication](https://github.com/KieronQuinn/AmazfitCommunication) library to access the internet over Bluetooth via your phone's internet connection.

Using this app, developers can simply make watch apps with internet access without having to worry about companion apps, and it means every watch app doesn't need its own companion app

## Usage
Simply download the [latest release](https://github.com/KieronQuinn/AmazfitInternetCompanion/releases) to your PHONE (again, this is a phone app, NOT a watch app), and install it as you would any other APK (you may need to enable unknown sources). Run the app at least once for it to work, and that's it!

You can also install the app over ADB, with your PHONE plugged into your PC, NOT your watch, using:
```
adb install -r AmazfitInternetCompanion-1.0.apk
```

The app can be hidden from your app drawer, to access it again (for debugging or to restart the service if it fails), simply add it as a shortcut to your homescreen, or if you're using Android 6.0 or above, go to the app info for the app and use the settings icon there

## Developers
Feel free to use this as a base app to make your own companion app, note how the actual listening and sending of data is done in a foreground service to allow it to work without this app being open

## Screenshot
This isn't really needed but here's what the companion app's UI looks like. Basic isn't it?

<img src="https://raw.githubusercontent.com/KieronQuinn/AmazfitInternetCompanion/master/Images/screen.png" width="400"/>
