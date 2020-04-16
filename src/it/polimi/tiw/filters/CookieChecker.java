package it.polimi.tiw.filters;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.UserDAO;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;
import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet Filter implementation class checker
 */

public class CookieChecker implements Filter {

    /**
     * Default constructor.
     */
    public CookieChecker() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @see Filter#destroy()
     */
    public void destroy() {
        // TODO Auto-generated method stub
    }

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        System.out.print("Cookie checker filter executing ...\n");

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String redirectPath = req.getServletContext().getContextPath();
        String loginPath = redirectPath + "/login";

        HttpSession s = req.getSession();
        if ((s.isNew() || req.getRequestURI().contains("register") || req.getRequestURI().contains("login")) && req.getCookies()!=null){
            String key = "progtiw-auth";
            Optional<String> authCookie = Arrays.stream(req.getCookies())
                    .filter(c -> key.equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst();

            if (authCookie.isPresent()){
                Connection connection = null;
                ServletContext context  = req.getServletContext();
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
                UserDAO userDAO = new UserDAO(connection);
                User user = null;
                try {
                    user = userDAO.getUserFromCookie(authCookie.get());
                } catch (NoSuchElementException e) {
                    Cookie toRemove = new Cookie("progtiw-auth",null);
                    toRemove.setMaxAge(0);
                    //toRemove.setSecure(true);
                    toRemove.setHttpOnly(true);
                    res.addCookie(toRemove);
                    res.sendRedirect(redirectPath+"/login");
                    return;
                } catch (SQLException ee){
                    res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    return;
                }
                s.setAttribute("user",user);
                if (user.getRole().equals("manager")) {
                    res.sendRedirect(redirectPath+"/manager");
                    return;
                }
                else if(user.getRole().equals("worker")){
                    res.sendRedirect(redirectPath + "/worker");
                    return;
                }
                else {
                    res.sendRedirect(loginPath);
                    return;
                }
            }
        }

        // pass the request along the filter chain
        chain.doFilter(request, response);
    }

    /**
     * @see Filter#init(FilterConfig)
     */
    public void init(FilterConfig fConfig) throws ServletException {
        // TODO Auto-generated method stub
    }

}
