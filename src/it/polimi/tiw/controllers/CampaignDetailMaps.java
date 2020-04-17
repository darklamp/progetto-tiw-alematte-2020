package it.polimi.tiw.controllers;

import it.polimi.tiw.beans.Alert;
import it.polimi.tiw.beans.Campaign;
import it.polimi.tiw.beans.Image;
import it.polimi.tiw.dao.CampaignDAO;
import it.polimi.tiw.dao.ImageDAO;
import it.polimi.tiw.utility.JsonMapConverter;
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
import java.io.PrintWriter;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

@WebServlet("/manager/campaign/maps")
public class CampaignDetailMaps extends HttpServlet {
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

        String path = "/WEB-INF/campaignDetailMaps.html";
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
        int campaignId;

        //Get all param
        if(!Utility.paramExists(req, resp, "campaignId")) return;

        try {
            campaignId = Integer.parseInt(req.getParameter("campaignId"));
        } catch (NumberFormatException e){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        try{
            CampaignDAO campaignDAO = new CampaignDAO(connection);
            Campaign campaign = campaignDAO.getCampaignById(campaignId);

        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        } catch (NoSuchElementException e){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        ArrayList<Image> images = null;
        try {
            images = (ArrayList<Image>) imageDAO.getCampaignImages(campaignId);
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        Alert alert = (Alert)req.getSession().getAttribute("campaignAlert");
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        String jsonConvertedMap = Utility.createMapGeoJSON(images);

        out.print(jsonConvertedMap);
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
