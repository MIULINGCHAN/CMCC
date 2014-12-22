/**
 * 
 */
package com.ocs.threadPool;

import com.ocs.utils.PropertiesUtils;


/**
 * @author MiuChan
 * @DATE 2014Äê12ÔÂ19ÈÕ
 */
/**
 * @author miumiu
 *
 */
public class OCSThreadPool {
	private static OCSThreadPool instance = null;
	private WorkerThreadPool cfWorkerThreadPool;
	private int cfWorkerMaxNum;
	private WorkerThreadPool rfWorkerThreadPool;
	private int rfWorkerMaxNum;
	
	public static synchronized OCSThreadPool getInstance() throws Exception {
		if (instance == null) {
			instance = new OCSThreadPool();
		}
		return instance;
	}
	
	/**
	 * 
	 */
	public OCSThreadPool() {
		super();
		cfWorkerThreadPool = new WorkerThreadPool();
		rfWorkerThreadPool = new WorkerThreadPool();
	}

	public void initPool(){
		this.cfWorkerMaxNum = PropertiesUtils.getCFThreadPoolMaxNum();
		this.rfWorkerMaxNum = PropertiesUtils.getRFThreadPoolMaxNum();
		cfWorkerThreadPool.initPool(cfWorkerMaxNum);
		rfWorkerThreadPool.initPool(rfWorkerMaxNum);
	}
	
	public void shutdownPool(){
		cfWorkerThreadPool.shutdownPool();
		rfWorkerThreadPool.shutdownPool();
	}
	
	/**
	 * @return the cfWorkerThreadPool
	 */
	public WorkerThreadPool getCfWorkerThreadPool() {
		return cfWorkerThreadPool;
	}

	/**
	 * @param cfWorkerThreadPool the cfWorkerThreadPool to set
	 */
	public void setCfWorkerThreadPool(WorkerThreadPool cfWorkerThreadPool) {
		this.cfWorkerThreadPool = cfWorkerThreadPool;
	}

	/**
	 * @return the rfWorkerThreadPool
	 */
	public WorkerThreadPool getRfWorkerThreadPool() {
		return rfWorkerThreadPool;
	}

	/**
	 * @param rfWorkerThreadPool the rfWorkerThreadPool to set
	 */
	public void setRfWorkerThreadPool(WorkerThreadPool rfWorkerThreadPool) {
		this.rfWorkerThreadPool = rfWorkerThreadPool;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "OCSThreadPool [cfWorkerThreadPool=" + cfWorkerThreadPool
				+ ", rfWorkerThreadPool=" + rfWorkerThreadPool + "]";
	}
	
}
