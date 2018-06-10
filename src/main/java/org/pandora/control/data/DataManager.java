package org.pandora.control.data;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class DataManager {

    private String fileName;

    public DataManager(String configFolder) {
        fileName = String.format("%s/logs.txt", configFolder);
    }

    public void storeData(String groupName, List<PuzzleData> puzzleData, Integer time, Boolean succeeded) {
        DataObject dataObject = convertToDataObject(groupName, puzzleData, time, succeeded);
        try {
            FileOutputStream foutput = new FileOutputStream(new File(fileName), true);
            ObjectOutputStream ooutput = new ObjectOutputStream(foutput);

            ooutput.writeObject(dataObject);

            foutput.close();
            ooutput.close();
        } catch (IOException e) {
            log.error("Failed storing data, possible to store data manual in '%s' with data: %s", fileName, dataObject.toString());
            log.error(e.getMessage());
        }
    }

    public List<DataObject> retrieveData() {
        try {
            FileInputStream finput = new FileInputStream(new File(fileName));
            ObjectInputStream oinput = new ObjectInputStream(finput);

            DataObject dataObject;
            List<DataObject> list = new ArrayList<>();
            while((dataObject = (DataObject) oinput.readObject()) != null) {
                list.add(dataObject);
            }
            finput.close();
            oinput.close();
            return list;
        } catch (FileNotFoundException e) {
            log.error("Failed retrieving data, file not found %s - %s", fileName, e.getMessage());
        } catch (IOException e) {
            log.error("Failed retrieving data - %s", e.getMessage());
        } catch (ClassNotFoundException e) {
            log.error("Failed retrieving data, class not found - %s", e.getMessage());
        }
        return new ArrayList<>();
    }

    public Optional<DataObject> retrieveData(String groupName) {
        try {
            FileInputStream finput = new FileInputStream(new File(fileName));
            ObjectInputStream oinput = new ObjectInputStream(finput);

            DataObject dataObject;
            while((dataObject = (DataObject) oinput.readObject()) != null) {
                if(dataObject.getGroupName().equals(groupName)) {
                    return Optional.of(dataObject);
                }
            }
            finput.close();
            oinput.close();
        } catch (FileNotFoundException e) {
            log.error("Failed retrieving data, file not found %s - %s", fileName, e.getMessage());
        } catch (IOException e) {
            log.error("Failed retrieving data - %s", e.getMessage());
        } catch (ClassNotFoundException e) {
            log.error("Failed retrieving data, class not found - %s", e.getMessage());
        }
        return Optional.empty();
    }

    private DataObject convertToDataObject(String groupName, List<PuzzleData> puzzleData, Integer timeLeft, Boolean succeeded) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String stringDate = sdf.format(date);
        return DataObject.builder()
                .date(stringDate)
                .groupName(groupName)
                .puzzleData(puzzleData)
                .timeLeft(timeLeft)
                .succeeded(succeeded)
                .build();
    }
}
