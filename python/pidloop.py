from time import sleep
import asyncio

class velocitySim ():
    def __init__(self):
        self.angle = 0
    
    async def runloop (self):
        pass

class PIDTesting ():
    PTERM = 0.5
    ITERM = 0.2
    DTERM = 0.0
    ITERTIME = .1

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

        Iout = (error + self.last_e * self.ITERTIME) * self.ITERM

        Dout = ((error - self.last_e) / self.ITERTIME) * self.DTERM

        self.last_e = error
        self.last_i = Iout

        return Pout + Iout + Dout

    async def runloop (self, target, current):
        self.target = target
        self.current = current
        
        while True:

            print(self.current)
            self.current = self.current + await self.calcpid(self.target, self.current)

            await asyncio.sleep(self.ITERTIME)


if __name__ == "__main__":
    targ = int(input("target value: "))
    curr = int(input("starting value: "))

    runner = PIDTesting()

    asyncio.run(runner.runloop(targ, curr))