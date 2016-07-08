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
			resp.getWriter().print(String.format(Constant.respGeneralFaile, "传入参数不足够,需要gettype参数.",7));
			return;
		}

		// 根据请求方式做相应处理.
		switch (gettype) {
		case Constant.GetTypeWithAll:
		case Constant.GetTypeWithProbably:
			getMachinesInfoALL(write);
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
	
	private void getMachinesInfoOne(PrintWriter write) {
		String sql = "select mi.id,mi.cpuratio,mi.freedisk,mi.useddisk,mi.freemem,mi.freephymem,mi.maxmem,m.isonline as online,"
				+ "mi.gettime,mi.totaldisk,mi.totalmem,mi.totalphymem,mi.totalthread,mi.usedphymem,m.mname,m.osname "
				+ "from machines m left join (select * from machinesinfo mi where not exists "
				+ "(select 1 from machinesinfo  where mi.id = id and mi.gettime = gettime and mi.gettime <gettime) and mi.id = "
				+ id + ") mi on m.id = mi.id and m.flag = 0;";

		ResultSet rs = dbh.select(sql);

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
				write.flush();

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
				+ "(select 1 from machinesinfo  where mi.id = id and mi.gettime <gettime)) mi on m.id = mi.id and m.flag = 0;";

		ResultSet rs = dbh.select(sql);

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
		sb.deleteCharAt(sb.length() - 1);
		sb.append("]}");
		// 加入头
		sb.insert(0, String.format("{res:true,count:%d,online:%d,offline:%d,data:[", on + off, on, off));

		write.println(sb.toString());
		write.flush();

		dbh.close(rs);
	}
}
