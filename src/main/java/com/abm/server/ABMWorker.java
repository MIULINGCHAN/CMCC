package com.abm.server;

import java.sql.Timestamp;
import java.util.ArrayList;

import com.ocs.bean.abm.Balance;
import com.ocs.bean.abm.Counter;
import com.ocs.bean.abm.DeductBalance;
import com.ocs.bean.abm.DeductCounter;
import com.ocs.bean.abm.ReserveBalance;
import com.ocs.bean.abm.Subscriber;
import com.ocs.protocol.diameter.AVP;
import com.ocs.protocol.diameter.AVP_Float32;
import com.ocs.protocol.diameter.AVP_Grouped;
import com.ocs.protocol.diameter.AVP_Integer32;
import com.ocs.protocol.diameter.AVP_Time;
import com.ocs.protocol.diameter.AVP_UTF8String;
import com.ocs.protocol.diameter.AVP_Unsigned32;
import com.ocs.protocol.diameter.AVP_Unsigned64;
import com.ocs.protocol.diameter.InvalidAVPLengthException;
import com.ocs.protocol.diameter.Message;
import com.ocs.protocol.diameter.ProtocolConstants;
import com.ocs.protocol.diameter.Utils;
import com.ocs.protocol.diameter.node.ConnectionKey;
import com.ocs.protocol.diameter.node.NodeManager;
import com.ocs.protocol.diameter.node.NotAnAnswerException;
import com.ocs.utils.MessageUtils;

public class ABMWorker implements Runnable{
	private NodeManager nodeManager;
	private ConnectionKey connKey;
	private Message request;
	
	public ABMWorker(NodeManager nodeManager, ConnectionKey connKey,
			Message request) {
		super();
		this.nodeManager = nodeManager;
		this.connKey = connKey;
		this.request = request;
	}
	
