package com.canvas.service.helperServices;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

/**
 * This class is responsible for parsing JSON data
 */
public class JSONParsingService {
    private final JSONObject jsonObj;

    /**
     * Constructor
     * @param jsonFilePath  file path for creating json object
     */
    public JSONParsingService(String jsonFilePath) {
        jsonObj = getJsonObject(jsonFilePath);
    }

    /**
     * TODO: what is this getting?
     *
     * @param key
     * @return
     */
    public String get(String key) {
        return (String) jsonObj.get(key);
    }

    /**
     * Getter for the json object
     *
     * @param jsonFilePath  String path for json file
     * @return              Jsonobject
     */
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
