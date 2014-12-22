package com.ocs.abmaccessor;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;

import com.ocs.bean.abm.AOQParams;
import com.ocs.bean.abm.AOQResult;
import com.ocs.bean.abm.ARQResult;
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

public class ABMAccessor {

	public enum OCF_MessageType {
		MsgT_ARQ, MsgT_ARS, MsgT_AOQ, MsgT_AOS,
	}

	private static String host_id = PropertiesUtils.getOCSServerIP();
	private static String realm = "cmcc.com";
	private static String dest_host = PropertiesUtils.getABMServerIP();
	private static int dest_port = PropertiesUtils.getABMServerPort();
	private SimpleSyncClient ssc;

	public ABMAccessor(SimpleSyncClient ssc) {
		super();
		this.ssc = ssc;
	}

	public ABMAccessor() {
		super();
	}

	/**
	 * @return the host_id
	 */
	public static String getHost_id() {
		return host_id;
	}

	/**
	 * @param host_id the host_id to set
	 */
	public static void setHost_id(String host_id) {
		ABMAccessor.host_id = host_id;
	}

	/**
	 * @return the realm
	 */
	public static String getRealm() {
		return realm;
	}

	/**
	 * @param realm the realm to set
	 */
	public static void setRealm(String realm) {
		ABMAccessor.realm = realm;
	}

	/**
	 * @return the dest_host
	 */
	public static String getDest_host() {
		return dest_host;
	}

	/**
	 * @param dest_host the dest_host to set
	 */
	public static void setDest_host(String dest_host) {
		ABMAccessor.dest_host = dest_host;
	}

	/**
	 * @return the dest_port
	 */
	public static int getDest_port() {
		return dest_port;
	}

	/**
	 * @param dest_port the dest_port to set
	 */
	public static void setDest_port(int dest_port) {
		ABMAccessor.dest_port = dest_port;
	}

	/**
	 * @return the ssc
	 */
	public SimpleSyncClient getSsc() {
		return ssc;
	}

	/**
	 * @param ssc the ssc to set
	 */
	public void setSsc(SimpleSyncClient ssc) {
		this.ssc = ssc;
	}

	public static final void main(String args[]) throws EmptyHostNameException,
			IOException, UnsupportedTransportProtocolException,
			InterruptedException {

		System.out.println("ABM Accessor is running");

		// 调用ARQ
		System.out.println("-------------ARQ1 is running");
		ABMAccessor abmaccessor = new ABMAccessor();
		ARQResult rslt1 = (ARQResult) abmaccessor.sendARQ("8613430321124",
				"12345677SSS");

		System.out.println(rslt1.toString());
		for (Balance b : rslt1.getBalances())
			System.out.println(b.toString());
		for (Counter c : rslt1.getCounters())
			System.out.println(c.toString());

		// // 测试调用sendAOQ
		// System.out.println("-------------AOQ1 is running");
		// AOQParams params = new AOQParams();
		// params.setSubscriberID("13430321124");
		// params.setAccountID("13430321124");
		// params.setSessionID("1223123ddddd");
		//
		// DeductBalance db = new DeductBalance();
		// db.setAccountID(13430321124L);
		// db.setAccountItemType(0);
		// db.setBalanceID(1000);
		// db.setBalanceType(0);
		// db.setChgValue(1);
		// db.setClearReserveIndicator(1);
		// params.getDeductBalances().add(db);
		//
		// DeductCounter dc = new DeductCounter();
		// dc.setCounterID(123);
		// dc.setChgValue(100);
		//
		// AOQResult rslt = sendAOQ(params);
		// System.out.println(rslt.toString());

	}

	private void prepareNode() throws EmptyHostNameException {
		Capability capability = new Capability();
		capability
				.addAuthApp(ProtocolConstants.DIAMETER_APPLICATION_CREDIT_CONTROL);
		// capability.addAcctApp(ProtocolConstants.DIAMETER_APPLICATION_CREDIT_CONTROL);

		NodeSettings node_settings;
		try {
			node_settings = new NodeSettings(host_id, realm, 99999, // vendor-id
					capability, 0, "OCF", 0x01000000);
		} catch (InvalidSettingException e) {
			System.out.println(e.toString());
			return;
		}

		Peer peers[] = new Peer[] { new Peer(dest_host, dest_port) };

		ssc = new SimpleSyncClient(node_settings, peers);

	}

