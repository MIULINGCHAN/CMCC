package com.ocs.dao.impl;

import com.ocs.bean.abm.ReserveBalance;
import com.ocs.bean.event.RatingResult;
import com.ocs.bean.session.CFSession;
import com.ocs.dao.CFSessionDAO;

public class TestTair {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("<<<<<<<<<<<存储会话数据.");
    	CFSession cfSession = new CFSession();
    	cfSession.setSessionID("kkkkkkkk");
    	
    	RatingResult ratingResult = new RatingResult();
    	ratingResult.setPkgID("1111");
    	ratingResult.setQuantity(1234);
    	ratingResult.setRuleID("11");
    	ratingResult.setRuleName("mmmm");
    	ratingResult.setTotalPrice(0.12);
    	cfSession.setDataBody(ratingResult);
    	
    	ReserveBalance rb = new ReserveBalance("kkk", 1234, 111, 1234, 0, 123, null, null, null);
    	cfSession.setReserveBalance(rb);
    	System.out.println(cfSession);
    	CFSessionDAO cfSessionDAO = new CFSessionDAOTairImpl();
    	if (cfSessionDAO.writeCFSession(cfSession) == 1) {
    		System.out.println("存储会话数据到tiar成功！");
    	}
    	else{
    		System.out.println("存储会话数据到tiar失败！");
    	}
	}

}
