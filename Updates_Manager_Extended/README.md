# Updates Manager Extended  
<p align="center">
  <img width="460" height="300" src="https://github.com/Senliast/xposed-modules/blob/main/Updates_Manager_Extended/docs/logo.jpg">
</p>

## About
This is an Xposed module, that allows you to block app updates (including automatic updates) for specific apps, no matter from which app store they were installed. After selecting apps, that should be blocked from updates, the app store will still detect updates for these apps, but will be unable to install these. Do note, it will as well not be possible to update selected apps with an APK. Another advantage of this module is that it doesnt brake SafetyNet / Play Integrity as it doesnt hook Google Play or Google Play Services.



## Compatibility
App stores: ALL  
CPUs: arm, arm64, x86, x64  
Android versions: guaranted to work on AOSP Android 12-14. It will NOT work on Android 11 and below. It will probably work on OEM skins (Samsung, MIUI), as long as the Android version matches, but no guaranty.  



## Download
You can download latest release from: https://github.com/Senliast/xposed-modules/releases/tag/Updates-Manager-Extended-v1.0.



## Installation
1. Install the APK.
2. Enable the module in LSPosed Manager, add to scope the following packages:
   - Android system
4. Reboot the device



## Uninstalling
1. Remove the APK.
2. Reboot the device.



## Questions and answers
Q: How does it work?  
A: The module hooks the PackageInstaller process and when an update starts - it checks if the name of the package, that is about to update, is present in the blacklist. If yes - it pretends, that something went wrong during package verification, this way causing the installation to fail.

Q: Why does it need access to Android system and not to Package Installer?  
A: The Package Installer application is more a GUI, mainly intended for installing APKs. The part that is handling the logic and installation process is part of Android, exactly said its located in com.android.server.pm., therefore, access to Android system is needed.

Q: Is it compatible with WSA?  
A: Yes 



## Key words
Google Play, disable android app updates, disable android app auto updates, Google Play updates, android app updates blocker, block android app updates, Xposed, LSPosed, Root, Maigisk
