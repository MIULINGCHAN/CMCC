package com.ocs.cf;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.ocs.abmaccessor.ABMAccessor;
import com.ocs.bean.abm.AOQParams;
import com.ocs.bean.abm.AOQResult;
import com.ocs.bean.abm.ARQResult;
import com.ocs.bean.abm.Balance;
import com.ocs.bean.abm.Counter;
import com.ocs.bean.abm.DeductBalance;
import com.ocs.bean.abm.DeductCounter;
import com.ocs.bean.abm.ReserveBalance;
import com.ocs.bean.account.Account;
import com.ocs.bean.account.PackageInfo;
import com.ocs.bean.account.RuleUsage;
import com.ocs.bean.event.DataTrafficEvent;
import com.ocs.bean.event.RatingResult;
import com.ocs.bean.packagerule.Package.ServiceType;
import com.ocs.bean.session.CFSession;
import com.ocs.dao.CFSessionDAO;
import com.ocs.dao.PackageDAO;
import com.ocs.dao.impl.BDBEnv;
import com.ocs.dao.impl.CFSessionDAOTairImpl;
import com.ocs.dao.impl.PackageDAOBDBImpl;
import com.ocs.protocol.diameter.AVP;
import com.ocs.protocol.diameter.AVP_Grouped;
import com.ocs.protocol.diameter.AVP_Unsigned32;
import com.ocs.protocol.diameter.AVP_Unsigned64;
import com.ocs.protocol.diameter.Message;
import com.ocs.protocol.diameter.ProtocolConstants;
import com.ocs.protocol.diameter.Utils;
import com.ocs.protocol.diameter.node.ConnectionKey;
import com.ocs.protocol.diameter.node.EmptyHostNameException;
import com.ocs.protocol.diameter.node.NodeManager;
import com.ocs.protocol.diameter.node.NotAnAnswerException;
import com.ocs.protocol.diameter.node.UnsupportedTransportProtocolException;
import com.ocs.rf.RFWorker;
import com.ocs.utils.LocationUtils;
import com.ocs.utils.MessageUtils;

/**
 * ���ڴ��������ĵ���
 * @author Wang Chao
 */
public class CFWorker implements Runnable {
	
	private OCSServer nodeManager;
	private ConnectionKey connKey;
	private Message request;
	private int requestType;
	private Message answer;
	
	public CFWorker(OCSServer nodeManager, ConnectionKey connKey,
			Message request, int requestType, Message answer) {
		super();
		this.nodeManager = nodeManager;
		this.connKey = connKey;
		this.request = request;
		this.requestType = requestType;
		this.answer = answer;
	}

