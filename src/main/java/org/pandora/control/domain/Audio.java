package org.pandora.control.domain;

import org.pandora.api.controller.model.AudioStatus;
import org.pandora.api.controller.model.TimeStatus;
import org.pandora.control.music.AudioManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.core.Response;

@RestController
@EnableScheduling
@RequestMapping("/audio")
public class Audio {

    private AudioManager audioManager;

    @Autowired
    public Audio(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    @RequestMapping(value = "/{audioName}", method = RequestMethod.POST, consumes = "application/json")
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
        }
        return Response.accepted().build();
    }

    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public Response getAudio() {
        return Response.ok().entity(audioManager.getAudioList()).build();
    }

}
