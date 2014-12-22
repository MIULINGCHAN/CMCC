package com.ocs.bean.session;

import java.io.Serializable;

import com.ocs.bean.abm.ReserveBalance;
import com.ocs.bean.event.RatingResult;

/**
 * CF�����ĻỰʵ���࣬��Ӧ������������"���ݿ����˵����"�е�PS_SESSION��
 * @author Wang Chao
 *
 */
@SuppressWarnings("serial")
public class CFSession implements Serializable {
	private String sessionID; //�ỰID
	private long ratingGroup; //�Ʒ���
	private String originHost; //����������
	private String originRealm; //����������
	private int seqNo; //�Ự���
	private int mgsType; //�Ự����
	private long CCASendTime; //CCA����ʱ��
	private int sessionState; //�Ự״̬
	private long beginTime; //�Ự��ʼʱ��
	private long endTime; //�Ự����ʱ��
	private String callingNum; //���к���
	private int dataLen; //�Ự����
	private RatingResult dataBody; //�Ự������
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
