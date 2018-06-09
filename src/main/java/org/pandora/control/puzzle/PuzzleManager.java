package org.pandora.control.puzzle;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import lombok.Data;
import org.pandora.control.model.Puzzle;
import org.pandora.control.model.state.PuzzleState;
import org.pandora.control.serialcomm.SerialInterpreter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PuzzleManager {

    private Map<String,Puzzle> puzzleMap;

    public PuzzleManager() throws IOException {
        URL url = SerialInterpreter.class.getResource("/applicationConf.json");
        String json = Resources.toString(url, Charsets.UTF_8);
        JsonObject jsonTree = new JsonParser().parse(json).getAsJsonObject();
        JsonArray array = jsonTree.getAsJsonArray("puzzles");
        List<Map<String,DeserializePuzzle>> list = new Gson().fromJson(array,
                new TypeToken<List<Map<String,DeserializePuzzle>>>(){}.getType());
        puzzleMap = flattenMap(list).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> convertToPuzzle(e.getKey(), e.getValue())));
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

    private Puzzle convertToPuzzle(String name, DeserializePuzzle puzzle) {
        return Puzzle.builder()
                .identifier(puzzle.identifier)
                .name(name)
                .SH(puzzle.SH)
                .SL(puzzle.SL)
                .PC_Puzzle(flattenMap(puzzle.PC_Puzzle))
                .Puzzle_PC(flattenMap(puzzle.Puzzle_PC))
                .build();
    }

    private <T> Map<String,T> flattenMap(List<Map<String,T>> nestedMap) {
        return nestedMap.stream()
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue));
    }

    @Data
    private class DeserializePuzzle {
        private String name;
        private String SL;
        private String SH;
        private Character identifier;
        private List<Map<String, Puzzle.Operation>> PC_Puzzle;
        private List<Map<String, Puzzle.Operation>> Puzzle_PC;
        private PuzzleState puzzleState;
    }

}
