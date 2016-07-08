package com.tk.monitor.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.tk.logger.Logging;
import com.tk.monitor.Constant;
import com.tk.monitor.users.User;
import com.tk.sql.DBType;
import com.tk.sql.DataBaseHandle;

public class InterfaceCallInfo extends HttpServlet{
	private static final long serialVersionUID = -8766613653174134108L;
	private static final Logger log = Logging.getLogger("Servlet");
	private static final DataBaseHandle dbh = DataBaseHandle.getDBHandle(DBType.MYSQL);
	private int id;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		resp.setCharacterEncoding("UTF-8");
		resp.setHeader("content-type", "text/html;charset=UTF-8");

		HttpSession session = req.getSession();
		User user = (User) session.getAttribute(Constant.sessionUserAttrib);

		// 判断此session是否已经存在用户,不存在则直接返回
		if (user == null) {
			log.warning("用户不存在,操作中止");
			resp.getWriter().println(Constant.respNotLogin);
			return;
		}

		PrintWriter write = resp.getWriter();

		String getType = req.getParameter(Constant.reqParaGetType);
		String beginTime = req.getParameter(Constant.reqParaBeginTime);
		String endTime = req.getParameter(Constant.reqParaEndTime);
		String interval = req.getParameter(Constant.reqParaInterval);

		if (getType == null && beginTime == null && endTime == null && interval == null) {
			log.warning("没有足够的参数,操作中止");
			resp.getWriter().print(String.format(Constant.respGeneralFaile, "传入参数不足够,请检查传入参数.", 7));
			return;
		}
		int inl = 1;

		try {
			SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			sf.parse(beginTime);
			sf.parse(endTime);
			inl = Integer.parseInt(interval);
		} catch (Exception e) {
			log.log(Level.SEVERE, "传入参数格式不正确" + e);
			resp.getWriter().print(String.format(Constant.respGeneralFaile, "传入参数格式不正确,请检查传入参数.", 8));
			return;
		}

		// 根据请求方式做相应处理.
		switch (getType) {
		case Constant.GetTypeWithAll:
		case Constant.GetTypeWithProbably:
			getInfoALL(write, beginTime, endTime, inl);
			break;
		case Constant.GetTypeWithOne:
			try {
				id = Integer.parseInt(req.getParameter(Constant.reqParaId));
			} catch (Exception e) {
				log.warning("没有id参数,或者Id参数不正确,操作中止");
				resp.getWriter().println(String.format(Constant.respLoginOk, "没有id参数,或者Id参数不正确,操作中止."));
				return;
			}
			getInfoOne(write, beginTime, endTime, inl);
			break;
		default:
			write.println(String.format(Constant.respGeneralFaile, "未知的请求方式!", 1));
			break;
		}

	}

	private void getInfoOne(PrintWriter write, String beginTime, String endTime, int inl) {
		String sql = "select a.*,b.mname from (select id,min(gettime) as mintime,max(gettime) as maxtime,avg(cost_time) as cost_time,sum(callnum) "
				+ "FROM interfaceinfo where id = %1$d and gettime between '%2$s' and '%3$s' group by id,gettime div (%4$d * 60)) a"
				+ " inner join machines b on a.id = b.id";

		ResultSet rs = dbh.select(String.format(sql, id, beginTime, endTime, inl));

		StringBuilder sb = new StringBuilder();
		String bt = null;
		String et = null;
		String na = null;
		try {
			while (rs.next()) {
				bt = rs.getString("mintime");
				et = rs.getString("maxtime");
				na = rs.getString("mname");

				sb.append("{cost_time:" + rs.getString("cost_time") + ",callnum:" + rs.getString("callnum") + "},");
			}
			dbh.close(rs);

			if (sb.length() > 0) {
				sb.deleteCharAt(sb.length() - 1);
			}

			sb.insert(0, String.format("{res:true,id:%1$d,name:\"%s\",mintime:\"%s\",maxtime:\"%s\",interval:%d,details:[",
					id, na, bt, et, inl)).append("]}");

			write.print(sb.toString());
		} catch (SQLException e) {
			log.log(Level.SEVERE, "获得数据发生异常:", e);
		}
	}

	private void getInfoALL(PrintWriter write, String beginTime, String endTime, int inl) {
		String sql = "select a.*,b.mname,b.online "
				+ " from (select id,min(gettime) as mintime,max(gettime) as maxtime,avg(cost_time) as cost_time,sum(callnum) "
				+ "FROM interfaceinfo where gettime between '%1$s' and '%2$s' group by id,gettime div (%3$d * 60)) a"
				+ " inner join machines b on a.id = b.id order by a.id";

		ResultSet rs = dbh.select(String.format(sql, beginTime, endTime, inl));

		try {
			StringBuilder sb = new StringBuilder();
			int on = 0, off = 0, pid = -1, tempon = 0;
			String bt = "", et = "", name = "";
			while (rs.next()) {
				if (pid != rs.getInt("id")) {	// 判断是否新的一个数据.
					if (sb.charAt(sb.length() - 1) == ',') {// 判断最后一个是否为逗号,是的话,删除.因为有可能没有任何数据.
						sb.deleteCharAt(sb.length() - 1);
					}
					
					if (sb.length() != 0) {// 判断缓冲为空.不为空加尾部.
						sb.append("]},");
					}
					// 第一次进来先取数据.
					pid = rs.getInt("id");
					bt = rs.getString("mintime");
					et = rs.getString("maxtime");
					name = rs.getString("mname");
					tempon = rs.getInt("online");

					// 增加信息体内容.
					sb.insert(0, String.format("{id:%d,name:\"%s\",mintime:\"%s\",maxtime:\"%s\",interval:%d,details:[", id, bt,
							et, name));

					if (tempon == 1) {
						on++;
					} else {
						off++;
					}
				}

				// 最详细内容
				sb.append(String.format("{cost_time:%s,callnum:%s},", rs.getString("cost_time"), rs.getString("callnum")));
			}
			
			
			// 当循环结束,再加一次尾,不再加逗号.
			sb.append("]}");

			dbh.close(rs);

			// 加最外层.
			sb.insert(0, String.format("{res:true,count:%d,online:%d,offline:%d,data:[", on + off, on, off)).append("]}");

			write.print(sb.toString());
		} catch (SQLException e) {
			log.log(Level.SEVERE, "获得数据发生异常:", e);
		}
	}
}
