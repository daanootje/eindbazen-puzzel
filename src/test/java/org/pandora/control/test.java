package org.pandora.control;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.junit.Test;
import org.pandora.control.model.Puzzle;
import org.pandora.control.music.AudioManager;

import java.net.URL;
import java.util.List;
import java.util.Map;

public class test {

    @Test
    public void test() throws Exception {
        AudioManager manager = new AudioManager("C:/Users/D.Rotman/Downloads/audio");
        manager.playMusic("cartoon001");

        Thread.sleep(5000);
    }

    @Test
    public void test2() throws Exception {
        URL url = test.class.getResource("/applicationConf.json");
        String json = Resources.toString(url, Charsets.UTF_8);
        JsonParser jsonParser = new JsonParser();
        Gson gson = new Gson();
        JsonObject jsonTree = jsonParser.parse(json).getAsJsonObject();
        JsonArray array = jsonTree.getAsJsonArray("puzzles");
        List<Map<String,Puzzle>> list = gson.fromJson(array, new TypeToken<List<Map<String,Puzzle>>>(){}.getType());
        System.out.println(list);
    }

}