	public Object sendMessage(Message msg) {

		// Send it
		Message answer = ssc.sendRequest(msg);

		return processResponse(answer);
	}

	private Message generateARQMassage(String sessionID, String subscriber_id,
			MessageHeader messageHeader) {

		messageHeader.command_code = ProtocolConstants._3GPP_COMMAND_AR;

		Message initialMessage = new Message(messageHeader);

		// Build Credit-Control Initial Request
		// <AQR> ::= < Diameter Header: 241, REQ, PXY >
		// < Session-Id >
		initialMessage.add(new AVP_UTF8String(ProtocolConstants.DI_SESSION_ID,
				sessionID));
		// { Origin-Host }
		// { Origin-Realm }
		ssc.node().addOurHostAndRealm(initialMessage);
		// { Destination-Realm }
		initialMessage.add(new AVP_UTF8String(
				ProtocolConstants.DI_DESTINATION_REALM, "cmcc.com"));
		// [ Destination-Host ]
		initialMessage.add(new AVP_UTF8String(
				ProtocolConstants.DI_DESTINATION_HOST, dest_host));

		// { CC-Request-Type } 不同的request类型不同了！
		initialMessage.add(new AVP_Unsigned32(
				ProtocolConstants.DI_CC_REQUEST_TYPE,
				ProtocolConstants.DI_CC_REQUEST_TYPE_INITIAL_REQUEST));
		// [ Event-Timestamp ]
		initialMessage.add(new AVP_Time(ProtocolConstants.DI_EVENT_TIMESTAMP,
				(int) (System.currentTimeMillis() / 1000)));
		// { Service-Identifier }
		initialMessage.add(new AVP_Unsigned32(
				ProtocolConstants.DI_SERVICE_IDENTIFIER, 0));
		// [ Actual-Time ]
		initialMessage.add(new AVP_Time(10014,
				(int) System.currentTimeMillis() / 1000));
		// [ Begin-Time ]
		initialMessage.add(new AVP_Time(10030,
				(int) System.currentTimeMillis() / 1000));

		// *[ Subscription-Id ]
		initialMessage
				.add(new AVP_Grouped(
						ProtocolConstants.DI_SUBSCRIPTION_ID,
						new AVP[] {
								new AVP_Unsigned32(
										ProtocolConstants.DI_SUBSCRIPTION_ID_TYPE,
										ProtocolConstants.DI_SUBSCRIPTION_ID_TYPE_END_USER_E164),
								new AVP_UTF8String(
										ProtocolConstants.DI_SUBSCRIPTION_ID_DATA,
										subscriber_id) }));

		initialMessage.add(new AVP_Unsigned32(
				ProtocolConstants.DI_AUTH_APPLICATION_ID,
				ProtocolConstants.DIAMETER_APPLICATION_CREDIT_CONTROL)); // a
																			// lie
																			// but
																			// a
																			// minor
																			// one
		return initialMessage;
	}

