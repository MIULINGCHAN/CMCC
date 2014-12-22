package com.ocs.dao;

import java.util.List;

import com.ocs.bean.session.CFSession;

/**
 * 存取CF产生的会话数据的接口
 * @author Wang Chao
 *
 */
public interface CFSessionDAO {
	public int writeCFSession(CFSession session);
	
	public CFSession getCFSession(String sessionID);

	public int deleteCFSession(List<String> sessionIDs); 
}
