package org.pandora.control.domain;

import org.pandora.control.display.DesktopApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.Response;

@RestController
@RequestMapping("/display")
public class Display {

    private DesktopApi desktopApi;

    @Autowired
    public Display(DesktopApi desktopApi) {
        this.desktopApi = desktopApi;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = "application/json")
    public Response startTimerOnDisplay(@RequestBody String uri) {
        desktopApi.openBrowser(uri);
        return Response.accepted().build();
    }

}