	private Message generateAOQMassage(AOQParams params,
			MessageHeader messageHeader) {
		messageHeader.command_code = ProtocolConstants._3GPP_COMMAND_AO;

		Message msg = new Message(messageHeader);

		// Build Credit-Control Initial Request
		// <AOR> ::= < Diameter Header: 251, REQ, PXY >
		// < Session-Id >
		msg.add(new AVP_UTF8String(ProtocolConstants.DI_SESSION_ID, params
				.getSessionID()));
		// { Origin-Host }
		// { Origin-Realm }
		ssc.node().addOurHostAndRealm(msg);
		// { Destination-Realm }
		msg.add(new AVP_UTF8String(ProtocolConstants.DI_DESTINATION_REALM,
				"cmcc.com"));
		// [ Destination-Host ]
		msg.add(new AVP_UTF8String(ProtocolConstants.DI_DESTINATION_HOST,
				dest_host));

		// { CC-Request-Type } 不同的request类型不同了！
		msg.add(new AVP_Unsigned32(ProtocolConstants.DI_CC_REQUEST_TYPE,
				ProtocolConstants.DI_CC_REQUEST_TYPE_INITIAL_REQUEST));
		// [ Event-Timestamp ]
		msg.add(new AVP_Time(ProtocolConstants.DI_EVENT_TIMESTAMP,
				(int) (System.currentTimeMillis() / 1000)));
		// // { Service-Identifier }
		// msg.add(new
		// AVP_Unsigned32(ProtocolConstants.DI_SERVICE_IDENTIFIER,0));
		// [ Actual-Time ]
		msg.add(new AVP_Time(10014, (int) System.currentTimeMillis() / 1000));

		// // *[ Subscription-Id ]
		// initialMessage.add(new
		// AVP_Grouped(ProtocolConstants.DI_SUBSCRIPTION_ID,
		// new AVP[] {new
		// AVP_Unsigned32(ProtocolConstants.DI_SUBSCRIPTION_ID_TYPE,
		// ProtocolConstants.DI_SUBSCRIPTION_ID_TYPE_END_USER_E164),
		// new AVP_UTF8String(ProtocolConstants.DI_SUBSCRIPTION_ID_DATA,
		// "13430321124")}));

		// { Subscriber-id }
		msg.add(new AVP_UTF8String(ProtocolConstants._3GPP_DI_SUBSCRIBER_ID,
				params.getSubscriberID())); // ？？

		// { Account-id }
		msg.add(new AVP_UTF8String(ProtocolConstants._3GPP_DI_ACCOUNT_ID,
				params.getAccountID())); // ？？

		// * [ Multiple-Deduct-Operation ]
		// { Service-Identifier }
		// * [ Balance ]
		// { Balance-Id }
		// { Balance-Type }
		// *[ Account-Item ]
		// [ Account-Item-Type ]
		// [ Change-Value ]
		// { Change-Value }
		// { Clear-Reserve-Indicator }
		// * { Counter }
		// { Counter-Id }
		// { Change-Value }
		ArrayList<AVP> deduct_operation_avps = new ArrayList<AVP>();
		deduct_operation_avps.add(new AVP_Unsigned32(
				ProtocolConstants.DI_SERVICE_IDENTIFIER, 0));

		if (params.getDeductBalances() != null
				&& params.getDeductBalances().size() > 0) {
			ArrayList<AVP_Grouped> account_items = new ArrayList<AVP_Grouped>();
			for (DeductBalance db : params.getDeductBalances()) {
				AVP_Grouped account_item = new AVP_Grouped(
						ProtocolConstants._3GPP_DI_ACCOUNT_ITEM,
						new AVP[] {
								new AVP_Unsigned64(
										ProtocolConstants._3GPP_DI_ACCOUNT_ITEM_TYPE,
										db.getAccountItemType()),
								new AVP_Float32(
										ProtocolConstants._3GPP_DI_CHANGE_VALUE,
										(float) db.getChgValue()) });
				account_items.add(account_item);
			}

			DeductBalance firstDb = params.getDeductBalances().get(0);
			ArrayList<AVP> items = new ArrayList<AVP>();
			items.add(new AVP_Unsigned64(ProtocolConstants._3GPP_DI_BALANCE_ID,
					firstDb.getBalanceID()));
			items.add(new AVP_Integer32(
					ProtocolConstants._3GPP_DI_BALANCE_TYPE, (int) firstDb
							.getBalanceType()));
			items.addAll(account_items);
			items.add(new AVP_Float32(ProtocolConstants._3GPP_DI_CHANGE_VALUE,
					0));
			items.add(new AVP_Unsigned32(
					ProtocolConstants._3GPP_DI_CLEAR_RESERVE_INDICATOR, firstDb
							.getClearReserveIndicator()));

			AVP[] itemsTmp = new AVP[items.size()];
			for (int i = 0; i < items.size(); i++) {
				itemsTmp[i] = items.get(i);
			}

			AVP_Grouped balance = new AVP_Grouped(
					ProtocolConstants._3GPP_DI_BALANCE, itemsTmp);
			deduct_operation_avps.add(balance);
		}

		if (params.getDeductCounters() != null
				&& params.getDeductCounters().size() > 0) {
			for (DeductCounter dc : params.getDeductCounters()) {
				AVP_Grouped counter_item = new AVP_Grouped(
						ProtocolConstants._3GPP_DI_COUNTER,
						new AVP[] {
								new AVP_Unsigned64(
										ProtocolConstants._3GPP_DI_COUNTER_ID,
										dc.getCounterID()),
								new AVP_Float32(
										ProtocolConstants._3GPP_DI_CHANGE_VALUE,
										(float) dc.getChgValue()) });
				deduct_operation_avps.add(counter_item);
			}
		}

		AVP[] operation_avps = new AVP[deduct_operation_avps.size()];
		for (int i = 0; i < deduct_operation_avps.size(); i++) {
			operation_avps[i] = deduct_operation_avps.get(i);
		}
		msg.add(new AVP_Grouped(
				ProtocolConstants._3GPP_DI_MULTIPLE_DEDUCT_OPERATION,
				operation_avps));

		// * [ Multiple-Reserve-Operation ]
		// { Service-Identifier }
		// * [ Balance ]
		// { Balance-Id }
		// { Balance-Type }
		// * [ Account-Item ]
		// [ Account-Item-Type ]
		// [ Change-Value ]
		// { Change-Value }
		ArrayList<AVP> reserve_operation_avps = new ArrayList<AVP>();
		reserve_operation_avps.add(new AVP_Unsigned32(
				ProtocolConstants.DI_SERVICE_IDENTIFIER, 0));

		if (params.getReserveBalances() != null
				&& params.getReserveBalances().size() > 0) {
			ArrayList<AVP_Grouped> account_items = new ArrayList<AVP_Grouped>();
			for (ReserveBalance rb : params.getReserveBalances()) {
				AVP_Grouped account_item = new AVP_Grouped(
						ProtocolConstants._3GPP_DI_ACCOUNT_ITEM,
						new AVP[] {
								new AVP_Unsigned64(
										ProtocolConstants._3GPP_DI_ACCOUNT_ITEM_TYPE,
										rb.getAccountID()),
								new AVP_Float32(
										ProtocolConstants._3GPP_DI_CHANGE_VALUE,
										(float) rb.getReserveAmount()) });
				account_items.add(account_item);
			}

			ReserveBalance firstRb = params.getReserveBalances().get(0);
			ArrayList<AVP> items = new ArrayList<AVP>();
			items.add(new AVP_Unsigned64(ProtocolConstants._3GPP_DI_BALANCE_ID,
					firstRb.getAccountItemID()));
			items.add(new AVP_Integer32(
					ProtocolConstants._3GPP_DI_BALANCE_TYPE, 0));
			items.addAll(account_items);
			items.add(new AVP_Float32(ProtocolConstants._3GPP_DI_CHANGE_VALUE,
					0));

			AVP[] itemsTmp = new AVP[items.size()];
			for (int i = 0; i < items.size(); i++) {
				itemsTmp[i] = items.get(i);
			}

			AVP_Grouped balance = new AVP_Grouped(
					ProtocolConstants._3GPP_DI_BALANCE, itemsTmp);
			reserve_operation_avps.add(balance);
		}

		AVP[] r_operation_avps = new AVP[reserve_operation_avps.size()];
		for (int i = 0; i < reserve_operation_avps.size(); i++) {
			r_operation_avps[i] = reserve_operation_avps.get(i);
		}
		msg.add(new AVP_Grouped(
				ProtocolConstants._3GPP_DI_MULTIPLE_RESERVE_OPERATION,
				r_operation_avps));

		msg.add(new AVP_Unsigned32(ProtocolConstants.DI_AUTH_APPLICATION_ID,
				ProtocolConstants.DIAMETER_APPLICATION_CREDIT_CONTROL)); // a
																			// lie
																			// but
																			// a
																			// minor
																			// one
		return msg;
	}

