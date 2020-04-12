package it.polimi.tiw.controllers;

import it.polimi.tiw.beans.Alert;
import it.polimi.tiw.beans.Campaign;
import it.polimi.tiw.dao.CampaignDAO;
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

@WebServlet("/manager/campaignActions")
public class CampaignActions extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;
    CampaignDAO campaignDAO;
    @Override
    public void init() throws ServletException {
        ServletContext context = getServletContext();
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
        campaignDAO = new CampaignDAO(connection);
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Campaign campaign;
        if(!req.getParameterMap().containsKey("campaignId")){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } else {
            int campaignId;
            try{
                campaignId = Integer.parseInt(req.getParameter("campaignId"));
            } catch (NumberFormatException e){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            try{
                campaign = campaignDAO.getCampaignById(campaignId);
            } catch (SQLException e){
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
            //All instruction here
            if(!req.getParameterMap().containsKey("action")){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            String action = req.getParameter("action");
            try{
                if(action.equals("setStart") && campaign.getState().equals(Campaign.CREATED)){
                    campaignDAO.setState(campaignId, Campaign.STARTED);
                } else if (action.equals("setClose") && campaign.getState().equals(Campaign.STARTED)){
                    campaignDAO.setState(campaignId, Campaign.CLOSED);
                } else {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
            } catch (SQLException e){
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            resp.sendRedirect(getServletContext().getContextPath() + "/manager/campaign?id="+campaignId);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Campaign campaign;
        if(!req.getParameterMap().containsKey("campaignId")){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } else {
            int campaignId;
            try{
                campaignId = Integer.parseInt(req.getParameter("campaignId"));
            } catch (NumberFormatException e){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            try{
                campaign = campaignDAO.getCampaignById(campaignId);
            } catch (SQLException e){
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
            //All instruction here
            if(!req.getParameterMap().containsKey("action")){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            String action = req.getParameter("action");
            if(action.equals("modifyData")){
                String name = req.getParameter("campaignName");
                String client = req.getParameter("campaignClient");
                Alert alert = (Alert)req.getSession().getAttribute("campaignAlert");
                if(!name.isEmpty() && !client.isEmpty()){
                    try{
                        if(campaignDAO.inNameFree(name) || name.equals(campaign.getName())){
                            campaignDAO.updateCampaign(campaignId, name, client);
                        } else {
                            alert.setContent("Name already in use");
                            alert.setType(Alert.DANGER);
                            alert.show();
                            alert.dismiss();
                            resp.sendRedirect(getServletContext().getContextPath() + "/manager/campaign?id="+campaignId);
                            return;
                        }
                    } catch (SQLException e){
                        resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                        return;
                    }

                } else {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            resp.sendRedirect(getServletContext().getContextPath() + "/manager/campaign?id="+campaignId);
        }
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