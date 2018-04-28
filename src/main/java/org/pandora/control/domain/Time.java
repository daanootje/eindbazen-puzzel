package org.pandora.control.domain;

import org.pandora.api.controller.model.TimeStatus;
import org.pandora.control.clock.CountDown;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.Response;

@RestController
@EnableScheduling
@RequestMapping("/time")
public class Time {

	private CountDown time;
    private SimpMessageSendingOperations template;

	@Autowired
	public Time(CountDown time, SimpMessageSendingOperations template) {
		this.time = time;
        this.template = template;
	}

	@MessageMapping("/time/remaining")
	@SendTo("/topic/time")
	public Integer send() {
		return time.getRemainingTimeInSeconds();
	}

    @Scheduled(fixedRate = 1000)
    private void updating() {
        this.template.convertAndSend("/topic/time", time.getRemainingTimeInSeconds());
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
