package org.pandora.control.domain;

import org.pandora.control.puzzle.PuzzleManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.Response;

@RestController
@RequestMapping("/puzzle")
public class Puzzle {

    private PuzzleManager puzzleManager;

    @Autowired
    public Puzzle(PuzzleManager puzzleManager) {
        this.puzzleManager = puzzleManager;
    }

    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public Response getPuzzleNames() {
        return Response.ok().entity(puzzleManager.getPuzzles().keySet()).build();
    }

    @RequestMapping(value = "/{puzzleName}", method = RequestMethod.GET, produces = "application/json")
    public Response getPuzzleState(@PathVariable String puzzleName) {
        return puzzleManager.getPuzzle(puzzleName)
                .map(org.pandora.control.puzzle.Puzzle::getPuzzleState)
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    @RequestMapping(value = "/{puzzleName}/info", method = RequestMethod.GET, produces = "application/json")
    public Response getPuzzleStateInfo(@PathVariable String puzzleName) {
        return puzzleManager.getPuzzle(puzzleName)
                .map(org.pandora.control.puzzle.Puzzle::getStateInfo)
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }

}
