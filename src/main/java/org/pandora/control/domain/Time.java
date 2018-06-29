package org.pandora.control.domain;

import org.pandora.api.controller.model.TimeStatus;
import org.pandora.control.clock.CountDown;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.TextMessage;

import javax.ws.rs.core.Response;

@RestController
@EnableScheduling
@RequestMapping("/time")
public class Time {

	private CountDown time;
	private CustomWebSocketHandler customWebSocketHandler;

	@Autowired
	public Time(CountDown time, CustomWebSocketHandler customWebSocketHandler) {
		this.customWebSocketHandler = customWebSocketHandler;
		this.time = time;
	}

    @Scheduled(fixedRate = 1000)
    private void updating() {
        customWebSocketHandler.sendMessageToUsers("time_remaining", new TextMessage( time.getRemainingTimeInSeconds().toString()));
    }

    @RequestMapping(method = RequestMethod.POST, consumes = "application/json")
	public Response startStopResetTime(@RequestBody TimeStatus timeStatus) {
		switch (timeStatus.getStatus()) {
			case resume:
				time.resume();
				break;
			case stop:
				time.pause();
				break;
			case start:
				time.start();
				break;
			case restart:
				time.restart();
				break;
		}
		return Response.accepted().build();
	}

}
