package it.polimi.tiw.utility;

import it.polimi.tiw.beans.Image;
import org.apache.commons.validator.routines.EmailValidator;

import java.util.List;

public class Utility {
    public static String getFileExtension(String fileName){
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }
    public static boolean isValidMailAddress(String email){
        return EmailValidator.getInstance().isValid(email);
    }
    public static boolean containsId(List<Image> list, int imageId){
        return list.stream().map(Image::getId).anyMatch(n -> n == imageId);
    }
}
