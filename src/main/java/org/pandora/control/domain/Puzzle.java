package org.pandora.control.domain;

import org.pandora.api.controller.model.AudioStatus;
import org.pandora.control.stateMachine.RoomSequence.RoomSM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.Response;

@RestController
@RequestMapping("/puzzle")
public class Puzzle {

    private RoomSM roomSM;

    @Autowired
    public Puzzle(RoomSM roomSM) {
        this.roomSM = roomSM;
    }

    @RequestMapping(value = "/{puzzleName}", method = RequestMethod.GET, produces = "application/json")
    public Response getPuzzleState(@PathVariable String puzzleName) {
        roomSM.
        return Response.ok().build();
    }

    @RequestMapping(value = "/{puzzleName}", method = RequestMethod.POST, consumes = "application/json")
    public Response startStopResetAudio(@PathVariable String audioName, @RequestBody AudioStatus audioStatus) {
        switch (audioStatus.getStatus()) {
            case pause:
                audioManager.pauseMusic(audioName);
                break;
            case play:
                audioManager.playMusic(audioName);
                break;
            case restart:
                audioManager.restartMusic(audioName);
                break;
            case resume:
        }
        return Response.accepted().build();
    }

}
