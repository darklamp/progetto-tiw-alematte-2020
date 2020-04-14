package it.polimi.tiw.controllers;

import it.polimi.tiw.beans.Alert;
import it.polimi.tiw.beans.Campaign;
import it.polimi.tiw.beans.Image;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.AnnotationDAO;
import it.polimi.tiw.dao.CampaignDAO;
import it.polimi.tiw.dao.ImageDAO;
import it.polimi.tiw.dao.UserDAO;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@WebServlet("/worker/campaign/*")
public class Gallery extends HttpServlet {

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

        String imagePath = req.getServletContext().getContextPath() + File.separator + "uploads/campaignImages";
        String path = "/WEB-INF/gallery.html";
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
        }

        if(campaign==null){
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        if(images == null){
            ctx.setVariable("isImageAvailable", false);
            images = new ArrayList<>();
        } else {
            ctx.setVariable("isImageAvailable", true);

        }
        User user = (User) req.getSession().getAttribute("user");
        ctx.setVariable("context", getServletContext().getContextPath());
        ctx.setVariable("campaign", campaign);
        ctx.setVariable("images", images);
        ctx.setVariable("user", user);
        ctx.setVariable("imagePath", imagePath+File.separator);
        ctx.setVariable("campaignAlert", req.getSession().getAttribute("campaignAlert"));
        templateEngine.process(path, ctx, resp.getWriter());
        if(campaignAlert.isDismissible()) campaignAlert.hide();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if(!req.getParameterMap().containsKey("userId") || req.getParameter("userId").isEmpty()){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        int userId;
        try {
            userId = Integer.parseInt(req.getParameter("userId"));
        } catch (NumberFormatException e){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }
        List<Image> images = (List<Image>) req.getSession().getAttribute("images");
        User user = (User)req.getSession().getAttribute("user");
        if(user.getId()!=userId){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid user");
            return;
        }
        UserDAO userDAO = new UserDAO(connection);
        ImageDAO imageDAO = new ImageDAO(connection);

        if (!user.getRole().equals("worker")){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
        String validity = req.getParameter("validity");
        String trust = req.getParameter("trust");
        int imageID = 0;
        try{
            imageID = Integer.parseInt(req.getParameter("imageId"));
        }
        catch (NumberFormatException e){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
        try {
            if (!images.contains(imageDAO.getImage(imageID))){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        catch (SQLException e){
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        if (!validity.equals("true") && !validity.equals("false")){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
        if (!trust.equals("high") && !trust.equals("medium") && !trust.equals("low")){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }


        String note = req.getParameter("annotationText");
        int validityToInt = 0;
        if (validity.equals("true")){
            validityToInt = 1;
        }
        if (note.length() > 1000){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
        AnnotationDAO annotationDAO = new AnnotationDAO(connection);
        annotationDAO.createAnnotation(userId,imageID,validityToInt,trust,note);
    }
}
