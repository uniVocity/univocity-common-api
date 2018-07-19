/*
 * Copyright (c) 2013 Univocity Software Pty Ltd. All rights reserved.
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

		final String source = "0123456789";
		final int notificationInterval = 10;
		final int waitTime = 5;
		final int expectedMaximimNotificationsReceived = (source.length() * 2) + 1;


		final boolean[] last = new boolean[]{false};
		final int[] notificationsReceived = new int[]{0};

		DataTransferStatistics<String, StringBuilder> stats = new DataTransferStatistics<String, StringBuilder>(notificationInterval, new NotificationHandler<DataTransferStatistics<String, StringBuilder>>() {
			@Override
			public void notify(DataTransferStatistics<String, StringBuilder> statistics, boolean lastNotification) {
				last[0] = lastNotification;
				if (lastNotification) {
					assertFalse(statistics.isRunning());
					assertFalse(statistics.isAborted());
					assertTrue(statistics.isStarted());
					notificationsReceived[0]++;
					assertEquals(statistics.getTransferPercentage(), 1.0);
				} else {
					assertTrue(statistics.isRunning());
					assertFalse(statistics.isAborted());
					assertTrue(statistics.isStarted());
					notificationsReceived[0]++;
				}
			}
		});

		StringBuilder target = new StringBuilder();
		stats.started(source, source.length(), target);

		for (int i = 0; i < source.length(); i++) {
			target.append(source.charAt(i));
			stats.transferred(source, 1, target);

			Thread.sleep(waitTime);
		}

		assertFalse(last[0]);
		stats.completed(source, target);
		assertTrue(last[0]);

		assertTrue(notificationsReceived[0] < expectedMaximimNotificationsReceived, notificationsReceived[0] + " should be less than" + expectedMaximimNotificationsReceived);
		assertTrue( notificationsReceived[0] > 4, notificationsReceived[0] + " should be more than" + 4);
	}

}