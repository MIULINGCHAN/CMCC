package com.ocs.dao.impl;

import java.util.ArrayList;
import java.util.List;

import com.ocs.bean.session.CFSession;
import com.ocs.dao.CFSessionDAO;
import com.ocs.utils.PropertiesUtils;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.taobao.tair.ResultCode;
import com.taobao.tair.impl.DefaultTairManager;

public class CFSessionDAOTairImpl implements CFSessionDAO {

	private DefaultTairManager tairManager;

	public CFSessionDAOTairImpl() {
		super();
		// 创建config server列表
		List<String> confServers = new ArrayList<String>();
		String tairConfigIP = PropertiesUtils.getTairConfigServerIP();
		int tairConfigPort = PropertiesUtils.getTairConfigServerPort();
		confServers.add(tairConfigIP + ":" + tairConfigPort);
		// confServers.add("CONFIG_SERVER_ADDREEE_2:PORT"); // 可选

		// 创建客户端实例
		this.tairManager = new DefaultTairManager();
		tairManager.setConfigServerList(confServers);

		// 设置组名
		tairManager.setGroupName("group_1");
		// 初始化客户端
		tairManager.init();
	}

	@Override
	public int writeCFSession(CFSession session) {
		ResultCode result = tairManager.put(0, session.getSessionID(), session);
		if (result.isSuccess()) {
			System.out.println("put success.");
			return 1;
		} 
		
		return -1;
	}
	
	@Override
	public CFSession getCFSession(String sessionID) {
		CFSession cfSession = null;
		
		Result<DataEntry> result2 = tairManager.get(0, sessionID);
		if (result2.isSuccess()) {
			DataEntry entry = result2.getValue();
			if (entry != null) {
				cfSession = (CFSession)entry.getValue();
			} 
		} 
		else {
			System.out.println("访问tair错误，错误码为：" + result2.getRc());
		}
		
		return cfSession;
	}

	/**
	 * 批量删除
	 */
	@Override
	public int deleteCFSession(List<String> sessionIDs) {
		ResultCode rc = tairManager.mdelete(0, sessionIDs);
		
		if ( rc.isSuccess() )
			return 1;
		
		return -1;
	}

}
