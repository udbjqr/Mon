package com.tk.monitor.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.tk.logger.Logging;
import com.tk.monitor.Constant;
import com.tk.monitor.FieldName;
import com.tk.monitor.users.User;
import com.tk.sql.DBType;
import com.tk.sql.DataBaseHandle;

public class InterfaceCallInfo extends HttpServlet{
	private static final long serialVersionUID = -8766613653174134108L;
	private static final Logger log = Logging.getLogger("Servlet");
	private static final DataBaseHandle dbh = DataBaseHandle.getDBHandle(DBType.Mysql);
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
		Date bt = null;
		Date et = null;
		int inl = 1;

		try {
			SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			bt = sf.parse(beginTime);
			et = sf.parse(endTime);
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
			getInfoALL(write, bt, et, inl);
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
				+ "FROM interfaceinfo where id = %1$d and gettime between '%2$s' and '%3$s' group by id,i.gettime div (%4$d * 60)) a"
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
				sb.delete(sb.length() - 1, sb.length());
			}

			sb.insert(0,
					String.format("{res:true,id:%1$d,name:\"%2$s\",mintime:\"%3$s\",maxtime:\"%4$s\",details:[", id, na, bt, et))
					.append("]}");

			write.print(sb.toString());
			return;
		} catch (SQLException e) {
			log.log(Level.SEVERE, "执行获得数据发生异常:", e);
		}
	}

	private void getInfoALL(PrintWriter write, Date beginTime, Date endTime, int inl) {
		String sql = "select mi.id,mi.cpuratio,mi.freedisk,mi.useddisk,mi.freemem,mi.freephymem,mi.maxmem,ifnull(mi.online,0) as online,"
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
		sb.delete(sb.length() - 1, sb.length());
		sb.append("]}");
		// 加入头
		sb.insert(0, String.format("{res:true,count:%d,online:%d,offline:%d,data:[", on + off, on, off));

		write.println(sb.toString());
		write.flush();

		dbh.close(rs);
	}
}
