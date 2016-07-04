package com.tk.monitor.users;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

/**
 * 指明一个监控的用户对象.
 * 
 * <p>
 * 不同的监控对象,应该从此类继承并改写登录方式 此类自维护一个列表,
 * 
 * @author yimin
 *
 */
public class User{
	private static Logger log = com.tk.monitor.logger.Logger.getLogger();

	protected static final Set<User> users = new HashSet<User>();
	private final String tostr;

	protected final String name;
	protected final int id;
	protected String passwd;
	protected final HttpSession session;
	
	/**
	 * 建创一个新的用户,一个新用户登录的时候调用此方法.
	 * 
	 * @param name
	 *          用户名
	 * @param passwd
	 *          密码
	 * @return 用户的实例,如果为null指明未找到此用户或者密码不正确
	 */
	public static User newUser(String name, String passwd,HttpSession session) {
		// 这里是判断用户账号密码是否正确的过程.
		boolean ok = true;
		int id = 0;
		User u = null;
		
		//TODO 目前这里使用任何用户名均通过的方式
		if (ok) {
			log.finest("新增加用户:" + name + "\t" + passwd);
			u = new User(name, id,session);
			
			users.add(u);
		} else {
			log.warning("用户名或密码错误:参数:" + name + "\t" + passwd);
		}

		return u;
	}

	/**
	 * 新增加一个用户 ,此操作不允许直接构造.
	 * 
	 * @param name
	 * @param id
	 */
	protected User(String name, int id,HttpSession session) {
		this.name = name;
		this.id = id;
		this.session = session;
		this.tostr = "name:" + name + ",id:" + id + "session:" + session;
	}

	private User() {
		this(null,-1,null);
		log.severe("不允许的操作.");
		// 不允许的操作
	}

	/**
	 * 增加一个用户.
	 * 
	 * @param user
	 */
	public void addUser(User user) {
		log.finest("用户集合增加用户:" + user);
		users.add(user);
	}

	public Set<User> getUsers() {
		return users;
	}

	/**
	 * 移除一个用户.
	 * 
	 * @param user
	 */
	public static void removeUser(User user) {
		log.finest("移除一个用户:" + user);
		users.remove(user);
	}

	/**
	 * 移除一个用户.
	 * 
	 * <p>
	 * 如果用户不存在,不做任何操作
	 * 
	 * @param user
	 */
	public static void removeUser(int id) {
		for (User user : users) {
			if (user.id == id) {
				removeUser(user);
				return;
			}
		}
		
		log.warning("要移除的用户未找到,id:" + id);
	}

	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return tostr;
	}
}
