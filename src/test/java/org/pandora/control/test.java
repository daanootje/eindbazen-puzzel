package org.pandora.control;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import lombok.Data;
import org.junit.Test;
import org.pandora.control.puzzle.Puzzle;
import org.pandora.control.music.AudioManager;
import org.pandora.control.puzzle.PuzzleManager;
import org.pandora.control.stateMachine.RoomSM;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class test {

    @Test
    public void test() throws Exception {
        AudioManager manager = new AudioManager("C:/Users/D.Rotman/Downloads/audio");
        manager.playMusic("cartoon001");

        Thread.sleep(5000);
    }

    @Test
    public void test2() throws Exception {
        URL url = test.class.getResource("/sequenceConf.json");
        String json = Resources.toString(url, Charsets.UTF_8);
        JsonObject jsonTree = new JsonParser().parse(json).getAsJsonObject();
        JsonArray array = jsonTree.getAsJsonArray("sequence");
        List<Map<String,State>> list = new Gson().fromJson(array,new TypeToken<List<Map<String,State>>>(){}.getType());

        List<State> litr = new ArrayList<>(flattenMap(list).values());

        System.out.println(litr);
    }

    private <T> Map<String,T> flattenMap(List<Map<String,T>> nestedMap) {
        return nestedMap.stream()
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue));
    }

    @Data
    private class State {
        private String current;
        private String to;
        private String event_trigger;
        private String action;
    }

}
