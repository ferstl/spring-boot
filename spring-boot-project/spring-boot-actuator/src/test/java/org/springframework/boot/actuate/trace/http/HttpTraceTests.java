package org.springframework.boot.actuate.trace.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.springframework.boot.actuate.trace.http.HttpTrace.Principal;
import org.springframework.boot.actuate.trace.http.HttpTrace.Request;
import org.springframework.boot.actuate.trace.http.HttpTrace.Response;
import org.springframework.core.io.ClassPathResource;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link HttpTrace}
 *
 * @author Stefan Ferstl
 */
public class HttpTraceTests {

	private final ObjectMapper objectMapper;
	private final String exampleJson;

	public HttpTraceTests() throws IOException {
		this.objectMapper = new ObjectMapper();
		this.objectMapper.registerModule(new JavaTimeModule());

		ClassPathResource exampleJsonResource = new ClassPathResource("trace/http/example-trace.json");
		try(InputStream is = exampleJsonResource.getInputStream()) {
			this.exampleJson = IOUtils.toString(is).trim();
		}
	}

	@Test
	public void jsonSerialization() throws IOException, URISyntaxException {
		HttpTrace trace = createHttpTrace();

		String jsonValue = this.objectMapper.writeValueAsString(trace);

		assertEquals(this.exampleJson, jsonValue);
	}

	@Test
	public void jsonDeserialization() throws IOException, URISyntaxException {
		HttpTrace deserializedTrace = this.objectMapper.readValue(this.exampleJson, HttpTrace.class);
		HttpTrace originalTrace = createHttpTrace();

		assertEquals(originalTrace.getTimestamp(), deserializedTrace.getTimestamp());
		assertEquals(originalTrace.getTimeTaken(), deserializedTrace.getTimeTaken());
		assertEquals(originalTrace.getPrincipal().getName(), deserializedTrace.getPrincipal().getName());
		assertEquals(originalTrace.getSession().getId(), deserializedTrace.getSession().getId());

		assertEquals(originalTrace.getRequest().getMethod(), deserializedTrace.getRequest().getMethod());
		assertEquals(originalTrace.getRequest().getUri(), deserializedTrace.getRequest().getUri());
		assertEquals(originalTrace.getRequest().getRemoteAddress(), deserializedTrace.getRequest().getRemoteAddress());
		assertEquals(originalTrace.getRequest().getHeaders(), deserializedTrace.getRequest().getHeaders());

		assertEquals(originalTrace.getResponse().getStatus(), deserializedTrace.getResponse().getStatus());
		assertEquals(originalTrace.getResponse().getHeaders(), deserializedTrace.getResponse().getHeaders());


	}

	@NotNull
	private static HttpTrace createHttpTrace() throws URISyntaxException {
		HttpTrace trace = new HttpTrace();
		Map<String, List<String>> requestHeader = Collections.singletonMap("X-Req-Header", Collections.singletonList("reqHeaderValue"));
		Map<String, List<String>> responseHeader = Collections.singletonMap("X-Resp-Header", Collections.singletonList("respHeaderValue"));
		Instant timestamp = Instant.parse("2018-10-09T04:16:26.979Z");

		trace.setTimestamp(timestamp);
		trace.setPrincipal(new Principal("principal"));
		trace.setResponse(new Response(200, requestHeader));
		trace.setRequest(new Request("GET", new URI("http://example.com"), responseHeader, "192.168.0.1"));
		trace.setSessionId("sessionId");
		trace.setTimeTaken(100L);
		return trace;
	}

}
