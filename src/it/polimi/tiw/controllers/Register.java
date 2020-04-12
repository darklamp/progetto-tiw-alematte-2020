package it.polimi.tiw.controllers;

import it.polimi.tiw.beans.Alert;
import it.polimi.tiw.dao.UserDAO;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

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

@WebServlet("/register")
@MultipartConfig(fileSizeThreshold = 6291456, // 6 MB
        maxFileSize = 10485760L, // 10 MB
        maxRequestSize = 20971520L // 20 MB
)
public class Register extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Connection connection = null;
    private TemplateEngine templateEngine;
    private static final String UPLOAD_DIR = "uploads/profileImages";

    @Override
    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");

        try{
            String driver = servletContext.getInitParameter("dbDriver");
            String url = servletContext.getInitParameter("dbUrl");
            String user = servletContext.getInitParameter("dbUser");
            String password = servletContext.getInitParameter("dbPassword");
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
        String path = "/WEB-INF/register.html";
        ServletContext servletContext = getServletContext();
        final WebContext webContext = new WebContext(req, resp, servletContext, req.getLocale());
        Alert alert;
        if(req.getSession().getAttribute("registerResult")==null){
            alert = new Alert(false, "", "");
        } else {
            alert = (Alert) req.getSession().getAttribute("registerResult");
        }

        req.getSession().setAttribute("registerResult", alert);
        webContext.setVariable("errorMessage", req.getSession().getAttribute("registerResult"));
        templateEngine.process(path, webContext, resp.getWriter());
        if(alert.isDismissible()) alert.hide();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String role = req.getParameter("role");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String password_cnf = req.getParameter("password_cnf");
        UserDAO userDAO = new UserDAO(connection);
        Alert alert = (Alert) req.getSession().getAttribute("registerResult");
        if(!password.equals(password_cnf)){
            alert.setType(Alert.DANGER);
            alert.setContent("Passwords not match");
            alert.show();
            resp.sendRedirect(getServletContext().getContextPath() + "/register");
            return;
        }
        else if(!role.equals("manager") && !role.equals("worker")){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid role");
            return;
        }

        try {
            if(userDAO.alreadyExists(username, email)){
                alert.setType(Alert.DANGER);
                alert.setContent("Account already exists.");
                alert.show();
                resp.sendRedirect(getServletContext().getContextPath() + "/register");
                return;
            }

            if (!userDAO.isEmailFree(email)) {
                alert.setType(Alert.DANGER);
                alert.setContent("This email is already in use.");
                alert.show();
                resp.sendRedirect(getServletContext().getContextPath() + "/register");
                return;
            }

            if(!userDAO.isUsernameFree(username)){
                alert.setType(Alert.DANGER);
                alert.setContent("This username is already in use.");
                alert.show();
                resp.sendRedirect(getServletContext().getContextPath() + "/register");
                return;
            }

            if(role.equals("manager")){
                //No image and level
                userDAO.addManagerUser(username, email, password, role);
            } else {
                //Get photo and level
                String experience = req.getParameter("experience");
                if(!experience.equals("low") && !experience.equals("medium") && !experience.equals("high")){
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid experience");
                    return;
                }

                // gets absolute path of the web application
                String applicationPath = req.getServletContext().getRealPath("");
                // constructs path of the directory to save uploaded file
                String uploadFilePath = applicationPath + File.separator + UPLOAD_DIR;
                // creates upload folder if it does not exists
                File uploadFolder = new File(uploadFilePath);
                if (!uploadFolder.exists()) {
                    uploadFolder.mkdirs();
                }
                Part part = req.getPart("photo");
                if(part != null && part.getSize()>0){
                    //File exists
                    String fileName = part.getSubmittedFileName();
                    String contentType = part.getContentType();
                    String savedFileName = username + getFileExtension(fileName);
                    // allows only JPEG and PNG files to be uploaded
                    if (!contentType.equalsIgnoreCase("image/jpeg") && !contentType.equalsIgnoreCase("image/png")) {
                        alert.setType(Alert.DANGER);
                        alert.setContent("Wrong file type");
                        alert.show();
                        resp.sendRedirect(getServletContext().getContextPath() + "/register");
                        return;
                    }
                    part.write(uploadFilePath + File.separator + fileName);
                    userDAO.addWorkerUser(username, email, password, role, experience, savedFileName);
                } else {
                    userDAO.addWorkerUser(username, email, password, role, experience, null);
                }

            }

        } catch (SQLException e){
            alert.setType(Alert.DANGER);
            alert.setContent("Database or SQL error" + e.getMessage());
            alert.show();
            resp.sendRedirect(getServletContext().getContextPath() + "/register");
            return;
        }

        // Send "Account created"
        alert.setType(Alert.SUCCESS);
        alert.setContent("Account created successfully. Please login <a href=\"login\">here</a>");
        alert.show();
        alert.dismiss();
        resp.sendRedirect(getServletContext().getContextPath() + "/register");
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

    private static String getFileExtension(String fileName) {
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }
}
