package it.polimi.tiw.controllers;

import it.polimi.tiw.beans.Annotation;
import it.polimi.tiw.beans.Image;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.AnnotationDAO;
import it.polimi.tiw.dao.ImageDAO;
import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.utility.JsonArrayConverter;
import it.polimi.tiw.utility.JsonMapConverter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/manager/getCampaignImageData")
public class GetCampaignImageData extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    @Override
    public void init() throws ServletException {
        ServletContext context = getServletContext();
        try{
            String driver = context.getInitParameter("dbDriver");
            String url = context.getInitParameter("dbUrl");
            String user = context.getInitParameter("dbUser");
            String password = context.getInitParameter("dbPassword");
            Class.forName(driver);
            connection = DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException e) {
            throw new UnavailableException("Can't load database driver");
        } catch (SQLException e) {
            throw new UnavailableException("Couldn't get db connection");
        }

    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if(!req.getParameterMap().containsKey("imageId")){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        int imageId;
        try {
            imageId = Integer.parseInt(req.getParameter("imageId"));
        }catch (NumberFormatException e){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        PrintWriter out = resp.getWriter();
        resp.setContentType("application/json");
        ImageDAO imageDAO = new ImageDAO(connection);
        AnnotationDAO annotationDAO = new AnnotationDAO(connection);
        UserDAO userDAO = new UserDAO(connection);
        JsonArrayConverter jsonArrayConverter = new JsonArrayConverter();
        JsonMapConverter jsonMapConverter = new JsonMapConverter();
        Map<String, String> resultMap = new LinkedHashMap<>();
        List<String> annotationsStr = new ArrayList<>();
        try{
            Image image = imageDAO.getImage(imageId);
            List<Annotation> annotations = annotationDAO.getAnnotations(imageId);

            if(image==null){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid image");
                return;
            }

            resultMap.put("image", image.convertToJSON());
            if(annotations==null){
                resultMap.put("annotations", "0");
                out.print(jsonMapConverter.convertToJson(resultMap));
                return;
            }
            for(Annotation annotation : annotations){
                Map<String, String > annotationMap = new LinkedHashMap<>();
                User aUser = userDAO.getUser(annotation.getWorkerId());
                if(aUser != null) {
                    annotationMap.put("user", aUser.convertToJSON());
                } else {
                    annotationMap.put("user", "0");
                }
                annotationMap.put("annotation", annotation.convertToJSON());
                annotationsStr.add(jsonMapConverter.convertToJson(annotationMap));
            }
        }catch (SQLException e){
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            return;
        }

        resultMap.put("annotations", jsonArrayConverter.convertToJson(annotationsStr));
        out.print(jsonMapConverter.convertToJson(resultMap));
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException sqle) {
        }
    }
}