	@Override
	public void run() {
		System.out.println(Thread.currentThread().getName() + "Started=================================");
		
		// server
		Message answer = new Message();
		answer.prepareResponse(request);
		
		switch (request.hdr.command_code) {
		case ProtocolConstants._3GPP_COMMAND_AR:
			handleARQ(request, connKey,answer);
			break;
		case ProtocolConstants._3GPP_COMMAND_AO:
			try {
				handleAOQ(request, connKey,answer);
			} catch (InvalidAVPLengthException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		default:
			break;
		}
		
		Utils.setMandatory_RFC3588(answer);

		try {
			nodeManager.answer(answer, connKey);
		} catch (NotAnAnswerException ex) {
		}
		
		System.out.println(Thread.currentThread().getName() + "Ended====================================\n");
	}
	
	private void handleARQ(Message request, ConnectionKey connkey, Message answer) {
		System.out.println("----- get ARQ -----");
		
		AVP avp;
		
		// < Session-ID >
		avp = request.find(ProtocolConstants.DI_SESSION_ID);
		if (avp != null)
			answer.add(avp);
		
		// { Origin-Host }
		// { Origin-Realm }
		nodeManager.node().addOurHostAndRealm(answer);
		
		// { Subscription-ID }
		avp = request.find(ProtocolConstants.DI_SUBSCRIPTION_ID);
		Subscriber subscribers = new Subscriber();
		if(avp != null){
			AVP[] avps;
			try {
				avps = new AVP_Grouped(avp).queryAVPs();
				for(AVP tmpAVP : avps ){
					int code = tmpAVP.code;
					switch (code) {
					case ProtocolConstants.DI_SUBSCRIPTION_ID_TYPE: 						
						subscribers.setSubscriberIdType(new AVP_Unsigned32(tmpAVP).queryValue());					
						break;
					case ProtocolConstants.DI_SUBSCRIPTION_ID_DATA:
						subscribers.setSubscriberIdData(new AVP_UTF8String(tmpAVP).queryValue());
						break;
					default:
						break;
					}
				}
				
				answer.add(avp);
			} catch (InvalidAVPLengthException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		// 准备链接数据库查询数据
		MySQLConnector c = new MySQLConnector();
		
		// 查找用户基本信息
		Subscriber dbSubscriber = c.getSubsInfo(Long.parseLong(subscribers.getSubscriberIdData()));
		if(dbSubscriber==null){
			// 数据库中无此用户
			System.out.println("找不到用户 ：" + subscribers.getSubscriberIdData());
			// { Result-Code }
			answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE,ProtocolConstants.DIAMETER_RESULT_USER_UNKNOWN));
			return;
		}
		else{
			subscribers.setBelongArea(dbSubscriber.getBelongArea());
			subscribers.setAcctID(dbSubscriber.getAcctID());
			System.out.println(subscribers.toString());
		}
		
		// { Subscriber-information }
		//    {Subscriber-Belong-Area-No}
		answer.add(new AVP_Grouped(ProtocolConstants._3GPP_DI_SUBSCRIBER_INFORMATION,
				new AVP[]{
				new AVP_UTF8String(ProtocolConstants._3GPP_DI_SUBSCRIBER_BELONG_AREA_NO, subscribers.getBelongArea())
		}));
		
		// {Account-Id}
		if(subscribers.getAcctID()!=null){
			answer.add(new AVP_UTF8String(ProtocolConstants._3GPP_DI_ACCOUNT_ID, subscribers.getAcctID()));
		}
		
		ArrayList<Balance> balances = c.getAccountBalance(Long.parseLong(subscribers.getAcctID()));
		ArrayList<Counter> counters = c.getCounterByObjectID(Long.parseLong(subscribers.getSubscriberIdData()));
		if((balances==null||balances.isEmpty())&&(counters==null||counters.isEmpty())){
			// 找不到用户
			System.out.println("找不到用户 ：" + subscribers.getSubscriberIdData());
			// { Result-Code }
			answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE,ProtocolConstants.DIAMETER_RESULT_USER_UNKNOWN));
			return;
		}
		else{
			// *[ Balance ]
			System.out.println(">>>>> There is "+balances.size()+" Balance item(s)");
			for(Balance b : balances){
				System.out.println(b.toString());
				answer.add(new AVP_Grouped(ProtocolConstants._3GPP_DI_BALANCE,
						new AVP[]{
							new AVP_Unsigned64(ProtocolConstants._3GPP_DI_BALANCE_ID, b.getBalanceID()),
							new AVP_Integer32(ProtocolConstants._3GPP_DI_BALANCE_TYPE, b.getBalanceType()),
							new AVP_Time(ProtocolConstants._3GPP_DI_BALANCE_EXPIRY_DATE, b.getBalanceExpDate()),
							new AVP_Float32(ProtocolConstants._3GPP_DI_BALANCE_VALUE, (float)b.getBalanceValue())
				} ));
			}
			
			// *[ Counter ]
			for(Counter cnt : counters){
				answer.add(new AVP_Grouped(ProtocolConstants._3GPP_DI_COUNTER,
						new AVP[]{
							new AVP_Unsigned64(ProtocolConstants._3GPP_DI_COUNTER_ID, cnt.getCounterID()),
							new AVP_UTF8String(ProtocolConstants._3GPP_DI_COUNTER_TYPE, cnt.getCounterType()),
							new AVP_Time(ProtocolConstants._3GPP_DI_COUNTER_EXPIRY_DATE, cnt.getCounterExpTime()),
							new AVP_Float32(ProtocolConstants._3GPP_DI_COUNTER_VALUE, (float)cnt.getCounterValue()),
							new AVP_Float32(ProtocolConstants._3GPP_DI_COUNTER_THRESHOLD, (float)cnt.getCounterThreshold())
				}));
			}
			
			// { Result-Code }
			answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE,
					ProtocolConstants.DIAMETER_RESULT_SUCCESS));
		}

	}
	
	private void handleAOQ(Message request, ConnectionKey connkey, Message answer) throws InvalidAVPLengthException {
		System.out.println("----- get AOQ -----");
		
		MySQLConnector connector = new MySQLConnector();
		
		AVP avp;
		
		// < Session-ID >
		String session_id = "";
		avp = request.find(ProtocolConstants.DI_SESSION_ID);
		if (avp != null){
			answer.add(avp);
			session_id = MessageUtils.querySessionID(request);
		}
		
		// { Origin-Host }
		// { Origin-Realm }
		nodeManager.node().addOurHostAndRealm(answer);
		
		// get subscriber-id
		avp = request.find(ProtocolConstants._3GPP_DI_SUBSCRIBER_ID);
		long subscriber_id = Long.parseLong(new AVP_UTF8String(avp).queryValue());
		System.out.println("subscriber id : " + subscriber_id);
		
		// get account-id
		avp = request.find(ProtocolConstants._3GPP_DI_ACCOUNT_ID);
		long account_id = Long.parseLong(new AVP_UTF8String(avp).queryValue());
		System.out.println("account id : " + account_id);
		
		// get deduct-operation
		int service_identifier = 0;
		ArrayList<DeductCounter> dcList = new ArrayList<DeductCounter>();
		ArrayList<DeductBalance> dbList = new ArrayList<DeductBalance>();
		avp = request.find(ProtocolConstants._3GPP_DI_MULTIPLE_DEDUCT_OPERATION);
		if(avp != null){
			AVP[] do_avps = new AVP_Grouped(avp).queryAVPs();
			for(AVP tmpAVP : do_avps){
				int code = tmpAVP.code;
				switch (code) {
				case ProtocolConstants.DI_SERVICE_IDENTIFIER: 
					service_identifier = new AVP_Unsigned32(tmpAVP).queryValue();
					break;
				case ProtocolConstants._3GPP_DI_BALANCE:
					parseDeductBalances(account_id,new AVP_Grouped(tmpAVP), dbList);
					
					System.out.println("扣减账本请求--");
					for( DeductBalance db : dbList )
						System.out.println(db.toString());
					System.out.println("扣减账本请求--END");
					break;
				case ProtocolConstants._3GPP_DI_COUNTER:
					DeductCounter dc = parseDeductCounter(new AVP_Grouped(tmpAVP));
					dcList.add(dc);
					break;
				default:
					break;
				}
			}
		}
			
		System.out.println("扣减累积量请求--");
		for( DeductCounter dc : dcList )
			System.out.println(dc.toString());
		System.out.println("扣减累积量请求--END");	
		
		// 进行扣减
		boolean deductBalanceRslt = connector.deductBalance(dbList);
		boolean deductCounterRslt = connector.deductCounter(dcList, String.valueOf(subscriber_id));
		boolean deductResult = deductBalanceRslt && deductCounterRslt;
		// * [ Multiple-Deduct-Operation ]
		// 		{ Service-Identifier }
		//      [ Check-Time ]   不知道是什么参数？
		//      { Result-Code }
			
		if(deductResult){
			answer.add(new AVP_Grouped(ProtocolConstants._3GPP_DI_MULTIPLE_DEDUCT_OPERATION,
					new AVP[]{
					new AVP_Unsigned32(ProtocolConstants.DI_SERVICE_IDENTIFIER,service_identifier),
					new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE,ProtocolConstants.DIAMETER_RESULT_SUCCESS)
			}));
		}
		
		// get reserve-operation
		ArrayList<ReserveBalance> rbList = new ArrayList<ReserveBalance>();
		avp = request.find(ProtocolConstants._3GPP_DI_MULTIPLE_RESERVE_OPERATION);
		if(avp != null){
			AVP[] do_avps = new AVP_Grouped(avp).queryAVPs();
			for(AVP tmpAVP : do_avps){
				int code = tmpAVP.code;
				switch (code) {
				case ProtocolConstants.DI_SERVICE_IDENTIFIER: 
					service_identifier = new AVP_Unsigned32(tmpAVP).queryValue();
					break;
				case ProtocolConstants._3GPP_DI_BALANCE:
					parserReserveBalances(session_id, service_identifier,new AVP_Grouped(tmpAVP), rbList);	
					break;
				default:
					break;
				}
			}
		}
		
		System.out.println("预留请求--");
		for( ReserveBalance rb : rbList )
			System.out.println(rb.toString());
		System.out.println("预留请求--END");	
		
		// 进行预留
		boolean reserveBalanceRslt = connector.addReserveBalances(rbList); 
		
		if(reserveBalanceRslt){
			answer.add(new AVP_Grouped(ProtocolConstants._3GPP_DI_MULTIPLE_RESERVE_OPERATION,
					new AVP[]{
					new AVP_Unsigned32(ProtocolConstants.DI_SERVICE_IDENTIFIER,service_identifier),
					new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE,ProtocolConstants.DIAMETER_RESULT_SUCCESS)
			}));
		}
		
		// { Subscriber-information }
		// 用户信息，暂时未能查询
		
		
		// 查询扣减后的余额
		System.out.println("\n扣减完成后状态");
		ArrayList<Balance> balances = connector.getAccountBalance(account_id);
		ArrayList<Counter> counters = connector.getCounterByObjectID(subscriber_id);
		
		// *[ Balance ]
		System.out.println(">>>>> There is "+balances.size()+" Balance item(s)");
		for(Balance b : balances){
			System.out.println(b.toString());
			answer.add(new AVP_Grouped(ProtocolConstants._3GPP_DI_BALANCE,
					new AVP[]{
						new AVP_Unsigned64(ProtocolConstants._3GPP_DI_BALANCE_ID, b.getBalanceID()),
						new AVP_Integer32(ProtocolConstants._3GPP_DI_BALANCE_TYPE, b.getBalanceType()),
						new AVP_Time(ProtocolConstants._3GPP_DI_BALANCE_EXPIRY_DATE, b.getBalanceExpDate()),
						new AVP_Float32(ProtocolConstants._3GPP_DI_BALANCE_VALUE, (float)b.getBalanceValue())
			} ));
		}
		
		// *[ Counter ]
		System.out.println(">>>>> There is "+counters.size()+" Counter item(s)");
		for(Counter cnt : counters){
			System.out.println(cnt.toString());
			answer.add(new AVP_Grouped(ProtocolConstants._3GPP_DI_COUNTER,
					new AVP[]{
						new AVP_Unsigned64(ProtocolConstants._3GPP_DI_COUNTER_ID, cnt.getCounterID()),
						new AVP_UTF8String(ProtocolConstants._3GPP_DI_COUNTER_TYPE, cnt.getCounterType()),
						new AVP_Time(ProtocolConstants._3GPP_DI_COUNTER_EXPIRY_DATE, cnt.getCounterExpTime()),
						new AVP_Float32(ProtocolConstants._3GPP_DI_COUNTER_VALUE, (float)cnt.getCounterValue()),
						new AVP_Float32(ProtocolConstants._3GPP_DI_COUNTER_THRESHOLD, (float)cnt.getCounterThreshold())
			}));
		}
		
		// { Result-Code }
		answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE,
				ProtocolConstants.DIAMETER_RESULT_SUCCESS));

	}
	
	private DeductCounter parseDeductCounter(AVP_Grouped avp) throws InvalidAVPLengthException {
		DeductCounter dcounter = new DeductCounter();
		
		AVP[] avps = avp.queryAVPs();
		for(AVP tmpAVP : avps ){
			int code = tmpAVP.code;
			switch (code) {
			case ProtocolConstants._3GPP_DI_COUNTER_ID: 
				dcounter.setCounterID(new AVP_Unsigned64(tmpAVP).queryValue());
				break;
			case ProtocolConstants._3GPP_DI_CHANGE_VALUE:
				dcounter.setChgValue(new AVP_Float32(tmpAVP).queryValue());
				break;
			default:
				break;
			}
		}
		
		return dcounter;
	}

	
	private void parseDeductBalances(long account_id,AVP_Grouped avp,ArrayList<DeductBalance> list) throws InvalidAVPLengthException{
	
		long balance_id = 0;
		long balance_type = 0;
		int clear_indicator = 0;
		
		for(AVP bavp : avp.queryAVPs()){
			int bcode = bavp.code;
			switch (bcode){
			case ProtocolConstants._3GPP_DI_BALANCE_ID:
				balance_id = new AVP_Unsigned64(bavp).queryValue();
				break;
			case ProtocolConstants._3GPP_DI_BALANCE_TYPE:
				balance_type = new AVP_Integer32(bavp).queryValue();
				break;
			case ProtocolConstants._3GPP_DI_CLEAR_RESERVE_INDICATOR:
				clear_indicator = new AVP_Unsigned32(bavp).queryValue();
				break;
			default:
				break;
			}
		}
		
		for(AVP bavp : avp.queryAVPs() ){
			if(bavp.code == ProtocolConstants._3GPP_DI_ACCOUNT_ITEM){
				DeductBalance db = new DeductBalance();
				db.setAccountID(account_id);
				db.setBalanceID(balance_id);
				db.setBalanceType(balance_type);
				db.setClearReserveIndicator(clear_indicator);
				
				for( AVP aiavp : new AVP_Grouped(bavp).queryAVPs()){
					int aiCode = aiavp.code;
					if(aiCode == ProtocolConstants._3GPP_DI_ACCOUNT_ITEM)
						db.setAccountItemType(new AVP_Unsigned64(aiavp).queryValue());
					if(aiCode == ProtocolConstants._3GPP_DI_CHANGE_VALUE)
						db.setChgValue(new AVP_Float32(aiavp).queryValue());
				}
				
				list.add(db);
			}
		}
	}
	
	private void parserReserveBalances(String session_id,long service_id,AVP_Grouped avp,ArrayList<ReserveBalance> list) throws InvalidAVPLengthException{
		
		long balance_id = 0;
		long balance_type = 0;
		
		for(AVP bavp : avp.queryAVPs()){
			int bcode = bavp.code;
			switch (bcode){
			case ProtocolConstants._3GPP_DI_BALANCE_ID:
				balance_id = new AVP_Unsigned64(bavp).queryValue();
				break;
			case ProtocolConstants._3GPP_DI_BALANCE_TYPE:
				balance_type = new AVP_Integer32(bavp).queryValue();
				break;
			default:
				break;
			}
		}
		
		for(AVP bavp : avp.queryAVPs() ){
			if(bavp.code == ProtocolConstants._3GPP_DI_ACCOUNT_ITEM){
				ReserveBalance rb= new ReserveBalance();
				rb.setSessionID(session_id);
				rb.setServiceID(service_id);
				rb.setServiceType(0);
				rb.setAccountItemID(balance_id);
				rb.setReserveDate(new Timestamp(System.currentTimeMillis()));
				rb.setExpDate(new Timestamp(System.currentTimeMillis()+5*60*1000));
				rb.setUpdateTime(rb.getReserveDate());
				
				
				for( AVP aiavp : new AVP_Grouped(bavp).queryAVPs()){
					int aiCode = aiavp.code;
					if(aiCode == ProtocolConstants._3GPP_DI_ACCOUNT_ITEM_TYPE)
						rb.setAccountID(new AVP_Unsigned64(aiavp).queryValue());
					if(aiCode == ProtocolConstants._3GPP_DI_CHANGE_VALUE)
						rb.setReserveAmount(new AVP_Float32(aiavp).queryValue());
				}
				
				list.add(rb);
			}
		}
		
	}

	
	public NodeManager getNodeManager() {
		return nodeManager;
	}

	public void setNodeManager(NodeManager nodeManager) {
		this.nodeManager = nodeManager;
	}

	public ConnectionKey getConnKey() {
		return connKey;
	}

	public void setConnKey(ConnectionKey connKey) {
		this.connKey = connKey;
	}

	public Message getRequest() {
		return request;
	}

	public void setRequest(Message request) {
		this.request = request;
	}

}
