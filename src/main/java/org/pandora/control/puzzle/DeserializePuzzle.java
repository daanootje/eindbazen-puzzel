package org.pandora.control.puzzle;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DeserializePuzzle {
    private String name;
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
