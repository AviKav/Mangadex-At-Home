/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mdnet.base;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ProxyInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;

import static org.apache.commons.io.IOUtils.EOF;

public class CachingInputStream extends ProxyInputStream {
	private final OutputStream cache;
	private final ExecutorService executor;
	private final Runnable onClose;

	public CachingInputStream(InputStream response, ExecutorService executor, OutputStream cache, Runnable onClose) {
		super(response);
		this.executor = executor;
		this.cache = cache;
		this.onClose = onClose;
	}

	@Override
	public void close() throws IOException {
		executor.submit(() -> {
			try {
				IOUtils.copy(in, cache);
			} catch (IOException ignored) {
			} finally {
				try {
					in.close();
				} catch (IOException ignored) {
				}
				try {
					cache.close();
				} catch (IOException ignored) {
				}
				onClose.run();
			}
		});
	}

	@Override
	public int read() throws IOException {
		final int ch = super.read();
		if (ch != EOF) {
			cache.write(ch);
		}
		return ch;
	}

	@Override
	public int read(final byte[] bts, final int st, final int end) throws IOException {
		final int n = super.read(bts, st, end);
		if (n != EOF) {
			cache.write(bts, st, n);
		}
		return n;
	}

	@Override
	public int read(final byte[] bts) throws IOException {
		final int n = super.read(bts);
		if (n != EOF) {
			cache.write(bts, 0, n);
		}
		return n;
	}
}
