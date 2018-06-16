package org.pandora.control.puzzle;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Operation {
    @JsonProperty("name")
    private String name;
    @JsonProperty("type")
    private String type;

    public Operation(){}
}
