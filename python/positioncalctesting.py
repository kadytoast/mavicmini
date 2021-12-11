from math import *


while True:
    print("x val 1 (origin)")
    x1 = int(input())

    print("y val 1 (origin)")
    y1 = int(input())

    print("origin heading, deg from true north")
    deg1 = int(input())
    
    print("x val 2 (target)")
    x2 = int(input())
    
    print("y val 2 (target)")
    y2 = int(input())

    print("hypotenuse angle")
    print(degrees(atan((y2-y1)/(x2-x1))))

    print("\n\n")
