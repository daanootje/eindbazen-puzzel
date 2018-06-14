package org.pandora.control.puzzle;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import lombok.Data;
import org.pandora.control.music.AudioManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.pandora.control.util.Deserializer.flattenMap;

public class PuzzleManager {

    private Map<String,Puzzle> puzzleMap;
    private AudioManager audioManager;

    @Autowired
    public PuzzleManager(AudioManager audioManager, String configFolder) throws IOException {
        this.audioManager = audioManager;
        String fileName = String.format("%s/puzzlesConf.json", configFolder);
        puzzleMap = parseConf(fileName);
    }

    public Optional<Puzzle> getPuzzle(byte identifier) {
        if(identifier == 0) {
            return Optional.empty();
        } else {
            return puzzleMap.values().stream()
                    .filter(puzzle -> puzzle.getIdentifier() == (char)identifier)
                    .findFirst();
        }
    }

    public Optional<Puzzle> getPuzzle(String name) {
        return Optional.ofNullable(puzzleMap.get(name));
    }

    public Map<String,Puzzle> getPuzzles() {
        return puzzleMap;
    }

    private Puzzle convertToPuzzle(String name, DeserializePuzzle puzzle) {
        return Puzzle.builder()
                .audioManager(audioManager)
                .identifier(puzzle.identifier)
                .name(name)
                .SH(puzzle.SH)
                .SL(puzzle.SL)
                .puzzleState("init")
                .stateInfo("initializing puzzle")
                .PC_Puzzle(flattenMap(puzzle.PC_Puzzle))
                .Puzzle_PC(flattenMap(puzzle.Puzzle_PC))
                .build();
    }

    @Data
    private class DeserializePuzzle {
        private String name;
        private String SL;
        private String SH;
        private Character identifier;
        private List<Map<String, Puzzle.Operation>> PC_Puzzle;
        private List<Map<String, Puzzle.Operation>> Puzzle_PC;
        private String puzzleState;
    }

    private Map<String,Puzzle> parseConf(String fileName) throws IOException {
        String json = new Gson().toJson(new FileReader(fileName));
        JsonObject jsonTree = new JsonParser().parse(json).getAsJsonObject();
        JsonArray array = jsonTree.getAsJsonArray("puzzles");
        List<Map<String,DeserializePuzzle>> list = new Gson().fromJson(array,
                new TypeToken<List<Map<String,DeserializePuzzle>>>(){}.getType());
        return flattenMap(list).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> convertToPuzzle(e.getKey(), e.getValue())));
    }

}
