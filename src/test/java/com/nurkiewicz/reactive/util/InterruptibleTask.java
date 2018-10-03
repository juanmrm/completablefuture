package com.nurkiewicz.reactive.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class InterruptibleTask implements Runnable, Supplier<Void> {

	private static final Logger log = LoggerFactory.getLogger(InterruptibleTask.class);

	private final CountDownLatch started = new CountDownLatch(1);
	private final CountDownLatch interrupted = new CountDownLatch(1);

	@Override
	public Void get() {
		run();
		return null;
	}

	@Override
	public void run() {
		started.countDown();
		try {
			log.debug("Task is running");
			Thread.sleep(10_000);
		} catch (InterruptedException ignored) {
			log.error("Task has been interrupted", ignored);
			interrupted.countDown();
		}
	}

	public void blockUntilStarted() throws InterruptedException {
		log.debug("Thread block until started");
		started.await();
	}

	public void blockUntilInterrupted() throws InterruptedException, TimeoutException {
		if (!interrupted.await(1, TimeUnit.SECONDS)) {
			log.debug("Thread has not been interrupted during 1 second");
			throw new TimeoutException("Not interrupted within 1 second");
		}
	}
}