	public ARQResult sendARQ(String subscriberID, String sessionID)
			throws EmptyHostNameException, IOException,
			UnsupportedTransportProtocolException, InterruptedException {
		prepareNode();

		ssc.start();
		ssc.waitForConnection(); // allow connection to be established.
									// 阻塞直到获得connection

		MessageHeader messageHeader = new MessageHeader(); // 要不要考虑那个hop-by-hop
															// id的字段？？
															// Node类有生成这个id

		messageHeader.application_id = ProtocolConstants.DIAMETER_APPLICATION_CREDIT_CONTROL; // ？
		messageHeader.setRequest(true);
		messageHeader.setProxiable(true);

		Message msg = generateARQMassage(sessionID, subscriberID, messageHeader);

		ARQResult r = (ARQResult) sendMessage(msg);

		ssc.stop();

		return r;
	}

	public AOQResult sendAOQ(AOQParams params) throws EmptyHostNameException,
			IOException, UnsupportedTransportProtocolException,
			InterruptedException {
		prepareNode();

		ssc.start();
		ssc.waitForConnection(); // allow connection to be established.
									// 阻塞直到获得connection

		MessageHeader messageHeader = new MessageHeader(); // 要不要考虑那个hop-by-hop
															// id的字段？？
															// Node类有生成这个id

		messageHeader.application_id = ProtocolConstants.DIAMETER_APPLICATION_CREDIT_CONTROL; // ？
		messageHeader.setRequest(true);
		messageHeader.setProxiable(true);

		Message msg = generateAOQMassage(params, messageHeader);

		AOQResult r = (AOQResult) sendMessage(msg);

		ssc.stop();

		return r;
	}

