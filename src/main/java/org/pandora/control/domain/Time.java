package org.pandora.control.domain;

import javax.ws.rs.core.Response;

import org.pandora.api.controller.NotFoundException;
import org.pandora.api.controller.TimeApi;
import org.pandora.api.controller.model.TimeStatus;
import org.pandora.control.clock.CountDown;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

@Component
@MessageMapping("/time")
@SendTo("/topic/messages")
public class Time implements TimeApi {

	private CountDown time;

	@Autowired
	public Time(CountDown time) {
		this.time = time;
	}

	@Override
	public Response retrieveTime() throws NotFoundException {
		return null;
	}

	@Override
	public Response startStopResetTime(TimeStatus timeStatus) {
		switch (timeStatus.getStatus()) {
			case stop:
				time.pause();
			case start:
				time.start();
			case restart:
				time.restart();
		}
		return Response.accepted().build();
	}

}
