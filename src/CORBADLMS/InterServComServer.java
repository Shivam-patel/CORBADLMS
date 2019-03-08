package CORBADLMS;




import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;


/**
 * This class acts as a server for any interserver communication happening in the whole project.
 * The request sent from InterServComClient is accepted here
 */
public class InterServComServer implements  Runnable{

    int MCG = 13131;
    int MON = 13132;
    int CON = 13133;
    ORB orb;
    org.omg.CORBA.Object objRef;
    NamingContextExt ncRef;
    LibraryMethods mcgUser ;
    LibraryMethods conUser ;
    LibraryMethods monUser ;
    DatagramSocket activeSocket = null;



        public InterServComServer(int flag,String[] args) {
         try {
            orb = ORB.init(args,null);
             objRef = orb.resolve_initial_references("NameService");
             ncRef = NamingContextExtHelper.narrow(objRef);


             if(flag == 1 || flag == 4 || flag == 7){
                 activeSocket = new DatagramSocket(MCG);
                 System.out.println("mcg active");
             }
             else if(flag == 2 || flag == 5 || flag == 8) {
                 activeSocket = new DatagramSocket(CON);
                 System.out.println("con active");
             }
             else {
                 activeSocket = new DatagramSocket(MON);
                 System.out.println("mon active");
             }
            }catch(Exception e){
                e.printStackTrace();
            }

        }


    @Override
    public void run() {
        while(true) {
                byte[] buffer = new byte[65000];
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);

                ObjectInputStream iStream ;
                try {
                    activeSocket.receive(request);
                    iStream = new ObjectInputStream(new ByteArrayInputStream(request.getData()));
                    DataModel pack = (DataModel) iStream.readObject();
                    iStream.close();
                    int op = pack.getFlag();
                    mcgUser = LibraryMethodsHelper.narrow(ncRef.resolve_str("MCG"));
                    monUser = LibraryMethodsHelper.narrow(ncRef.resolve_str("MON"));
                    conUser = LibraryMethodsHelper.narrow(ncRef.resolve_str("CON"));
                    String reply = "";
                    switch (op) {
                        case 1:
                            System.out.println("I was here");
                            reply = mcgUser.borrowItem(pack.getUserId(),pack.getItemId(),pack.getDaysToBorrow());
                            break;
                        case 4:
                            reply = mcgUser.findItem(pack.getUserId(),pack.getItemName());
                            break;
                        case 7:
                            reply = mcgUser.returnItem(pack.getUserId(),pack.getItemId());
                            break;
                        case 2:
                            reply = monUser.borrowItem(pack.getUserId(),pack.getItemId(),pack.getDaysToBorrow());
                            break;
                        case 5:
                            reply = monUser.findItem(pack.getUserId(),pack.getItemName());
                            break;
                        case 8:

                            reply = monUser.returnItem(pack.getUserId(),pack.getItemId());
                            break;
                        case 3:
                            reply = conUser.borrowItem(pack.getUserId(),pack.getItemId(),pack.getDaysToBorrow());
                            break;
                        case 6:

                            reply = conUser.findItem(pack.getUserId(),pack.getItemName());
                            break;
                        case 9:
                            reply = conUser.returnItem(pack.getUserId(),pack.getItemId());
                            break;



                    }
                    DatagramPacket response;
                    System.out.println(reply);
                    byte[] rep = reply.getBytes();
                    response = new DatagramPacket(rep, rep.length,request.getAddress(),request.getPort());
                    activeSocket.send(response);


                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (CannotProceed cannotProceed) {
                    cannotProceed.printStackTrace();
                } catch (InvalidName invalidName) {
                    invalidName.printStackTrace();
                } catch (NotFound notFound) {
                    notFound.printStackTrace();
                }
        }
    }

}
