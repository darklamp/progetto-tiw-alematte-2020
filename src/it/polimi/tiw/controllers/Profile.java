package it.polimi.tiw.controllers;

import it.polimi.tiw.beans.Alert;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.UserDAO;
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
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import static it.polimi.tiw.utility.Utility.isValidMailAddress;

@WebServlet("/profile")
public class Profile extends HttpServlet {
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
        //Show page
        String path;
        User user = (User) req.getSession().getAttribute("user");
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(req, resp, servletContext, req.getLocale());
        Alert profileAlert;
        if(req.getSession().getAttribute("profileAlert")==null){
            profileAlert = new Alert(false, Alert.DANGER, "");
            req.getSession().setAttribute("profileAlert", profileAlert);
        } else {
            profileAlert = (Alert) req.getSession().getAttribute("profileAlert");
        }

        if(user.getRole().equals("worker")){
            //Load worker profile template
            path = "/WEB-INF/profile/workerProfile.html";
            String applicationPath = req.getServletContext().getContextPath();
            ctx.setVariable("imageURL", applicationPath + "/uploads/profileImages/" + user.getImageURL());
        }else if(user.getRole().equals("manager")){
            //Load manager profile templete
            path = "/WEB-INF/profile/managerProfile.html";
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        ctx.setVariable("profileAlert", req.getSession().getAttribute("profileAlert"));
        ctx.setVariable("user", user);
        templateEngine.process(path, ctx, resp.getWriter());
        if(profileAlert.isDismissible()) profileAlert.hide();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if(!req.getParameterMap().containsKey("action") || !req.getParameterMap().containsKey("userId") || req.getParameter("action").isEmpty() || req.getParameter("userId").isEmpty()){
           resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
           return;
        }
        int userId;
        String action = req.getParameter("action");
        try {
            userId = Integer.parseInt(req.getParameter("userId"));
        } catch (NumberFormatException e){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }
        User user = (User)req.getSession().getAttribute("user");
        if(user.getId()!=userId){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid user");
            return;
        }
        UserDAO userDAO = new UserDAO(connection);
        if(user.getRole().equals("manager")){

            if(action.equals("updateData")){
                String username = req.getParameter("username");
                String email = req.getParameter("email");
                //Invalid param -> impossible from a webpage
                if(username.isEmpty() || email.isEmpty()){
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid param");
                    return;
                }
                try{
                    if((userDAO.isUsernameFree(username) || username.equals(user.getUsername())) && (userDAO.isEmailFree(email) || email.equals(user.getEmail()))){
                        if (!isValidMailAddress(email)){
                            setAlert(req,resp,Alert.DANGER,"Invalid mail address.","/profile");
                            return;
                        }
                        user.setEmail(email);
                        user.setUsername(username);
                        userDAO.updateUser(user);
                    } else {
                        setAlert(req,resp,Alert.DANGER,"Username or Email already in use.","/profile");
                        return;
                    }
                } catch (SQLException e){
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SQL query error");
                    return;
                }


            } else if(action.equals("updatePassword")){
                String oldPassword = req.getParameter("oldPassword");
                String newPassword = req.getParameter("newPassword");
                String newPasswordCnf = req.getParameter("newPasswordCnf");
                if(oldPassword.isEmpty() || newPassword.isEmpty() || newPasswordCnf.isEmpty()){
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid param");
                    return;
                }
                try{
                    try{
                        userDAO.checkCredentials(user.getUsername(), oldPassword);
                    } catch(NoSuchElementException e){
                        setAlert(req,resp,Alert.DANGER,"Invalid old password.","/profile");
                        return;
                    }

                    if(!newPassword.equals(newPasswordCnf)){
                        Alert alert = (Alert) req.getSession().getAttribute("profileAlert");
                        alert.setType(Alert.DANGER);
                        alert.setContent("Password not match");
                        alert.show();
                        alert.dismiss();
                        resp.sendRedirect(getServletContext().getContextPath()+"/profile");
                        return;
                    }

                    userDAO.updateUserPassword(userId, newPassword);
                    Alert alert = (Alert) req.getSession().getAttribute("profileAlert");
                    alert.setType(Alert.SUCCESS);
                    alert.setContent("Password changed correctly");
                    alert.show();
                    alert.dismiss();

                } catch (SQLException e){
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SQL query error");
                    return;
                }


            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
                return;
            }

        } else if(user.getRole().equals("worker")){
            if(action.equals("updateData")){
                String username = req.getParameter("username");
                String email = req.getParameter("email");
                String experience = req.getParameter("experience");
                Part photo = req.getPart("photo");
                boolean modified = false;
                //Invalid param -> impossible from a webpage
                if(username.isEmpty() || email.isEmpty()){
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid param");
                    return;
                }
                try{
                    if(!experience.equals("low") && !experience.equals("medium") && !experience.equals("high")){
                        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid experience");
                        return;
                    }

                    if((userDAO.isUsernameFree(username) || username.equals(user.getUsername())) && (userDAO.isEmailFree(email) || email.equals(user.getEmail()))){
                        if (!isValidMailAddress(email)){
                            setAlert(req,resp,Alert.DANGER,"Invalid mail address.","/profile");
                            return;
                        }
                    } else {
                        setAlert(req,resp,Alert.DANGER,"Username or Email already used by an other user","/profile");
                        return;
                    }

                    if(photo != null && photo.getSize()>0){
                        String applicationPath = req.getServletContext().getRealPath("");
                        String uploadFilePath = applicationPath + File.separator + "uploads/profileImages";
                        String fileName = photo.getSubmittedFileName();
                        String contentType = photo.getContentType();
                        String savedFileName = username + "." + Utility.getFileExtension(fileName);
                        // allows only JPEG and PNG files to be uploaded
                        if (!contentType.equalsIgnoreCase("image/jpeg") && !contentType.equalsIgnoreCase("image/png")) {
                            setAlert(req,resp,Alert.DANGER,"Wrong file type","/profile");
                            return;
                        }
                        photo.write(uploadFilePath + File.separator + savedFileName);
                    }
                    user.setEmail(email);
                    user.setUsername(username);
                    user.setLevel(experience);
                    userDAO.updateUser(user);

                } catch (SQLException e){
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SQL query error");
                    return;
                }


            } else if(action.equals("updatePassword")){
                String oldPassword = req.getParameter("oldPassword");
                String newPassword = req.getParameter("newPassword");
                String newPasswordCnf = req.getParameter("newPasswordCnf");
                if(oldPassword.isEmpty() || newPassword.isEmpty() || newPasswordCnf.isEmpty()){
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid param");
                    return;
                }
                try{
                    try{
                        userDAO.checkCredentials(user.getUsername(), oldPassword);
                    } catch(NoSuchElementException e){
                        setAlert(req,resp,Alert.DANGER,"Invalid old password.","/profile");
                        return;
                    }

                    if(!newPassword.equals(newPasswordCnf)){
                        setAlert(req,resp,Alert.DANGER,"Passwords do not match.","/profile");
                        return;
                    }

                    userDAO.updateUserPassword(userId, newPassword);
                    setAlert(req,resp,Alert.SUCCESS,"Password changed correctly!",null);

                } catch (SQLException e){
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SQL query error");
                    return;
                }


            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
                return;
            }

        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid user role");
            return;
        }

        resp.sendRedirect(getServletContext().getContextPath()+"/profile");

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

    void setAlert(HttpServletRequest req, HttpServletResponse resp, String alertType, String alertContent, String redirectPath) throws IOException {
        Alert alert = (Alert) req.getSession().getAttribute("profileAlert");
        alert.setType(alertType);
        alert.setContent(alertContent);
        alert.show();
        alert.dismiss();
        if (redirectPath != null){
            resp.sendRedirect(getServletContext().getContextPath()+redirectPath);
        }
    }
    
}
