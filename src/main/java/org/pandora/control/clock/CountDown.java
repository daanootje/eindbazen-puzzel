package org.pandora.control.clock;

import java.util.Timer;
import java.util.TimerTask;

public class CountDown {

    private Timer timer;
    private Integer timeRemaining;
    private Integer maxTime;
	private TimerTask task;
    
    public CountDown(int startTimeInSeconds) {
    	timer = new Timer();
    	maxTime = startTimeInSeconds;
    	timeRemaining = startTimeInSeconds;
    	task = new TimerTask() {
            public void run() {
            	reduceTime(1);
            }
        };
    }
    
    public void start() {
        timer.scheduleAtFixedRate(task, 1000, 1000);
    }
    
	public Integer getRemainingTimeInSeconds() {
		return timeRemaining;
	}
	
	public void pause() {
		timer.cancel();
	}
	
	public void resume() {
	    this.timer = new Timer();
	    this.timer.schedule(task, 1000, 1000);
	}
	
	public void restart() {
		pause();
		timeRemaining = maxTime;
		resume();
	}
	
	private final void reduceTime(int reduction) {
		if(timeRemaining <= 0) {
			pause();
		} else {
			timeRemaining -= reduction;
		}
	}
    
}
