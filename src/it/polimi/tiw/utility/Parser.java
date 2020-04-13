package it.polimi.tiw.utility;

import org.apache.commons.validator.routines.EmailValidator;

public class Parser {
    public static String getFileExtension(String fileName){
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }
    public static boolean isValidMailAddress(String email){
        return EmailValidator.getInstance().isValid(email);
    }
}