	@Override
	public void run() {
		System.out.println(this.getClass().getName() + " " +Thread.currentThread().getName() + "Started=================================");
		
		switch (this.requestType) {
		case ProtocolConstants.DI_CC_REQUEST_TYPE_INITIAL_REQUEST:
			try {
				processInitialMessage();
			} catch (EmptyHostNameException | IOException
					| UnsupportedTransportProtocolException
					| InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case ProtocolConstants.DI_CC_REQUEST_TYPE_UPDATE_REQUEST:
			try {
				processUpdateMessage();
			} catch (EmptyHostNameException | IOException
					| UnsupportedTransportProtocolException
					| InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case ProtocolConstants.DI_CC_REQUEST_TYPE_TERMINATION_REQUEST:
			try {
				processTerminalMessage();
			} catch (EmptyHostNameException | IOException
					| UnsupportedTransportProtocolException
					| InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		default:
			break;
		}		
		
		// ����Ӧ��
		Utils.setMandatory_RFC3588(answer);

		try {
			nodeManager.answer(answer, connKey);
		} catch (NotAnAnswerException ex) {
		}
		
		System.out.println(this.getClass().getName() + " " + Thread.currentThread().getName() + "Ended====================================\n");
	}
	
	private void processInitialMessage() throws EmptyHostNameException, IOException, UnsupportedTransportProtocolException, InterruptedException  {
		System.out.println(Thread.currentThread().getName() + "����I����...");
		
		/** �����û�����֤����Ȩ 
		 * 1.�û������ڣ����ش���
		 * 2.���û��ܷ�ʹ�ø�ҵ�񣬲����򷵻ش���
		 * 3.����Ƿ���㣬�����򷵻ش���
		 */
		
		/** ���sessionID */
		String sessionID = MessageUtils.querySessionID(request);
		
		/** ���õ��û���ID�����û��ֻ��� */
		String subscriptionID = MessageUtils.querySubscriptionID(request);
		if ( subscriptionID == null ) {
			//���ش���
			System.out.println("�û�����Ϊ��");
			answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_USER_UNKNOWN));
			return;
		}
		
		/** ����ABM����ȡ�˻���Ϣ */
		ARQResult arqResult = null;
		// ����ARQ
		try {
			ABMAccessor abmaccessor = new ABMAccessor();
			arqResult = (ARQResult)abmaccessor.sendARQ(subscriptionID, MessageUtils.querySessionID(request));
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(arqResult);
		if(arqResult==null){
			//���ش���--�Ҳ����û�
			System.out.println("�Ҳ����û���Ϣ���û����룺"+subscriptionID);
			answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_USER_UNKNOWN));
			return;
		}
		
		/** ���ɼƷ��¼� */
		// �û�������
		String subsBelongArea = arqResult.getSubsInfo().getBelongArea();
		// �¼�������
		String eventProdLocation = MessageUtils.queryUserLocation(request);
		// �¼��������λ��
		String relativeLocation="";
		try {
			relativeLocation = LocationUtils.getInstance().getRelativeLocation(subsBelongArea, eventProdLocation);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("[�û������أ�"+subsBelongArea+", ���������أ�"+eventProdLocation+",���λ�ã�"+relativeLocation+"]");
		
		DataTrafficEvent event = new DataTrafficEvent(subscriptionID,
				eventProdLocation, 
				relativeLocation,
				"All", 
				MessageUtils.queryRequestedServiceUnit(request),
				"",//��ʼʱ��
				"");//����ʱ��
		System.out.println(event.toString());
		
		/** �����˻���Ϣ */
		Account account = new Account();
		account.setAccountID(arqResult.getSubsInfo().getAcctID());
		account.setNumberAttribution("Ԥ����");
		account.setPhoneNumber(subscriptionID);
		//�˴�Ҫ����ABM�ûص�arqResult��װΪһ��account����
		//Ŀǰ�����еĹ���ʹ�����������mainService�У�����������ҵ������ֵҵ��
        // Package - mainService
    	account.mainService = new PackageInfo();
    	account.mainService.packageID = "DG3G19";
    	account.mainService.packageName = "���еش�3G���Ŀ�19Ԫ�ײ�";
    	account.mainService.usages = new ArrayList<RuleUsage>();
  
    	if(arqResult.getCounters().size()>0){
    		for(int i=0;i<arqResult.getCounters().size();i++){
    			RuleUsage ru = new RuleUsage(arqResult.getCounters().get(i).getCounterType(),
    					arqResult.getCounters().get(i).getCounterType(),
    					arqResult.getCounters().get(i).getCounterValue());
    			account.mainService.usages.add(ru);
    		}
    	}
    	System.out.println(account.toString());
    	//��ֵҵ��ʹ���������ʱ��ʹ��    	
    	account.additionalServices = new ArrayList<PackageInfo>();
    	
    	/** ����RF���й���ƥ��  */
    	RFWorker rfworker = new RFWorker(account, event);
    	System.out.println("begin ��" + account.getPhoneNumber());
    	synchronized (rfworker) {
    		nodeManager.getOcsThreadPool().getRfWorkerThreadPool().getExecutor().execute(rfworker);
    		rfworker.wait();
		}
    	System.out.println("end : "+ account.getPhoneNumber());
    	RatingResult ratingResult = rfworker.getRatingResult();
    	if(ratingResult==null){
    		//�޷��ҵ�ƥ��Ĺ��򣬼�Ȩʧ��
    		System.out.println("�޷�ƥ����򣬼�Ȩʧ��");
    		answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_END_USER_SERVICE_DENIED));
    		return;
    	}
    	
