/**
 * The MIT License
 * Copyright (c) 2016 Avanza Bank AB
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.avanza.heartbeat.agent;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

public class UrlResourcePropertySource implements PropertySource {

	private final Logger log = new Logger(UrlResourcePropertySource.class);

	private final URL url;

	public UrlResourcePropertySource(URL url) {
		this.url = url;
	}

	@Override
	public Properties getProperties() {
		try {
			return tryGetPropertiesWithRetries(10);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Properties tryGetPropertiesWithRetries(int maxTries) throws IOException, InterruptedException {
		int attempt = 0;
		while(attempt++ < maxTries-1) {
			try {
				return tryGetProperties();
			} catch (Exception e) {
				log.error("Failed to resolve properties (attempt " + attempt + " of " + maxTries + ")");
				Thread.sleep(1000);
			}
		}
		return tryGetProperties();
	}
	
	private Properties tryGetProperties() throws IOException {
		URLConnection connection = url.openConnection();
		connection.setConnectTimeout(1000);
		connection.setReadTimeout(3000);
		connection.connect();
		Properties p = new Properties();
		try (InputStream stream = connection.getInputStream()) {
			p.load(stream);
		}
		return p;
	}

    @Override
    public String toString() {
        return "UrlResourcePropertySource [url=" + url + "]";
    }

}
