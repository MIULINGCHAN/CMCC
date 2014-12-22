package com.ggsn.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.ocs.protocol.diameter.AVP;
import com.ocs.protocol.diameter.AVP_Grouped;
import com.ocs.protocol.diameter.AVP_OctetString;
import com.ocs.protocol.diameter.AVP_Time;
import com.ocs.protocol.diameter.AVP_UTF8String;
import com.ocs.protocol.diameter.AVP_Unsigned32;
import com.ocs.protocol.diameter.AVP_Unsigned64;
import com.ocs.protocol.diameter.InvalidAVPLengthException;
import com.ocs.protocol.diameter.Message;
import com.ocs.protocol.diameter.MessageHeader;
import com.ocs.protocol.diameter.ProtocolConstants;
import com.ocs.protocol.diameter.node.Capability;
import com.ocs.protocol.diameter.node.EmptyHostNameException;
import com.ocs.protocol.diameter.node.InvalidSettingException;
import com.ocs.protocol.diameter.node.NodeSettings;
import com.ocs.protocol.diameter.node.Peer;
import com.ocs.protocol.diameter.node.SimpleSyncClient;
import com.ocs.protocol.diameter.node.UnsupportedTransportProtocolException;
import com.ocs.utils.MessageUtils;
import com.ocs.utils.PropertiesUtils;

public class UserSimulation implements Runnable{
	static private long defaultTimeQuantity = 1024; //默认时间片大小
	
	private String phonenumber; // 手机号码
	private String produceLocation; // 产生地点 例如：广州
	private long totalRqstQuantity; // 模拟期间需要设置的总请求量
	private long rqstedQuantity; // 目前已使用的量
	private int rqstTimes; // 请求次数（即包的个数）
	private String sessionID; // 分配的sessionid
	private boolean isFinish; // 是否完成模拟
	
	public UserSimulation(String phonenumber, String produceLocation,
			long totalRqstQuantity) {
		super();
		this.phonenumber = phonenumber;
		this.produceLocation = produceLocation;
		this.totalRqstQuantity = totalRqstQuantity;
		this.rqstedQuantity = 0;
		this.rqstTimes = 0;
		this.sessionID = null;
		this.isFinish = false;
	}

	public static long getDefaultTimeQuantity() {
		return defaultTimeQuantity;
	}

	public static void setDefaultTimeQuantity(long defaultTimeQuantity) {
		UserSimulation.defaultTimeQuantity = defaultTimeQuantity;
	}

	public String getPhonenumber() {
		return phonenumber;
	}

	public void setPhonenumber(String phonenumber) {
		this.phonenumber = phonenumber;
	}

	public String getProduceLocation() {
		return produceLocation;
	}

	public void setProduceLocation(String produceLocation) {
		this.produceLocation = produceLocation;
	}

	public long getTotalRqstQuantity() {
		return totalRqstQuantity;
	}

	public void setTotalRqstQuantity(long totalRqstQuantity) {
		this.totalRqstQuantity = totalRqstQuantity;
	}

	public long getRqstedQuantity() {
		return rqstedQuantity;
	}

	public void setRqstedQuantity(long rqstedQuantity) {
		this.rqstedQuantity = rqstedQuantity;
	}

	public int getRqstTimes() {
		return rqstTimes;
	}

	public void setRqstTimes(int rqstTimes) {
		this.rqstTimes = rqstTimes;
	}

