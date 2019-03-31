package org.hemant.thakkar.financialExchangeclient;

public class FinancialExchangeClientApplication {

	/**
	 * Main program to instantiate the client instance and perform couple of operations
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		//basicTwoOrder();
		manyOrdersSingleProductSlow();
	}

	static void basicTwoOrder() {
		FinancialExchangeClient client = 
				new FinancialExchangeClient("http://localhost:8080");

		try {
			client.addEquities("./src/main/resources/Products.csv");
			client.addBrokers("./src/main/resources/Participants.csv");
			client.addOrders("./src/main/resources/BasicTwoOrders.csv");
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	static void manyOrdersSingleProductSlow() {
		FinancialExchangeClient client = 
				new FinancialExchangeClient("http://localhost:8080");

		try {
			client.addEquities("./src/main/resources/Products.csv");
			client.addBrokers("./src/main/resources/Participants.csv");
			client.addOrders("./src/main/resources/ManyOrdersSingleProduct.csv");
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

}
