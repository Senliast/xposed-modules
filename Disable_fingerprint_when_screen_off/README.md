# Disable fingerprint when screen off  
## About
This module disables the recognizing of fingeprint, when the screen is off (including AOD), preventing unwanted unlocks or failed attempts, that leads to biometric lockout. Works for physical and under-display fingerpint sensors. The module doesnt have UI, it just works once enabled.

## Compatibility
Tested on AOSP Android 13 with LSPosed.

## Download
You can download latest release from: https://github.com/Senliast/xposed-modules/releases/tag/Disable-fingerprint-when-screen-off-v1.0.

## Installation
1. Install the APK.
2. Enable the module in EdXposed / LSPosed Manager, whitelist (in case of using EdXposed with whitelist mode) or add to scope (in case of using LSPosed) the following packages:
 - System UI
3. Reboot the device

## Uninstalling
1. Remove the APK.
2. Reboot the device.

## Key words
fingerprint when screen off, AOD fingerprint