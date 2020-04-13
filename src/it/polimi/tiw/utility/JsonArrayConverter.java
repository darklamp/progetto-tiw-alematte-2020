package it.polimi.tiw.utility;

import java.util.List;

public class JsonArrayConverter implements JsonConverter<List<String>> {
    @Override
    public String convertToJson(List<String> obj) {
        String result = "[";
        for(int i=0; i<obj.size(); i++){
            result += obj;
            if(i!=obj.size()-1){
                result += ",";
            }
        }
        result += "]";
        return result;
    }
}
