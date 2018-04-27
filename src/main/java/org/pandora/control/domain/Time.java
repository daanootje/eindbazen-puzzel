package org.pandora.control.domain;

import javax.ws.rs.core.Response;

import lombok.extern.slf4j.Slf4j;
import org.pandora.api.controller.NotFoundException;
import org.pandora.api.controller.TimeApi;
import org.pandora.api.controller.model.TimeStatus;
import org.pandora.control.clock.CountDown;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

//@Component
@Slf4j
@Controller
public class Time implements TimeApi{

	private CountDown time;

	@Autowired
	public Time(CountDown time) {
		this.time = time;
	}

	@Override
	public Response retrieveTime() throws NotFoundException {
		Gson gson = new Gson();
		return null;
	}

	@MessageMapping("/time/remaining")
	@SendTo("/topic/time")
	public Greeting send(HelloMessage message) throws Exception {
		System.out.println("SOMETHING");
		Thread.sleep(1000); // simulated delay
		return new Greeting("Hello, " + HtmlUtils.htmlEscape(message.getName()) + "!");
	}

	@Override
	public Response startStopResetTime(TimeStatus timeStatus) {
		switch (timeStatus.getStatus()) {
			case resume:
				time.resume();
			case stop:
				time.pause();
			case start:
				time.start();
			case restart:
				time.restart();
		}
		return Response.accepted().build();
	}

	public class Greeting {

		private String content;

		public Greeting() {
		}

		public Greeting(String content) {
			this.content = content;
		}

		public String getContent() {
			return content;
		}

	}

	public class HelloMessage {

		private String name;

		public HelloMessage() {
		}

		public HelloMessage(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

}
