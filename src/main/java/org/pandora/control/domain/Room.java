package org.pandora.control.domain;

import org.pandora.api.controller.model.RoomStatus;
import org.pandora.control.stateMachine.RoomSequence.RoomSM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
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

    @RequestMapping(method = RequestMethod.POST, consumes = "application/json")
    public Response startStopResetAudio(@PathVariable String audioName, @RequestBody RoomStatus roomStatus) {
        switch (roomStatus.getStatus()) {
            case start:
                roomSM.startPuzzles();
                break;
            case finish:
                roomSM.finishSM();
                break;
            case stop:
                roomSM.stopSM();
                break;
            case restart:
                roomSM;
                break;
            case resume:
                roomSM;
                break;
        }
        return Response.accepted().build();
    }

}
