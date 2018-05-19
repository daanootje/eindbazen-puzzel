package org.pandora.control.serialcomm;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import gnu.io.SerialPortEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

@Slf4j
@Component
public class SerialInterpreter extends SerialCommunicator {

    public SerialInterpreter() throws IOException {
        super();
        URL url = SerialInterpreter.class.getResource("/applicationConf.json");
        String json = Resources.toString(url, Charsets.UTF_8);
        Gson gson = new Gson();
        Map<String, Object> list = gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
    }

    @Override
    public void serialEvent(SerialPortEvent evt) {
        if (evt.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                byte singleData = (byte)input.read();
                String data = new String(new byte[] {singleData});
            } catch (IOException e) {
                log.error("Failed to read incoming data - %s", e.getMessage());
            }
        }
    }

}
