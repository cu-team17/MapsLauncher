# MapsLauncher
This is the launcher for our Team17 RPI3 device (though it could be used for any android device)

## Limitations ##
* Location support does work, but is only tested using the emulator (and assumed to work on any device with working GPS) as the RPI3 is currently presenting some HW limitations regarding location.
* Only selects from our list of installed apps (more can easily be added if desired) which include:
  * Google Play Store
  * Spotify
  * Settings
  * Team17 Bluetooth Setup App
  * Contacts
  * Chrome
  * Calendar
* Layout XML has been written using px instead of the normal dp, so it will only display correctly at a resolution of 1280 x 720 (regardless of size now however).

## Install Notes ##
When running the app from Android Studio, in order to prevent the "No Default Activity" error when running: 
* Click the dropdown menu to the left of the run button in the toolbar and choose 'edit configurations'
* Then change the 'default activity' option to 'specified activity' and select the MapsActivity
