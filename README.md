# MapsLauncher
This is the launcher for our Team17 RPI3 device (though it could be used for any android device)

## Limitations ##
* There is no GPS support currently, only a default location to demonstrate map functionality.
  * Actual location support is in the works, limited by RPI3 location issues currently.
* Only selects from our list of installed apps (more can easily be added if desired) which include:
  * Google Play Store
  * Spotify
  * Settings
  * Team17 Bluetooth Setup App
* Layout XML has been optimized for RPI3 output to an external monitor/TV through HDMI, so scalaing and sizes may be off on different devices

## Install Notes ##
When running the app from Android Studio, in order to prevent the "No Default Activity" error when running: 
* Click the dropdown menu to the left of the run button in the toolbar and choose 'edit configurations'
* Then change the 'default activity' option to 'specified activity' and select the MapsActivity
