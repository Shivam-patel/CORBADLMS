package CORBADLMS;


import java.io.IOException;

/**
* CORBADLMS/LibraryMethodsPOA.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from LibraryInterface.idl
* Thursday, March 7, 2019 at 12:06:37 AM Eastern Standard Time
*/

public abstract class LibraryMethodsPOA extends org.omg.PortableServer.Servant
 implements CORBADLMS.LibraryMethodsOperations, org.omg.CORBA.portable.InvokeHandler
{

  // Constructors

  private static java.util.Hashtable _methods = new java.util.Hashtable ();
  static
  {
    _methods.put ("addUser", new java.lang.Integer (0));
    _methods.put ("addManager", new java.lang.Integer (1));
    _methods.put ("addItem", new java.lang.Integer (2));
    _methods.put ("removeItem", new java.lang.Integer (3));
    _methods.put ("listItemAvailability", new java.lang.Integer (4));
    _methods.put ("validate", new java.lang.Integer (5));
    _methods.put ("borrowItem", new java.lang.Integer (6));
    _methods.put ("findItem", new java.lang.Integer (7));
    _methods.put ("returnItem", new java.lang.Integer (8));
    _methods.put ("addToWaitlist", new java.lang.Integer (9));
    _methods.put ("exchangeItem", new java.lang.Integer (10));
    _methods.put ("shutdown", new java.lang.Integer (11));
  }

  public org.omg.CORBA.portable.OutputStream _invoke (String $method,
                                org.omg.CORBA.portable.InputStream in,
                                org.omg.CORBA.portable.ResponseHandler $rh)
  {
    org.omg.CORBA.portable.OutputStream out = null;
    java.lang.Integer __method = (java.lang.Integer)_methods.get ($method);
    if (__method == null)
      throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);

    switch (__method.intValue ())
    {
       case 0:  // CORBADLMS/LibraryMethods/addUser
       {
         String managerId = in.read_string ();
         String userId = in.read_string ();
         String $result = null;
         $result = this.addUser (managerId, userId);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 1:  // CORBADLMS/LibraryMethods/addManager
       {
         String managerId = in.read_string ();
         String newManagerId = in.read_string ();
         String $result = null;
         $result = this.addManager (managerId, newManagerId);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 2:  // CORBADLMS/LibraryMethods/addItem
       {
         String managerId = in.read_string ();
         String itemId = in.read_string ();
         String itemName = in.read_string ();
         int quantity = in.read_long ();
         String $result = null;
           try {
               $result = this.addItem (managerId, itemId, itemName, quantity);
           } catch (IOException e) {
               e.printStackTrace();
           }
           out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 3:  // CORBADLMS/LibraryMethods/removeItem
       {
         String managerId = in.read_string ();
         String itemId = in.read_string ();
         int quantity = in.read_long ();
         String $result = null;
         $result = this.removeItem (managerId, itemId, quantity);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 4:  // CORBADLMS/LibraryMethods/listItemAvailability
       {
         String managerId = in.read_string ();
         String $result = null;
         $result = this.listItemAvailability (managerId);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 5:  // CORBADLMS/LibraryMethods/validate
       {
         String managerId = in.read_string ();
         String clientType = in.read_string ();
         boolean $result = false;
         $result = this.validate (managerId, clientType);
         out = $rh.createReply();
         out.write_boolean ($result);
         break;
       }

       case 6:  // CORBADLMS/LibraryMethods/borrowItem
       {
         String userId = in.read_string ();
         String itemId = in.read_string ();
         int numberOfDays = in.read_long ();
         String $result = null;
           try {
               $result = this.borrowItem (userId, itemId, numberOfDays);
           } catch (IOException e) {
               e.printStackTrace();
           }
           out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 7:  // CORBADLMS/LibraryMethods/findItem
       {
         String userId = in.read_string ();
         String itemName = in.read_string ();
         String $result = null;
           try {
               $result = this.findItem (userId, itemName);
           } catch (IOException e) {
               e.printStackTrace();
           }
           out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 8:  // CORBADLMS/LibraryMethods/returnItem
       {
         String userId = in.read_string ();
         String itemId = in.read_string ();
         String $result = null;
           try {
               $result = this.returnItem(userId, itemId);
           } catch (IOException e) {
               e.printStackTrace();
           }
           out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 9:  // CORBADLMS/LibraryMethods/addToWaitlist
       {
         String userId = in.read_string ();
         String itemId = in.read_string ();
         int numberOfDays = in.read_long ();
         String $result = null;
         $result = this.addToWaitlist (userId, itemId, numberOfDays);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 10:  // CORBADLMS/LibraryMethods/exchangeItem
       {
         String userId = in.read_string ();
         String newItem = in.read_string ();
         String oldItem = in.read_string ();
         String $result = null;
         $result = this.exchangeItem (userId, newItem, oldItem);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 11:  // CORBADLMS/LibraryMethods/shutdown
       {
         this.shutdown ();
         out = $rh.createReply();
         break;
       }

       default:
         throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    }

    return out;
  } // _invoke

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:CORBADLMS/LibraryMethods:1.0"};

  public String[] _all_interfaces (org.omg.PortableServer.POA poa, byte[] objectId)
  {
    return (String[])__ids.clone ();
  }

  public LibraryMethods _this() 
  {
    return LibraryMethodsHelper.narrow(
    super._this_object());
  }

  public LibraryMethods _this(org.omg.CORBA.ORB orb) 
  {
    return LibraryMethodsHelper.narrow(
    super._this_object(orb));
  }


} // class LibraryMethodsPOA