    	/** ��ѯ�ʷ� */
    	double fee = 0;
    	fee = queryTariff(ratingResult.pkgID, ratingResult.ruleName);
    	if (fee < 0)
    		fee = 0;
    	ratingResult.totalPrice = ratingResult.getQuantity()*fee/1024;
    	System.out.println("<<<<<<<<<<<ƥ������"+ratingResult.toString());
    	
    	/** ���Ԥ��������Ҫ���ѣ�����ABM����ִ�л���Ԥ������ */
    	ReserveBalance rb = null;
    	if ( fee>0 ) {
    		boolean hasEnoughReserveMoney = false;
    		ArrayList<Balance> blns = arqResult.getBalances();
    		Timestamp now = new Timestamp(System.currentTimeMillis());
    		if(!blns.isEmpty()){
    			for(Balance b : blns){
    				if(b.getBalanceValue()>fee){
    					hasEnoughReserveMoney = true;
    					rb = new ReserveBalance(sessionID,
    							111,//ҵ���ʶ
    							111,//ҵ������
    							Long.parseLong(arqResult.getSubsInfo().getAcctID()),
    							b.getBalanceID(),
    							fee,
    							now,
    							now,
    							now);
    					break;
    				}
    			}
    		}
    		
    		if(!hasEnoughReserveMoney||rb==null){
    			//���㣬�޷�Ԥ��  
    			answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_CREDIT_LIMIT_REACHED));
    			return;
    		}
    		
    		//���л���Ԥ��
        	ArrayList<ReserveBalance> rbs = new ArrayList<ReserveBalance>();
        	rbs.add(rb);
        	AOQParams aoqParams = new AOQParams(sessionID, account.getPhoneNumber(), account.getAccountID(), rbs);
        	ABMAccessor abmaccessor = new ABMAccessor();
    		AOQResult aoqResult = (AOQResult)abmaccessor.sendAOQ(aoqParams);
    		if(!aoqResult.isReserveResult()){
    			//����Ԥ������
    			answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_UNABLE_TO_COMPLY));
    			return;
    		}
    	}
    		
    	/** ��Ự���� */
    	boolean saveSessionSuccess = saveSession(sessionID, MessageUtils.queryCCReqeustNumber(request), ratingResult,rb);
    	
    	System.out.println("<<<<<<<<<<<�û�������" + MessageUtils.queryRequestedServiceUnit(request) + "KB.");
    	System.out.println("����" + ratingResult.getRuleID());
    	System.out.println("���������׼���·���");
    	answer.add(new AVP_Grouped(ProtocolConstants.DI_GRANTED_SERVICE_UNIT, 
						new AVP[] {new AVP_Unsigned64(ProtocolConstants.DI_CC_TOTAL_OCTETS, MessageUtils.queryRequestedServiceUnit(request))}));
    	System.out.println("<<<<<<<<<<<<<<<<<<<");
    	
		answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE,
		ProtocolConstants.DIAMETER_RESULT_SUCCESS));
		
	}
	
	private void processUpdateMessage() throws EmptyHostNameException, IOException, UnsupportedTransportProtocolException, InterruptedException {
		System.out.println(Thread.currentThread().getName() + "����U����...");
		
		/** �����û�����֤����Ȩ 
		 * 1.�û������ڣ����ش���
		 * 2.�۳���һʱ��Ƭ�ķ���
		 * 3.���û��ܷ�ʹ�ø�ҵ�񣬲����򷵻ش���
		 * 4.����Ƿ���㣬�����򷵻ش���
		 */
		
		/** ���sessionID */
		String sessionID = MessageUtils.querySessionID(request);
		
		/** ���õ��û���ID�����û��ֻ��� */
		String subscriptionID = MessageUtils.querySubscriptionID(request);
		if ( subscriptionID == null ) {
			//���ش���
			System.out.println("�û�����Ϊ��");
			answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_USER_UNKNOWN));
			return;  
		}
		
		/** ����ABM����ȡ�˻���Ϣ */
		ARQResult arqResult = null;
		// ����ARQ
		try {
			ABMAccessor abmaccessor = new ABMAccessor();
			arqResult = (ARQResult)abmaccessor.sendARQ(subscriptionID, MessageUtils.querySessionID(request));
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(arqResult);
		if(arqResult==null){
			//���ش���--�Ҳ����û�
			System.out.println("�Ҳ����û���Ϣ���û����룺"+subscriptionID);
			answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_USER_UNKNOWN));
			return;
		}
		
		/** ���ɼƷ��¼� */
		// �û�������
		String subsBelongArea = arqResult.getSubsInfo().getBelongArea();
		// �¼�������
		String eventProdLocation = MessageUtils.queryUserLocation(request);
		// �¼��������λ��
		String relativeLocation="";
		try {
			relativeLocation = LocationUtils.getInstance().getRelativeLocation(subsBelongArea, eventProdLocation);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("[�û������أ�"+subsBelongArea+", ���������أ�"+eventProdLocation+",���λ�ã�"+relativeLocation+"]");
		
		DataTrafficEvent event = new DataTrafficEvent(subscriptionID,
				eventProdLocation, 
				relativeLocation,
				"All", 
				MessageUtils.queryRequestedServiceUnit(request),
				"",//��ʼʱ��
				"");//����ʱ��
		System.out.println(event.toString());
		
		/** �����˻���Ϣ */
		Account account = new Account();
		account.setAccountID(arqResult.getSubsInfo().getAcctID());
		account.setNumberAttribution("Ԥ����");
		account.setPhoneNumber(subscriptionID);
		//�˴�Ҫ����ABM�ûص�arqResult��װΪһ��account����
		//Ŀǰ�����еĹ���ʹ�����������mainService�У�����������ҵ������ֵҵ��
        // Package - mainService
    	account.mainService = new PackageInfo();
    	account.mainService.packageID = "DG3G19";
    	account.mainService.packageName = "���еش�3G���Ŀ�19Ԫ�ײ�";
    	account.mainService.usages = new ArrayList<RuleUsage>();
  
    	if(arqResult.getCounters().size()>0){
    		for(int i=0;i<arqResult.getCounters().size();i++){
    			RuleUsage ru = new RuleUsage(arqResult.getCounters().get(i).getCounterType(),
    					arqResult.getCounters().get(i).getCounterType(),
    					arqResult.getCounters().get(i).getCounterValue());
    			account.mainService.usages.add(ru);
    		}
    	}
    	System.out.println(account.toString());
    	//��ֵҵ��ʹ���������ʱ��ʹ��    	
    	account.additionalServices = new ArrayList<PackageInfo>();
		
		/** ��ȡ��һʱ��ƬԤ��������&���� */
		CFSession cfSession = getSession(sessionID, MessageUtils.queryCCReqeustNumber(request));
		if(cfSession==null){
			//��ȡ������һ�λỰ������
			System.out.println("��ȡ������һ�λỰ�����ݣ�"+subscriptionID);
			answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_CREDIT_CONTROL_NOT_APPLICABLE));
			return;
		}
		
		/** ����һʱ��Ƭ��ʹ�ý��п۷� */
		// �۳�����
    	ArrayList<DeductCounter> dcs = new ArrayList<DeductCounter>();
    	if(cfSession.getDataBody()!=null){
    		RatingResult rr = cfSession.getDataBody();
    		long counterID = 0;
    		double deductQuantity = 0;
    		for(Counter cnt : arqResult.getCounters()){
    			if(cnt.getCounterType().equals(rr.getRuleID())&&cnt.getCounterValue()>0){
    				counterID = cnt.getCounterID();
    				deductQuantity = rr.getQuantity();
    				break;
    			}
    		}
    		if(deductQuantity>0){
    			DeductCounter dc = new DeductCounter(counterID, deductQuantity);
    			dcs.add(dc);
    		}
    	}    
    	// �۳�����
    	ReserveBalance sessionRB = cfSession.getReserveBalance();
    	ArrayList<DeductBalance> dbs = new ArrayList<DeductBalance>();
    	if(sessionRB!=null){
    		DeductBalance db = new DeductBalance(Long.parseLong(account.getAccountID()), 
    				sessionRB.getAccountItemID(), 
    				0, 
    				0, 
    				sessionRB.getReserveAmount(), 
    				1);
    		dbs.add(db);
    	}
    	// ���л���&�����ۼ�
    	AOQParams aoqParams = new AOQParams(sessionID, subscriptionID, account.getAccountID(), dbs,dcs);
    	System.out.println(aoqParams);
    	ABMAccessor abmaccessor = new ABMAccessor();
		AOQResult aoqResult = (AOQResult)abmaccessor.sendAOQ(aoqParams);
		System.out.println(aoqResult);
		if(!aoqResult.isDeductResult()){
			//����Ԥ������
			answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_UNABLE_TO_COMPLY));
			return;
		}
		if(!aoqResult.isReserveResult()){
			//����Ԥ������
			answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_UNABLE_TO_COMPLY));
			return;
		}
		// ���¿۷Ѻ���˻���Ϣ
		account.mainService.usages = new ArrayList<RuleUsage>();  
    	if(aoqResult.getCounters().size()>0){
    		for(int i=0;i<aoqResult.getCounters().size();i++){
    			RuleUsage ru = new RuleUsage(aoqResult.getCounters().get(i).getCounterType(),
    					aoqResult.getCounters().get(i).getCounterType(),
    					aoqResult.getCounters().get(i).getCounterValue());
    			account.mainService.usages.add(ru);
    		}
    	}
    	System.out.println(account.toString());
    	
    	/** ����RF���й���ƥ��  */
    	RFWorker rfworker = new RFWorker(account, event);
    	System.out.println("begin ��" + account.getPhoneNumber());
    	synchronized (rfworker) {
    		nodeManager.getOcsThreadPool().getRfWorkerThreadPool().getExecutor().execute(rfworker);
    		rfworker.wait();
		}
    	System.out.println("end : "+ account.getPhoneNumber());
    	RatingResult ratingResult = rfworker.getRatingResult();
    	if(ratingResult==null){
    		//�޷��ҵ�ƥ��Ĺ��򣬼�Ȩʧ��
    		System.out.println("�޷�ƥ����򣬼�Ȩʧ��");
    		answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_END_USER_SERVICE_DENIED));
    		return;
    	}
    	
    	/** ��ѯ�ʷ� */
    	double fee = 0;
    	fee = queryTariff(ratingResult.pkgID, ratingResult.ruleName);
    	if (fee < 0)
    		fee = 0;
    	ratingResult.totalPrice = ratingResult.getQuantity()*fee/1024;
    	System.out.println("<<<<<<<<<<<ƥ������"+ratingResult.toString());
    	
    	/** ���Ԥ��������Ҫ���ѣ�����ABM����ִ�л���Ԥ������ */
    	ReserveBalance rb = null;
    	if ( fee>0 ) {
    		boolean hasEnoughReserveMoney = false;
    		ArrayList<Balance> blns = aoqResult.getBalances();
    		if(!blns.isEmpty()){
    			for(Balance b : blns){
    				if(b.getBalanceValue()>fee){
    					hasEnoughReserveMoney = true;
    					rb = new ReserveBalance(sessionID,
    							111,//ҵ���ʶ
    							111,//ҵ������
    							Long.parseLong(account.getAccountID()),
    							b.getBalanceID(),
    							fee,
    							null,
    							null,
    							null);
    					break;
    				}
    			}
    		}
    		
    		if(!hasEnoughReserveMoney||rb==null){
    			//���㣬�޷�Ԥ��  
    			answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_CREDIT_LIMIT_REACHED));
    			return;
    		}
    		
    		//���л���Ԥ��
        	ArrayList<ReserveBalance> rbs = new ArrayList<ReserveBalance>();
        	rbs.add(rb);
        	AOQParams aoqParams2 = new AOQParams(sessionID, account.getPhoneNumber(), account.getAccountID(), rbs);
        	ABMAccessor abmaccessor2 = new ABMAccessor();
    		AOQResult aoqResult2 = (AOQResult)abmaccessor2.sendAOQ(aoqParams2);
    		if(!aoqResult2.isReserveResult()){
    			//����Ԥ������
    			answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_UNABLE_TO_COMPLY));
    			return;
    		}
    	}
