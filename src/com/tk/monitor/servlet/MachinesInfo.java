package com.tk.monitor.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONObject;
import com.tk.monitor.Constant;
import com.tk.monitor.users.User;
import com.tk.sql.DBType;
import com.tk.sql.DataBaseHandle;

public class MachinesInfo extends HttpServlet{
	private static final long serialVersionUID = -8502155827609533702L;
	private static final Logger log = com.tk.monitor.logger.Logger.getLogger();
	private static final DataBaseHandle dbh = DataBaseHandle.getDBHandle(DBType.Mysql);
	private int id = -1;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setCharacterEncoding("UTF-8");
		resp.setHeader("content-type", "text/html;charset=UTF-8");

		HttpSession session = req.getSession();
		User user = (User) session.getAttribute(Constant.sessionUserAttrib);

		// 判断此session是否已经存在用户,不存在则直接返回
		if (user == null) {
			log.warning("用户不存在,操作中止");
			resp.getWriter().write(Constant.respNotLogin);
			return;
		}

		PrintWriter write = resp.getWriter();

		String parastr = req.getParameter(Constant.reqPara);
		log.fine("收到请求:" + parastr);
		JSONObject para = new JSONObject(parastr);

		try {
			id = Integer.parseInt(para.getString(Constant.reqParaId));
		} catch (NumberFormatException e) {
			log.log(Level.SEVERE, "转换ID出现错误,传入的值:" + para.getString(Constant.reqParaId), e);
			write.write(String.format(Constant.respGeneralFalse, "参数:ID错误", 2));
			write.flush();
			return;
		}

		// 根据请求方式做相应处理.
		switch (para.getString(Constant.reqParaGetType)) {
		case Constant.GetTypeWithAll:
			getMachinesInfoALL(write, para);
			break;
		case Constant.GetTypeWithOne:
			getMachinesInfoOne(write, para);
			break;
		case Constant.GetTypeWithProbably:
			getMachinesInfoProbably(write, para);
			break;

		default:
			write.write(String.format(Constant.respGeneralFalse, "未知的请求方式!", 1));
			break;
		}

	}

	private void getMachinesInfoProbably(PrintWriter write, JSONObject para) {
		// TODO Auto-generated method stub

	}

	private void getMachinesInfoOne(PrintWriter write, JSONObject para) {
		ResultSet rs = dbh
				.select("select  m.*,m1.osName from machinesinfo m right join machines m1 on m1.id = m.id where m1.id = " + id
						+ " order by time desc  limit 1");

		try {
			while (rs.next()) {
				if(rs.getInt("online") == 0){
					//TODO 返回机器离线信息.
					return;
				} 
				
				//TODO 返回机器实时信息.
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		dbh.close(rs);
	}

	private void getMachinesInfoALL(PrintWriter write, JSONObject para) {
		// TODO Auto-generated method stub

	}
}
