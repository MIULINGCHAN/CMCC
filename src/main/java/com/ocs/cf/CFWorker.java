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
 * 用于处理请求报文的类
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
		
		// 返回应答
		Utils.setMandatory_RFC3588(answer);

		try {
			nodeManager.answer(answer, connKey);
		} catch (NotAnAnswerException ex) {
		}
		
		System.out.println(this.getClass().getName() + " " + Thread.currentThread().getName() + "Ended====================================\n");
	}
	
	private void processInitialMessage() throws EmptyHostNameException, IOException, UnsupportedTransportProtocolException, InterruptedException  {
		System.out.println(Thread.currentThread().getName() + "处理I包中...");
		
		/** 进行用户的认证和授权 
		 * 1.用户不存在，返回错误
		 * 2.该用户能否使用该业务，不能则返回错误
		 * 3.余额是否充足，不足则返回错误
		 */
		
		/** 获得sessionID */
		String sessionID = MessageUtils.querySessionID(request);
		
		/** 先拿到用户的ID，即用户手机号 */
		String subscriptionID = MessageUtils.querySubscriptionID(request);
		if ( subscriptionID == null ) {
			//返回错误
			System.out.println("用户号码为空");
			answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_USER_UNKNOWN));
			return;
		}
		
		/** 访问ABM，获取账户信息 */
		ARQResult arqResult = null;
		// 调用ARQ
		try {
			ABMAccessor abmaccessor = new ABMAccessor();
			arqResult = (ARQResult)abmaccessor.sendARQ(subscriptionID, MessageUtils.querySessionID(request));
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(arqResult);
		if(arqResult==null){
			//返回错误--找不到用户
			System.out.println("找不到用户信息，用户号码："+subscriptionID);
			answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_USER_UNKNOWN));
			return;
		}
		
		/** 生成计费事件 */
		// 用户归属地
		String subsBelongArea = arqResult.getSubsInfo().getBelongArea();
		// 事件发生地
		String eventProdLocation = MessageUtils.queryUserLocation(request);
		// 事件发生相对位置
		String relativeLocation="";
		try {
			relativeLocation = LocationUtils.getInstance().getRelativeLocation(subsBelongArea, eventProdLocation);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("[用户归属地："+subsBelongArea+", 流量产生地："+eventProdLocation+",相对位置："+relativeLocation+"]");
		
		DataTrafficEvent event = new DataTrafficEvent(subscriptionID,
				eventProdLocation, 
				relativeLocation,
				"All", 
				MessageUtils.queryRequestedServiceUnit(request),
				"",//开始时间
				"");//结束时间
		System.out.println(event.toString());
		
		/** 生成账户信息 */
		Account account = new Account();
		account.setAccountID(arqResult.getSubsInfo().getAcctID());
		account.setNumberAttribution("预付费");
		account.setPhoneNumber(subscriptionID);
		//此处要将从ABM拿回的arqResult封装为一个account对象
		//目前把所有的规则使用情况都放于mainService中，不区分是主业务还是增值业务
        // Package - mainService
    	account.mainService = new PackageInfo();
    	account.mainService.packageID = "DG3G19";
    	account.mainService.packageName = "动感地带3G网聊卡19元套餐";
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
    	//增值业务使用情况，暂时不使用    	
    	account.additionalServices = new ArrayList<PackageInfo>();
    	
    	/** 调用RF进行规则匹配  */
    	RFWorker rfworker = new RFWorker(account, event);
    	System.out.println("begin ：" + account.getPhoneNumber());
    	synchronized (rfworker) {
    		nodeManager.getOcsThreadPool().getRfWorkerThreadPool().getExecutor().execute(rfworker);
    		rfworker.wait();
		}
    	System.out.println("end : "+ account.getPhoneNumber());
    	RatingResult ratingResult = rfworker.getRatingResult();
    	if(ratingResult==null){
    		//无法找到匹配的规则，鉴权失败
    		System.out.println("无法匹配规则，鉴权失败");
    		answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_END_USER_SERVICE_DENIED));
    		return;
    	}
    	
    	/** 查询资费 */
    	double fee = 0;
    	fee = queryTariff(ratingResult.pkgID, ratingResult.ruleName);
    	if (fee < 0)
    		fee = 0;
    	ratingResult.totalPrice = ratingResult.getQuantity()*fee/1024;
    	System.out.println("<<<<<<<<<<<匹配结果："+ratingResult.toString());
    	
    	/** 余额预留，若需要话费，需向ABM申请执行话费预留动作 */
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
    							111,//业务标识
    							111,//业务类型
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
    			//余额不足，无法预留  
    			answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_CREDIT_LIMIT_REACHED));
    			return;
    		}
    		
    		//进行话费预留
        	ArrayList<ReserveBalance> rbs = new ArrayList<ReserveBalance>();
        	rbs.add(rb);
        	AOQParams aoqParams = new AOQParams(sessionID, account.getPhoneNumber(), account.getAccountID(), rbs);
        	ABMAccessor abmaccessor = new ABMAccessor();
    		AOQResult aoqResult = (AOQResult)abmaccessor.sendAOQ(aoqParams);
    		if(!aoqResult.isReserveResult()){
    			//话费预留出错
    			answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_UNABLE_TO_COMPLY));
    			return;
    		}
    	}
    		
    	/** 存会话数据 */
    	boolean saveSessionSuccess = saveSession(sessionID, MessageUtils.queryCCReqeustNumber(request), ratingResult,rb);
    	
    	System.out.println("<<<<<<<<<<<用户请求配额：" + MessageUtils.queryRequestedServiceUnit(request) + "KB.");
    	System.out.println("规则" + ratingResult.getRuleID());
    	System.out.println("请求配额批准，下发。");
    	answer.add(new AVP_Grouped(ProtocolConstants.DI_GRANTED_SERVICE_UNIT, 
						new AVP[] {new AVP_Unsigned64(ProtocolConstants.DI_CC_TOTAL_OCTETS, MessageUtils.queryRequestedServiceUnit(request))}));
    	System.out.println("<<<<<<<<<<<<<<<<<<<");
    	
		answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE,
		ProtocolConstants.DIAMETER_RESULT_SUCCESS));
		
	}
	
	private void processUpdateMessage() throws EmptyHostNameException, IOException, UnsupportedTransportProtocolException, InterruptedException {
		System.out.println(Thread.currentThread().getName() + "处理U包中...");
		
		/** 进行用户的认证和授权 
		 * 1.用户不存在，返回错误
		 * 2.扣除上一时间片的费用
		 * 3.该用户能否使用该业务，不能则返回错误
		 * 4.余额是否充足，不足则返回错误
		 */
		
		/** 获得sessionID */
		String sessionID = MessageUtils.querySessionID(request);
		
		/** 先拿到用户的ID，即用户手机号 */
		String subscriptionID = MessageUtils.querySubscriptionID(request);
		if ( subscriptionID == null ) {
			//返回错误
			System.out.println("用户号码为空");
			answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_USER_UNKNOWN));
			return;  
		}
		
		/** 访问ABM，获取账户信息 */
		ARQResult arqResult = null;
		// 调用ARQ
		try {
			ABMAccessor abmaccessor = new ABMAccessor();
			arqResult = (ARQResult)abmaccessor.sendARQ(subscriptionID, MessageUtils.querySessionID(request));
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(arqResult);
		if(arqResult==null){
			//返回错误--找不到用户
			System.out.println("找不到用户信息，用户号码："+subscriptionID);
			answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_USER_UNKNOWN));
			return;
		}
		
		/** 生成计费事件 */
		// 用户归属地
		String subsBelongArea = arqResult.getSubsInfo().getBelongArea();
		// 事件发生地
		String eventProdLocation = MessageUtils.queryUserLocation(request);
		// 事件发生相对位置
		String relativeLocation="";
		try {
			relativeLocation = LocationUtils.getInstance().getRelativeLocation(subsBelongArea, eventProdLocation);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("[用户归属地："+subsBelongArea+", 流量产生地："+eventProdLocation+",相对位置："+relativeLocation+"]");
		
		DataTrafficEvent event = new DataTrafficEvent(subscriptionID,
				eventProdLocation, 
				relativeLocation,
				"All", 
				MessageUtils.queryRequestedServiceUnit(request),
				"",//开始时间
				"");//结束时间
		System.out.println(event.toString());
		
		/** 生成账户信息 */
		Account account = new Account();
		account.setAccountID(arqResult.getSubsInfo().getAcctID());
		account.setNumberAttribution("预付费");
		account.setPhoneNumber(subscriptionID);
		//此处要将从ABM拿回的arqResult封装为一个account对象
		//目前把所有的规则使用情况都放于mainService中，不区分是主业务还是增值业务
        // Package - mainService
    	account.mainService = new PackageInfo();
    	account.mainService.packageID = "DG3G19";
    	account.mainService.packageName = "动感地带3G网聊卡19元套餐";
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
    	//增值业务使用情况，暂时不使用    	
    	account.additionalServices = new ArrayList<PackageInfo>();
		
		/** 获取上一时间片预留的流量&费用 */
		CFSession cfSession = getSession(sessionID, MessageUtils.queryCCReqeustNumber(request));
		if(cfSession==null){
			//获取不到上一次会话的数据
			System.out.println("获取不到上一次会话的数据："+subscriptionID);
			answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_CREDIT_CONTROL_NOT_APPLICABLE));
			return;
		}
		
		/** 对上一时间片的使用进行扣费 */
		// 扣除流量
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
    	// 扣除话费
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
    	// 进行话费&流量扣减
    	AOQParams aoqParams = new AOQParams(sessionID, subscriptionID, account.getAccountID(), dbs,dcs);
    	System.out.println(aoqParams);
    	ABMAccessor abmaccessor = new ABMAccessor();
		AOQResult aoqResult = (AOQResult)abmaccessor.sendAOQ(aoqParams);
		System.out.println(aoqResult);
		if(!aoqResult.isDeductResult()){
			//话费预留出错
			answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_UNABLE_TO_COMPLY));
			return;
		}
		if(!aoqResult.isReserveResult()){
			//话费预留出错
			answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_UNABLE_TO_COMPLY));
			return;
		}
		// 更新扣费后的账户信息
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
    	
    	/** 调用RF进行规则匹配  */
    	RFWorker rfworker = new RFWorker(account, event);
    	System.out.println("begin ：" + account.getPhoneNumber());
    	synchronized (rfworker) {
    		nodeManager.getOcsThreadPool().getRfWorkerThreadPool().getExecutor().execute(rfworker);
    		rfworker.wait();
		}
    	System.out.println("end : "+ account.getPhoneNumber());
    	RatingResult ratingResult = rfworker.getRatingResult();
    	if(ratingResult==null){
    		//无法找到匹配的规则，鉴权失败
    		System.out.println("无法匹配规则，鉴权失败");
    		answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_END_USER_SERVICE_DENIED));
    		return;
    	}
    	
    	/** 查询资费 */
    	double fee = 0;
    	fee = queryTariff(ratingResult.pkgID, ratingResult.ruleName);
    	if (fee < 0)
    		fee = 0;
    	ratingResult.totalPrice = ratingResult.getQuantity()*fee/1024;
    	System.out.println("<<<<<<<<<<<匹配结果："+ratingResult.toString());
    	
    	/** 余额预留，若需要话费，需向ABM申请执行话费预留动作 */
    	ReserveBalance rb = null;
    	if ( fee>0 ) {
    		boolean hasEnoughReserveMoney = false;
    		ArrayList<Balance> blns = aoqResult.getBalances();
    		if(!blns.isEmpty()){
    			for(Balance b : blns){
    				if(b.getBalanceValue()>fee){
    					hasEnoughReserveMoney = true;
    					rb = new ReserveBalance(sessionID,
    							111,//业务标识
    							111,//业务类型
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
    			//余额不足，无法预留  
    			answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_CREDIT_LIMIT_REACHED));
    			return;
    		}
    		
    		//进行话费预留
        	ArrayList<ReserveBalance> rbs = new ArrayList<ReserveBalance>();
        	rbs.add(rb);
        	AOQParams aoqParams2 = new AOQParams(sessionID, account.getPhoneNumber(), account.getAccountID(), rbs);
        	ABMAccessor abmaccessor2 = new ABMAccessor();
    		AOQResult aoqResult2 = (AOQResult)abmaccessor2.sendAOQ(aoqParams2);
    		if(!aoqResult2.isReserveResult()){
    			//话费预留出错
    			answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_UNABLE_TO_COMPLY));
    			return;
    		}
    	}
