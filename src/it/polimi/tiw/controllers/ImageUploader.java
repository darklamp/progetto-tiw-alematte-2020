package it.polimi.tiw.controllers;

import it.polimi.tiw.beans.Alert;
import it.polimi.tiw.beans.Image;
import it.polimi.tiw.dao.ImageDAO;
import it.polimi.tiw.utility.Utility;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@WebServlet("/imageUploader")
@MultipartConfig(fileSizeThreshold = 6291456, // 6 MB
        maxFileSize = 10485760L, // 10 MB
        maxRequestSize = 20971520L // 20 MB
)
public class ImageUploader extends HttpServlet {

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
        ImageDAO imageDAO = new ImageDAO(connection);

        // gets absolute path of the web application
        String applicationPath = req.getServletContext().getRealPath("");
        // constructs path of the directory to save uploaded file
        String uploadFilePath = applicationPath + File.separator + "uploads/campaignImages";
        // creates upload folder if it does not exists
        File uploadFolder = new File(uploadFilePath);
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }
        Part part = req.getPart("image");

        //Get all param
        int campaignId = Integer.parseInt(req.getParameter("campaignId"));
        Alert alert = (Alert)req.getSession().getAttribute("campaignAlert");
        Image image = new Image();
        String path = getServletContext().getContextPath() + "/manager/campaign?id="+campaignId;
        String latitudeStr = req.getParameter("latitude").replace(',', '.');
        String longitudeStr = req.getParameter("longitude").replace(',','.');
        String resolution = req.getParameter("resolution");
        String source = req.getParameter("source");
        String region = req.getParameter("region");
        String town = req.getParameter("town");
        if(latitudeStr.isEmpty() || longitudeStr.isEmpty() || resolution.isEmpty() || source.isEmpty() || region.isEmpty() || town.isEmpty() || part == null || part.getSize()<=0){
            alert.setContent("Please fill all form data");
            alert.setType(Alert.DANGER);
            alert.show();
            alert.dismiss();
            resp.sendRedirect(path);
            return;
        }
        if (!resolution.equals("high") && !resolution.equals("medium") && !resolution.equals("low")){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
        //try parsing strings in float
        float latitude;
        float longitude;
        try {
            latitude = Float.parseFloat(latitudeStr);
            longitude = Float.parseFloat(longitudeStr);
        } catch (NumberFormatException e){
            alert.setContent("NumberFormatException in latitude or longitude");
            alert.setType(Alert.DANGER);
            alert.show();
            alert.dismiss();
            resp.sendRedirect(path);
            return;
        }
        //Insert image on database
        //File exists
        int lastImageIndex;
        try{
            lastImageIndex = imageDAO.getImagesNumber(campaignId);
        } catch (SQLException e){
            System.out.println("SQL Exeption on getting image index");
            alert.setContent("SQL error");
            alert.setType(Alert.DANGER);
            alert.show();
            alert.dismiss();
            resp.sendRedirect(path);
            return;
        }
        String fileName = part.getSubmittedFileName();
        String contentType = part.getContentType();
        String savedFileName = "campaign"+ campaignId + "_" + "image" + (lastImageIndex+1) + "." + Utility.getFileExtension(fileName);
        // allows only JPEG and PNG files to be uploaded
        if (!contentType.equalsIgnoreCase("image/jpeg") && !contentType.equalsIgnoreCase("image/png")) {
            alert.setType(Alert.DANGER);
            alert.setContent("Wrong file type");
            alert.show();
            alert.dismiss();
            resp.sendRedirect(path);
            return;
        }
        part.write(uploadFilePath + File.separator + savedFileName);
        image.setUrl(savedFileName);
        image.setTown(town);
        image.setRegion(region);
        image.setSource(source);
        image.setResolution(resolution);
        image.setLongitude(longitude);
        image.setLatitude(latitude);
        try{
            imageDAO.insertNewImage(campaignId, image);
        } catch (SQLException e){
            alert.setContent("An error occurred while saving image");
            alert.setType(Alert.DANGER);
            alert.show();
            alert.dismiss();
            resp.sendRedirect(path);
            return;
        }


        resp.sendRedirect(path);
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
