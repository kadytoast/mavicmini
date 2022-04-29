# converts log file to a kml plot
# uses start/end time to convert through range of points
# kml plot will be single file with stack of points
# point id will be the time of the data

import re

logtitle = input("name of target log (XX-XX-XXXX): ")
with open(f"mavic_logs/{logtitle}", 'r') as logf:
    rawlog = logf.read()

##### add proper headings/endings to logs then come back #####

# slice rawlog into list of entries
endline = False
entrylist = []
curentry = ""
for char in rawlog:
    # append character to entry
    curentry += char
    # check if its ending line (last line contains Y for the offset)
    if char == "Y":
        endline = True
    # if its on the last line and at the end of the line, 
    # reset the entry and add to list    
    if endline and char == "\n":
        endline = False
        entrylist.append(curentry)
        curentry = ""

# entrylist now contains all log entries in seperate indexes
for entry in entrylist:
    # get lines inside entry
    entrylines = entry.split("\n")
    # seperate and format time value
    time = entrylines[0].split(" :")[0]
    time = re.sub("T", " ", time)
    

"""
<Point id="ID">
  <extrude>0</extrude>                        <!-- boolean -->
  <altitudeMode>clampToGround</altitudeMode>
        <!-- kml:altitudeModeEnum: clampToGround, relativeToGround, or absolute -->
        <!-- or, substitute gx:altitudeMode: clampToSeaFloor, relativeToSeaFloor -->
  <coordinates>...</coordinates>              <!-- lon,lat[,alt] -->
</Point>
"""