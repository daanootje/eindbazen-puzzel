package org.pandora.control.domain;

import org.pandora.control.stateMachine.RoomSM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.Response;

@RestController
@RequestMapping("/log")
public class Log {

    private RoomSM roomSM;

    @Autowired
    public Log(RoomSM roomSM) {
        this.roomSM = roomSM;
    }

    @RequestMapping(value = "/store", method = RequestMethod.POST, consumes = "application/json")
    public Response storeData(@RequestBody String groupname) {
        if (roomSM.getFinished()) {
            roomSM.storeData(groupname);
        }
        return Response.accepted().build();
    }

    @RequestMapping(value = "/retrieve", method = RequestMethod.GET, produces = "application/json")
    public Response retrieveData() {
        return Response.accepted().entity(roomSM.retrieveData()).build();
    }

    @RequestMapping(value = "/retrieve/{groupname}", method = RequestMethod.GET, produces = "application/json")
    public Response retrieveData(@PathVariable String groupname) {
        return Response.accepted().entity(roomSM.retrieveData(groupname)).build();
    }

}
