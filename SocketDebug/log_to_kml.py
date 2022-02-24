# converts log file to a kml plot
# uses start/end time to convert through range of points
# kml plot will be single file with stack of points
# point id will be the time of the data

logtitle = input("name of target log (XX-XX-XXXX): ")
with open(f"mavic_logs/{logtitle}") as logf:
    rawlog = logf.read()
