package org.pandora.control.data;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class DataObject implements Serializable {

    private String groupName;
    private String date;
    private Integer timeLeft;
    private Boolean succeeded;
    private List<PuzzleData> puzzleData = new ArrayList<>();

}
