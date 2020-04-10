package it.polimi.tiw.controllers;

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

@WebServlet("/CheckLogin")
public class CheckLogin extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    @Override
    public void init() throws ServletException {
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
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String usrn = req.getParameter("username");
        String pwd = req.getParameter("password");
        UserDAO usr = new UserDAO(connection);
        User u = null;
        try {
            u = usr.checkCredentials(usrn, pwd);
        } catch (SQLException e) {
            // throw new ServletException(e); for debugging
            resp.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure in database credential checking");
        }
        String path = getServletContext().getContextPath();
        if (u == null) {
            path = getServletContext().getContextPath() + "/index.html";
        } else {
            req.getSession().setAttribute("user", u);
            String target = (u.getRole().equals("admin")) ? "/GoToHomeAdmin" : "/GoToHomeWorker";
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
