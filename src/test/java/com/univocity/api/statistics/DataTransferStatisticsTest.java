/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.api.statistics;

import com.univocity.api.common.*;
import org.testng.annotations.*;

import static org.testng.Assert.*;

public class DataTransferStatisticsTest {

	@Test
	public void testStatisticsReporting() throws Exception {
		DataTransferStatistics<String, StringBuilder> stats = new DataTransferStatistics<String, StringBuilder>();

		String source = "0123456789";

		StringBuilder target = new StringBuilder();

		assertFalse(stats.isRunning());
		assertFalse(stats.isAborted());
		assertFalse(stats.isStarted());
		assertEquals(stats.toString(), "Not started");

		stats.started(source, source.length(), target);

		assertTrue(stats.isRunning());
		assertFalse(stats.isAborted());
		assertTrue(stats.isStarted());

		assertEquals(stats.getTransferPercentage(), 0.0);

		//System.out.println(stats.toString());

		for (int i = 0; i < source.length(); i++) {
			target.append(source.charAt(i));

			assertEquals(stats.getTotalTransferredSoFar(), (double) i);
			assertEquals((int) (stats.getTransferPercentage() * 100), i * 10);

			stats.transferred(source, 1, target);

			assertEquals(stats.getTotalTransferredSoFar(), (double) i + 1);
			assertEquals((int) (stats.getTransferPercentage() * 100), (i + 1) * 10);


			assertTrue(stats.isRunning());
			assertFalse(stats.isAborted());
			assertTrue(stats.isStarted());

			//	System.out.println(stats.toString());
		}

		stats.completed(source, target);

		assertFalse(stats.isRunning());
		assertFalse(stats.isAborted());
		assertTrue(stats.isStarted());

		assertEquals(stats.getTransferPercentage(), 1.0);
		//System.out.println(stats.toString());
	}

	@Test
	public void testStatisticsReportingAtGivenIntervals() throws Exception {

		final boolean[] last = new boolean[]{false};
		final int[] count = new int[]{0};

		DataTransferStatistics<String, StringBuilder> stats = new DataTransferStatistics<String, StringBuilder>(10, new NotificationHandler<DataTransferStatistics<String, StringBuilder>>() {
			@Override
			public void notify(DataTransferStatistics<String, StringBuilder> statistics, boolean lastNotification) {
				last[0] = lastNotification;
				if (lastNotification) {
					assertFalse(statistics.isRunning());
					assertFalse(statistics.isAborted());
					assertTrue(statistics.isStarted());

					assertEquals(statistics.getTransferPercentage(), 1.0);
				} else {
					assertTrue(statistics.isRunning());
					assertFalse(statistics.isAborted());
					assertTrue(statistics.isStarted());
					count[0]++;
				}
			}
		});

		String source = "0123456789";

		StringBuilder target = new StringBuilder();
		stats.started(source, source.length(), target);

		for (int i = 0; i < source.length(); i++) {
			target.append(source.charAt(i));
			stats.transferred(source, 1, target);

			Thread.sleep(5);
		}

		assertFalse(last[0]);
		stats.completed(source, target);
		assertTrue(last[0]);

		assertTrue(count[0] < (source.length() / 2) + 1 && count[0] > 4);
	}

}