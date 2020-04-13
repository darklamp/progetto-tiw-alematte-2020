package it.polimi.tiw.utility;

import java.util.List;

public class JsonArrayConverter implements JsonConverter<List<String>> {
    @Override
    public String convertToJson(List<String> obj) {
        StringBuilder result = new StringBuilder("[");
        for(int i=0; i<obj.size(); i++){
            result.append(obj.get(i));
            if(i!=obj.size()-1){
                result.append(",");
            }
        }
        result.append("]");
        return result.toString();
    }
}
