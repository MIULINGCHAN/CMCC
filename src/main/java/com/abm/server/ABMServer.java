package com.abm.server;

import com.ocs.protocol.diameter.AVP;
import com.ocs.protocol.diameter.AVP_Unsigned32;
import com.ocs.protocol.diameter.Message;
import com.ocs.protocol.diameter.ProtocolConstants;
import com.ocs.protocol.diameter.node.Capability;
import com.ocs.protocol.diameter.node.ConnectionKey;
import com.ocs.protocol.diameter.node.InvalidSettingException;
import com.ocs.protocol.diameter.node.NodeManager;
import com.ocs.protocol.diameter.node.NodeSettings;
import com.ocs.protocol.diameter.node.NotAnAnswerException;
import com.ocs.protocol.diameter.node.Peer;
import com.ocs.utils.PropertiesUtils;


public class ABMServer extends NodeManager{
	private ABMWorkerThreadPool abmWorkerThreadPool;
	
	public ABMServer(NodeSettings nodeSettings) {
		super(nodeSettings);
	}

	public static final void main(String args[]) throws Exception {
                                                             	
//		String host_id = "222.200.185.54";
//		String realm = "cmcc.com";
//		int port = 3878;
		
//		String host_id = "127.0.0.1";
//		String realm = "cmcc.com";
//		int port = 3878;
		
		String host_id = PropertiesUtils.getABMServerIP();
		int port = PropertiesUtils.getABMServerPort();
		String realm = "cmcc.com";

		Capability capability = new Capability();
		capability.addAuthApp(ProtocolConstants.DIAMETER_APPLICATION_CREDIT_CONTROL);

		NodeSettings node_settings;
		try {
			node_settings = new NodeSettings(host_id, realm, 99999, // vendor-id
					capability, port, "cc_test_abm", 0x01000000);
		} catch (InvalidSettingException e) {
			System.out.println(e.toString());
			return;
		}

		ABMServer tss = new ABMServer(node_settings);
		ABMWorkerThreadPool abmWorkerThreadPool = new ABMWorkerThreadPool();
		abmWorkerThreadPool.initPool();
		tss.setAbmWorkerThreadPool(abmWorkerThreadPool);
		
		tss.start();

		System.out.println("*************ABM server is running...");
		System.out.println("Hit enter to terminate server");
		System.in.read();

		tss.stop(50); // Stop but allow 50ms graceful shutdown
		System.out.println("*************ABM server shutdown.");
	}

	protected void handleRequest(Message request, ConnectionKey connkey, Peer peer) {
		System.out.println("ABM - handleRequest");
		// this is not the way to do it, but fine for a lean-and-mean test
		
		this.abmWorkerThreadPool.getExecutor().execute(new ABMWorker(this, connkey, request));

	}
	
	
	void answerError(Message answer, ConnectionKey connkey, int result_code, AVP[] error_avp) {
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

	public ABMWorkerThreadPool getAbmWorkerThreadPool() {
		return abmWorkerThreadPool;
	}

	public void setAbmWorkerThreadPool(ABMWorkerThreadPool abmWorkerThreadPool) {
		this.abmWorkerThreadPool = abmWorkerThreadPool;
	}
	
}

