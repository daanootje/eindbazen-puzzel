package org.pandora.control.domain;

import org.pandora.control.stateMachine.RoomSequence.RoomSM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/puzzle")
public class Puzzle {

    private RoomSM roomSM;

    @Autowired
    public Puzzle(RoomSM roomSM) {
        this.roomSM = roomSM;
    }




}
