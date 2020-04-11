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
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@WebServlet("/Register")
public class Register extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Connection connection = null;
    private TemplateEngine templateEngine;

    @Override
    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");

        try{
            ServletContext context = getServletContext();
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
        Alert alert = (Alert) req.getSession().getAttribute("registerResult");
        if(!password.equals(password_cnf)){
            alert.setType("danger");
            alert.setContent("Passwords not match");
            alert.show();
            resp.sendRedirect(getServletContext().getContextPath() + "/Register");
            return;
        }

        try {
            if(UserDAO.alreadyExists(connection, username, email)){
                alert.setType("danger");
                alert.setContent("Account already exist");
                alert.show();
                resp.sendRedirect(getServletContext().getContextPath() + "/Register");
                return;
            }

            if (!UserDAO.isEmailFree(connection, email)) {
                alert.setType("danger");
                alert.setContent("This email il already in use");
                alert.show();
                resp.sendRedirect(getServletContext().getContextPath() + "/Register");
                return;
            }

            if(!UserDAO.isUsernameFree(connection, username)){
                alert.setType("danger");
                alert.setContent("This username is already in use");
                alert.show();
                resp.sendRedirect(getServletContext().getContextPath() + "/Register");
                return;
            }



            UserDAO.addUser(connection, username, email, password, role);

        } catch (SQLException e){
            alert.setType("danger");
            alert.setContent("Database or SQL error");
            alert.show();
            resp.sendRedirect(getServletContext().getContextPath() + "/Register");
            return;
        }

        // Send "Account created"
        alert.setType("success");
        alert.setContent("Account created successfully. Please login <a href=\"/Login\">here</a>");
        alert.show();
        alert.dismiss();
        resp.sendRedirect(getServletContext().getContextPath() + "/Register");
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
