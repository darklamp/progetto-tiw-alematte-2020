package it.polimi.tiw.utility;

import it.polimi.tiw.beans.Image;
import org.apache.commons.validator.routines.EmailValidator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Utility {
    public static String getFileExtension(String fileName){
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }
    public static boolean isValidMailAddress(String email){
        return EmailValidator.getInstance().isValid(email);
    }
    public static boolean containsId(List<Image> images, int imageID){
        return images.stream().map(Image::getId).anyMatch(n -> n == imageID);
    }

    public static boolean paramExists(HttpServletRequest req, HttpServletResponse resp, List<String> params) throws IOException {
        Map<String,String[]> parameterMap = req.getParameterMap();
        if(params.stream().anyMatch(parameter -> !parameterMap.containsKey(parameter))){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return false;
        }
        return true;
    }
    public static boolean paramExists(HttpServletRequest req, HttpServletResponse resp, String param) throws IOException {
            if(!req.getParameterMap().containsKey(param)){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter " + param + " not found");
                return false;
            }
        return true;
    }


    public static boolean paramIsEmpty(HttpServletRequest req, HttpServletResponse resp, List<String> params) throws IOException{
        for (String param : params) {
            if (req.getParameter(param).isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter " + param + " is empty");
                return true;
            }
        }
        return false;
    }
    public static boolean paramIsEmpty(HttpServletRequest req, HttpServletResponse resp, String param) throws IOException{
        if(req.getParameter(param).isEmpty()){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter " + param + " is empty");
            return true;
        }
        return false;
    }

    public static String createMapGeoJSON(ArrayList<Image> images, ArrayList<Integer> annotatedImages){
        String output = "{\"type\": \"FeatureCollection\", \"features\": [";
        output = images != null ? images.stream().map(img -> img.convertToGeoJSON(annotatedImages)).reduce(output, (old,newPart) -> old + newPart + ",") : null;
        if (output == null) return null;
        output = JsonMapConverter.removeLastCharacter(output);
        output += "]}";
        return output;
    }
}
