package org.pandora.control.model;

import lombok.Value;
import org.apache.commons.lang3.time.StopWatch;


@Value
public class Clock {

    private StopWatch time;

    public Clock() {
        time = new StopWatch();
    }

    public

}
