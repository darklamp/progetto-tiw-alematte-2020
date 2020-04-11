package it.polimi.tiw.filters;

import it.polimi.tiw.beans.User;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class ManagerChecker implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;
        String redirectPath = req.getServletContext().getContextPath();

        HttpSession session = req.getSession();
        User user = (User) session.getAttribute("user");;
        if (! user.getRole().equals("manager")) {
            if(user.getRole().equals("worker")){
                redirectPath = redirectPath + "/worker";
            } else {
                redirectPath = redirectPath + "/index.html";
            }
            res.sendRedirect(redirectPath);
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void destroy() {

    }
}
