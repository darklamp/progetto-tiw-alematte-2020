package it.polimi.tiw.controllers;

import it.polimi.tiw.beans.Alert;
import it.polimi.tiw.beans.Campaign;
import it.polimi.tiw.beans.Image;
import it.polimi.tiw.dao.CampaignDAO;
import it.polimi.tiw.dao.ImageDAO;
import it.polimi.tiw.utility.Utility;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

@WebServlet("/manager/campaign")
public class CampaignDetail extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Connection connection = null;
    private TemplateEngine templateEngine;

    @Override
    public void init() throws ServletException {
        ServletContext context = getServletContext();
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(context);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");

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
        if(!Utility.paramExists(req,resp,"id"))return;
        int campaignId;
        try{
            campaignId = Integer.parseInt(req.getParameter("id"));
        } catch (Exception e){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }
        Alert campaignAlert;
        if(req.getSession().getAttribute("campaignAlert")==null){
           campaignAlert = new Alert(false, Alert.DANGER, "");
           req.getSession().setAttribute("campaignAlert", campaignAlert);
        } else {
            campaignAlert = (Alert) req.getSession().getAttribute("campaignAlert");
        }

        String applicationPath = req.getServletContext().getContextPath();
        String uploadFilePath = applicationPath + File.separator + "uploads/campaignImages";
        File uploadFolder = new File(uploadFilePath);
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }

        String path = "/WEB-INF/campaignDetail.html";
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(req, resp, servletContext, req.getLocale());

        CampaignDAO campaignDAO = new CampaignDAO(connection);
        ImageDAO imageDAO = new ImageDAO(connection);
        Campaign campaign = null;
        List<Image> images = null;
        try{
            campaign = campaignDAO.getCampaignById(campaignId);
            images = imageDAO.getCampaignImages(campaignId);
        } catch (SQLException e){
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        } catch (NoSuchElementException e){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        if(images == null){
            ctx.setVariable("isImageAvailable", false);
            images = new ArrayList<>();
        } else {
            ctx.setVariable("isImageAvailable", true);

        }
        ctx.setVariable("context", getServletContext().getContextPath());
        ctx.setVariable("campaign", campaign);
        ctx.setVariable("images", images);
        ctx.setVariable("imagePath", uploadFolder.getAbsolutePath()+File.separator);
        ctx.setVariable("campaignAlert", req.getSession().getAttribute("campaignAlert"));
        templateEngine.process(path, ctx, resp.getWriter());
        if(campaignAlert.isDismissible()) campaignAlert.hide();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ImageDAO imageDAO = new ImageDAO(connection);
        int campaignId, imageId;

        List<String> params = new ArrayList<>(Arrays.asList("campaignId", "imageId", "viewMode", "latitude", "longitude", "resolution", "source", "region", "town"));
        if(!Utility.paramExists(req, resp, params) || Utility.paramIsEmpty(req, resp, params)) return;

        try {
            campaignId = Integer.parseInt(req.getParameter("campaignId"));
            imageId = Integer.parseInt(req.getParameter("imageId"));
        } catch (NumberFormatException e){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        try{
            CampaignDAO campaignDAO = new CampaignDAO(connection);
            Campaign campaign = campaignDAO.getCampaignById(campaignId);
            if(!campaign.getState().equals("created")){
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        } catch (SQLException e){
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            return;
        } catch (NoSuchElementException e){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        Alert alert = (Alert)req.getSession().getAttribute("campaignAlert");
        Image image = new Image();

        String path = getServletContext().getContextPath() + "/manager/campaign?id="+campaignId;
        if(req.getParameter("viewMode").equals("grid")) {
            path = getServletContext().getContextPath() + "/manager/campaign?id="+campaignId;
        }else if(req.getParameter("viewMode").equals("maps")){
            path = getServletContext().getContextPath() + "/manager/campaign/maps?id="+campaignId;
        }

        String latitudeStr = req.getParameter("latitude").replace(',', '.');
        String longitudeStr = req.getParameter("longitude").replace(',','.');
        String resolution = req.getParameter("resolution");
        String source = req.getParameter("source");
        String region = req.getParameter("region");
        String town = req.getParameter("town");
        if (!resolution.equals("high") && !resolution.equals("medium") && !resolution.equals("low")){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if(latitudeStr.isEmpty() || longitudeStr.isEmpty() || source.isEmpty() || region.isEmpty() || town.isEmpty()){
           alert.setContent("Please fill all form data");
            alert.setType(Alert.DANGER);
            alert.show();
            alert.dismiss();
            resp.sendRedirect(path);
            return;
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
        image.setId(imageId);
        image.setTown(town);
        image.setRegion(region);
        image.setSource(source);
        image.setResolution(resolution);
        image.setLongitude(longitude);
        image.setLatitude(latitude);
        try{
            imageDAO.updateImageMetadata(image);
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
        } catch (SQLException ignored) {
        }
    }
}
