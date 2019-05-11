package org.hemant.thakkar.financialExchangeclient;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.http.HttpHost;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class FinancialExchangeClient {

	private String scheme;
	private String domain;
	private int port;
	private String basePath;
	private HttpHost target;
	private HttpClientContext context;
	private CloseableHttpClient httpclient;
	private ObjectMapper jsonMapper;

	public FinancialExchangeClient(String url) {
		
		// Let's work through an example URL http://localhost
		
		// This token will separate the scheme and the remainder of the URL
		// by splitting on the "//". The first token will be "https:"" and
		// second token will be localhost
		String[] urlTokens = url.split("//");
		this.scheme = urlTokens[0].replace(":", "");
		
		// Let's isolate the domain part from the second token above by splitting
		// the second token by the "/". Result will be that the first token is
		// localhost which is the domain.
		this.domain = urlTokens[1].split("/")[0];
		
		// Sometimes domain part includes the host and non-standard port. If 
		// port is absent then standard https port is 443 and http port is 80.
		// However if there is an explicit port specified such as, for example,
		// http://localhost:8080, we want to identify that explicitly specified port. 
		// In the case of http://localhost:8080, the port is 3443. The determination 
		// of the presence of specific port is made by looking for the ":" character.
		if (domain.contains(":")) {
			this.domain = urlTokens[1].split(":")[0];
			this.port = Integer.parseInt(urlTokens[1].split(":")[1]);
		} else {
			if (this.scheme.equals("https")) {
				port = 443;
			} else {
				port = 80;
			}
		}
		
		// The base path is the InvestOne URL specified as above. The 
		// API endpoints are typically the standard InvestOne URL concatenated
		// with relative path of the API.
		this.basePath = url;
        this.target = new HttpHost(this.domain, this.port, this.scheme);
        this.context = HttpClientContext.create();
		this.jsonMapper = new ObjectMapper();
		this.httpclient = HttpClients.createDefault();
		this.jsonMapper.enable(SerializationFeature.INDENT_OUTPUT);
	}

	/**
	 * Invoke the API operation to add a product
	 * 
	 * @return
	 */
	public void addEquities(String dataFile) {
		try {	
			System.out.println("========== Add Equities ===========");
			String url = this.basePath + "/product/equity/";
			doPost(url, dataFile);
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	public void addBrokers(String dataFile) {
		try {	
			System.out.println("========== Add Brokers ===========");
			String url = this.basePath + "/participant/broker/";
			doPost(url, dataFile);
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	public void addOrders(String dataFile) {
		try {	
			System.out.println("========== Add Orders ===========");
			String url = this.basePath + "/order/";
			doPost(url, dataFile);
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	private void doPost(String apiEndpoint, String dataFile) throws IOException {
		List<String> lines = Files.lines(Paths.get(dataFile)).collect(Collectors.toList());
		String headerLine = lines.remove(0);
		String headerTypeLine = lines.remove(0);
		lines.stream().forEach(s -> {
			doPost(apiEndpoint, headerLine, headerTypeLine, s);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}

	private void doPost(String apiEndpoint, String headerLine, String headerTypeLine, String dataLine) {
		
		CloseableHttpResponse response = null;
		
		try {
			String[] headers = headerLine.split(",");
			String[] headerTypes = headerTypeLine.split(",");
			String[] data = dataLine.split(",");
			final Map<String, Object> bodyParameters = new HashMap<String, Object>();

			IntStream.range(0, headers.length).forEach(i -> {
				String key = headers[i].trim();
				String keyType = headerTypes[i].trim();
				String value = data[i].trim();
				Object valueObject = null;
				
				switch (keyType) {
				case "Integer":
						valueObject = Long.parseLong(value);
						break;
				case "Boolean": 
						valueObject = Boolean.parseBoolean(value);
						break;
				case "Decimal":
						valueObject = new BigDecimal(value);
						break;
				default:
					valueObject = value;
				}
				bodyParameters.put(key, valueObject);
			});
			

			HttpPost request = new HttpPost(apiEndpoint);

			// Accept/produce JSON
			request.setHeader("Accept", "application/json");
			request.setHeader("Content-type", "application/json");
			
			// Get the POST method body which is the JSON string for posting transaction.
			String requestJson = jsonMapper.writeValueAsString(bodyParameters);
			
			// Add the post transaction JSON to the HTTP body
			StringEntity entity = new StringEntity(requestJson);
			request.setEntity(entity);

			// Invoke the API
			response = httpclient.execute(this.target, request, this.context);

			// Collect the result
			String responsBody = EntityUtils.toString(response.getEntity());
			Object responseJson = jsonMapper.readValue(responsBody, Object.class);
			String beautifiedResponseJson = 
					jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseJson);
			System.out.println("--------------------------");
			System.out.println("Request");
			System.out.println(requestJson);
			System.out.println("Response");
			System.out.println(beautifiedResponseJson);
		} catch (ParseException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
