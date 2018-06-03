package org.pandora.control.serialcomm;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import gnu.io.SerialPortEvent;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.pandora.control.model.Puzzle;
import org.pandora.control.model.state.PuzzleState;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SerialInterpreter extends SerialCommunicator {

    private Map<Character,Puzzle> puzzleMap;

    public SerialInterpreter() throws IOException {
        super();
        URL url = SerialInterpreter.class.getResource("/applicationConf.json");
        String json = Resources.toString(url, Charsets.UTF_8);
        JsonObject jsonTree = new JsonParser().parse(json).getAsJsonObject();
        JsonArray array = jsonTree.getAsJsonArray("puzzles");
        List<Map<String,DeserializePuzzle>> list = new Gson().fromJson(array,
                new TypeToken<List<Map<String,DeserializePuzzle>>>(){}.getType());
        puzzleMap = flattenMap(list).entrySet().stream()
                .collect(Collectors.toMap(e -> e.getValue().identifier, e -> convertToPuzzle(e.getKey(), e.getValue())));
    }

    @Override
    public void serialEvent(SerialPortEvent evt) {
        if (evt.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            byte[] aReceiveBuffer = new byte[5];
            try {
                input.read(aReceiveBuffer,0,5);
                getPuzzle(aReceiveBuffer[0])
                        .map(puzzle -> {
                            char c;
                            byte b;
                            StringBuilder s = new StringBuilder();
                            for(int i = 1; i < aReceiveBuffer.length; i++) {
                                b = aReceiveBuffer[i];
                                if(b != 0) {
                                    c = (char)b;
                                    if(c == '!') {
                                        break;
                                    } else {
                                        s.append(c);
                                    }
                                }
                            }

                            return false;
                        });
            } catch (IOException e) {
                log.error("Failed to read incoming data - %s", e.getMessage());
            }
        }
    }

    private Optional<Puzzle> getPuzzle(byte identifier) {
        if(identifier == 0) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(puzzleMap.get((char)identifier));
        }
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
        private List<Map<String, Operation>> PC_Puzzle;
        private List<Map<String, Operation>> Puzzle_PC;
        private PuzzleState puzzleState;
    }

    @Data
    public class Operation {
        private String name;
        private String type;
    }

}
