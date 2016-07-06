package com.tk.monitor.collection;

/**
 * 实现此接口的对象知道如何将数据保存至持久层.
 * 
 * <p>所有采集对象需要此接口用以将采集到的数据保存.
 * 
 * @author yimin
 *
 */
public interface CollectionRecord{

	/**
	 * 简单的将采集到的数据形成的Json格式进行保存.
	 * @param collData 采集到的数据的json格式.
	 */
	public void save(CollectionType coll,String collDataJson);
}
