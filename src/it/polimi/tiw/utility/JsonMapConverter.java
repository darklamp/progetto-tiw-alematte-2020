package it.polimi.tiw.utility;

import java.util.Map;

public class JsonMapConverter implements JsonConverter<Map<String,String>>{
    @Override
    public String convertToJson(Map<String, String> obj) {
        String result = "{";
        for(String key: obj.keySet()){
            String internalObj = "\""+key+"\":";
            if(obj.get(key).startsWith("{") || obj.get(key).startsWith("[")){
                internalObj += obj.get(key)+",";
            } else {
                internalObj += "\""+obj.get(key)+"\",";
            }
        }
        result = removeLastCharacter(result);
        result += "}";
        return result;
    }

    private String removeLastCharacter(String str) {
        String result = null;
        if ((str != null) && (str.length() > 0)) {
            result = str.substring(0, str.length() - 1);
        }
        return result;
    }
}
