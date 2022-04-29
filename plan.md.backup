## Current Control Schema
* Vertical
	* Velocity over Time
* Roll/Pitch
	* Angle over Time
* Yaw
	* Absolute angle (adjusted against north)

## Control layout
* class containes flightqueue, a linked list holding flightqueuedata objects
* to put a command in the queue, add a flightqueuedata object to the queue, and it will execute on time sequentially
* emergency level button will empty the queue, reset any timings, and set all flight positional data to 0