//   
    	boolean saveSessionSuccess = saveSession(sessionID, MessageUtils.queryCCReqeustNumber(request), ratingResult,rb);
    	
    	System.out.println("<<<<<<<<<<<用户请求配额：" + MessageUtils.queryRequestedServiceUnit(request) + "KB.");
    	System.out.println("规则" + ratingResult.getRuleID());
    	System.out.println("请求配额批准，下发。");
    	answer.add(new AVP_Grouped(ProtocolConstants.DI_GRANTED_SERVICE_UNIT, 
						new AVP[] {new AVP_Unsigned64(ProtocolConstants.DI_CC_TOTAL_OCTETS, MessageUtils.queryRequestedServiceUnit(request))}));
    	System.out.println("<<<<<<<<<<<<<<<<<<<");
   
		answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE,
		ProtocolConstants.DIAMETER_RESULT_SUCCESS));
	}
	
	private void processTerminalMessage() throws EmptyHostNameException, IOException, UnsupportedTransportProtocolException, InterruptedException {
		System.out.println(Thread.currentThread().getName() + "处理T包中...");
		
		/** 进行用户的认证和授权 
		 * 1.用户不存在，返回错误
		 * 2.扣除上一时间片的费用
		 * 3.结束会话
		 */
		
		/** 获得sessionID */
		String sessionID = MessageUtils.querySessionID(request);
		
		/** 先拿到用户的ID，即用户手机号 */
		String subscriptionID = MessageUtils.querySubscriptionID(request);
		if ( subscriptionID == null ) {
			//返回错误
			System.out.println("用户号码为空");
			answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_USER_UNKNOWN));
			return;  
		}
		
		/** 访问ABM，获取账户信息 */
		ARQResult arqResult = null;
		// 调用ARQ
		try {
			ABMAccessor abmaccessor = new ABMAccessor();
			arqResult = (ARQResult)abmaccessor.sendARQ(subscriptionID, MessageUtils.querySessionID(request));
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(arqResult);
		if(arqResult==null){
			//返回错误--找不到用户
			System.out.println("找不到用户信息，用户号码："+subscriptionID);
			answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_USER_UNKNOWN));
			return;
		}
		
		/** 生成账户信息 */
		Account account = new Account();
		account.setAccountID(arqResult.getSubsInfo().getAcctID());
		account.setNumberAttribution("预付费");
		account.setPhoneNumber(subscriptionID);
		//此处要将从ABM拿回的arqResult封装为一个account对象
		//目前把所有的规则使用情况都放于mainService中，不区分是主业务还是增值业务
        // Package - mainService
    	account.mainService = new PackageInfo();
    	account.mainService.packageID = "DG3G19";
    	account.mainService.packageName = "动感地带3G网聊卡19元套餐";
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
    	//增值业务使用情况，暂时不使用    	
    	account.additionalServices = new ArrayList<PackageInfo>();
		
		/** 获取上一时间片预留的流量&费用 */
		CFSession cfSession = getSession(sessionID, MessageUtils.queryCCReqeustNumber(request));
		if(cfSession==null){
			//获取不到上一次会话的数据
			System.out.println("获取不到上一次会话的数据："+subscriptionID);
			answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_CREDIT_CONTROL_NOT_APPLICABLE));
			return;
		}
		
		/** 获得此次使用量 */
		double usageQuantity = MessageUtils.queryGrantedServiceUnit(request);
		if(usageQuantity <0)
			usageQuantity = 0;
		
		/** 对上一时间片的使用进行扣费 */
		// 扣除流量
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
    	// 扣除话费
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
    	// 进行话费&流量扣减
    	AOQParams aoqParams = new AOQParams(sessionID, subscriptionID, account.getAccountID(), dbs,dcs);
    	System.out.println(aoqParams);
    	ABMAccessor abmaccessor = new ABMAccessor();
		AOQResult aoqResult = (AOQResult)abmaccessor.sendAOQ(aoqParams);
		System.out.println(aoqResult);
		if(!aoqResult.isDeductResult()){
			//话费预留出错
			answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_UNABLE_TO_COMPLY));
			return;
		}
		if(!aoqResult.isReserveResult()){
			//话费预留出错
			answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_UNABLE_TO_COMPLY));
			return;
		}
		// 更新扣费后的账户信息
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
	
		    		    	
		/** 删除所有相关的session信息  */
		boolean cleanSessionSuccess = cleanSession(sessionID, MessageUtils.queryCCReqeustNumber(request));
		    	
		System.out.println("会话结束！");
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
    	System.out.println("<<<<<<<<<<<存储会话数据.");
    	CFSession cfSession = new CFSession();
    	cfSession.setSessionID(sessionID + "_" + i);
    	cfSession.setDataBody(ratingResult);
    	cfSession.setReserveBalance(rb);
    	System.out.println(cfSession);
    	CFSessionDAO cfSessionDAO = new CFSessionDAOTairImpl();
    	if (cfSessionDAO.writeCFSession(cfSession) == 1) {
    		System.out.println("存储会话数据到tiar成功！");
    		return true;
    	}
    	else{
    		System.out.println("存储会话数据到tiar失败！");
    		return false;
    	}
	}
	
	private CFSession getSession(String sessionID,int ccRequestNum){
		//将上一预留数据存在会话数据，然后才此时拿来直接扣费其实是有问题的，因为上次匹配时没有考虑时间区间！！ 先不管了。
		System.out.println("<<<<<<<<<<<<取上一时间片会话数据");
    	CFSessionDAO cfSessionDAO = new CFSessionDAOTairImpl();
    	String lastSessionID = sessionID + "_" + (ccRequestNum-1);
    	CFSession cfSession = cfSessionDAO.getCFSession(lastSessionID);
    	if (cfSession != null) {
    		System.out.println("从tiar获取成功！");
    		System.out.println(cfSession);
    		System.out.println("<<<<<<<<<<<<上一时间片使用："+cfSession.getDataBody().getQuantity()+"KB.");
    	}
    	else
    		System.out.println("从tiar获取失败！");
		
		
		return cfSession;
	}
	
	private boolean cleanSession(String sessionID,int ccRequestNum){
		//删除所有会话数据
    	System.out.println("<<<<<<<<<<<删除与本次会话相关的所有会话数据.");
    	List<String> cfSessionIDs = new ArrayList<String>();
    	for ( int i = 0; i < ccRequestNum; i++ ) {
    		cfSessionIDs.add(sessionID + "_" + i);
    	}
    	CFSessionDAO cfSessionDAO2 = new CFSessionDAOTairImpl();
    	if (cfSessionDAO2.deleteCFSession(cfSessionIDs) == 1) {
    		System.out.println("删除所有会话数据成功！");
    		return true;
    	}
    	else{
    		System.out.println("删除所有会话数据失败！");
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
