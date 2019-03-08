package CORBADLMS;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * <p>This class serves as a client that enables inter server communication and forwards the request to appropriate server.
 * Any interserver request is forwarded by the host server to InterServComClient. Hence this acts as a client for interserver communication.
 * It also recieves the answer and routes it to its destination
 *
 */
public class InterServComClient {
    private final int port;
    private int flag ;
    public InterServComClient(int port, int flag) throws IOException {
        this.port = port;
        this.flag = flag;
    }

    /**
     * <p>
     *     The method operate takes a DataModel object as an input.
     *     This method extracts the input object to forward the request
     * </p>
     * @param pack This is a DataModel object that holds information regarding the operation to be executed.
     * @return replyString this string contains the reply from the executed operation.
     *
     */
    public String operate(DataModel pack){
        String defaultReply = "System Failure";
        try{
            DatagramSocket aSocket = new DatagramSocket();
            System.out.println("I am a caller. calling...");
            ByteArrayOutputStream bStream = new ByteArrayOutputStream();
            ObjectOutput oo = new ObjectOutputStream(bStream);
            pack.setFlag(flag);
            oo.writeObject(pack);


            byte[] message = bStream.toByteArray();
            InetAddress aHost = InetAddress.getByName("localhost");
            DatagramPacket request = new DatagramPacket(message, message.length, aHost, port);
            System.out.println("sending...");
            aSocket.send(request);

            byte [] buffer1 = new byte[1000];
            DatagramPacket rep = new DatagramPacket(buffer1, buffer1.length);
            aSocket.receive(rep);


            String replyString = new String(rep.getData());
            System.out.println(replyString);
          //  aSocket = null;
            return replyString;

        }catch(SocketException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return defaultReply;
    }

}