	private Object processResponse(Message answer) {
		System.out.println(">> CF(ABM Accessor) received an message.");

		// Now look at the result
		if (answer == null) {
			System.out.println("No response");
			return null;
		}
		AVP result_code = answer.find(ProtocolConstants.DI_RESULT_CODE);
		if (result_code == null) {
			System.out.println("No result code");
			return null;
		}

		int cmdCode = answer.hdr.command_code;
		switch (cmdCode) {
		case ProtocolConstants._3GPP_COMMAND_AR:
			return processARQResponse(answer);
		case ProtocolConstants._3GPP_COMMAND_AO:
			return processAOQResponse(answer);
		default:
			return null;
		}
	}

	private ARQResult processARQResponse(Message answer) {
		AVP result_code = answer.find(ProtocolConstants.DI_RESULT_CODE);
		ARQResult arqRslt = null;

		try {
			System.out.print("Result:");
			AVP_Unsigned32 result_code_u32 = new AVP_Unsigned32(result_code);
			int rc = result_code_u32.queryValue();
			switch (rc) {
			case ProtocolConstants.DIAMETER_RESULT_SUCCESS:
				System.out.println("Success");
				arqRslt = new ARQResult();

				// get subscriber
				AVP savp = answer.find(ProtocolConstants.DI_SUBSCRIPTION_ID);
				Subscriber s = null;
				if (savp != null) {
					s = new Subscriber();
					AVP[] avps = new AVP_Grouped(savp).queryAVPs();
					for (AVP tmpAvp : avps) {
						int code = tmpAvp.code;
						switch (code) {
						case ProtocolConstants.DI_SUBSCRIPTION_ID_TYPE:
							s.setSubscriberIdType(new AVP_Unsigned32(tmpAvp)
									.queryValue());
							break;
						case ProtocolConstants.DI_SUBSCRIPTION_ID_DATA:
							s.setSubscriberIdData(new AVP_UTF8String(tmpAvp)
									.queryValue());
							break;
						default:
							break;
						}
					}
					// 获取归属地
					s.setBelongArea(MessageUtils
							.querySubscriberBelongArea(answer));
					// 获取账户id
					s.setAcctID(MessageUtils.queryAccountId(answer));
					System.out.println(s.toString());
				}
				arqRslt.setSubsInfo(s);

				// get balances
				for (AVP avp : answer
						.subset(ProtocolConstants._3GPP_DI_BALANCE)) {
					AVP[] avps = new AVP_Grouped(avp).queryAVPs();
					Balance b = new Balance();
					for (AVP tmpAvp : avps) {
						int code = tmpAvp.code;
						switch (code) {
						case ProtocolConstants._3GPP_DI_BALANCE_ID:
							b.setBalanceID(new AVP_Unsigned64(tmpAvp)
									.queryValue());
							break;
						case ProtocolConstants._3GPP_DI_BALANCE_TYPE:
							b.setBalanceType(new AVP_Integer32(tmpAvp)
									.queryValue());
							break;
						case ProtocolConstants._3GPP_DI_BALANCE_EXPIRY_DATE:
							b.setBalanceExpDate(new Timestamp(new AVP_Time(
									tmpAvp).queryDate().getTime()));
							break;
						case ProtocolConstants._3GPP_DI_BALANCE_VALUE:
							b.setBalanceValue(new AVP_Float32(tmpAvp)
									.queryValue());
							break;
						default:
							break;
						}
					}
					System.out.println(b.toString());
					arqRslt.getBalances().add(b);
				}

				// get counters
				for (AVP avp : answer
						.subset(ProtocolConstants._3GPP_DI_COUNTER)) {
					AVP[] avps = new AVP_Grouped(avp).queryAVPs();
					Counter c = new Counter();
					for (AVP tmpAvp : avps) {
						int code = tmpAvp.code;
						switch (code) {
						case ProtocolConstants._3GPP_DI_COUNTER_ID:
							c.setCounterID(new AVP_Unsigned64(tmpAvp)
									.queryValue());
							break;
						case ProtocolConstants._3GPP_DI_COUNTER_TYPE:
							c.setCounterType(new AVP_UTF8String(tmpAvp)
									.queryValue());
							break;
						case ProtocolConstants._3GPP_DI_COUNTER_EXPIRY_DATE:
							c.setCounterExpTime(new Timestamp(new AVP_Time(
									tmpAvp).queryDate().getTime()));
							break;
						case ProtocolConstants._3GPP_DI_COUNTER_VALUE:
							c.setCounterValue(new AVP_Float32(tmpAvp)
									.queryValue());
							break;
						case ProtocolConstants._3GPP_DI_COUNTER_THRESHOLD:
							c.setCounterThreshold(new AVP_Float32(tmpAvp)
									.queryValue());
							break;
						default:
							break;
						}
					}
					System.out.println(c.toString());
					arqRslt.getCounters().add(c);
				}
				break;
			case ProtocolConstants.DIAMETER_RESULT_END_USER_SERVICE_DENIED:
				System.out.println("End user service denied");
				break;
			case ProtocolConstants.DIAMETER_RESULT_CREDIT_CONTROL_NOT_APPLICABLE:
				System.out.println("Credit-control not applicable");
				break;
			case ProtocolConstants.DIAMETER_RESULT_CREDIT_LIMIT_REACHED:
				System.out.println("Credit-limit reached");
				break;
			case ProtocolConstants.DIAMETER_RESULT_USER_UNKNOWN:
				System.out.println("User unknown");
				break;
			case ProtocolConstants.DIAMETER_RESULT_RATING_FAILED:
				System.out.println("Rating failed");
				break;
			default:
				// Some other error
				// There are too many to decode them all.
				// We just print the classification
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

			return arqRslt;

		} catch (InvalidAVPLengthException ex) {
			System.out.println("result-code was illformed");
			return null;
		}
	}

	private AOQResult processAOQResponse(Message answer) {
		AOQResult aoqRslt = new AOQResult();

		AVP result_code = answer.find(ProtocolConstants.DI_RESULT_CODE);

		try {
			System.out.print("Result:");
			AVP_Unsigned32 result_code_u32 = new AVP_Unsigned32(result_code);
			int rc = result_code_u32.queryValue();
			switch (rc) {
			case ProtocolConstants.DIAMETER_RESULT_SUCCESS:
				System.out.println("Success");

				// // get subscriber
				// AVP savp = answer.find(ProtocolConstants.DI_SUBSCRIPTION_ID);
				// if(savp!=null){
				// Subscriber s = new Subscriber();
				// AVP[] avps = new AVP_Grouped(savp).queryAVPs();
				// for( AVP tmpAvp : avps ){
				// int code = tmpAvp.code;
				// switch (code){
				// case ProtocolConstants.DI_SUBSCRIPTION_ID_TYPE:
				// s.setSubscriberIdType(new
				// AVP_Unsigned32(tmpAvp).queryValue());
				// break;
				// case ProtocolConstants.DI_SUBSCRIPTION_ID_DATA:
				// s.setSubscriberIdData(new
				// AVP_UTF8String(tmpAvp).queryValue());
				// break;
				// default:
				// break;
				// }
				// }
				// System.out.println(s.toString());
				// }

				// get balances
				for (AVP avp : answer
						.subset(ProtocolConstants._3GPP_DI_BALANCE)) {
					AVP[] avps = new AVP_Grouped(avp).queryAVPs();
					Balance b = new Balance();
					for (AVP tmpAvp : avps) {
						int code = tmpAvp.code;
						switch (code) {
						case ProtocolConstants._3GPP_DI_BALANCE_ID:
							b.setBalanceID(new AVP_Unsigned64(tmpAvp)
									.queryValue());
							break;
						case ProtocolConstants._3GPP_DI_BALANCE_TYPE:
							b.setBalanceType(new AVP_Integer32(tmpAvp)
									.queryValue());
							break;
						case ProtocolConstants._3GPP_DI_BALANCE_EXPIRY_DATE:
							b.setBalanceExpDate(new Timestamp(new AVP_Time(
									tmpAvp).queryDate().getTime()));
							break;
						case ProtocolConstants._3GPP_DI_BALANCE_VALUE:
							b.setBalanceValue(new AVP_Float32(tmpAvp)
									.queryValue());
							break;
						default:
							break;
						}
					}
					aoqRslt.getBalances().add(b);
				}

				// get counters
				for (AVP avp : answer
						.subset(ProtocolConstants._3GPP_DI_COUNTER)) {
					AVP[] avps = new AVP_Grouped(avp).queryAVPs();
					Counter c = new Counter();
					for (AVP tmpAvp : avps) {
						int code = tmpAvp.code;
						switch (code) {
						case ProtocolConstants._3GPP_DI_COUNTER_ID:
							c.setCounterID(new AVP_Unsigned64(tmpAvp)
									.queryValue());
							break;
						case ProtocolConstants._3GPP_DI_COUNTER_TYPE:
							c.setCounterType(new AVP_UTF8String(tmpAvp)
									.queryValue());
							break;
						case ProtocolConstants._3GPP_DI_COUNTER_EXPIRY_DATE:
							c.setCounterExpTime(new Timestamp(new AVP_Time(
									tmpAvp).queryDate().getTime()));
							break;
						case ProtocolConstants._3GPP_DI_COUNTER_VALUE:
							c.setCounterValue(new AVP_Float32(tmpAvp)
									.queryValue());
							break;
						case ProtocolConstants._3GPP_DI_COUNTER_THRESHOLD:
							c.setCounterThreshold(new AVP_Float32(tmpAvp)
									.queryValue());
							break;
						default:
							break;
						}
					}
					aoqRslt.getCounters().add(c);
				}

				// get deduct-result
				boolean deductRslt = true;
				for (AVP avp : answer
						.subset(ProtocolConstants._3GPP_DI_MULTIPLE_DEDUCT_OPERATION)) {
					AVP[] gavps = new AVP_Grouped(avp).queryAVPs();
					for (AVP a : gavps) {
						if (a.code == ProtocolConstants.DI_RESULT_CODE) {
							int v = new AVP_Integer32(a).queryValue();
							deductRslt = deductRslt
									& (v == ProtocolConstants.DIAMETER_RESULT_SUCCESS);
						}
					}
				}
				aoqRslt.setDeductResult(deductRslt);

				// get reserve-result
				boolean reserveRslt = true;
				for (AVP avp : answer
						.subset(ProtocolConstants._3GPP_DI_MULTIPLE_RESERVE_OPERATION)) {
					AVP[] gavps = new AVP_Grouped(avp).queryAVPs();
					for (AVP a : gavps) {
						if (a.code == ProtocolConstants.DI_RESULT_CODE) {
							int v = new AVP_Integer32(a).queryValue();
							reserveRslt = reserveRslt
									& (v == ProtocolConstants.DIAMETER_RESULT_SUCCESS);
						}
					}
				}
				aoqRslt.setReserveResult(reserveRslt);

				break;
			case ProtocolConstants.DIAMETER_RESULT_END_USER_SERVICE_DENIED:
				System.out.println("End user service denied");
				break;
			case ProtocolConstants.DIAMETER_RESULT_CREDIT_CONTROL_NOT_APPLICABLE:
				System.out.println("Credit-control not applicable");
				break;
			case ProtocolConstants.DIAMETER_RESULT_CREDIT_LIMIT_REACHED:
				System.out.println("Credit-limit reached");
				break;
			case ProtocolConstants.DIAMETER_RESULT_USER_UNKNOWN:
				System.out.println("User unknown");
				break;
			case ProtocolConstants.DIAMETER_RESULT_RATING_FAILED:
				System.out.println("Rating failed");
				break;
			default:
				// Some other error
				// There are too many to decode them all.
				// We just print the classification
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

			return aoqRslt;

		} catch (InvalidAVPLengthException ex) {
			System.out.println("result-code was illformed");
			return null;
		}
	}

}
