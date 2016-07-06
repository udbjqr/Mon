package com.tk.monitor.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tk.logger.Logging;
import com.tk.monitor.Constant;
import com.tk.monitor.Server;
import com.tk.monitor.collection.CollectionType;

/**
 * 跟Agent交换数据.
 * 
 * @author yimin
 *
 */
public class AgentServlet extends HttpServlet{
	private static final long serialVersionUID = 2970774480360621613L;
	private static final Logger log = Logging.getLogger();
	private static final Server server = Server.getIns();

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		resp.setCharacterEncoding("UTF-8");
		resp.setHeader("content-type", "text/html;charset=UTF-8");
		PrintWriter pw = resp.getWriter();

		// agent初始化请求,根据IP地址获得得相应采集数据
		if (req.getParameter(Constant.AgentExPara_InitData) != null) {
			pw.println(server.getAgentCollectionsJson(getAddress(req)));
			return;
		}

		//agent调用保存方法.
		String type = req.getParameter(Constant.AgentExPara_Type);
		String data = req.getParameter(Constant.AgentExPara_Data);
		if (type != null && data != null) {
			CollectionType cy;
			//判断类型是否可以转换
			try {
				cy = CollectionType.values()[Integer.parseInt(type)];
			} catch (Exception e) {
				log.log(Level.SEVERE, "不正确的参数值:type.值:" + type, e);
				pw.print(String.format(Constant.respGeneralFaile, "不正确的参数值:type.值:" + type, 9));
				return;
			}
			
			//调用保存是否成功
			try {
				server.save(cy, data);
				pw.print(String.format(Constant.respLoginOk));
			} catch (Exception e) {
				log.log(Level.SEVERE, "无法保存,请检查参数是否正确."+ data,e);
				pw.print(String.format(Constant.respGeneralFaile,"无法保存,请检查参数是否正确.",e));
			}
			return;
		}
	}

  private String getAddress(HttpServletRequest request) {  
    String ip = request.getHeader("x-forwarded-for");  
    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {  
        ip = request.getHeader("Proxy-Client-IP");  
    }  
    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {  
        ip = request.getHeader("WL-Proxy-Client-IP");  
    }  
    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {  
        ip = request.getRemoteAddr();  
    }  
    return ip;  
}  
	
}
