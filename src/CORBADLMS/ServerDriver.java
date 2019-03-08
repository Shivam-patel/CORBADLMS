package CORBADLMS;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class ServerDriver {
    public static void main(String[] args){
        try{
            ORB orb = ORB.init(args,null);
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();


            ConServer cs = new ConServer();
            MonServer ms = new MonServer();
            McgServer mc = new McgServer();
            InterServComServer mcg = new InterServComServer(1,args);
            InterServComServer mon = new InterServComServer(2,args);
            InterServComServer con = new InterServComServer(3,args);
            Thread interServCon = new Thread(con);
            interServCon.start();
            Thread interServMon = new Thread(mon);
            interServMon.start();
            Thread interServmcg = new Thread(mcg);
            interServmcg.start();
            Thread mont = new Thread(ms);
            mont.start();
            Thread conc = new Thread(cs);
            conc.start();
            Thread mcgi = new Thread(mc);
            mcgi.start();
            Object concordiaRef = rootpoa.servant_to_reference(cs);
            Object MontrealRef = rootpoa.servant_to_reference(ms);
            Object McGillRef = rootpoa.servant_to_reference(mc);
            LibraryMethods concoridaHref = LibraryMethodsHelper.narrow(concordiaRef);
            LibraryMethods MontrealHref = LibraryMethodsHelper.narrow(MontrealRef);
            LibraryMethods McGillHref = LibraryMethodsHelper.narrow(McGillRef);

            Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            NameComponent[] concordiaPath = ncRef.to_name("CON");
            ncRef.rebind(concordiaPath,concoridaHref);
            System.out.println("Concordia ready");
            NameComponent[] montrealPath = ncRef.to_name("MON");
            ncRef.rebind(montrealPath,MontrealHref);
            System.out.println("Montreal ready");
            NameComponent[] mcGillPath = ncRef.to_name("MCG");
            System.out.println("McGill ready");
            ncRef.rebind(mcGillPath,McGillHref);
            while (true){
                orb.run();
            }


        }catch (Exception e){

        }
    }
}
