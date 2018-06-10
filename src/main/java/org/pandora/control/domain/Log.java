package org.pandora.control.domain;

import org.pandora.control.data.DataManager;
import org.pandora.control.puzzle.PuzzleManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.Response;

@RestController
@RequestMapping("/log")
public class Log {

    private DataManager dataManager;

    @Autowired
    public Log(PuzzleManager puzzleManager) {
        this.dataManager = dataManager;
    }

    @RequestMapping(value = "/store", method = RequestMethod.POST, consumes = "application/json")
    public Response storeData(@RequestBody String groupName) {
        dataManager.storeData(groupName);
        return Response.accepted().build();
    }

    @RequestMapping(value = "/retrieve", method = RequestMethod.GET, produces = "application/json")
    public Response retrieveData() {
        dataManager.storeData(groupName);
        return Response.accepted().entity().build();
    }

}
