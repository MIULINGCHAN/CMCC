package com.ocs.cf;

import java.io.IOException;

import com.ocs.protocol.diameter.AVP;
import com.ocs.protocol.diameter.AVP_Grouped;
import com.ocs.protocol.diameter.AVP_Unsigned32;
import com.ocs.protocol.diameter.InvalidAVPLengthException;
import com.ocs.protocol.diameter.Message;
import com.ocs.protocol.diameter.ProtocolConstants;
import com.ocs.protocol.diameter.node.Capability;
import com.ocs.protocol.diameter.node.ConnectionKey;
import com.ocs.protocol.diameter.node.InvalidSettingException;
import com.ocs.protocol.diameter.node.NodeManager;
import com.ocs.protocol.diameter.node.NodeSettings;
import com.ocs.protocol.diameter.node.NotAnAnswerException;
import com.ocs.protocol.diameter.node.Peer;
import com.ocs.protocol.diameter.node.UnsupportedTransportProtocolException;
import com.ocs.threadPool.OCSThreadPool;
import com.ocs.utils.PropertiesUtils;

/**
 * @author MiuChan
 * @DATE 2014年12月17日
 */

public class OCSServer extends NodeManager {

	private OCSThreadPool ocsThreadPool;
	
	/**
	 * @param nodeSettings
	 */
	public OCSServer(NodeSettings nodeSettings) {
		super(nodeSettings);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the ocsThreadPool
	 */
	public OCSThreadPool getOcsThreadPool() {
		return ocsThreadPool;
	}

	/**
	 * @param ocsThreadPool the ocsThreadPool to set
	 */
	public void setOcsThreadPool(OCSThreadPool ocsThreadPool) {
		this.ocsThreadPool = ocsThreadPool;
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String host_id = PropertiesUtils.getOCSServerIP();
		int port = PropertiesUtils.getOCSServerPort();
		String realm = "cmcc.com";

		Capability capability = new Capability();
		capability
				.addAuthApp(ProtocolConstants.DIAMETER_APPLICATION_CREDIT_CONTROL);

		NodeSettings node_settings;
		try {
			node_settings = new NodeSettings(host_id, realm, 99999, // vendor-id
					capability, port, "cc_test_server", 0x01000000);
		} catch (InvalidSettingException e) {
			System.out.println(e.toString());
			return;
		}

		OCSServer ocs = new OCSServer(node_settings);
		try {
			ocs.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedTransportProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		OCSThreadPool ocsWorkerThreadPool = OCSThreadPool.getInstance();
		ocsWorkerThreadPool.initPool();
		ocs.setOcsThreadPool(ocsWorkerThreadPool);
//		CFWorkerThreadPool cfWorkerThreadPool = new CFWorkerThreadPool();
//		cfWorkerThreadPool.initPool();
//		ocs.setCfWorkerThreadPool(cfWorkerThreadPool);

		System.out.println("*************OCS Server is running...");
		System.out.println("Hit enter to terminate server");
		try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ocs.getOcsThreadPool().shutdownPool();
		ocs.stop(50); // Stop but allow 50ms graceful shutdown

		System.out.println("*************OCS Server shutdown.");
	}

	// Select线程在跑的方法
	protected void handleRequest(Message request, ConnectionKey connkey,
			Peer peer) {
		// this is not the way to do it, but fine for a lean-and-mean test
		// server
		Message answer = new Message();
		answer.prepareResponse(request);
		AVP avp;
		avp = request.find(ProtocolConstants.DI_SESSION_ID);
		if (avp != null)
			answer.add(avp);
		node().addOurHostAndRealm(answer);

		avp = request.find(ProtocolConstants.DI_CC_REQUEST_TYPE); // 获取请求类型的AVP
		if (avp == null) {
			answerError(answer, connkey,
					ProtocolConstants.DIAMETER_RESULT_MISSING_AVP,
					new AVP[] { new AVP_Grouped(
							ProtocolConstants.DI_FAILED_AVP,
							new AVP[] { new AVP(
									ProtocolConstants.DI_CC_REQUEST_TYPE,
									new byte[] {}) }) });
			return;
		}
		int cc_request_type = -1;
		try {
			cc_request_type = new AVP_Unsigned32(avp).queryValue();
		} catch (InvalidAVPLengthException ex) {
		}

		// 如果请求类型不是I、U、T或者Event包中的一个，则返回错误
		if (cc_request_type != ProtocolConstants.DI_CC_REQUEST_TYPE_INITIAL_REQUEST
				&& cc_request_type != ProtocolConstants.DI_CC_REQUEST_TYPE_UPDATE_REQUEST
				&& cc_request_type != ProtocolConstants.DI_CC_REQUEST_TYPE_TERMINATION_REQUEST
				&& cc_request_type != ProtocolConstants.DI_CC_REQUEST_TYPE_EVENT_REQUEST) {
			answerError(
					answer,
					connkey,
					ProtocolConstants.DIAMETER_RESULT_INVALID_AVP_VALUE,
					new AVP[] { new AVP_Grouped(
							ProtocolConstants.DI_FAILED_AVP, new AVP[] { avp }) });
			return;
		}

		// This test server does not support multiple-services-cc
		// avp = request
		// .find(ProtocolConstants.DI_MULTIPLE_SERVICES_CREDIT_CONTROL);
		// if (avp != null) {
		// answerError(
		// answer,
		// connkey,
		// ProtocolConstants.DIAMETER_RESULT_INVALID_AVP_VALUE,
		// new AVP[] { new AVP_Grouped(
		// ProtocolConstants.DI_FAILED_AVP, new AVP[] { avp }) });
		// return;
		// }
		// avp = request.find(ProtocolConstants.DI_MULTIPLE_SERVICES_INDICATOR);
		// if (avp != null) {
		// int indicator = -1;
		// try {
		// indicator = new AVP_Unsigned32(avp).queryValue();
		// } catch (InvalidAVPLengthException ex) {
		// }
		// if (indicator !=
		// ProtocolConstants.DI_MULTIPLE_SERVICES_INDICATOR_MULTIPLE_SERVICES_NOT_SUPPORTED)
		// {
		// answerError(answer, connkey,
		// ProtocolConstants.DIAMETER_RESULT_INVALID_AVP_VALUE,
		// new AVP[] { new AVP_Grouped(
		// ProtocolConstants.DI_FAILED_AVP,
		// new AVP[] { avp }) });
		// return;
		// }
		// }

		// answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE,
		// ProtocolConstants.DIAMETER_RESULT_SUCCESS));
		avp = request.find(ProtocolConstants.DI_AUTH_APPLICATION_ID);
		if (avp != null)
			answer.add(avp);
		avp = request.find(ProtocolConstants.DI_CC_REQUEST_TYPE);
		if (avp != null)
			answer.add(avp);
		avp = request.find(ProtocolConstants.DI_CC_REQUEST_NUMBER);
		if (avp != null)
			answer.add(avp);

		switch (cc_request_type) {
		case ProtocolConstants.DI_CC_REQUEST_TYPE_INITIAL_REQUEST:
		case ProtocolConstants.DI_CC_REQUEST_TYPE_UPDATE_REQUEST:
		case ProtocolConstants.DI_CC_REQUEST_TYPE_TERMINATION_REQUEST:
			// grant whatever is requested
			// =======================在此处调用线程池======================
			avp = request.find(ProtocolConstants.DI_REQUESTED_SERVICE_UNIT);
			if (avp != null) {
				AVP g = new AVP(avp);
				g.code = ProtocolConstants.DI_GRANTED_SERVICE_UNIT;
				answer.add(avp);
			}
			this.ocsThreadPool.getCfWorkerThreadPool().getExecutor().execute(
					new CFWorker(this, connkey, request, cc_request_type,
							answer));
			break;

		// event类型的请求，先注释掉，本项目不涉及
		// case ProtocolConstants.DI_CC_REQUEST_TYPE_EVENT_REQUEST: {
		// //Event类型请求的分支
		// // examine requested-action
		// avp = request.find(ProtocolConstants.DI_REQUESTED_ACTION);
		// if (avp == null) {
		// answerError(answer, connkey,
		// ProtocolConstants.DIAMETER_RESULT_MISSING_AVP,
		// new AVP[] { new AVP_Grouped(
		// ProtocolConstants.DI_FAILED_AVP,
		// new AVP[] { new AVP(
		// ProtocolConstants.DI_REQUESTED_ACTION,
		// new byte[] {}) }) });
		// return;
		// }
		// int requested_action = -1;
		// try {
		// requested_action = new AVP_Unsigned32(avp).queryValue();
		// } catch (InvalidAVPLengthException ex) {
		// }
		// switch (requested_action) {
		// case ProtocolConstants.DI_REQUESTED_ACTION_DIRECT_DEBITING:
		// // nothing. just indicate success
		// break;
		// case ProtocolConstants.DI_REQUESTED_ACTION_REFUND_ACCOUNT:
		// // nothing. just indicate success
		// break;
		// case ProtocolConstants.DI_REQUESTED_ACTION_CHECK_BALANCE:
		// // report back that the user has sufficient balance
		// answer.add(new AVP_Unsigned32(
		// ProtocolConstants.DI_CHECK_BALANCE_RESULT,
		// ProtocolConstants.DI_DI_CHECK_BALANCE_RESULT_ENOUGH_CREDIT));
		// break;
		// case ProtocolConstants.DI_REQUESTED_ACTION_PRICE_ENQUIRY:
		// // report back a price of DKK42.17 per kanelsnegl
		// answer.add(new AVP_Grouped(
		// ProtocolConstants.DI_COST_INFORMATION,
		// new AVP[] {
		// new AVP_Grouped(
		// ProtocolConstants.DI_UNIT_VALUE,
		// new AVP[] {
		// new AVP_Integer64(
		// ProtocolConstants.DI_VALUE_DIGITS,
		// 4217),
		// new AVP_Integer32(
		// ProtocolConstants.DI_EXPONENT,
		// -2) }),
		// new AVP_Unsigned32(
		// ProtocolConstants.DI_CURRENCY_CODE, 208),
		// new AVP_UTF8String(
		// ProtocolConstants.DI_COST_UNIT,
		// "kanelsnegl") }));
		// break;
		// default: {
		// answerError(answer, connkey,
		// ProtocolConstants.DIAMETER_RESULT_INVALID_AVP_VALUE,
		// new AVP[] { new AVP_Grouped(
		// ProtocolConstants.DI_FAILED_AVP,
		// new AVP[] { avp }) });
		// return;
		// }
		// }
		// }

		}
	}

	void answerError(Message answer, ConnectionKey connkey, int result_code,
			AVP[] error_avp) {
		answer.hdr.setError(true);
		answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE,
				result_code));
		for (AVP avp : error_avp)
			answer.add(avp);
		try {
			answer(answer, connkey);
		} catch (NotAnAnswerException ex) {
		}
	}

}
