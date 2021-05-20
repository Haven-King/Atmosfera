/*
 * Copyright 2021 Haven King
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.hephaestus.atmosfera.client.sound.util;

import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

public class Profiler {
	private static final Map<String, Long> TIMES = new TreeMap<>();
	private static final Map<String, Long> COUNTS = new TreeMap<>();
	private static final Stack<Section> STACK = new Stack<>();

	public static void push(String name) {
		STACK.push(new Section(name));
	}

	public static void pop(Logger logger) {
		STACK.pop().pop(logger);
	}

	public static void pop() {
		STACK.pop().pop();
	}

	public static void report(Logger logger) {
		logger.debug("Times per category:");

		for (Map.Entry<String, Long> entry : TIMES.entrySet()) {
			logger.debug("  {}: {}", entry.getKey(), ((double) entry.getValue()) / 1_000_000_000.0);
		}

		logger.debug("Average time per category:");

		for (Map.Entry<String, Long> entry : TIMES.entrySet()) {
			logger.debug("  {}: {}", entry.getKey(), (((double) entry.getValue()) / ((double) COUNTS.get(entry.getKey()))) / 1_000_000_000.0);
		}
	}

	private static class Section {
		private final String name;
		private final long timeStart;

		private Section(String name) {
			this.name = name;
			this.timeStart = System.nanoTime();
		}

		private void pop(Logger logger) {
			long time = System.nanoTime() - this.timeStart;
			Profiler.TIMES.compute(this.name, (key, val) -> (val == null ? 0 : val) + time);
			Profiler.COUNTS.compute(this.name, (key, val) -> (val == null ? 0 : val) + 1);

			if (logger != null) {
				logger.debug("{}: {}ns", this.name, time);
			}
		}

		private void pop() {
			this.pop(null);
		}
	}
}
