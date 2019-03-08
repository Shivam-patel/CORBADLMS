package CORBADLMS;


/**
* CORBADLMS/LibraryMethodsHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from LibraryInterface.idl
* Thursday, March 7, 2019 at 12:06:37 AM Eastern Standard Time
*/

abstract public class LibraryMethodsHelper
{
  private static String  _id = "IDL:CORBADLMS/LibraryMethods:1.0";

  public static void insert (org.omg.CORBA.Any a, CORBADLMS.LibraryMethods that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static CORBADLMS.LibraryMethods extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = org.omg.CORBA.ORB.init ().create_interface_tc (CORBADLMS.LibraryMethodsHelper.id (), "LibraryMethods");
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static CORBADLMS.LibraryMethods read (org.omg.CORBA.portable.InputStream istream)
  {
    return narrow (istream.read_Object (_LibraryMethodsStub.class));
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, CORBADLMS.LibraryMethods value)
  {
    ostream.write_Object ((org.omg.CORBA.Object) value);
  }

  public static CORBADLMS.LibraryMethods narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof CORBADLMS.LibraryMethods)
      return (CORBADLMS.LibraryMethods)obj;
    else if (!obj._is_a (id ()))
      throw new org.omg.CORBA.BAD_PARAM ();
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      CORBADLMS._LibraryMethodsStub stub = new CORBADLMS._LibraryMethodsStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

  public static CORBADLMS.LibraryMethods unchecked_narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof CORBADLMS.LibraryMethods)
      return (CORBADLMS.LibraryMethods)obj;
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      CORBADLMS._LibraryMethodsStub stub = new CORBADLMS._LibraryMethodsStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

}
