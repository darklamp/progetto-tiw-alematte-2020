package it.polimi.tiw.controllers;

import it.polimi.tiw.beans.Alert;
import it.polimi.tiw.beans.User;
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
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

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
       //Update information and then resp.sendRedirect(getServletContext().getContextPath()+"/profile");
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
        //If someone chenge userId in request
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
                        user.setEmail(email);
                        user.setUsername(username);
                        userDAO.updateUser(user);
                    } else {
                        Alert alert = (Alert) req.getSession().getAttribute("profileAlert");
                        alert.setType(Alert.DANGER);
                        alert.setContent("Username or Email already used by an other user");
                        alert.show();
                        alert.dismiss();
                        resp.sendRedirect(getServletContext().getContextPath()+"/profile");
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
                    if(userDAO.checkCredentials(user.getUsername(), oldPassword) == null){
                        Alert alert = (Alert) req.getSession().getAttribute("profileAlert");
                        alert.setType(Alert.DANGER);
                        alert.setContent("Invalid old password");
                        alert.show();
                        alert.dismiss();
                        resp.sendRedirect(getServletContext().getContextPath()+"/profile");
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
        } catch (SQLException sqle) {
        }
    }
}
