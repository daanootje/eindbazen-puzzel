package org.pandora.control.clock;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CountDown {

	private volatile boolean isRunning = false;

    private Integer timeRemaining;
    private Integer maxTime;

	private ScheduledExecutorService execService = Executors.newSingleThreadScheduledExecutor();
	private Future<?> future = null;
    
    public CountDown(Integer startTimeInSeconds) {
    	maxTime = startTimeInSeconds;
    	timeRemaining = startTimeInSeconds;
    }
    
    public void start() {
		if (isRunning)
			return;

		isRunning = true;
		future = execService.scheduleWithFixedDelay(this::reduceTime, 0, 1000, TimeUnit.MILLISECONDS);
    }

	public void pause() {
		if(!isRunning) return;
		future.cancel(false);
		isRunning = false;
	}
    
	public Integer getRemainingTimeInSeconds() {
		return timeRemaining;
	}

	public Integer getElapsedTime() {
    	return maxTime - timeRemaining;
	}
	
	public void restart() {
		pause();
		timeRemaining = maxTime;
	}
	
	private synchronized void reduceTime() {
		if(timeRemaining <= 0) {
			future.cancel(false);
		} else {
			timeRemaining -= 1;
		}
	}

}
