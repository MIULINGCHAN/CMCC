package com.abm.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ABMWorkerThreadPool {
	private ExecutorService executor;
	private static final int POOL_MAX_THREAD_NUM = 100;
	
	public void initPool() {
		this.executor = Executors.newFixedThreadPool(POOL_MAX_THREAD_NUM);
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
