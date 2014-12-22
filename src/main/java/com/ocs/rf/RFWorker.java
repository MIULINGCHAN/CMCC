/**
 * 
 */
package com.ocs.rf;


import com.ocs.bean.account.Account;
import com.ocs.bean.event.DataTrafficEvent;
import com.ocs.bean.event.RatingResult;

/**
 * @author MiuChan
 * @DATE 2014年12月19日
 */
/**
 * @author miumiu
 *
 */
public class RFWorker implements Runnable{

	private RatingResult ratingResult = null;
	private Account account;
	private DataTrafficEvent event;

	/**
	 * @param account
	 * @param event
	 */
	public RFWorker(Account account, DataTrafficEvent event) {
		super();
		this.account = account;
		this.event = event;
	}

	/**
	 * @return the ratingResult
	 */
	public RatingResult getRatingResult() {
		return ratingResult;
	}



	/**
	 * @param ratingResult the ratingResult to set
	 */
	public void setRatingResult(RatingResult ratingResult) {
		this.ratingResult = ratingResult;
	}



	/**
	 * @return the account
	 */
	public Account getAccount() {
		return account;
	}



	/**
	 * @param account the account to set
	 */
	public void setAccount(Account account) {
		this.account = account;
	}



	/**
	 * @return the event
	 */
	public DataTrafficEvent getEvent() {
		return event;
	}



	/**
	 * @param event the event to set
	 */
	public void setEvent(DataTrafficEvent event) {
		this.event = event;
	}



	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		synchronized (this) {
			System.out.println(this.getClass().getName() + " " +Thread.currentThread().getName() + "Started=================================");
			
	    	RatingFunction ratingFunction = new RatingFunctionImpl();
	    	System.out.println("<<<<<<<<<<<调用RF进行规则匹配");
	    	ratingResult = ratingFunction.dataTrafficRating(account, event);
	    	
	    	System.out.println(this.getClass().getName() + " " + Thread.currentThread().getName() + "Ended====================================\n");
		
	    	notify();
		}
	}

}
