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
	private int pagecount = 20;
	private int pagenum = 0;

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
			if (req.getParameter(Constant.reqParaPageCount) != null) {
				pagecount = Integer.parseInt(req.getParameter(Constant.reqParaPageCount));
			}

			if (req.getParameter(Constant.reqParaPageNum) != null) {
				pagenum = Integer.parseInt(req.getParameter(Constant.reqParaPageNum)) - 1;
			}
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
		case Constant.GetTypeWithSystemCall:
			getInfoSystemCall(write, beginTime, endTime);
			break;
		default:
			write.println(String.format(Constant.respGeneralFaile, "未知的请求方式!", 1));
			break;
		}

	}

	private void getInfoSystemCall(PrintWriter write, String beginTime, String endTime) {
		String sql = "select  m.id,min(m.mname) as mname,sum(i.callnum) as callnum,avg(i.cost_time) as cost_time "
				+ " from machines m inner join interfaceinfo i on m.id = i.collid where m. colldimension = 2 "
				+ "and i.gettime between '%s' and '%s' group by m.id order by m.id;";

		ResultSet rs = dbh.select(String.format(sql, beginTime, endTime));
		StringBuilder sb = new StringBuilder();
		try {

			// {res:true,mintime:"XXX",maxtime:"XXX",details:[{id:xx,name:"XXX",cost_time:xx,callnum:xx}...]}
			while (rs.next()) {
				sb.append("{\"cost_time\":").append(rs.getDouble("cost_time")).append(",");
				sb.append("\"id\":").append(rs.getString("id")).append(",");
				sb.append("\"name\":\"").append(rs.getString("mname")).append("\",");
				sb.append("\"callnum\":").append(rs.getString("callnum")).append(",");
				sb.append("},");
			}
			dbh.close(rs);

			if (sb.length() > 0) {
				sb.deleteCharAt(sb.length() - 1);
			}

			sb.insert(0,
					String.format("{\"res\":true,\"mintime\":\"%s\",\"maxtime\":\"%s\",\"details\":[", beginTime, endTime))
					.append("]}");

			write.print(sb.toString());
		} catch (SQLException e) {
			log.log(Level.SEVERE, "获得监控调用数据发生异常:", e);
		}
	}

	private void getInfoOne(PrintWriter write, String beginTime, String endTime, int inl) {
		String sql = "select a.*,b.mname from (select collid,min(gettime) as mintime,max(gettime) as maxtime,avg(cost_time) as cost_time,sum(callnum) "
				+ "FROM interfaceinfo where collid = %1$d and gettime between '%2$s' and '%3$s' group by id,UNIX_TIMESTAMP(gettime) div (%4$d * 60)) a"
				+ " inner join machines b on a.collid = b.id order by a.collid,mintime asc ";

		ResultSet rs = dbh.select(String.format(sql, id, beginTime, endTime, inl));

		StringBuilder sb = new StringBuilder();
		String na = null;
		try {
			while (rs.next()) {
				na = rs.getString("mname");

				sb.append("{\"cost_time\":").append(rs.getDouble("cost_time")).append(",");
				sb.append("\"callnum\":").append(rs.getString("callnum")).append(",");
				sb.append("\"mintime\":\"").append(rs.getString("mintime")).append("\",");
				sb.append("\"maxtime\":\"").append(rs.getString("maxtime")).append("\"");
				sb.append("},");
			}
			dbh.close(rs);

			if (sb.length() > 0) {
				sb.deleteCharAt(sb.length() - 1);
			}

			sb.insert(0, String.format("{\"res\":true,\"id\":%d,\"name\":\"%s\",\"interval\":%d,\"details\":[", id, na, inl))
					.append("]}");

			write.print(sb.toString());
		} catch (SQLException e) {
			log.log(Level.SEVERE, "获得数据发生异常:", e);
		}
	}

	private void getInfoALL(PrintWriter write, String beginTime, String endTime, int inl) {
		String sql = "select a.*,b.mname,b.isonline "
				+ " from (select collid,min(gettime) as mintime,max(gettime) as maxtime,avg(cost_time) as cost_time,sum(callnum) as callnum "
				+ " FROM interfaceinfo where gettime between '%1$s' and '%2$s' group by collid,UNIX_TIMESTAMP(gettime) div (%3$d * 60)) a"
				+ " inner join machines b on a.collid = b.id order by a.collid,mintime asc limit %4$d offset %5$d";

		ResultSet rs = dbh.select(String.format(sql, beginTime, endTime, inl, pagecount, pagecount * pagenum));

		try {
			StringBuilder sb = new StringBuilder();
			int on = 0, off = 0, pid = -1, tempon = 0, ind = 0;
			String bt = "", et = "", name = "";
			while (rs.next()) {
				// 最详细内容
				sb.append(
						String.format("{\"cost_time\":%s,\"callnum\":%s},", rs.getString("cost_time"), rs.getString("callnum")));
				et = rs.getString("maxtime");

				if (tempon == 1) {
					on++;
				} else {
					off++;
				}

				if (pid != rs.getInt("collid")) {
					bt = rs.getString("mintime");
					name = rs.getString("mname");
					tempon = rs.getInt("isonline");

					if (pid != -1) {
						if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ',') {// 判断最后一个是否为逗号,是的话,删除.因为有可能没有任何数据.
							sb.deleteCharAt(sb.length() - 1);
						}
						// 增加信息体内容.
						sb.insert(ind,
								String.format(
										"{\"id\":%d,\"name\":\"%s\",\"mintime\":\"%s\",\"maxtime\":\"%s\",\"interval\":%d,\"details\":[",
										pid, name, bt, et, inl))
								.append("]},");

						ind = sb.length() - 1;
					}
				}
				pid = rs.getInt("collid");
			}

			sb.deleteCharAt(sb.length() - 1);

			// 当循环结束,再加一次尾,不再加逗号.
			sb.insert(ind,
					String.format(
							"{\"id\":%d,\"name\":\"%s\",\"mintime\":\"%s\",\"maxtime\":\"%s\",\"interval\":%d,\"details\":[", pid,
							name, bt, et, inl))
					.append("]}");

			dbh.close(rs);

			// 加最外层.
			sb.insert(0,
					String.format("{\"res\":true,\"count\":%d,\"online\":%d,\"offline\":%d,\"data\":[", on + off, on, off))
					.append("]}");

			write.print(sb.toString());
		} catch (SQLException e) {
			log.log(Level.SEVERE, "获得数据发生异常:", e);
		}
	}
}
