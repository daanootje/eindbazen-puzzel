package org.pandora.control.puzzle;

import com.fasterxml.jackson.core.type.TypeReference;
import org.javatuples.Pair;
import org.pandora.control.music.AudioManager;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.pandora.control.util.Deserializer.flattenMap;

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
        Optional<String> state = this.getPuzzle(puzzleName).map(Puzzle::getPuzzleState);
        if(state.isPresent()) {
            return state;
        } else {
            return Optional.ofNullable(puzzleMap.get(puzzleName))
                    .map(Puzzle::getPuzzleState);
        }
    }

    public Map<String,String> getPuzzleStates() {
        return puzzleMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getPuzzleState()));
    }

    public Optional<String> getPuzzleStateInfo(String puzzleName) {
        Optional<String> stateInfo = this.getPuzzle(puzzleName).map(Puzzle::getStateInfo);
        if(stateInfo.isPresent()) {
            return stateInfo;
        } else {
            return Optional.ofNullable(puzzleMap.get(puzzleName))
                    .map(Puzzle::getStateInfo);
        }
    }

    public Optional<String> getPuzzleName(String puzzleName) {
        Optional<String> name = this.getPuzzle(puzzleName).map(Puzzle::getName);
        if(name.isPresent()) {
            return name;
        } else {
            return Optional.ofNullable(puzzleMap.get(puzzleName))
                    .map(Puzzle::getName);
        }
    }

    public Map<String,Puzzle> getPuzzles() {
        return puzzleMap;
    }

    public Optional<Pair<String, String>> getPC_PuzzleCommand(String puzzleName, String name) {
        Optional<Pair<String, String>> pair =
                this.getPuzzle(puzzleName)
                        .map(puzzle -> {
                            Optional<String> command = puzzle.getPC_PuzzleCommand(name);
                            return command.map(s -> new Pair<>(puzzle.getIdentifier(), s)).orElse(null);
                        });
        if(pair.isPresent()) {
            return pair;
        } else {
            return Optional.ofNullable(puzzleMap.get(puzzleName))
                    .map(puzzle -> {
                        Optional<String> command = puzzle.getPC_PuzzleCommand(name);
                        return command.map(s -> new Pair<>(puzzle.getIdentifier(), s)).orElse(null);
                    });
        }
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
                .puzzleState("Unknown")
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
