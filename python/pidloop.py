from time import sleep
import asyncio

ITERTIME = 0.1

class VelocitySim ():
    ANGLEMAX = 20

    def __init__(self):
        self.angle = 0
        self.pos = 0

    def setpos (self, pos):
        self.pos = pos

    def setangle (self, angle):
        self.angle = angle

    async def runloop (self):
        print("starting", self.pos)
        while True:

            if self.angle > self.ANGLEMAX:
                self.angle = self.ANGLEMAX
            
            self.pos += (20 * (self.angle / 100))

            print(self.pos)

            await asyncio.sleep(ITERTIME)


        

class PIDTesting ():
    PTERM = 0.5
    ITERM = 0.2
    DTERM = 0.0

    def __init__(self) -> None:
        self.last_e = None
        self.last_i = None

        self.target = None
        self.current = None

    async def calcpid (self, sp, pv):
        error = sp - pv

        if self.last_e is None and self.last_i is None:
            self.last_e = 0
            self.last_i = 0
        
        Pout = error * self.PTERM

        Iout = (error + self.last_e * ITERTIME) * self.ITERM

        Dout = ((error - self.last_e) / ITERTIME) * self.DTERM

        self.last_e = error
        self.last_i = Iout

        return Pout + Iout + Dout

    async def runloop (self, sim, target, current):
        self.target = target
        self.current = current

        sim.setpos(current)

        asyncio.create_task(sim.runloop())
        
        while True:

            #print(self.current)
            self.current = sim.pos

            print("current", self.current)

            self.current = self.current + await self.calcpid(self.target, self.current)

            sim.setangle(current)

            await asyncio.sleep(ITERTIME)


if __name__ == "__main__":
    targ = int(input("target value: "))
    curr = int(input("starting value: "))

    runner = PIDTesting()

    velocity = VelocitySim()


    asyncio.run(runner.runloop(velocity, targ, curr))