//   
    	boolean saveSessionSuccess = saveSession(sessionID, MessageUtils.queryCCReqeustNumber(request), ratingResult,rb);
    	
    	System.out.println("<<<<<<<<<<<�û�������" + MessageUtils.queryRequestedServiceUnit(request) + "KB.");
    	System.out.println("����" + ratingResult.getRuleID());
    	System.out.println("���������׼���·���");
    	answer.add(new AVP_Grouped(ProtocolConstants.DI_GRANTED_SERVICE_UNIT, 
						new AVP[] {new AVP_Unsigned64(ProtocolConstants.DI_CC_TOTAL_OCTETS, MessageUtils.queryRequestedServiceUnit(request))}));
    	System.out.println("<<<<<<<<<<<<<<<<<<<");
   
		answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE,
		ProtocolConstants.DIAMETER_RESULT_SUCCESS));
	}
	
	private void processTerminalMessage() throws EmptyHostNameException, IOException, UnsupportedTransportProtocolException, InterruptedException {
		System.out.println(Thread.currentThread().getName() + "����T����...");
		
		/** �����û�����֤����Ȩ 
		 * 1.�û������ڣ����ش���
		 * 2.�۳���һʱ��Ƭ�ķ���
		 * 3.�����Ự
		 */
		
		/** ���sessionID */
		String sessionID = MessageUtils.querySessionID(request);
		
		/** ���õ��û���ID�����û��ֻ��� */
		String subscriptionID = MessageUtils.querySubscriptionID(request);
		if ( subscriptionID == null ) {
			//���ش���
			System.out.println("�û�����Ϊ��");
			answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_USER_UNKNOWN));
			return;  
		}
		
		/** ����ABM����ȡ�˻���Ϣ */
		ARQResult arqResult = null;
		// ����ARQ
		try {
			ABMAccessor abmaccessor = new ABMAccessor();
			arqResult = (ARQResult)abmaccessor.sendARQ(subscriptionID, MessageUtils.querySessionID(request));
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(arqResult);
		if(arqResult==null){
			//���ش���--�Ҳ����û�
			System.out.println("�Ҳ����û���Ϣ���û����룺"+subscriptionID);
			answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_USER_UNKNOWN));
			return;
		}
		
		/** �����˻���Ϣ */
		Account account = new Account();
		account.setAccountID(arqResult.getSubsInfo().getAcctID());
		account.setNumberAttribution("Ԥ����");
		account.setPhoneNumber(subscriptionID);
		//�˴�Ҫ����ABM�ûص�arqResult��װΪһ��account����
		//Ŀǰ�����еĹ���ʹ�����������mainService�У�����������ҵ������ֵҵ��
        // Package - mainService
    	account.mainService = new PackageInfo();
    	account.mainService.packageID = "DG3G19";
    	account.mainService.packageName = "���еش�3G���Ŀ�19Ԫ�ײ�";
    	account.mainService.usages = new ArrayList<RuleUsage>();
  
    	if(arqResult.getCounters().size()>0){
    		for(int i=0;i<arqResult.getCounters().size();i++){
    			RuleUsage ru = new RuleUsage(arqResult.getCounters().get(i).getCounterType(),
    					arqResult.getCounters().get(i).getCounterType(),
    					arqResult.getCounters().get(i).getCounterValue());
    			account.mainService.usages.add(ru);
    		}
    	}
    	System.out.println(account.toString());
    	//��ֵҵ��ʹ���������ʱ��ʹ��    	
    	account.additionalServices = new ArrayList<PackageInfo>();
		
		/** ��ȡ��һʱ��ƬԤ��������&���� */
		CFSession cfSession = getSession(sessionID, MessageUtils.queryCCReqeustNumber(request));
		if(cfSession==null){
			//��ȡ������һ�λỰ������
			System.out.println("��ȡ������һ�λỰ�����ݣ�"+subscriptionID);
			answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_CREDIT_CONTROL_NOT_APPLICABLE));
			return;
		}
		
		/** ��ô˴�ʹ���� */
		double usageQuantity = MessageUtils.queryGrantedServiceUnit(request);
		if(usageQuantity <0)
			usageQuantity = 0;
		
		/** ����һʱ��Ƭ��ʹ�ý��п۷� */
		// �۳�����
    	ArrayList<DeductCounter> dcs = new ArrayList<DeductCounter>();
    	if(cfSession.getDataBody()!=null){
    		RatingResult rr = cfSession.getDataBody();
    		long counterID = 0;
    		double deductQuantity = 0;
    		for(Counter cnt : arqResult.getCounters()){
    			if(cnt.getCounterType().equals(rr.getRuleID())&&cnt.getCounterValue()>0){
    				counterID = cnt.getCounterID();
    				deductQuantity = usageQuantity;
    				break;
    			}
    		}
    		if(deductQuantity>0){
    			DeductCounter dc = new DeductCounter(counterID, deductQuantity);
    			dcs.add(dc);
    		}
    	}    
    	// �۳�����
    	ReserveBalance sessionRB = cfSession.getReserveBalance();
    	ArrayList<DeductBalance> dbs = new ArrayList<DeductBalance>();
    	if(sessionRB!=null){
    		DeductBalance db = new DeductBalance(Long.parseLong(account.getAccountID()), 
    				sessionRB.getAccountItemID(), 
    				0, 
    				0, 
    				sessionRB.getReserveAmount(), 
    				1);
    		dbs.add(db);
    	}
    	// ���л���&�����ۼ�
    	AOQParams aoqParams = new AOQParams(sessionID, subscriptionID, account.getAccountID(), dbs,dcs);
    	System.out.println(aoqParams);
    	ABMAccessor abmaccessor = new ABMAccessor();
		AOQResult aoqResult = (AOQResult)abmaccessor.sendAOQ(aoqParams);
		System.out.println(aoqResult);
		if(!aoqResult.isDeductResult()){
			//����Ԥ������
			answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_UNABLE_TO_COMPLY));
			return;
		}
		if(!aoqResult.isReserveResult()){
			//����Ԥ������
			answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_UNABLE_TO_COMPLY));
			return;
		}
		// ���¿۷Ѻ���˻���Ϣ
		account.mainService.usages = new ArrayList<RuleUsage>();  
    	if(aoqResult.getCounters().size()>0){
    		for(int i=0;i<aoqResult.getCounters().size();i++){
    			RuleUsage ru = new RuleUsage(aoqResult.getCounters().get(i).getCounterType(),
    					aoqResult.getCounters().get(i).getCounterType(),
    					aoqResult.getCounters().get(i).getCounterValue());
    			account.mainService.usages.add(ru);
    		}
    	}
    	System.out.println(account.toString());
	
		    		    	
		/** ɾ��������ص�session��Ϣ  */
		boolean cleanSessionSuccess = cleanSession(sessionID, MessageUtils.queryCCReqeustNumber(request));
		    	
		System.out.println("�Ự������");
		answer.add(new AVP_Grouped(ProtocolConstants.DI_GRANTED_SERVICE_UNIT, 
				new AVP[] {new AVP_Unsigned64(ProtocolConstants.DI_CC_TOTAL_OCTETS, MessageUtils.queryRequestedServiceUnit(request))}));
		System.out.println("<<<<<<<<<<<<<<<<<<<");
		    
		answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE,ProtocolConstants.DIAMETER_RESULT_SUCCESS));
	}
	
    private static final double queryTariff(String pkgID,String ruleID){
       	BDBEnv env = new BDBEnv();
    	env.setup(false);
    	PackageDAO dao = new PackageDAOBDBImpl(env);
    	
    	double fee = dao.queryTariff(pkgID, ServiceType.DATA_TRAFFIC, ruleID);
    	System.out.println(pkgID + "," + ruleID + ":" + dao.queryTariff(pkgID, ServiceType.DATA_TRAFFIC, ruleID));
    	
    	env.close();
    	return fee;
    }

	private boolean saveSession(String sessionID,int i,RatingResult ratingResult,ReserveBalance rb){
    	System.out.println("<<<<<<<<<<<�洢�Ự����.");
    	CFSession cfSession = new CFSession();
    	cfSession.setSessionID(sessionID + "_" + i);
    	cfSession.setDataBody(ratingResult);
    	cfSession.setReserveBalance(rb);
    	System.out.println(cfSession);
    	CFSessionDAO cfSessionDAO = new CFSessionDAOTairImpl();
    	if (cfSessionDAO.writeCFSession(cfSession) == 1) {
    		System.out.println("�洢�Ự���ݵ�tiar�ɹ���");
    		return true;
    	}
    	else{
    		System.out.println("�洢�Ự���ݵ�tiarʧ�ܣ�");
    		return false;
    	}
	}
	
	private CFSession getSession(String sessionID,int ccRequestNum){
		//����һԤ�����ݴ��ڻỰ���ݣ�Ȼ��Ŵ�ʱ����ֱ�ӿ۷���ʵ��������ģ���Ϊ�ϴ�ƥ��ʱû�п���ʱ�����䣡�� �Ȳ����ˡ�
		System.out.println("<<<<<<<<<<<<ȡ��һʱ��Ƭ�Ự����");
    	CFSessionDAO cfSessionDAO = new CFSessionDAOTairImpl();
    	String lastSessionID = sessionID + "_" + (ccRequestNum-1);
    	CFSession cfSession = cfSessionDAO.getCFSession(lastSessionID);
    	if (cfSession != null) {
    		System.out.println("��tiar��ȡ�ɹ���");
    		System.out.println(cfSession);
    		System.out.println("<<<<<<<<<<<<��һʱ��Ƭʹ�ã�"+cfSession.getDataBody().getQuantity()+"KB.");
    	}
    	else
    		System.out.println("��tiar��ȡʧ�ܣ�");
		
		
		return cfSession;
	}
	
	private boolean cleanSession(String sessionID,int ccRequestNum){
		//ɾ�����лỰ����
    	System.out.println("<<<<<<<<<<<ɾ���뱾�λỰ��ص����лỰ����.");
    	List<String> cfSessionIDs = new ArrayList<String>();
    	for ( int i = 0; i < ccRequestNum; i++ ) {
    		cfSessionIDs.add(sessionID + "_" + i);
    	}
    	CFSessionDAO cfSessionDAO2 = new CFSessionDAOTairImpl();
    	if (cfSessionDAO2.deleteCFSession(cfSessionIDs) == 1) {
    		System.out.println("ɾ�����лỰ���ݳɹ���");
    		return true;
    	}
    	else{
    		System.out.println("ɾ�����лỰ����ʧ�ܣ�");
    		return false;
    	}
	}
    
    public NodeManager getNodeManager() {
		return nodeManager;
	}

	public void setNodeManager(OCSServer nodeManager) {
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

	public int getRequestType() {
		return requestType;
	}

	public void setRequestType(int requestType) {
		this.requestType = requestType;
	}

	public Message getAnswer() {
		return answer;
	}

	public void setAnswer(Message answer) {
		this.answer = answer;
	}
	
}
