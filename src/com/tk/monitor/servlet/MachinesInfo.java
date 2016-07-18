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
import com.tk.monitor.FieldName;

public class MachinesInfo extends HttpServlet{
	private static final long serialVersionUID = -8502155827609533702L;
	private static final Logger log = Logging.getLogger("Servlet");
	private static final DataBaseHandle dbh = DataBaseHandle.getDBHandle(DBType.MYSQL);
	private int id = -1;
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

		String gettype = req.getParameter(Constant.reqParaGetType);

		if (gettype == null) {
			log.warning("没有gettype参数,操作中止");
			resp.getWriter().print(String.format(Constant.respGeneralFaile, "传入参数不足够,需要gettype参数.", 7));
			return;
		}

		try {
			pagecount = Integer.parseInt(req.getParameter(Constant.reqParaPageCount));
			pagenum = Integer.parseInt(req.getParameter(Constant.reqParaPageNum)) - 1;
		} catch (Exception e) {
			log.log(Level.SEVERE, "传入参数格式不正确" + e);
			resp.getWriter().print(String.format(Constant.respGeneralFaile, "传入参数格式不正确,请检查传入参数.", 10));
			return;
		}

		// 根据请求方式做相应处理.
		switch (gettype) {
		case Constant.GetTypeWithAll:
		case Constant.GetTypeWithProbably:
			getMachinesInfoALL(write);
			break;
		case Constant.GetTypeWithOneCount:
			String beginTime = req.getParameter(Constant.reqParaBeginTime);
			String endTime = req.getParameter(Constant.reqParaEndTime);
			int inl;

			try {
				SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				sf.parse(beginTime);
				sf.parse(endTime);
				inl = Integer.parseInt(req.getParameter(Constant.reqParaInterval));
				id = Integer.parseInt(req.getParameter(Constant.reqParaId));
			} catch (Exception e) {
				log.log(Level.SEVERE, "传入参数格式不正确" + e);
				resp.getWriter().print(String.format(Constant.respGeneralFaile, "传入参数格式不正确,请检查传入参数.", 9));
				return;
			}

			getMachinesInfoOneCount(write, beginTime, endTime, inl);
			break;
		case Constant.GetTypeWithOne:
			try {
				id = Integer.parseInt(req.getParameter(Constant.reqParaId));
			} catch (Exception e) {
				log.warning("没有id参数,或者Id参数不正确,操作中止");
				resp.getWriter().println(String.format(Constant.respLoginOk, "没有id参数,或者Id参数不正确,操作中止."));
				return;
			}
			getMachinesInfoOne(write);
			break;
		default:
			write.println(String.format(Constant.respGeneralFaile, "未知的请求方式!", 1));
			break;
		}

	}

	private void getMachinesInfoOneCount(PrintWriter write, String beginTime, String endTime, int inl) {
		String sql = "select a.*,b.mname from (select id,min(gettime) as mintime,max(gettime) as maxtime,"
				+ "floor(avg(totalmem)) as totalmem,floor(avg(freemem)) as freemem,floor(avg(maxmem)) as maxmem,"
				+ "floor(avg(totalphymem)) as totalphymem,floor(avg(freephymem)) as freephymem,"
				+ "floor(avg(usedphymem)) as usedphymem,floor(avg(totalthread)) as totalthread,"
				+ "avg(cpuratio) as cpuratio,floor(avg(totaldisk)) as totaldisk,floor(avg(useddisk)) as useddisk,"
				+ "floor(avg(freedisk)) as freedisk FROM machinesinfo "
				+ " where id = %d and gettime between '%s' and '%s' group by UNIX_TIMESTAMP(gettime) div (%d * 60 )) a "
				+ " inner join machines b on a.id = b.id order by a.id,mintime asc limit %d offset %d";

		ResultSet rs = dbh.select(String.format(sql, id, beginTime, endTime, inl, pagecount, pagecount * pagenum));

		StringBuilder sb = new StringBuilder();
		String na = null;
		try {
			while (rs.next()) {
				na = rs.getString("mname");

				sb.append("{\"mintime\":\"").append(rs.getString("mintime")).append("\",");
				sb.append("\"maxtime\":\"").append(rs.getString("maxtime")).append("\",");
				sb.append("\"totalmemory\":").append(rs.getInt("totalmem")).append(",");
				sb.append("\"freememory\":").append(rs.getInt("freemem")).append(",");
				sb.append("\"maxmemory\":").append(rs.getInt("maxmem")).append(",");
				sb.append("\"totalphysicalmemory\":").append(rs.getInt("totalphymem")).append(",");
				sb.append("\"freephysicalmemory\":").append(rs.getInt("freephymem")).append(",");
				sb.append("\"usedphysicalmemory\":").append(rs.getInt("usedphymem")).append(",");
				sb.append("\"totalthread\":").append(rs.getInt("totalthread")).append(",");
				sb.append("\"cpuratio\":").append(rs.getDouble("cpuratio")).append(",");
				sb.append("\"totaldisk\":").append(rs.getInt("totaldisk")).append(",");
				sb.append("\"usedDisk\":").append(rs.getInt("useddisk")).append(",");
				sb.append("\"freedisk\":").append(rs.getInt("freedisk"));
				sb.append("},");
			}
			dbh.close(rs);

			if (sb.length() > 0) {
				sb.deleteCharAt(sb.length() - 1);
			}
			sb.insert(0, String.format("{\"res\":true,\"id\":%d,\"name\":\"%s\",\"interval\":%d,\"details\":[", id, na, inl))
					.append("]}");

			String str = sb.toString();

			log.finest(str);
			write.print(str);
		} catch (

		SQLException e) {
			log.log(Level.SEVERE, "获得数据发生异常:", e);
		}
	}

	private void getMachinesInfoOne(PrintWriter write) {
		String sql = "select mi.id,mi.cpuratio,mi.freedisk,mi.useddisk,mi.freemem,mi.freephymem,mi.maxmem,m.isonline as online,"
				+ "mi.gettime,mi.totaldisk,mi.totalmem,mi.totalphymem,mi.totalthread,mi.usedphymem,m.mname,m.osname "
				+ "from machines m left join (select * from machinesinfo mi where not exists "
				+ "(select 1 from machinesinfo  where mi.id = id and mi.gettime = gettime and mi.gettime <gettime and id = %d) and mi.id = "
				+ id + ") mi on m.id = mi.id and m.flag = 0 order by id ;";

		ResultSet rs = dbh.select(String.format(sql, id));

		try {
			while (rs.next()) {
				if (rs.getInt("online") == 0) {
					write.println(String.format(Constant.oneMachinesInfo, id, rs.getString(FieldName.M_Name), "false", 0, 0, 0, 0,
							0, 0, 0, 0, 0, 0, 0, 0));

				} else {
					write.println(String.format(Constant.oneMachinesInfo, id, rs.getString(FieldName.M_Name), "true",
							rs.getInt(FieldName.MI_TotalMem), rs.getInt(FieldName.MI_FreeMem), rs.getInt(FieldName.MI_MaxMem),
							rs.getString(FieldName.M_OSName), rs.getInt(FieldName.MI_TotalPhyMem), rs.getInt(FieldName.MI_FreePhyMem),
							rs.getInt(FieldName.MI_UsedPhyMem), rs.getInt(FieldName.MI_TotalThread), rs.getInt(FieldName.MI_CPURatio),
							rs.getInt(FieldName.MI_TotalDisk), rs.getInt(FieldName.MI_UsedDisk), rs.getInt(FieldName.MI_FreeDisk)));
				}

				dbh.close(rs);
				return;
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, "执行获得数据发生异常:", e);
		}

	}

	private void getMachinesInfoALL(PrintWriter write) {
		String sql = "select mi.id,mi.cpuratio,mi.freedisk,mi.useddisk,mi.freemem,mi.freephymem,mi.maxmem,m.isonline as online,"
				+ "mi.gettime,mi.totaldisk,mi.totalmem,mi.totalphymem,mi.totalthread,mi.usedphymem,m.mname,m.osname "
				+ "from machines m left join (select * from machinesinfo mi where not exists "
				+ "(select 1 from machinesinfo  where mi.id = id and mi.gettime < gettime)) mi on m.id = mi.id where m.flag = 0 "
				+ " order by m.id limit %d offset %d;";

		ResultSet rs = dbh.select(String.format(sql, pagecount, pagecount * pagenum));

		StringBuilder sb = new StringBuilder();
		int on = 0;
		int off = 0;

		String onlineStr;
		try {
			while (rs.next()) {
				if (rs.getInt(FieldName.MI_Online) > 0) {
					onlineStr = "true";
					on++;
				} else {
					onlineStr = "false";
					off++;
				}

				sb.append(String.format(Constant.oneMachinesInfo, rs.getInt(FieldName.Id), rs.getString(FieldName.M_Name),
						onlineStr, rs.getInt(FieldName.MI_TotalMem), rs.getInt(FieldName.MI_FreeMem),
						rs.getInt(FieldName.MI_MaxMem), rs.getString(FieldName.M_OSName), rs.getInt(FieldName.MI_TotalPhyMem),
						rs.getInt(FieldName.MI_FreePhyMem), rs.getInt(FieldName.MI_UsedPhyMem), rs.getInt(FieldName.MI_TotalThread),
						rs.getInt(FieldName.MI_CPURatio), rs.getInt(FieldName.MI_TotalDisk), rs.getInt(FieldName.MI_UsedDisk),
						rs.getInt(FieldName.MI_FreeDisk))).append(",");
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, "执行获得数据发生异常", e);
		}
		// 删除多余的一个逗号,并加入尾部
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		sb.append("]}");
		// 加入头
		sb.insert(0,
				String.format("{\"res\":true,\"count\":%d,\"online\":%d,\"offline\":%d,\"data\":[", on + off, on, off));

		write.println(sb.toString());
		write.flush();

		dbh.close(rs);
	}
}
