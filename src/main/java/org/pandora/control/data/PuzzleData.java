package org.pandora.control.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PuzzleData {

    private String name;
    private Integer duration;
    private String hints;
    private Boolean succeeded;

}
