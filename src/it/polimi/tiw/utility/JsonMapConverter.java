package it.polimi.tiw.utility;

import java.util.Map;

public class JsonMapConverter implements JsonConverter<Map<String,String>>{
    @Override
    public String convertToJson(Map<String, String> obj) {
        StringBuilder result = new StringBuilder("{");
        for(String key: obj.keySet()){
            String internalObj = "\""+key+"\":";
            if(obj.get(key).startsWith("{") || obj.get(key).startsWith("[")){
                internalObj += obj.get(key)+",";
            } else {
                internalObj += "\""+obj.get(key)+"\",";
            }
            result.append(internalObj);
        }
        result = new StringBuilder(removeLastCharacter(result.toString()));
        result.append("}");
        return result.toString();
    }

    static String removeLastCharacter(String str) {
        String result = null;
        if ((str != null) && (str.length() > 0)) {
            result = str.substring(0, str.length() - 1);
        }
        return result;
    }
}
