package org.pandora.control.domain;

import org.pandora.control.stateMachine.RoomSM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.Response;

@RestController
@RequestMapping("/room")
public class Room {

    private RoomSM roomSM;

    @Autowired
    public Room(RoomSM roomSM) {
        this.roomSM = roomSM;
    }

    @RequestMapping(value = "/start", method = RequestMethod.POST, consumes = "application/json")
    public Response startRoom(@RequestBody String port) {
        roomSM.startSM(port);
        return Response.accepted().build();
    }

    @RequestMapping(value = "/stop", method = RequestMethod.POST, consumes = "application/json")
    public Response stopRoom() {
        roomSM.stopSM();
        return Response.accepted().build();
    }

    @RequestMapping(value = "/reset", method = RequestMethod.POST, consumes = "application/json")
    public Response resetRoom() {
        roomSM.resetSM();
        return Response.accepted().build();
    }

    @RequestMapping(value = "/finish", method = RequestMethod.POST, consumes = "application/json")
    public Response finishRoom() {
        roomSM.finishSM();
        return Response.accepted().build();
    }

    @RequestMapping(value = "/puzzles/start", method = RequestMethod.POST, consumes = "application/json")
    public Response startPuzzles() {
        roomSM.startPuzzles();
        return Response.accepted().build();
    }

    @RequestMapping(value = "/state", method = RequestMethod.GET, produces = "application/json")
    public Response getState() {
        return Response.ok().entity(roomSM.getState()).build();
    }

    @RequestMapping(value = "/ports", method = RequestMethod.GET, produces = "application/json")
    public Response getPorts() {
        return Response.ok().entity(roomSM.getAvailablePorts()).build();
    }

}