	public String getSessionID() {
		return sessionID;
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

	@Override
	public String toString() {
		return "UserSimulation [phonenumber=" + phonenumber
				+ ", produceLocation=" + produceLocation
				+ ", totalRqstQuantity=" + totalRqstQuantity
				+ ", rqstedQuantity=" + rqstedQuantity + ", rqstTimes="
				+ rqstTimes + ", sessionID=" + sessionID + ", isFinish="
				+ isFinish + "]";
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
//		String host_id = "127.0.0.1";
		InetAddress addr = null;
		try {
			addr = InetAddress.getLocalHost();
		} catch (UnknownHostException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		String host_id = addr.getHostAddress().toString();//获得本机IP
		String realm = "cmcc.com";
		String dest_host = PropertiesUtils.getOCSServerIP();
		int dest_port = PropertiesUtils.getOCSServerPort();
		
		Capability capability = new Capability();
		capability.addAuthApp(ProtocolConstants.DIAMETER_APPLICATION_CREDIT_CONTROL);
		//capability.addAcctApp(ProtocolConstants.DIAMETER_APPLICATION_CREDIT_CONTROL);
		
		final NodeSettings node_settings;
		try {
			node_settings  = new NodeSettings(
				host_id, realm,
				99999, //vendor-id
				capability,
				0,
				"GGSN", 0x01000000);
		} catch (InvalidSettingException e) {
			System.out.println(e.toString());
			return;
		}
		
		Peer peers[] = null;
		try {
			peers = new Peer[]{
				new Peer(dest_host,dest_port)
			};
		} catch (EmptyHostNameException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		SimpleSyncClient ssc = new SimpleSyncClient(
				node_settings, peers);

		try {
			simulateASession(ssc);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("模拟结束");
	}
	
	private void simulateASession(SimpleSyncClient ssc) throws InterruptedException{
		while(!isFinish){		
			try {
				ssc.start();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (UnsupportedTransportProtocolException e) {
				e.printStackTrace();
			}
			try {
				ssc.waitForConnection();// 阻塞直到获得connection
			} catch (InterruptedException e) {
				e.printStackTrace();
			} // allow connection to be established.
			
			if(sessionID==null){
				sessionID = ssc.node().makeNewSessionId();
			}
			System.out.println("生成session ID：" + sessionID);
			// 生成报文头
			MessageHeader messageHeader = new MessageHeader(); //要不要考虑那个hop-by-hop id的字段？？ Node类有生成这个id
			messageHeader.command_code = ProtocolConstants.DIAMETER_COMMAND_CC;
			messageHeader.application_id = ProtocolConstants.DIAMETER_APPLICATION_CREDIT_CONTROL;
			messageHeader.setRequest(true);
			messageHeader.setProxiable(true);
			
			// Generate message
			Message message = null;
			if(rqstTimes==0){ // generate I
				System.out.println("--生成I包，预留时间片：" + defaultTimeQuantity + " KB.");
				message = generateInitialMessage(ssc, sessionID, defaultTimeQuantity, messageHeader);
			}
			else if((totalRqstQuantity-rqstedQuantity)>defaultTimeQuantity){ // generate U
				System.out.println("--生成U包，预留/使用时间片：" + defaultTimeQuantity + " KB.");
				message = generateUpdateMessage(ssc, sessionID, defaultTimeQuantity, messageHeader);
			}
			else{ // generate T
				System.out.println("--生成T包，实际使用：" + (totalRqstQuantity-rqstedQuantity) + " KB.");
				message = generateTerminalMessage(ssc, sessionID, totalRqstQuantity-rqstedQuantity, messageHeader);
			}
			System.out.println("**用户模拟情况：" + this.toString());
			
			// Send it
			Message CCA = ssc.sendRequest(message);
			// Process 
			processResponse(CCA);
			
			// Stop the stack
			ssc.stop();
			
			Thread.sleep(1000);
		}
		
		if(isFinish&&totalRqstQuantity==rqstedQuantity){
			System.out.println("--模拟正常结束--");
		}
		else{
			System.out.println("--模拟异常结束--");
		}
		System.out.println("用户"+this.phonenumber + "计划请求"+totalRqstQuantity + " KB,实际使用了" + rqstedQuantity + " KB,共请求了"+(rqstTimes+1)+"次。");
	}
	
	private Message generateInitialMessage(SimpleSyncClient simpleSyncClient, String sessionID, long quantity,MessageHeader messageHeader) {
		
		Message initialMessage = new Message(messageHeader);
		
		//Build Credit-Control Initial Request
		// <Credit-Control-Request> ::= < Diameter Header: 272, REQ, PXY >
		//  < Session-Id >
		initialMessage.add(new AVP_UTF8String(ProtocolConstants.DI_SESSION_ID, sessionID));
		//  { Origin-Host }
		//  { Origin-Realm }
		simpleSyncClient.node().addOurHostAndRealm(initialMessage);
		//  { Destination-Realm }
		initialMessage.add(new AVP_UTF8String(ProtocolConstants.DI_DESTINATION_REALM,"cmcc.com"));
		//  { Auth-Application-Id }
		initialMessage.add(new AVP_Unsigned32(ProtocolConstants.DI_AUTH_APPLICATION_ID,ProtocolConstants.DIAMETER_APPLICATION_CREDIT_CONTROL)); // a lie but a minor one
		//  { Service-Context-Id }
		initialMessage.add(new AVP_UTF8String(ProtocolConstants.DI_SERVICE_CONTEXT_ID,"gprs@cmcc.com"));
		//  { CC-Request-Type }  : I/U/T/Event
		initialMessage.add(new AVP_Unsigned32(ProtocolConstants.DI_CC_REQUEST_TYPE,ProtocolConstants.DI_CC_REQUEST_TYPE_INITIAL_REQUEST));
		/*  { CC-Request-Number }
		 * Hint：当CC_REQUEST_TYPE = UPDATE_REQUEST时，表示当前CC_UPDATE问讯的次数，从1开始；
		 * 当CC_REQUEST_TYPE为其他时，此值为0；
		 */
		initialMessage.add(new AVP_Unsigned32(ProtocolConstants.DI_CC_REQUEST_NUMBER,0));
		//  [ Destination-Host ]
		initialMessage.add(new AVP_UTF8String(ProtocolConstants.DI_DESTINATION_HOST, "BOSS@cmcc.com"));
		//  [ User-Name ]
		//  [ Origin-State-Id ] //移动所给报文中有这个AVP
		//  [ Event-Timestamp ]
		initialMessage.add(new AVP_Time(ProtocolConstants.DI_EVENT_TIMESTAMP,(int)(System.currentTimeMillis()/1000)));
		/* *[ Subscription-Id ]
		 * 		[ Subscription-id-type ]
		 * 		[ Subscription-id-data ]
		 */
		initialMessage.add(new AVP_Grouped(ProtocolConstants.DI_SUBSCRIPTION_ID,
									new AVP[] {new AVP_Unsigned32(ProtocolConstants.DI_SUBSCRIPTION_ID_TYPE, ProtocolConstants.DI_SUBSCRIPTION_ID_TYPE_END_USER_E164),
											   new AVP_UTF8String(ProtocolConstants.DI_SUBSCRIPTION_ID_DATA, phonenumber)}));
		//  [ Termination-Cause ] I/U/Event不需要此字段，在T包中需要包含
		
		// *[ Multiple-Services-Indicator ] 表示是否支持MSCC(Multiple-Service-Credit-Control)
		initialMessage.add(new AVP_Unsigned32(ProtocolConstants.DI_MULTIPLE_SERVICES_INDICATOR, 1));
		// *[ Multiple-Services-Credit-Control ]
		initialMessage.add(new AVP_Grouped(ProtocolConstants.DI_MULTIPLE_SERVICES_CREDIT_CONTROL, 
									new AVP[] {new AVP_Grouped(ProtocolConstants.DI_REQUESTED_SERVICE_UNIT, 
											new AVP[]{ new AVP_Unsigned64(ProtocolConstants.DI_CC_TOTAL_OCTETS, quantity) }  )}));
		/* 	[ Service-Information ]
		 * 		[ PS-Information ] GPRS业务需要此信息
		 * 			{ 3GPP-Charging-Id }
		 * 			[ 3GPP-PDP-Type ]
		 * 			[ PDP-Address ]
		 * 			{ SGSN-Address }
		 * 			[ GGSN-Address ]
		 * 			[ CG-Address ]
		 * 			[ 3GPP-GGSN-MCC-MNC ]
		 * 			[ 3GPP-NSAPI ]
		 * 			[ Called-Station-Id ]
		 * 			[ 3GPP-User-Location-Info ]
		 */
		initialMessage.add(new AVP_Grouped(ProtocolConstants._3GPP_SERVICE_INFORMATION, 
				new AVP[] {new AVP_Grouped(ProtocolConstants._3GPP_PS_INFORMATION,
						//要实现：将位置信息编码为byte[]
						new AVP[]{new AVP_OctetString(ProtocolConstants._3GPP_USER_LOCATION_INFO,
								ProtocolConstants.VENDOR_ID_3GPP,
								MessageUtils.string2ByteArray(produceLocation))}  )}));
		
		return initialMessage;
	}
	
	private Message generateUpdateMessage(SimpleSyncClient simpleSyncClient, String sessionID,long quantity, MessageHeader messageHeader) {
		Message updateMessage = new Message(messageHeader);
				
		//Build Credit-Control Initial Request
		// <Credit-Control-Request> ::= < Diameter Header: 272, REQ, PXY >
		//  < Session-Id >
		updateMessage.add(new AVP_UTF8String(ProtocolConstants.DI_SESSION_ID, sessionID));
		//  { Origin-Host }
		//  { Origin-Realm }
		simpleSyncClient.node().addOurHostAndRealm(updateMessage);
		//  { Destination-Realm }
		updateMessage.add(new AVP_UTF8String(ProtocolConstants.DI_DESTINATION_REALM,"cmcc.com"));
		//  { Auth-Application-Id }
		updateMessage.add(new AVP_Unsigned32(ProtocolConstants.DI_AUTH_APPLICATION_ID,ProtocolConstants.DIAMETER_APPLICATION_CREDIT_CONTROL)); // a lie but a minor one
		//  { Service-Context-Id }
		updateMessage.add(new AVP_UTF8String(ProtocolConstants.DI_SERVICE_CONTEXT_ID,"gprs@cmcc.com"));
		//  { CC-Request-Type } : I/U/T/Event
		updateMessage.add(new AVP_Unsigned32(ProtocolConstants.DI_CC_REQUEST_TYPE,ProtocolConstants.DI_CC_REQUEST_TYPE_UPDATE_REQUEST));
		/*  { CC-Request-Number }
		 * Hint：当CC_REQUEST_TYPE = UPDATE_REQUEST时，表示当前CC_UPDATE问讯的次数，从1开始；
		 * 当CC_REQUEST_TYPE为其他时，此值为0；
		 */
		updateMessage.add(new AVP_Unsigned32(ProtocolConstants.DI_CC_REQUEST_NUMBER,rqstTimes));
		//  [ Destination-Host ]
		updateMessage.add(new AVP_UTF8String(ProtocolConstants.DI_DESTINATION_HOST, "BOSS@cmcc.com"));
		//  [ User-Name ]
		//  [ Origin-State-Id ] //移动所给报文中有这个AVP
		//  [ Event-Timestamp ]
		updateMessage.add(new AVP_Time(ProtocolConstants.DI_EVENT_TIMESTAMP,(int)(System.currentTimeMillis()/1000)));
		// *[ Subscription-Id ] 
		updateMessage.add(new AVP_Grouped(ProtocolConstants.DI_SUBSCRIPTION_ID,
									new AVP[] {new AVP_Unsigned32(ProtocolConstants.DI_SUBSCRIPTION_ID_TYPE, ProtocolConstants.DI_SUBSCRIPTION_ID_TYPE_END_USER_E164),
											   new AVP_UTF8String(ProtocolConstants.DI_SUBSCRIPTION_ID_DATA, phonenumber)}));
		// *[ Multiple-Services-Indicator ] 表示是否支持MSCC(Multiple-Service-Credit-Control)
		updateMessage.add(new AVP_Unsigned32(ProtocolConstants.DI_MULTIPLE_SERVICES_INDICATOR, 1));
		// *[ Multiple-Services-Credit-Control ]
		updateMessage.add(new AVP_Grouped(ProtocolConstants.DI_MULTIPLE_SERVICES_CREDIT_CONTROL, 
									new AVP[] {new AVP_Grouped(ProtocolConstants.DI_REQUESTED_SERVICE_UNIT, 
														new AVP[]{ new AVP_Unsigned64(ProtocolConstants.DI_CC_TOTAL_OCTETS, quantity) }  )}));
		/* 	[ Service-Information ]
		 * 		[ PS-Information ] GPRS业务需要此信息
		 * 			{ 3GPP-Charging-Id }
		 * 			[ 3GPP-PDP-Type ]
		 * 			[ PDP-Address ]
		 * 			{ SGSN-Address }
		 * 			[ GGSN-Address ]
		 * 			[ CG-Address ]
		 * 			[ 3GPP-GGSN-MCC-MNC ]
		 * 			[ 3GPP-NSAPI ]
		 * 			[ Called-Station-Id ]
		 * 			[ 3GPP-User-Location-Info ]
		 */
		updateMessage.add(new AVP_Grouped(ProtocolConstants._3GPP_SERVICE_INFORMATION, 
				new AVP[] {new AVP_Grouped(ProtocolConstants._3GPP_PS_INFORMATION,
							//要实现：将位置信息编码为byte[]
							new AVP[]{ new AVP_OctetString(ProtocolConstants._3GPP_USER_LOCATION_INFO, ProtocolConstants.VENDOR_ID_3GPP, MessageUtils.string2ByteArray(produceLocation))}  )}));
		
		return updateMessage;
	}
	
	private Message generateTerminalMessage(SimpleSyncClient simpleSyncClient, String sessionID, long quantity,MessageHeader messageHeader) {
		Message terminalMessage = new Message(messageHeader);
		
		//Build Credit-Control Initial Request
		// <Credit-Control-Request> ::= < Diameter Header: 272, REQ, PXY >
		//  < Session-Id >
		terminalMessage.add(new AVP_UTF8String(ProtocolConstants.DI_SESSION_ID, sessionID));
		//  { Origin-Host }
		//  { Origin-Realm }
		simpleSyncClient.node().addOurHostAndRealm(terminalMessage);
		//  { Destination-Realm }
		terminalMessage.add(new AVP_UTF8String(ProtocolConstants.DI_DESTINATION_REALM,"cmcc.com"));
		//  { Auth-Application-Id }
		terminalMessage.add(new AVP_Unsigned32(ProtocolConstants.DI_AUTH_APPLICATION_ID,ProtocolConstants.DIAMETER_APPLICATION_CREDIT_CONTROL)); // a lie but a minor one
		//  { Service-Context-Id }
		terminalMessage.add(new AVP_UTF8String(ProtocolConstants.DI_SERVICE_CONTEXT_ID,"gprs@cmcc.com"));
		
		//  { CC-Request-Type } I/U/T/Event
		terminalMessage.add(new AVP_Unsigned32(ProtocolConstants.DI_CC_REQUEST_TYPE,ProtocolConstants.DI_CC_REQUEST_TYPE_TERMINATION_REQUEST));
		/*  { CC-Request-Number }
		 * Hint：当CC_REQUEST_TYPE = UPDATE_REQUEST时，表示当前CC_UPDATE问讯的次数，从1开始；
		 * 当CC_REQUEST_TYPE为其他时，此值为0；
		 */
		terminalMessage.add(new AVP_Unsigned32(ProtocolConstants.DI_CC_REQUEST_NUMBER,rqstTimes));
		//  [ Destination-Host ]
		terminalMessage.add(new AVP_UTF8String(ProtocolConstants.DI_DESTINATION_HOST, "BOSS@cmcc.com"));
		//  [ User-Name ]
		//  [ Origin-State-Id ] //移动所给报文中有这个AVP
		//  [ Event-Timestamp ]
		terminalMessage.add(new AVP_Time(ProtocolConstants.DI_EVENT_TIMESTAMP,(int)(System.currentTimeMillis()/1000)));
		/* *[ Subscription-Id ]
		 * 		[ Subscription-id-type ]
		 * 		[ Subscription-id-data ]
		 */
		terminalMessage.add(new AVP_Grouped(ProtocolConstants.DI_SUBSCRIPTION_ID,
									new AVP[] {new AVP_Unsigned32(ProtocolConstants.DI_SUBSCRIPTION_ID_TYPE, ProtocolConstants.DI_SUBSCRIPTION_ID_TYPE_END_USER_E164),
											   new AVP_UTF8String(ProtocolConstants.DI_SUBSCRIPTION_ID_DATA, phonenumber)}));
		//  [ Termination-Cause ] I/U/Event不需要此字段，在T包中需要包含
		terminalMessage.add(new AVP_Unsigned32(ProtocolConstants.DI_TERMINATION_CAUSE, ProtocolConstants.DI_TERMINATION_CAUSE_DIAMETER_LOGOUT));
		// *[ Multiple-Services-Indicator ] 表示是否支持MSCC(Multiple-Service-Credit-Control)
		terminalMessage.add(new AVP_Unsigned32(ProtocolConstants.DI_MULTIPLE_SERVICES_INDICATOR, 1));
		// [ Multiple-Services-Credit-Control]
		terminalMessage.add(new AVP_Grouped(ProtocolConstants.DI_MULTIPLE_SERVICES_CREDIT_CONTROL, 
									new AVP[] {new AVP_Grouped(ProtocolConstants.DI_REQUESTED_SERVICE_UNIT, 
														new AVP[]{  new AVP_Unsigned64(ProtocolConstants.DI_CC_TOTAL_OCTETS, quantity) }  )}));
		/* 	[ Service-Information ]
		 * 		[ PS-Information ] GPRS业务需要此信息
		 * 			{ 3GPP-Charging-Id }
		 * 			[ 3GPP-PDP-Type ]
		 * 			[ PDP-Address ]
		 * 			{ SGSN-Address }
		 * 			[ GGSN-Address ]
		 * 			[ CG-Address ]
		 * 			[ 3GPP-GGSN-MCC-MNC ]
		 * 			[ 3GPP-NSAPI ]
		 * 			[ Called-Station-Id ]
		 * 			[ 3GPP-User-Location-Info ]
		 */
		terminalMessage.add(new AVP_Grouped(ProtocolConstants._3GPP_SERVICE_INFORMATION, 
				new AVP[] {new AVP_Grouped(ProtocolConstants._3GPP_PS_INFORMATION,
									//要实现：将位置信息编码为byte[]
									new AVP[]{ new AVP_OctetString(ProtocolConstants._3GPP_USER_LOCATION_INFO, ProtocolConstants.VENDOR_ID_3GPP,MessageUtils.string2ByteArray(produceLocation))}  )}));
		
		return terminalMessage;
	}

	private void processResponse(Message answer) {
		// Now look at the result
		if (answer == null) {
			System.out.println("No response");
			isFinish = true;
			return;
		}
		AVP result_code = answer.find(ProtocolConstants.DI_RESULT_CODE);
		
		if (result_code == null) {
			System.out.println("No result code");
			isFinish = true;
			return;
		}
		try {
			AVP_Unsigned32 result_code_u32 = new AVP_Unsigned32(result_code);
			int rc = result_code_u32.queryValue();
			switch (rc) {
			case ProtocolConstants.DIAMETER_RESULT_SUCCESS:
				System.out.println("Success");
				AVP avp = answer.find(ProtocolConstants.DI_CC_REQUEST_TYPE);
				if ( avp != null ) {
					AVP_Unsigned32 result_code_u32_2 = new AVP_Unsigned32(avp);
					int type = result_code_u32_2.queryValue();
					if ( type == ProtocolConstants.DI_CC_REQUEST_TYPE_INITIAL_REQUEST) {
						rqstTimes++;
						System.out.println("成功收到I包回复。------会话建立");
						System.out.println("成功下发：" + defaultTimeQuantity + "KB.");
						System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<");
					}
					if ( type == ProtocolConstants.DI_CC_REQUEST_TYPE_UPDATE_REQUEST) {
						rqstedQuantity += defaultTimeQuantity;
						rqstTimes++;
						System.out.println("成功收到U包回复。------配额预留成功");
						System.out.println("成功下发：" + defaultTimeQuantity + "KB.");
						System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<");
					}
					if ( type == ProtocolConstants.DI_CC_REQUEST_TYPE_TERMINATION_REQUEST){
						rqstedQuantity = totalRqstQuantity; // 这里应该是加上会话请求中止时请求数量
						isFinish = true;
						System.out.println("成功收到T包回复。------结束会话");
						System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<");
					}
				}
				break;
			case ProtocolConstants.DIAMETER_RESULT_END_USER_SERVICE_DENIED:
				isFinish = true;
				System.out.println("End user service denied");
				break;
			case ProtocolConstants.DIAMETER_RESULT_CREDIT_CONTROL_NOT_APPLICABLE:
				isFinish = true;
				System.out.println("Credit-control not applicable");
				break;
			case ProtocolConstants.DIAMETER_RESULT_CREDIT_LIMIT_REACHED:
				isFinish = true;
				System.out.println("Credit-limit reached");
				break;
			case ProtocolConstants.DIAMETER_RESULT_USER_UNKNOWN:
				isFinish = true;
				System.out.println("User unknown");
				break;
			case ProtocolConstants.DIAMETER_RESULT_RATING_FAILED:
				isFinish = true;
				System.out.println("Rating failed");
				break;
			default:
				// Some other error
				// There are too many to decode them all.
				// We just print the classification
				isFinish = true;
				if (rc >= 1000 && rc < 1999)
					System.out.println("Informational: " + rc);
				else if (rc >= 2000 && rc < 2999)
					System.out.println("Success: " + rc);
				else if (rc >= 3000 && rc < 3999)
					System.out.println("Protocl error: " + rc);
				else if (rc >= 4000 && rc < 4999)
					System.out.println("Transient failure: " + rc);
				else if (rc >= 5000 && rc < 5999)
					System.out.println("Permanent failure: " + rc);
				else
					System.out.println("(unknown error class): " + rc);

			}
		} catch (InvalidAVPLengthException ex) {
			System.out.println("result-code was illformed");
			return;
		}
	}
}

