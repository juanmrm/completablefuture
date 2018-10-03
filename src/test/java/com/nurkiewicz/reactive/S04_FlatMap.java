package com.nurkiewicz.reactive;

import com.nurkiewicz.reactive.stackoverflow.Question;
import com.nurkiewicz.reactive.util.AbstractFuturesTest;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.util.concurrent.CompletableFuture;

public class S04_FlatMap extends AbstractFuturesTest {

	private static final Logger log = LoggerFactory.getLogger(S04_FlatMap.class);

	// thenApply should be used if you have a synchronous mapping function.
	@Test
	public void thenApplyIsWrong() throws Exception {
		final CompletableFuture<CompletableFuture<Question>> future =
				javaQuestions()
						.thenApply(this::findMostInterestingQuestion);
		Question question = future.get().get();// This does not make much sense
		System.out.println(question);
	}

	@Test
	public void thenAcceptIsPoor() {
		javaQuestions().thenAccept(document -> {
			findMostInterestingQuestion(document).thenAccept(question -> {
				googleAnswer(question).thenAccept(answer -> {
					postAnswer(answer).thenAccept(status -> {
						if (status == HttpStatus.OK.value()) {
							log.debug("OK");
						} else {
							log.error("Wrong status code: {}", status);
						}
					});
				});
			});
		});
	}

	/*
		thenCompose is used if you have an asynchronous mapping function (i.e. one that returns a CompletableFuture).
		It will then return a future with the result directly, rather than a nested future.
	 */
	@Test
	public void thenCompose() {
		final CompletableFuture<Document> java = javaQuestions();

		final CompletableFuture<Question> questionFuture = java.thenCompose(this::findMostInterestingQuestion);

		final CompletableFuture<String> answerFuture = questionFuture.thenCompose(this::googleAnswer);

		final CompletableFuture<Integer> httpStatusFuture = answerFuture.thenCompose(this::postAnswer);

		httpStatusFuture.thenAccept(status -> {
			if (status == HttpStatus.OK.value()) {
				log.debug("OK");
			} else {
				log.error("Wrong status code: {}", status);
			}
		});
	}

	// the same as the previous method without intermediate variables
	@Test
	public void chained() {
		javaQuestions().
				thenCompose(this::findMostInterestingQuestion).
				thenCompose(this::googleAnswer).
				thenCompose(this::postAnswer).
				thenAccept(status -> {
					if (status == HttpStatus.OK.value()) {
						log.debug("OK");
					} else {
						log.error("Wrong status code: {}", status);
					}
				});
	}

	private CompletableFuture<Document> javaQuestions() {
		return CompletableFuture.supplyAsync(() ->
						client.mostRecentQuestionsAbout("java"),
				executorService
		);
	}

	private CompletableFuture<Question> findMostInterestingQuestion(Document document) {
		return CompletableFuture.completedFuture(new Question());
	}

	private CompletableFuture<String> googleAnswer(Question q) {
		return CompletableFuture.completedFuture("42");
	}

	private CompletableFuture<Integer> postAnswer(String answer) {
		return CompletableFuture.completedFuture(200);
	}

}
