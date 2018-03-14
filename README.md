# MapsLauncher
This is the launcher for our Team17 RPI3 device (though it could be used for any android device)

There is no GPS support currently, only a default location to demonstrate map functionality. Actual location support is in the works.

## Install Notes ##
When running the app from Android Studio, in order to prevent the "No Default Activity" error when running: 
* Click the dropdown menu to the left of the run button in the toolbar and choose 'edit configurations'
* Then change the 'default activity' option to 'specified activity' and select the MapsActivity
