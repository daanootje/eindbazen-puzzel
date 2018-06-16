package org.pandora.control.puzzle;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.pandora.control.music.AudioManager;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.pandora.control.util.Deserializer.flattenMap;

@Slf4j
public class PuzzleManager {

    private Map<String,Puzzle> puzzleMap;

    public PuzzleManager(AudioManager audioManager, String configFolder) throws IOException {
        puzzleMap = parseConf(audioManager, String.format("%s/puzzlesConf.json", configFolder));
    }

    private Optional<Puzzle> getPuzzle(String identifier) {
        return puzzleMap.values().stream()
                .filter(puzzle -> puzzle.getIdentifier().equals(identifier))
                .findFirst();
    }

    public Optional<String> getPuzzleState(String puzzleName) {
        return Optional.ofNullable(puzzleMap.get(puzzleName))
                .map(Puzzle::getPuzzleState);
    }

    public Optional<String> getPuzzleStateInfo(String puzzleName) {
        return Optional.ofNullable(puzzleMap.get(puzzleName))
                .map(Puzzle::getStateInfo);
    }

    public Boolean isPresent(String puzzleName) {
        return puzzleMap.containsKey(puzzleName);
    }

  public Map<String,Puzzle> getPuzzles() {
        return puzzleMap;
    }

    public Optional<Pair<String, String>> getPC_PuzzleCommand(String puzzleName, String name) {
        return Optional.ofNullable(puzzleMap.get(puzzleName))
                .map(puzzle -> {
                    Optional<String> command = puzzle.getPC_PuzzleCommand(name);
                    return command.map(s -> new Pair<>(puzzle.getIdentifier(), s)).orElse(null);
                });
    }

    public Optional<Pair<String, String>> applyToPuzzle(String puzzleID, String message) {
        return this.getPuzzle(puzzleID)
                .map(puzzle -> new Pair<>(puzzle.getName(), puzzle.apply(message)));
    }

    private Puzzle convertToPuzzle(String name, DeserializePuzzle puzzle, AudioManager audioManager) {
        return Puzzle.builder()
                .audioManager(audioManager)
                .identifier(puzzle.getIdentifier())
                .name(name)
                .SH(puzzle.getSH())
                .SL(puzzle.getSL())
                .puzzleState("init")
                .stateInfo("initializing puzzle")
                .PC_Puzzle(flattenMap(puzzle.getPC_Puzzle()))
                .Puzzle_PC(flattenMap(puzzle.getPuzzle_PC()))
                .build();
    }

    private Map<String,Puzzle> parseConf(AudioManager audioManager, String configFolder) throws IOException {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        List<Map<String,DeserializePuzzle>> list = mapper.readValue(new File(configFolder),
                new TypeReference<List<Map<String,DeserializePuzzle>>>(){});
        return flattenMap(list).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> convertToPuzzle(e.getKey(), e.getValue(), audioManager)));

    }
}
