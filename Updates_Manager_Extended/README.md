# Updates Manager Extended  

## About
This is an Xposed module, that allows you to block app updates (including automatic updates) for specific apps, no matter from which app store they were installed. After selecting apps, that should be blocked from updates, the app store will still detect updates for these apps, but will be unable to install these. You can create groups of apps and select which app stores are allowed to update which apps. There are as well timers - you can temporarily disable the group, the module or a single app and then just perform needed updates - no need to go back and re-enable group / module / app, it will happen automatically - this way, you will as well not forget to do it.

What makes this module unique is the fact that the apps, that are blocked from updates, still can verify their license in Google Play and make in-app purchases. It means, you can disable updates for your licensed apps from Google Play without breaking their license. Another advantage of this module is that it doesnt break SafetyNet / Play Integrity as it doesnt hook Google Play or Google Play Services.

The main reason for creating this module was aggressive auto-update policy of Google Play, ignoring and resetting the auto-update setting for specific apps, set by user, see proof:
 - https://android.stackexchange.com/questions/216208/play-store-silently-updates-apps-although-auto-update-is-disabled
 - https://android.stackexchange.com/questions/246911/what-is-google-up-to-google-play-store-updating-apps-even-though-auto-update-is
 - https://android.stackexchange.com/questions/162909/apps-auto-updating-even-if-disabled
 - https://forums.androidcentral.com/threads/google-play-store-just-auto-updated-all-my-apps-on-its-own-wtf.273772/
 - https://eu.community.samsung.com/t5/galaxy-s22-series/google-play-apps-keep-automatically-updating-when-i-ve-turned/td-p/7338657

## Compatibility
 - app stores: ALL  
 - CPUs: arm, arm64, x86, x64  
 - Android versions:
   - Android 8.0 - 17
   - will probably work on OEM skins (Samsung, MIUI, etc.), as long as the Android version matches, but no guaranty
- Xposed frameworks:
   - EdXposed, LSposed, Vector
   - will not work with original Xposed

## Download
You can download latest release from: https://github.com/Senliast/xposed-modules/releases/tag/Updates-Manager-Extended-v6.0.

## Installation
1. Install the APK.
2. Enable the module in your Xposed manager, if scope is used - add to scope the following packages:
   - System Framework
3. Reboot the device

## Uninstalling
1. Remove the app.
2. Reboot the device.

## Questions and answers
Q: How does it work?  
A: The module hooks the package installation procedure and when an update starts - it checks if the app should be blocked from updating according to rules. If yes - it pretends, that something went wrong during package verification, this way causing the installation to fail.

Q: Why does it need access to Android system and not to Package Installer?  
A: The Package Installer application is more a GUI, mainly intended for installing APKs. The part that is handling the logic and installation process is part of Android, exactly said its located in com.android.server.pm, therefore, access to Android system is needed.

Q: Is it compatible with WSA?  
A: Yes 

## Key words
Google Play, disable android app updates, disable android app auto updates, Google Play updates, android app updates blocker, block android app updates, Xposed, LSPosed, root, Magisk, Vector, EdXposed
