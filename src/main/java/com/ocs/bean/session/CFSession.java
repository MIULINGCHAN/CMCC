package com.ocs.bean.session;

import java.io.Serializable;

import com.ocs.bean.abm.ReserveBalance;
import com.ocs.bean.event.RatingResult;

/**
 * CF产生的会话实体类，对应从兴所给资料"数据库设计说明书"中的PS_SESSION表
 * @author Wang Chao
 *
 */
@SuppressWarnings("serial")
public class CFSession implements Serializable {
	private String sessionID; //会话ID
	private long ratingGroup; //计费组
	private String originHost; //发送主机名
	private String originRealm; //发送主机域
	private int seqNo; //会话序号
	private int mgsType; //会话类型
	private long CCASendTime; //CCA发送时间
	private int sessionState; //会话状态
	private long beginTime; //会话开始时间
	private long endTime; //会话结束时间
	private String callingNum; //主叫号码
	private int dataLen; //会话长度
	private RatingResult dataBody; //会话数据体
	private ReserveBalance reserveBalance; //
	
	public String getSessionID() {
		return sessionID;
	}
	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}
	public long getRatingGroup() {
		return ratingGroup;
	}
	public void setRatingGroup(long ratingGroup) {
		this.ratingGroup = ratingGroup;
	}
	public String getOriginHost() {
		return originHost;
	}
	public void setOriginHost(String originHost) {
		this.originHost = originHost;
	}
	public String getOriginRealm() {
		return originRealm;
	}
	public void setOriginRealm(String originRealm) {
		this.originRealm = originRealm;
	}
	public int getSeqNo() {
		return seqNo;
	}
	public void setSeqNo(int seqNo) {
		this.seqNo = seqNo;
	}
	public int getMgsType() {
		return mgsType;
	}
	public void setMgsType(int mgsType) {
		this.mgsType = mgsType;
	}
	public long getCCASendTime() {
		return CCASendTime;
	}
	public void setCCASendTime(long cCASendTime) {
		CCASendTime = cCASendTime;
	}
	public int getSessionState() {
		return sessionState;
	}
	public void setSessionState(int sessionState) {
		this.sessionState = sessionState;
	}
	public long getBeginTime() {
		return beginTime;
	}
	public void setBeginTime(long beginTime) {
		this.beginTime = beginTime;
	}
	public long getEndTime() {
		return endTime;
	}
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	public String getCallingNum() {
		return callingNum;
	}
	public void setCallingNum(String callingNum) {
		this.callingNum = callingNum;
	}
	public int getDataLen() {
		return dataLen;
	}
	public void setDataLen(int dataLen) {
		this.dataLen = dataLen;
	}
	public RatingResult getDataBody() {
		return dataBody;
	}
	public void setDataBody(RatingResult dataBody) {
		this.dataBody = dataBody;
	}
	public ReserveBalance getReserveBalance() {
		return reserveBalance;
	}
	public void setReserveBalance(ReserveBalance reserveBalance) {
		this.reserveBalance = reserveBalance;
	}
	@Override
	public String toString() {
		return "CFSession [sessionID=" + sessionID + ", ratingGroup="
				+ ratingGroup + ", originHost=" + originHost + ", originRealm="
				+ originRealm + ", seqNo=" + seqNo + ", mgsType=" + mgsType
				+ ", CCASendTime=" + CCASendTime + ", sessionState="
				+ sessionState + ", beginTime=" + beginTime + ", endTime="
				+ endTime + ", callingNum=" + callingNum + ", dataLen="
				+ dataLen + ", dataBody=" + dataBody + ", reserveBalance="
				+ reserveBalance + "]";
	}
	
	
}
