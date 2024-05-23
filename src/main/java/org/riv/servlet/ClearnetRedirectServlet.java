package org.riv.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ClearnetRedirectServlet extends HttpServlet {
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String remoteIp = req.getRemoteAddr();
		if(remoteIp.startsWith("fc") || remoteIp.startsWith("fd")) {
			System.out.println("RiV-mesh. IP:"+remoteIp);
			resp.sendRedirect(req.getContextPath() + "/mesh");
			return;
		}
        System.out.println("Redirection to clearnet page. IP:"+remoteIp);
        resp.sendRedirect(req.getContextPath() + "/clearnet");
    }

}
