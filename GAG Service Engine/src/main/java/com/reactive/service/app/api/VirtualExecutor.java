package com.reactive.service.app.api;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.Executors;

public class VirtualExecutor implements ExecutorService{
	 private ExecutorService executor;
	 	
	    public VirtualExecutor( int maximumPoolSize) {
	        executor = Executors.newVirtualThreadPerTaskExecutor();
	    }

	    @Override
	    public void execute(Runnable command) {
	        executor.execute(command);
	    }

	    @Override
	    public void shutdown() {
	        executor.shutdown();
	    }

	    @Override
	    public List<Runnable> shutdownNow() {
	        return executor.shutdownNow();
	    }

	    @Override
	    public boolean isShutdown() {
	        return executor.isShutdown();
	    }

	    @Override
	    public boolean isTerminated() {
	        return executor.isTerminated();
	    }

	    @Override
	    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
	        return executor.awaitTermination(timeout, unit);
	    }

	    @Override
	    public <T> Future<T> submit(Callable<T> task) {
	        return executor.submit(task);
	    }

	    @Override
	    public <T> Future<T> submit(Runnable task, T result) {
	        return executor.submit(task, result);
	    }

	    @Override
	    public Future<?> submit(Runnable task) {
	        return executor.submit(task);
	    }

	    @Override
	    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
	        return executor.invokeAll(tasks);
	    }

	    @Override
	    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
	            throws InterruptedException {
	        return executor.invokeAll(tasks, timeout, unit);
	    }

	    @Override
	    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
	        return executor.invokeAny(tasks);
	    }

	    @Override
	    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
	            throws InterruptedException, ExecutionException, TimeoutException {
	        return executor.invokeAny(tasks, timeout, unit);
	    }
	}
