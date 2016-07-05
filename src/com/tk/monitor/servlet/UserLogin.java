package com.tk.monitor.servlet;

import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;

import com.tk.monitor.Constant;
import com.tk.monitor.users.User;

public class UserLogin extends HttpServlet{
	
	private static final long serialVersionUID = 1365502106021685020L;
	private static final Logger log = com.tk.monitor.logger.Logger.getLogger();
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setCharacterEncoding("UTF-8");
		resp.setHeader("content-type","text/html;charset=UTF-8");
		
		HttpSession session = req.getSession();
		User user = (User)session.getAttribute(Constant.sessionUserAttrib);
		
		//判断此session是否已经存在用户,存在则不允许再次登录.
		if(user != null){
			log.warning("用户已经存在,不再进行登录操作:" + user);
			resp.getWriter().println(String.format(Constant.respLoginTrue,"用户已经存在,请先退出."));
			return;
		}
		
		String username = req.getParameter(Constant.loginUserName);
		String passwd = req.getParameter(Constant.loginPasswd);

		if(username == null || passwd == null){
			log.fine("参数不全,退出:");
			resp.getWriter().println(String.format(Constant.respLoginTrue,"传入参数不足够,需要username和passwd参数."));
			return;
		}
		
		
		//获得用户,并将用户于session绑定在一起.
		user = User.newUser(username, passwd,session);
		
		if(user != null){
			session.setAttribute(Constant.sessionUserAttrib, user);
			
			//返回登录成功.
			resp.getWriter().println(Constant.respLoginTrue);
		}else{
			//失败,返回错误信息.
			log.info("用户为空,不记录");
			resp.getWriter().print(Constant.respLoginFalseWithUserOrPass);
		}
		
		super.doGet(req, resp);
	}
	
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}
}
