package com.dji.launchpad;

public class PIDControllerHandler {
    /*
    error_prior = 0
    integral_prior = 0
    KP = Some value you need to come up (see tuning section below)
    KI = Some value you need to come up (see tuning section below)
    KD = Some value you need to come up (see tuning section below)
    bias = 0 (see below)

    while(1) {
    error = desired_value – actual_value
    integral = integral_prior + error * iteration_time
    derivative = (error – error_prior) / iteration_time
    output = KP*error + KI*integral + KD*derivative + bias
    error_prior = error
    integral_prior = integral
    sleep(iteration_time)
    }

    */
}
