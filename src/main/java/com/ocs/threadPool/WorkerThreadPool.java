/**
 * 
 */
package com.ocs.threadPool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author MiuChan
 * @DATE 2014Äê12ÔÂ19ÈÕ
 */
/**
 * @author miumiu
 *
 */
public class WorkerThreadPool {
	private ExecutorService executor;
	private static final int DEFAULT_POOL_MAX_THREAD_NUM = 5;
	
	public void initPool() {
		this.executor = Executors.newFixedThreadPool(DEFAULT_POOL_MAX_THREAD_NUM);
	}
	
	public void initPool(int max){
		if(max<0)
			this.executor = Executors.newFixedThreadPool(DEFAULT_POOL_MAX_THREAD_NUM);
		else
			this.executor = Executors.newFixedThreadPool(max);
	}
	
	public void shutdownPool() {
		this.executor.shutdown();
        while (!this.executor.isTerminated()) {
        }
	}
	
	public ExecutorService getExecutor() {
		return executor;
	}

	public void setExecutor(ExecutorService executor) {
		this.executor = executor;
	}
}
