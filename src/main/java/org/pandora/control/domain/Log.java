package org.pandora.control.domain;

import org.pandora.control.clock.CountDown;
import org.pandora.control.data.DataManager;
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

    private DataManager dataManager;
    private RoomSM roomSM;
    private CountDown countDown;

    @Autowired
    public Log(DataManager dataManager, RoomSM roomSM, CountDown countDown) {
        this.dataManager = dataManager;
        this.roomSM = roomSM;
        this.countDown = countDown;
    }

    @RequestMapping(value = "/store", method = RequestMethod.POST, consumes = "application/json")
    public Response storeData(@RequestBody String groupName) {
        if (roomSM.getFinished()) {
            dataManager.storeData(groupName, roomSM.getPuzzleData(), countDown.getRemainingTimeInSeconds(), roomSM.getSucceeded());
        }
        return Response.accepted().build();
    }

    @RequestMapping(value = "/retrieve", method = RequestMethod.GET, produces = "application/json")
    public Response retrieveData() {
        return Response.accepted().entity(dataManager.retrieveData()).build();
    }

    @RequestMapping(value = "/retrieve/{groupname}", method = RequestMethod.GET, produces = "application/json")
    public Response retrieveData(@PathVariable String groupname) {
        return Response.accepted().entity(dataManager.retrieveData(groupname)).build();
    }

}
