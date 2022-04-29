try:
    import usocket as socket
except:
    import socket

import network

import esp
esp.osdebug(None)

import gc
gc.collect()

ssid = 'ESP_SocketDebugTest'
password = '1234567890'

ap = network.WLAN(network.AP_IF)
ap.active(True)
ap.config(essid=ssid, password=password)

while ap.active() == False:
    pass

print('Connection successful')
print(ap.ifconfig())