## Plan Page for Launchpad App
### Steps

* start with sdk registration, then move to binding to aircraft and takeoff
* add in basic buttons to control (takeoff, home, land, set home)
* add page for video feed?


### Notes
* mavic mini original
* wifi communication direct with device, should not need controller
* class wifilink is used to change drone's wifi settings
* **AS IT SEEMS** you can just connect to drones wifi and then call some of the on product connect methods to initialize product

### SDK Class/Method Tree
* DJISDKManager
	* register app
	* sdk manager instance
	* get product connection
	
### Methodology
* use virtual stick control with position checking to have set forward/backward distance buttons (on button press, set threshold, tilt craft, check until threshold is met, flatten craft, tilt in opposite direction to correct unless craft does this automatically)


# Make simpler api
* tilt functionality and return position relative to home, simplistic and clean, abstract test out to other classes 