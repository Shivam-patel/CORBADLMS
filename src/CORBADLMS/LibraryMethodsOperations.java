package CORBADLMS;


import java.io.IOException;

/**
* CORBADLMS/LibraryMethodsOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from LibraryInterface.idl
* Thursday, March 7, 2019 at 12:06:37 AM Eastern Standard Time
*/

public interface LibraryMethodsOperations 
{
  String addUser (String managerId, String userId);
  String addManager (String managerId, String newManagerId);
  String addItem (String managerId, String itemId, String itemName, int quantity) throws IOException;
  String removeItem (String managerId, String itemId, int quantity);
  String listItemAvailability (String managerId);
  boolean validate (String managerId, String clientType);
  String borrowItem (String userId, String itemId, int numberOfDays) throws IOException;
  String findItem (String userId, String itemName) throws IOException;
  String returnItem(String userId, String itemId) throws IOException;
  String addToWaitlist (String userId, String itemId, int numberOfDays);
  String exchangeItem (String userId, String newItem, String oldItem);
  void shutdown ();
} // interface LibraryMethodsOperations
