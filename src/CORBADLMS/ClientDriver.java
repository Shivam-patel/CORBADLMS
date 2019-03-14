package CORBADLMS;

import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientDriver implements Runnable {

	private final static Logger logger = Logger.getLogger(ClientDriver.class.getName());
	static private FileHandler fileTxt;
	ORB orb;
	org.omg.CORBA.Object objRef;
	NamingContextExt ncRef;
	LibraryMethods managerInt;
	LibraryMethods userInt;
	public ClientDriver(String[] args) throws InvalidName {
		orb = ORB.init(args,null);
		objRef = orb.resolve_initial_references("NameService");
		ncRef = NamingContextExtHelper.narrow(objRef);
	}
	public void run() {
		boolean append = true;

		Character userType = null;
		String clientId = "";
		String userServer = "";
		System.out.println("Enter 0 to exit.");
		System.out.println("To continue, enter your ID.");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		Scanner input = new Scanner(System.in);
		String clientInput1 = input.next();
		//int option1 = Integer.parseInt(clientInput1);
		//	logger.info("ClientInput: "+clientInput1);
		if (clientInput1.equals("0")) {
			System.out.println("Exiting the system...");
/*
			logger.info("exit code: 0");
*/
			System.exit(0);
		} else {
			clientId = clientId.concat(clientInput1);
			userType = clientId.charAt(3);
			userServer = userServer.concat(clientId.substring(0, 3));
			System.out.println(userType + "\n" + userServer + "\n" + clientId);
		}
		try {

			if (userType == 'M') {

				managerInt = LibraryMethodsHelper.narrow(ncRef.resolve_str(userServer));
				if ((managerInt.validate(clientId, userType.toString()))) {
					managerClient(clientId);
					logger.info("Client is a manager");
				} else {
					System.out.println("Wrong Manager Id. Try again!");
					System.exit(0);
				}

			} else if (userType == 'U') {
				userInt = LibraryMethodsHelper.narrow(ncRef.resolve_str(userServer));
				if ((userInt.validate(clientId, userType.toString()))) {
					userClient(clientId);
					logger.info("Client is a user");
				} else {
					System.out.println("Wrong User Id. Try again!");
					System.exit(0);
				}

			} else {
				System.out.println("Wrong client Id. Try again!");
				System.exit(0);
			}
		} catch (CannotProceed cannotProceed) {
			cannotProceed.printStackTrace();
		} catch (org.omg.CosNaming.NamingContextPackage.InvalidName invalidName) {
			invalidName.printStackTrace();
		} catch (NotFound notFound) {
			notFound.printStackTrace();
		}
		//input.close();
	}

	public static void main(String[] args) throws IOException, InvalidName {


		logger.setLevel(Level.INFO);
		fileTxt = new FileHandler("ClientLog.txt");
		logger.addHandler(fileTxt);

		new Thread(new ClientDriver(args)).start();

	}

	public void managerClient(String managerId) {
		System.out.println("Press 1 to add a new User in the library");
		System.out.println("Press 2 to add a new Manager in the library");
		System.out.println("Press 3 to add an item to the library");
		System.out.println("Press 4 to remove an item from the library");
		System.out.println("Press 5 to list item-availability of the library");
		System.out.println("Press 6 to exit");
		Scanner in = new Scanner(System.in);
		int op = in.nextInt();
		String libCode = "";
		libCode = libCode.concat(managerId.substring(0, 3));
		System.out.println(libCode);
		String reply = null;
		try {
			switch (op) {
				case 1:

					String userId;

					System.out.println("Please enter the Id of new user");
					userId = in.next();
					reply = managerInt.addUser(managerId, userId);
					break;
				case 2:
					String newManagerId;
					System.out.println("Please enter the Id of new manager");
					newManagerId = in.next();
					reply = managerInt.addManager(managerId, newManagerId);
					break;
				case 3:
					String itemId, itemName;
					int quantity;
					System.out.println("Enter the itemId, itemName, and quantity");
					itemId = in.next();
					itemName = in.next();
					quantity = in.nextInt();
					reply = managerInt.addItem(managerId, itemId, itemName, quantity);
					break;

				case 4:
					System.out.println("Enter the itemId, and quantity");
					itemId = in.next();
					quantity = in.nextInt();
					reply = managerInt.removeItem(managerId, itemId, quantity);
					break;

				case 5:

					reply = managerInt.listItemAvailability(managerId);
					break;
				case 6:
					System.exit(0);

			}

			System.out.println("The result of the operation is as follows: \n" + reply);
		}  catch (IOException e) {
			e.printStackTrace();
		}
		in.close();
	}

	public void userClient(String userId) {
		System.out.println("Press 1 to borrow an item from the library");
		System.out.println("Press 2 to find an item in the library");
		System.out.println("Press 3 to return an to from the library");
		System.out.println("Press 4 to exchange a book in the library");
		System.out.println("Press 5 to exit");
		Scanner in = new Scanner(System.in);
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		logger.info(" userClient: run");
		int op = 0;
		try {
			op = Integer.parseInt(br.readLine());
		} catch (IOException e) {
			e.printStackTrace();
		}
		String libCode = "";
		libCode = libCode.concat(userId.substring(0,3));
		System.out.println(libCode);
		String reply = null;
		try {
			switch(op) {
				case 1:
					String itemId;
					int numberOfDays;
					System.out.println("Enter corresponding itemId, and number of borrowing days");
					itemId = in.next();
					numberOfDays = in.nextInt();
					logger.info("borrowItem");
					reply = userInt.borrowItem(userId, itemId, numberOfDays);
					logger.info(userId+"\t"+itemId+"\t"+numberOfDays);
					logger.info(reply);
					if(reply.startsWith("waitlist")) {
						System.out.println(reply + "Press 1 to add, else press 0.");
						if(in.nextInt()==1) {
							logger.info("Add to waitlist");
							reply = userInt.addToWaitlist(userId, itemId, numberOfDays);
							logger.info(reply);
						}
						else {
							reply = "Exitting...";
							logger.info(reply);
						}
					}
					break;

				case 2:
					String itemName;
					logger.info("findItem");
					System.out.println("Enter item name");
					itemName = in.next();
					logger.info(itemName);
					reply = userInt.findItem(userId, itemName);
					logger.info(reply);

					break;

				case 3:
					System.out.println("Enter itemId");
					itemId = in.next();
					logger.info("return item");
					logger.info(itemId);
					reply = userInt.returnItem(userId, itemId);
					break;
				case 4:
					System.out.println("Enter New itemId and Old itemId");
					String newItem = in.next();
					String oldItem = in.next();
					logger.info("return item");
					logger.info(userId+" "+newItem+" "+oldItem);
					reply = userInt.exchangeItem(userId,newItem,oldItem);
					break;
				case 5:
					System.exit(0);

					reply = null;
			}
			logger.info(reply);
			System.out.println("The result of the operation is as follows \n" + reply);




		} catch (IOException e) {
			e.printStackTrace();
		}
		in.close();
	}
}