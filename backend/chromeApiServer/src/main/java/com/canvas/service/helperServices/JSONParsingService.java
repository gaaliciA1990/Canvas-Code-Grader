package com.canvas.service.helperServices;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

public class JSONParsingService {
    private final JSONObject jsonObj;

    public JSONParsingService(String jsonFilePath) {
        jsonObj = getJsonObject(jsonFilePath);
    }

    public String get(String key) {
        return (String) jsonObj.get(key);
    }

    private JSONObject getJsonObject(String jsonFilePath) {
        try {
            FileReader fileReader = new FileReader(jsonFilePath);
            JSONObject jsonObj = (JSONObject) new JSONParser().parse(fileReader);
            fileReader.close();
            return jsonObj;
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

}
