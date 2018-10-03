package com.nurkiewicz.reactive;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.nurkiewicz.reactive.util.AbstractFuturesTest;
import com.nurkiewicz.reactive.util.InterruptibleTask;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class S11_Cancelling extends AbstractFuturesTest {

	private static final Logger log = LoggerFactory.getLogger(S11_Cancelling.class);
	private static ExecutorService myThreadPool;

	@BeforeClass
	public static void init() {
		myThreadPool = Executors.newFixedThreadPool(10, new ThreadFactoryBuilder().setNameFormat("Custom-%d").build());
	}

	@AfterClass
	public static void close() {
		myThreadPool.shutdownNow();
	}

	@Test
	public void shouldCancelFuture() throws InterruptedException, TimeoutException {
		//given
		InterruptibleTask task = new InterruptibleTask();
		Future future = myThreadPool.submit(task);
		task.blockUntilStarted();

		//when
		log.debug("Interrupt signal has been send");
		future.cancel(true);

		//then
		log.debug("Block the thread until interrupted");
		task.blockUntilInterrupted();
	}

	@Ignore("Fails with CompletableFuture")
	@Test
	public void shouldCancelCompletableFuture() throws InterruptedException, TimeoutException {
		//given
		InterruptibleTask task = new InterruptibleTask();
		CompletableFuture<Void> future = CompletableFuture.supplyAsync(task, myThreadPool);
		task.blockUntilStarted();

		//when
		log.debug("Interrupt signal has been send");
		future.cancel(true);

		//then
		log.debug("Block the thread until interrupted");
		task.blockUntilInterrupted();
	}
}

