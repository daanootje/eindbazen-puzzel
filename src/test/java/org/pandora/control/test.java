package org.pandora.control;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.junit.Test;
import org.pandora.control.music.AudioManager;

import java.io.File;
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
        ObjectMapper mapper = new ObjectMapper();
        String json = "[{\"SH\":\"test\",\"SL\":\"test\",\"identifier\":\"test\"}, {\"SH\":\"test\",\"SL\":\"test\",\"identifier\":\"test\"}]";

        mapper.readValue(new File("C:/Users/D.Rotman/Downloads/room/config/puzzlesConf.json"),
                new TypeReference<List<Map<String,DeserializePuzzle>>>(){});
//        JsonObject jsonTree = new JsonParser().parse(json).getAsJsonObject();
//        JsonArray array = jsonTree.getAsJsonArray("sequence");
//        List<Map<String,State>> list = new Gson().fromJson(array,new TypeToken<List<Map<String,State>>>(){}.getType());
//
//        List<State> litr = new ArrayList<>(flattenMap(list).values());
//
//        System.out.println(litr);
    }

    @Data
    private static class Test0 {
        private List<Test1> puzzles = new ArrayList<>();

        public Test0(List<Test1> puzzles) {
            this.puzzles = puzzles;
        }
        public Test0() {}
    }

    @Data
    private class Test1 {
        private Map<String,DeserializePuzzle> puzzles;

        public Test1() {

        }
    }

    @Data
    private static class DeserializePuzzle {
        @JsonProperty("SL")
        private String SL;
        @JsonProperty("SH")
        private String SH;
        @JsonProperty("identifier")
        private String identifier;
        @JsonProperty("PC_Puzzle")
        private List<Map<String, Operation>> PC_Puzzle;
        @JsonProperty("Puzzle_PC")
        private List<Map<String, Operation>> Puzzle_PC;
        private String puzzleState;

        public DeserializePuzzle(){}

    }

    @Data
    public static class Operation {
        @JsonProperty("name")
        private String name;
        @JsonProperty("type")
        private String type;

        public Operation(){}
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
