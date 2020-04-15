package it.polimi.tiw.utility;

import it.polimi.tiw.beans.Alert;
import javassist.NotFoundException;
import org.apache.commons.validator.routines.EmailValidator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Utility {
    public static String getFileExtension(String fileName){
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }

    public static boolean isValidMailAddress(String email){
        return EmailValidator.getInstance().isValid(email);
    }
}
