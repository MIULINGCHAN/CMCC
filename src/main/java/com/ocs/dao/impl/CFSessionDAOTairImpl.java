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
		// ����config server�б�
		List<String> confServers = new ArrayList<String>();
		String tairConfigIP = PropertiesUtils.getTairConfigServerIP();
		int tairConfigPort = PropertiesUtils.getTairConfigServerPort();
		confServers.add(tairConfigIP + ":" + tairConfigPort);
		// confServers.add("CONFIG_SERVER_ADDREEE_2:PORT"); // ��ѡ

		// �����ͻ���ʵ��
		this.tairManager = new DefaultTairManager();
		tairManager.setConfigServerList(confServers);

		// ��������
		tairManager.setGroupName("group_1");
		// ��ʼ���ͻ���
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
			System.out.println("����tair���󣬴�����Ϊ��" + result2.getRc());
		}
		
		return cfSession;
	}

	/**
	 * ����ɾ��
	 */
	@Override
	public int deleteCFSession(List<String> sessionIDs) {
		ResultCode rc = tairManager.mdelete(0, sessionIDs);
		
		if ( rc.isSuccess() )
			return 1;
		
		return -1;
	}

}
