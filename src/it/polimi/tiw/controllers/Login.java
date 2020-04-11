package it.polimi.tiw.controllers;

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

@WebServlet("/Login")
public class Login extends HttpServlet {
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
        String path = "/WEB-INF/login.html";
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(req, resp, servletContext, req.getLocale());
        String errorMessage = "";
        if(req.getSession().getAttribute("loginResult") == null) {
            errorMessage = "";
        } else if((boolean)req.getSession().getAttribute("loginResult")) {
            User u = (User) req.getSession().getAttribute("user");
            String target = getServletContext().getContextPath() + ((u.getRole().equals("manager")) ? "/ManagerHome" : "/WorkerHome");
            resp.sendRedirect(target);
        } else {
            errorMessage = "Invalid credential";
        }
        ctx.setVariable("errorMessage", errorMessage);
        templateEngine.process(path, ctx, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        UserDAO usr = new UserDAO(connection);
        User u = null;
        try {
            u = usr.checkCredentials(username, password);
        } catch (SQLException e) {
            // throw new ServletException(e); for debugging
            resp.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure in database credential checking");
        }
        String path = getServletContext().getContextPath();
        if (u == null) {
            req.getSession().setAttribute("loginResult", false);
            path = getServletContext().getContextPath() + "/Login";
        } else {
            req.getSession().setAttribute("user", u);
            req.getSession().setAttribute("loginResult", true);
            String target = (u.getRole().equals("manager")) ? "/ManagerHome" : "/WorkerHome";
            path = path + target;
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
