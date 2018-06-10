package org.pandora.control.util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Deserializer {

    public static <T> Map<String,T> flattenMap(List<Map<String,T>> nestedMap) {
        return nestedMap.stream()
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue));
    }

}
