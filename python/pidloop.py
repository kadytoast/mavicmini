



class PIDTesting ():
    PTERM = 0.5
    ITERM = 0.1
    DTERM = 0.1

    def __init__(self) -> None:
        self.last_e = None
        self.last_d = None

    def calcpid (self, sp, pv):
        error = sp - pv

        if self.last_e is None and self.last_d is None:
            self.last_e = 0
        
        Pout = error * self.PTERM

        Iout = error + self.last_e

    def runloop (self):
        while True:
            pass