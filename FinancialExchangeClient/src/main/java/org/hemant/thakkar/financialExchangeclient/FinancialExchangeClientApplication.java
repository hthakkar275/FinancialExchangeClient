package org.hemant.thakkar.financialExchangeclient;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FinancialExchangeClientApplication {

	/**
	 * Main program to instantiate the client instance and perform couple of operations
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		//manyOrdersSingleProductSlow();
		//String hostAndPort = "http://ec2-13-58-226-155.us-east-2.compute.amazonaws.com:8082";
		//String hostAndPort = "http://ec2-3-15-14-182.us-east-2.compute.amazonaws.com:8080";
		//String hostAndPort = "http://localhost:8080";
		String hostAndPort = "http://finex-load-balancer-1348257361.us-east-2.elb.amazonaws.com";
		basicTwoOrder(hostAndPort);
		//addProducts(hostAndPort);
		//addProductsAndParticipants(hostAndPort);
		//addParticipants(hostAndPort);
		//generateRandomOrders(hostAndPort);
	}
		
	static void addProducts(String hostAndPort) {
		FinancialExchangeClient client = 
				new FinancialExchangeClient(hostAndPort);

		try {
			client.addEquities("./src/main/resources/Products.csv");
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	static void addParticipants(String hostAndPort) {
		FinancialExchangeClient client = 
				new FinancialExchangeClient(hostAndPort);

		try {
			client.addBrokers("./src/main/resources/Participants.csv");
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	static void addProductsAndParticipants(String hostAndPort) {
		FinancialExchangeClient client = 
				new FinancialExchangeClient(hostAndPort);

		try {
			client.addEquities("./src/main/resources/Products.csv");
			client.addBrokers("./src/main/resources/Participants.csv");
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}


	static void basicTwoOrder(String hostAndPort) {
		FinancialExchangeClient client = 
				new FinancialExchangeClient(hostAndPort);

		try {
//			client.addEquities("./src/main/resources/Products.csv");
//			client.addBrokers("./src/main/resources/Participants.csv");
			client.addOrders("./src/main/resources/BasicTwoOrders.csv");
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	static void manyOrdersSingleProductSlow(String hostAndPort) {
		FinancialExchangeClient client = 
				new FinancialExchangeClient(hostAndPort);

		try {
			client.addEquities("./src/main/resources/Products.csv");
			client.addBrokers("./src/main/resources/Participants.csv");
			client.addOrders("./src/main/resources/ManyOrdersSingleProduct.csv");
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	static void generateRandomOrders(String hostAndPort)  {
		try {
			Path orderFile = Paths.get("./src/main/resources/RandomOrders.csv");
			String header = "side,quantity,price,type,longevity,productId,participantId\n";
			String dataTypes = "String,Integer,Decimal,String,String,Integer,Integer\n";
			if (!Files.exists(orderFile)) {
				Files.createFile(orderFile);
			}
			Files.write(orderFile, header.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
			Files.write(orderFile, dataTypes.getBytes(), StandardOpenOption.APPEND);
			
			Random random = new Random();
			
			IntStream.rangeClosed(0, 1000).forEach(i -> {
				List<String> tokens = new ArrayList<>();
				tokens.add(random.nextBoolean() ? "BUY" : "SELL");
				tokens.add(Integer.toString((random.nextInt(9) + 1)*100));
				tokens.add(new BigDecimal(random.nextInt(9) + 1).setScale(2).toString());
				tokens.add("LIMIT");
				tokens.add("DAY");
				tokens.add(Integer.toString(getInt(random, 133, 137)));
				tokens.add(Integer.toString(getInt(random, 138, 141)));
				String orderLine = tokens.stream().collect(Collectors.joining(","));
				orderLine = orderLine + "\n";
				
				try {
					Files.write(orderFile, orderLine.getBytes(), StandardOpenOption.APPEND);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			FinancialExchangeClient client = 
					new FinancialExchangeClient(hostAndPort);
			client.addOrders("./src/main/resources/RandomOrders.csv");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static int getInt(Random random, int from, int to) {
		int value = random.nextInt(to);
		while (value < from) {
			value = random.nextInt(to);
		}
		return value;
	}

}
