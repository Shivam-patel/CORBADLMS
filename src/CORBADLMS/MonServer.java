package CORBADLMS;

import org.omg.CORBA.ORB;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MonServer extends LibraryMethodsPOA implements Runnable{
    private ORB orb;


    private static final long serialVersionUID = 1L;
    private HashMap<String, DataModel> monLibrary = new HashMap<String, DataModel>();
    private HashMap<String, ArrayList<DataModel>> monWaitlist = new HashMap<>();
    private ArrayList<String> removedItems = new ArrayList<>();

    private HashMap<String,DataModel> itemsBorrowed = new HashMap<>();
    private ArrayList<DataModel> users = new ArrayList<DataModel>();
    private ArrayList<String> managers = new ArrayList<>();
    private Object lock;
    int MCG = 13131;
    int CON = 13133;

    private final static Logger logger = Logger.getLogger(MonServer.class.getName());
    static private FileHandler fileTxt;
    public void setORB(ORB orb_val){
        orb = orb_val;
    }
    /**
     * The class constructor that initiates and engenders new books, users, and managers at the very beginning.
     */
    public MonServer() throws Exception{
        super();
        DataModel book1 = new DataModel();
        DataModel book2 = new DataModel();
        DataModel book3 = new DataModel();
        book1.setItemName("CLRS");
        book2.setItemName("DS");
        book3.setItemName("PDA");
        book1.setQuantity(4);
        book2.setQuantity(2);
        book3.setQuantity(0);
        book1.setItemId("MON0001");
        book2.setItemId("MON0002");
        book3.setItemId("MON0003");
        monLibrary.put("MON0001", book1);
        monLibrary.put("MON0002", book2);
        monLibrary.put("MON0003", book3);
        lock = new Object();

        logger.setLevel(Level.INFO);
        fileTxt = new FileHandler("monServerLog.txt");
        logger.addHandler(fileTxt);
        System.out.println(book1);
        System.out.println(book2);
        System.out.println(book3);


        for(int i=1;i<10;i++) {
            DataModel user = new DataModel();
            user.setUserId("MONU000"+i);
            users.add(user);
        }
        for(int i=1;i<3;i++) {
            managers.add("MONM000"+i);
        }

        ArrayList<DataModel> wait02 = new ArrayList<>();
        ArrayList<DataModel> wait03 = new ArrayList<>();
        ArrayList<DataModel> wait = new ArrayList<>();
        DataModel waitBook[] = new DataModel[3];
        for (int i=0;i<3;i++){
            waitBook[i] = new DataModel();
            waitBook[i].setUserId("MONU000"+(i+1));
            waitBook[i].setDaysToBorrow(25);
            waitBook[i].setItemId("MON000"+"i");
            wait.add(waitBook[i]);
        }
        monWaitlist.put("MON0003", wait03);
        monWaitlist.put("MON0002", wait02);
        monWaitlist.put("MON0001", wait);
        Thread t = new Thread(this);

    }

    public void run() {
        try {
            this.getWaitRequest();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    /**
     * @throws IOException
     * @throws ClassNotFoundException
     * This method performs the interserver waitlisting for all the libraries.
     */
    public void getWaitRequest() throws IOException, ClassNotFoundException {
        DatagramSocket aSocket = new DatagramSocket(9986);
        byte[] buffer = new byte[1000];
        DatagramPacket request = new DatagramPacket(buffer,buffer.length);
        System.out.println("monwait");
        aSocket.receive(request);
        System.out.println("request received");
        ObjectInputStream iStream ;
        iStream = new ObjectInputStream(new ByteArrayInputStream(request.getData()));
        DataModel pack = (DataModel) iStream.readObject();
        iStream.close();
        String reply;
        try {
            if (pack.getUserId().equals("")) {
                int intReply = getItemAvailability(pack.getItemId());
                byte[] response = Integer.toString(intReply).getBytes();
                DatagramPacket re = new DatagramPacket(response, response.length, request.getAddress(), request.getPort());
                aSocket.send(re);
            } else {
                reply = this.addToWaitlist(pack.getUserId(), pack.getItemId(), pack.getDaysToBorrow());
                byte[] response = reply.getBytes();
                DatagramPacket re = new DatagramPacket(response, response.length, request.getAddress(), request.getPort());
                aSocket.send(re);
            }
        } catch (Exception e) {
            System.out.println("Exception in accessing the userId in getWaitRequest");
        }
    }
    /**This method adds a new user when called by a manager of corresponding server.
     * @param managerId
     * @param userId
     * @return
     */
    @Override
    public String addUser(String managerId, String userId) {
        logger.info("addUser");
        logger.info(managerId +"\t" + userId);
        Iterator<DataModel> iter = users.iterator();
        while(iter.hasNext()){
            if(iter.next().getUserId().startsWith(userId)) {
                return "Id already exist.";
            }
        }
        if(userId.substring(0, 3).equals("MON") && userId.charAt(3)=='U' && userId.substring(4).matches(".*\\d+.*")) {
            // make a new user
            synchronized (lock) {
                DataModel user = new DataModel();
                user.setUserId(userId);
                users.add(user);
                logger.info("Success");
            }
            return "Success";
        }
        else {
            String reply = "Wrong userId format. Please try again";
            logger.info(reply);
            return reply;
        }
    }
    /** This method adds a new manager if the manager does not already exist in the library.
     * @param managerId
     * @param newManagerId
     * @return
     */
    @Override
    public String addManager(String managerId, String newManagerId) {

        logger.info("addMananger");

        logger.info(managerId +"\t" + newManagerId);
        if(managers.contains(newManagerId)) {

            return "Id already exist.";
        }
        else if(managerId.substring(0, 3).equals("MON") && managerId.substring(3,4).equals("M") && managerId.substring(4).matches(".*\\d+.*")) {
            managers.add(newManagerId);
            logger.info("Success");

            return "Success";
        }
        else {
            logger.info("Failure");

            return "Failure";
        }
    }

    /**This method adds a new item in the current library when called by one of the managers of the library.
     * If the item already exists, it adds to the quantity of item, the input quantity
     * @param managerId
     * @param itemId
     * @param itemName
     * @param quantity
     * @return
     * @throws IOException
     */
    @Override
    public String addItem(String managerId, String itemId, String itemName, int quantity) throws IOException {
        boolean old = false;
        logger.info("addItem");
        logger.info(managerId +"\t" + itemId+"\t" + itemName+"\t" + quantity);
        for(String id : monLibrary.keySet()) {
            if (id.equals(itemId)) {
                old = true;
                break;
            }
        }
        if(old) {
            synchronized (lock) {
                DataModel value = monLibrary.get(itemId);
                Integer itemCount = value.getQuantity();
                itemCount += quantity;
                value.setQuantity(itemCount);
                logger.info("Success");
                this.moveWaitlist(itemId);
            }
            return "Success.";
        }
        DataModel value = new DataModel();
        value.setItemName(itemName);
        value.setQuantity(quantity);
        value.setItemId(itemId);
        monLibrary.put(itemId, value);
        logger.info("Success");
        synchronized (lock) {
            this.moveWaitlist(itemId);
        }
        return "Success";
    }
    /**
     * This method removes an item from the library or it decreases the availability of that item as per the parameters passed by the manager.
     * @param managerId
     * @param itemId
     * @param quantity
     * @return
     */
    @Override
    public String removeItem(String managerId, String itemId, int quantity) {
        try {
            synchronized (lock) {
                DataModel value = monLibrary.get(itemId);
                logger.info("removeItem");
                logger.info(managerId + "\t" + itemId + "\t" + quantity);

                Integer numb = value.getQuantity();
                if (quantity > numb)
                    return "Incorrect Quantity";
                if (quantity == numb || quantity == -1) {
                    monLibrary.remove(itemId);

                    removeFromWaitlist(itemId);
                    logger.info("Success");
                }

                else {
                    numb-=quantity;
                    value.setQuantity(numb);
                    logger.info("Success");
                }
            }
            return "Success";
        }
        catch(Exception e) {
            logger.info("tem not present in the library");

            return "Item not present in the library";
        }
    }
    /**This method lists all the item with its availability in the current library when called by a manager
     * @param managerId
     * @return
     */
    @Override
    public String listItemAvailability(String managerId) {
        String reply = "";

        logger.info("listItemAvailability");
        logger.info(managerId );
        Iterator<Map.Entry<String, DataModel>>iter = monLibrary.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry<String, DataModel> entry = iter.next();
            reply = reply.concat(entry.getKey());
            reply = reply.concat("  ");
            DataModel values = entry.getValue();
            reply = reply.concat(values.getItemName());
            reply = reply.concat("  ");
            reply = reply.concat(values.getQuantity().toString());
            reply = reply.concat("\n");
        }

        logger.info(reply);
        return reply;
    }
    /**This method is invoked when a user wants to borrow a book and has the necessary information to borrow that book.
     * All the preconditions to borrow a book as mentioned in provided literature is applied in this method.
     * @param userId
     * @param itemId
     * @param numberOfDays
     * @return
     * @throws IOException
     */
    @Override
    public String borrowItem(String userId, String itemId, int numberOfDays) throws IOException {
        String reply;

        logger.info("borrowItem");
        logger.info(userId+"\t"+itemId+"\t"+numberOfDays);
        if(itemsBorrowed.containsKey(userId)){
            if(itemsBorrowed.get(userId).getBorrowedBooks().containsKey(itemId)){
                reply = "Can not borrow the same book again.";
                return reply;
            }
        }
        if(monLibrary.containsKey(itemId)) {
            DataModel value;
            value = monLibrary.get(itemId);
           /* System.out.println(value.toString());
            System.out.println(value.getItemId());
            System.out.println(value.getItemName());*/
            int quantity = value.getQuantity();
            if(quantity != 0) {
                quantity--;
                value.setQuantity(quantity);
                value.setQuantity(quantity);
                DataModel borrowed;
                synchronized (lock) {
                    if (itemsBorrowed.containsKey(userId)) {
                        borrowed = itemsBorrowed.get(userId);
                        borrowed.setBorrowedBooks(itemId, numberOfDays);
                    } else {
                        borrowed = new DataModel();

                        borrowed.setBorrowedBooks(itemId, numberOfDays);
                        itemsBorrowed.put(userId, borrowed);
/*
                        System.out.println(userId+"   "+itemsBorrowed.keySet());
*/
                    }
                }
                reply = "Success";
                for(DataModel temp: users){
                    if(temp.getUserId().startsWith(userId)){
                        temp.setBorrowedBooks(itemId,numberOfDays);
                    }
                }
            }
            else {
                reply = "waitlist";
            }
            logger.info(reply);
            return reply;
        }
        else {
            DataModel user = new DataModel();
            if(itemId.startsWith("MCG")) {
                synchronized (lock) {
                    Iterator<DataModel> iter = users.iterator();
                    while (iter.hasNext()) {
                        user = iter.next();
                        if (user.getUserId().startsWith(userId)) {
                            if (user.getBooksMcg() == 1) {
                                logger.info("you can not get two books from a foreign library");
                                return "you can not get two books from a foreign library";
                            }
                            break;
                        }
                    }
                    logger.info("requesting McGill server");
                }
                InterServComClient temp = new InterServComClient(MCG, 1);
                DataModel pack = new DataModel();
                pack.setUserId(userId);
                pack.setItemId(itemId);
                pack.setDaysToBorrow(numberOfDays);
                reply = temp.operate(pack);
                if(reply.startsWith("Succ")){
                    user.setBooksMcg(1);
                    for(DataModel tempUser: users){
                        if(tempUser.getUserId().startsWith(userId)){
                            tempUser.setBorrowedBooks(itemId,numberOfDays);
                        }
                    }
                }
            }
            else if(itemId.startsWith("CON")) {
                synchronized (lock) {
                    Iterator<DataModel> iter = users.iterator();
                    while (iter.hasNext()) {
                        user = iter.next();
                        if (user.getUserId().startsWith(userId)) {
                            if (user.getBooksCon() == 1) {
                                logger.info("you can not get two books from a foreign library");
                                return "you can not get two books from a foreign library";
                            }
                            break;
                        }
                    }
                    logger.info("requesting Concordia server");
                }
                InterServComClient temp = new InterServComClient(CON, 3);
                DataModel pack = new DataModel();
                pack.setUserId(userId);
                pack.setItemId(itemId);
                pack.setDaysToBorrow(numberOfDays);
                reply = temp.operate(pack);
                if(reply.startsWith("Succ")){
                    user.setBooksCon(1);
                    for(DataModel tempUser: users){
                        if(tempUser.getUserId().startsWith(userId)){
                            tempUser.setBorrowedBooks(itemId,numberOfDays);
                        }
                    }
                }

            }
            else {
                reply = "Invalid itemId";
            }

        }
        logger.info(reply);

        return reply;
    }
    /**This method is called when a user wants to find a particular item by its name.
     * The method returns books with specified name across
     * @param userId
     * @param itemName
     * @throws IOException
     */
    @Override
    public String findItem(String userId, String itemName) throws IOException {
        String reply = "";
        logger.info("findItem");
        logger.info(userId+"\t"+itemName);
        synchronized (lock) {
            Iterator<Map.Entry<String, DataModel>> iter = monLibrary.entrySet().iterator();
            int count = 0;
            while (iter.hasNext()) {
                Map.Entry<String, DataModel> pair = iter.next();
                DataModel value = pair.getValue();
/*
                System.out.println(count++);
*/
                if (value.getItemName().equals(itemName)) {
                    reply = pair.getKey();
                    reply = reply.concat("\t");
                    reply = reply.concat(value.getQuantity().toString());
                    reply = reply.concat("\n");
                }
            }
        }
        boolean home = false;
        for(DataModel di:users){
            if(di.getUserId().startsWith(userId))
                home = true;
        }
        if(home)  {
            logger.info("calling McGill Server");
            InterServComClient temp = new InterServComClient(MCG, 4);
            logger.info("calling Concordia Server");
            InterServComClient temp1 = new InterServComClient(CON, 6);
            DataModel pack = new DataModel();
            pack.setUserId(userId);
            pack.setItemName(itemName);
            DataModel pack1 = new DataModel();
            pack1.setUserId(userId);
            pack1.setItemName(itemName);
            String replyMCG = temp.operate(pack);
            String replyMON = temp1.operate(pack1);
            reply += replyMCG;
            reply += replyMON;
        }
        logger.info(reply);
        return reply;
    }
    /**This method enables user to return any library item possessed by the user after checking all the preconditions.
     * @param userId
     * @param itemId
     * @return
     * @throws IOException
     */
    @Override
    public String returnItem(String userId, String itemId) throws IOException {
        logger.info("returnItem");

        logger.info(userId+"\t"+itemId);
        String reply = null;
        if(itemId.startsWith("MON")) {
            if (removedItems.contains(itemId)) {
                reply = "Success";
                synchronized (lock) {
                    DataModel value = itemsBorrowed.get(userId);
                    if (value.getBorrowedBooks().containsKey(itemId)) {
                        value.getBorrowedBooks().remove(itemId);
                    }
                }
            } else if (itemsBorrowed.containsKey(userId)) {
                synchronized (lock) {
                    DataModel value = itemsBorrowed.get(userId);
                    if (value.getBorrowedBooks().containsKey(itemId)) {
                        value.getBorrowedBooks().remove(itemId);
                        DataModel item = monLibrary.get(itemId);
                        int quantity = item.getQuantity();
                        item.setQuantity(quantity + 1);
                        reply = this.moveWaitlist(itemId);

                        if (value.getBorrowedBooks().isEmpty()) {
                            synchronized (lock) {
                                itemsBorrowed.remove(userId);
                                reply = "Success";
                            }
                        }

                    }
                }
            } else {
                reply = "You can not submit this book.";
            }

        }
        else if(itemId.startsWith("CON")){
            logger.info("Calling Concordia Server");

            InterServComClient temp = new InterServComClient(CON,9);
            DataModel pack = new DataModel();
            pack.setUserId(userId);
            pack.setItemId(itemId);
            reply = temp.operate(pack);
            if(reply.startsWith("Succ")){
                if(userId.startsWith("MON")){
                    for(DataModel tempUser : users){
                        if(tempUser.getUserId().startsWith(userId)){
                            tempUser.setBooksCon(0);
                        }
                    }
                }
            }
        }
        else if(itemId.startsWith("MCG")) {
            logger.info("Calling McGill Server");

            InterServComClient temp = new InterServComClient(MCG,7);
            DataModel pack = new DataModel();
            pack.setUserId(userId);
            pack.setItemId(itemId);
            reply = temp.operate(pack);
            if(reply.startsWith("Succ")){
                if(userId.startsWith("MON")){
                    for(DataModel tempUser : users){
                        if(tempUser.getUserId().startsWith(userId)){
                            tempUser.setBooksMcg(0);
                        }
                    }
                }
            }
        }
        logger.info(reply);

        return reply;
    }
    /**This method checks if the Id provided by the client is valid or not.
     * @param userId
     * @param userType
     * @return
     */
    @Override
    public boolean validate(String userId, String userType)
    {
        logger.info("Validate");
        logger.info(userId+"\t"+userType);
        if(userType.equals("U")) {
            Iterator<DataModel> iter = users.iterator();
            while (iter.hasNext()) {
                if (iter.next().getUserId().startsWith(userId)) {
                    return true;
                }
            }
            return false;
        }
        else
            return managers.contains(userId);
    }
    /**This method is called when a user wants to borrow a book but the availability is zero and the users wishes to be added to the waitlist of that book.
     * @param userId
     * @param itemId
     * @param numberOfDays
     * @return
     */

    @Override
    public String addToWaitlist(String userId, String itemId, int numberOfDays) {
        logger.info("addToWaitlist");
        logger.info(userId+"\t"+itemId+"\t"+numberOfDays);

        if(monLibrary.containsKey(itemId)) {
            synchronized (lock) {
                ArrayList<DataModel> value;
                DataModel pack = new DataModel();
                value = monWaitlist.get(itemId);
                try {
                    if (value.isEmpty()) {
                        value = new ArrayList<>();
                    }
                } catch (NullPointerException e) {
                    value = new ArrayList<>();

                }
                pack.setUserId(userId);
                pack.setDaysToBorrow(numberOfDays);
                value.add(pack);
                monWaitlist.put(itemId, value);

                logger.info("Success");
                return "Success";
            }
        }

        else
        {
            try {

                int mcgPort = 9987;
                int conPort = 9988;
                DatagramSocket aSocket = new DatagramSocket();
                DataModel pack = new DataModel();
                pack.setUserId(userId);
                pack.setDaysToBorrow(numberOfDays);
                pack.setItemId(itemId);
                ByteArrayOutputStream bStream = new ByteArrayOutputStream();
                ObjectOutput oo = new ObjectOutputStream(bStream);
                oo.writeObject(pack);
                byte[] request = bStream.toByteArray();
                InetAddress aHost = InetAddress.getLocalHost();
                if(itemId.startsWith("MCG")){
                    DatagramPacket req = new DatagramPacket(request, request.length,aHost,mcgPort);
                    aSocket.send(req);
                    byte [] buffer1 = new byte[1000];
                    DatagramPacket rep = new DatagramPacket(buffer1, buffer1.length);
                    aSocket.receive(rep);
                    String replyString = new String(rep.getData());
                    return replyString;
                }
                else if(itemId.startsWith("CON")){
                    DatagramPacket req = new DatagramPacket(request, request.length,aHost,conPort);
                    aSocket.send(req);
                    byte [] buffer1 = new byte[1000];
                    DatagramPacket rep = new DatagramPacket(buffer1, buffer1.length);
                    aSocket.receive(rep);
                    String replyString = new String(rep.getData());
                    return replyString;
                }
                aSocket.close();
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "Error. Please check the inputs.";
        }


    }
    /**This method is not directly associated with any user input but it is called whenever the item is made available(availability becomes non zero).
     * @param itemId
     * @return
     */
    public String moveWaitlist(String itemId) {
        logger.info("moveWaitList");
        logger.info(itemId);
        String reply = null;
        try{
        synchronized (lock) {
            ArrayList<DataModel> list = monWaitlist.get(itemId);
            for (int i = 0; i < list.size(); i++) {
                if (monLibrary.get(itemId).getQuantity() != 0) {
                    DataModel user = list.get(i);
                    reply = this.borrowItem(user.getUserId(), itemId, user.getDaysToBorrow());
                    if (reply.startsWith("Succ")) {
                        list.remove(user);
                        logger.info("User: "+user.getUserId() +" :automatically given the book: " + itemId);
                    } else {
                        reply = "Some error in moving the waitlist";
                    }
                } else {
                    break;
                }
            }
        }
        }catch (Exception e){
            System.out.println("No waitlist to move");
        }
        logger.info(reply);
        return reply;
    }
    /**This method is called when a manager requests to remove an item and the availability reduces to zero.
     * @param itemId
     */
    public void removeFromWaitlist(String itemId){
        logger.info("removeFromWaitlist");
        logger.info(itemId);
        synchronized (lock) {
            monWaitlist.remove(itemId);
            removedItems.add(itemId);
            logger.info("Success");
        }
    }

    @Override
    public String exchangeItem(String userId, String newItem, String oldItem) {
        logger.info("Exchange Item");
        logger.info(userId+" "+newItem+" "+oldItem);
       String reply;
       DataModel user1=null;
        for(DataModel temp:users){
            if(temp.getUserId().equals(userId)){
                user1 = temp;
                break;
            }
        }
        if (itemsBorrowed.containsKey(userId) || removedItems.contains(oldItem)||user1.getBorrowedBooks().containsKey(oldItem)) {
            DataModel user = user1;

                int avail = getItemAvailability(newItem);
                if(avail==-1){
                    logger.info("exception" + avail);

                    return "Some exception in getting the availability";
                }else if(avail==0){
                    logger.info("avail: "+avail);
                    return "The newitem is not available";
                }
                try {
                    reply = returnItem(userId,oldItem);
                    if(reply.startsWith("Succ")) {
                        reply = borrowItem(userId, newItem, 5);
                        if(!reply.startsWith("Succ")){
                            reply = borrowItem(userId,oldItem,5);
                            logger.info(reply);
                            return reply;
                        }
                    }else{
                        logger.info(reply);
                        reply = "Exception in returning the item";
                    }
                    logger.info(reply);
                    return reply;
                } catch (IOException e) {
                    return e.toString();
                }


        } else {
            reply = "You have not borrowed the book";

        }
        logger.info(reply);
        return reply;
    }
    public int getItemAvailability(String itemId){
        logger.info("getItemAvailability");
        logger.info(itemId);
        if(monLibrary.containsKey(itemId)){
            return monLibrary.get(itemId).getQuantity();
        }
        else {
            try {
                int mcgPort = 9987;
                int conPort = 9988;
                DatagramSocket aSocket = new DatagramSocket();
                DataModel pack = new DataModel();
                pack.setItemId(itemId);
                ByteArrayOutputStream bStream = new ByteArrayOutputStream();
                ObjectOutput oo = new ObjectOutputStream(bStream);
                oo.writeObject(pack);
                byte[] request = bStream.toByteArray();
                InetAddress aHost = InetAddress.getLocalHost();
                if (itemId.startsWith("MCG")) {
                    DatagramPacket req = new DatagramPacket(request, request.length, aHost, mcgPort);
                    aSocket.send(req);
                    byte[] buffer1 = new byte[1000];
                    DatagramPacket rep = new DatagramPacket(buffer1, buffer1.length);
                    aSocket.receive(rep);
                    String replyString = new String(rep.getData());
                   /* aSocket.close();*/
                    logger.info(replyString.trim());
                    return Integer.parseInt(replyString.trim());
                } else if (itemId.startsWith("CON")) {
                    DatagramPacket req = new DatagramPacket(request, request.length, aHost, conPort);
                    aSocket.send(req);
                    byte[] buffer1 = new byte[1000];
                    DatagramPacket rep = new DatagramPacket(buffer1, buffer1.length);
                    aSocket.receive(rep);
                    String replyString = new String(rep.getData());
/*
                    aSocket.close();
*/                  logger.info(replyString.trim());
                    return Integer.parseInt(replyString.trim());
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
                return -1;
            } catch (SocketException e) {
                e.printStackTrace();
                return -1;
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
            return -1;
        }
    }
    @Override
    public void shutdown() {

    }
}
