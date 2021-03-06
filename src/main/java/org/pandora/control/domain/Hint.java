package org.pandora.control.domain;

import org.pandora.control.hints.HintManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.Response;

@RestController
@RequestMapping("/puzzle")
public class Hint {

    private HintManager hintManager;

    @Autowired
    public Hint(HintManager hintManager) {
        this.hintManager = hintManager;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = "application/json")
    public Response createHintForPuzzle(@RequestBody String puzzleName) {
        hintManager.displayHint(puzzleName);
        return Response.accepted().build();
    }

}
