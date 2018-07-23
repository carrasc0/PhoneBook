import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/***In this particular case, I decided to isolate the top 5 in another list, consider that it is more efficient than
 be varying among general orders the main list when you need to sort by name
 and then by number of calls. If this list had many records these constant orderings
 they would consume many resources. On the other hand isolating only 5 elements the order will always have low complexity.
 On the other hand in another scenario this variant could have been optimized more by saving some ID of the object
 and not duplicating it in another list.
 I attach the file that you use for example. Extension .txt and with the information format '' name, number 'by line**/


class PhoneBook {

    private static List<Contact> phoneBook = new ArrayList<>();
    private static List<Contact> top5OutgoingNumbers = new ArrayList<>(5);
    private static Scanner sc;
    private static boolean isFileAlreadyReaded;
    private static final String FILE_ROUTE = "src/data.txt";

    public static void main(String[] args) {


        if (!isFileAlreadyReaded) {
            System.out.println("Reading file...");
            readFileWithContacts();
            isFileAlreadyReaded = true;
        }
        sc = new Scanner(System.in);
        printValidActions();
        String action = sc.nextLine();

        switch (action) {
            case "1":
                printAllContactsOrderedByName();
                break;
            case "2":
                printAllContactsWithOutgoingCallsOrderedByName();
                break;
            case "3":
                if (addContact()) {
                    System.out.println("Successfully added contact");
                    Collections.sort(phoneBook);
                } else {
                    System.out.println("Error adding contact. Try again...");
                }
                break;
            case "4":
                System.out.println(getPhoneNumberByContactName());
                break;
            case "5":
                System.out.println(removeContactByName());
                break;
            case "6":
                System.out.println(makeCall());
                break;
            case "7":
                printTop5PhoneNumbersByOutgoingCalls();
                break;
            default:
                System.out.println("Invalid action. Try again...");
                break;
        }


        restoreConsole();

    }

    private static void restoreConsole() {
        main(null);
    }

    private static void printValidActions() {
        System.out.println(" ");
        System.out.println("OPTIONS");
        System.out.println(" ");
        System.out.println("Print PhoneBook press 1");
        System.out.println("Print PhoneBook with count outgoing calls press 2");
        System.out.println("Add new contact press 3");
        System.out.println("Get a phone number press 4");
        System.out.println("Remove a contact press 5");
        System.out.println("Make a call press 6");
        System.out.println("Print the top 5 phone numbers with most outgoing calls press 7");
    }

    private static void readFileWithContacts() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(FILE_ROUTE));
            String sLine;
            while ((sLine = br.readLine()) != null) {
                Contact contact = extractContact(sLine);
                if (contact != null) {
                    phoneBook.add(contact);
                }
            }

            if (phoneBook.isEmpty()) {
                System.out.println("No contacts available. Please check the file");
                System.exit(0);
            } else {
                System.out.println("Contacts imported successfully");
                Collections.sort(phoneBook);
            }

        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    private static String removeContactByName() {

        System.out.println("Write the name of the contact...");
        String name = sc.nextLine();

        for (Contact contact : phoneBook) {
            if (contact.name.equals(name)) {
                phoneBook.remove(contact);
                if (removeInTop5IfExistsAfterRemoveInPhoneBook(contact)) {
                    updateTop5AfterRemoveContact();
                }
                return "Contact removed correctly";
            }
        }
        return "The given contact doesn't exists. Try again...";
    }

    private static String getPhoneNumberByContactName() {

        System.out.println("Insert the name of the contact...");
        String name = sc.nextLine();

        if (!name.isEmpty()) {
            for (Contact contact : phoneBook) {
                if (contact.name.equals(name)) {
                    return contact.phoneNumber;
                }
            }
        } else {
            return "You need to insert the name. Try again";
        }

        return "The given contact doesn't exists. Try again";
    }

    private static String makeCall() {

        System.out.println("Write the number...");
        String phoneNumber = sc.nextLine();

        for (Contact contact : phoneBook) {
            if (contact.phoneNumber.equals(phoneNumber)) {
                contact.outCalls++;
                updateTop5OutgoingPhoneNumbers(contact);
                return "Call maked succesfully";
            }
        }
        return "The given number doesn't exists. Try again...";
    }

    private static void orderTop5() {

        Collections.sort(top5OutgoingNumbers, new Comparator<Contact>() {
            @Override
            public int compare(Contact o1, Contact o2) {
                return Integer.compare(o2.outCalls, o1.outCalls);
            }
        });
    }

    private static void updateTop5AfterRemoveContact() {

        Contact pivoteContact = null;

        for (Contact contact : phoneBook) {
            if (!existsContactInTopList(contact)) {
                pivoteContact = contact;
                break;
            }
        }

        if (pivoteContact != null) {

            for (Contact contact : phoneBook) {
                if (contact.outCalls > pivoteContact.outCalls) {
                    if (!existsContactInTopList(contact)) {
                        pivoteContact = contact;
                    }
                }
            }
            top5OutgoingNumbers.add(pivoteContact);
            orderTop5();
        }

    }

    private static void printAllContactsOrderedByName() {

        for (Contact contact : phoneBook) {

            StringBuilder builder = new StringBuilder();
            builder.append("Name: ").append(contact.name)
                    .append(" Phone number: ").append(contact.phoneNumber);

            System.out.println(builder);
        }
    }

    private static void printAllContactsWithOutgoingCallsOrderedByName() {

        for (Contact contact : phoneBook) {

            StringBuilder builder = new StringBuilder();
            builder.append("Name: ").append(contact.name)
                    .append(" Phone number: ").append(contact.phoneNumber)
                    .append(" Outgoing calls: ").append(contact.outCalls);

            System.out.println(builder);
        }
    }

    private static void printTop5PhoneNumbersByOutgoingCalls() {

        if (!top5OutgoingNumbers.isEmpty()) {
            for (Contact contact : top5OutgoingNumbers) {

                StringBuilder builder = new StringBuilder();
                builder.append("Name: ").append(contact.name)
                        .append(" Number: ").append(contact.phoneNumber)
                        .append(" Outgoing calls: ").append(contact.outCalls);

                System.out.println(builder);
            }
        } else {
            System.out.println("No outgoing calls..");
        }
    }

    private static void updateTop5OutgoingPhoneNumbers(Contact contact) {

        if (top5OutgoingNumbers.size() < 5 && !existsContactInTopList(contact)) {
            top5OutgoingNumbers.add(contact);
        } else {
            if (!existsContactInTopList(contact)) {
                for (Contact contact1 : top5OutgoingNumbers) {
                    if (!contact.equals(contact1) && (contact.outCalls > contact1.outCalls)) {
                        top5OutgoingNumbers.remove(contact1);
                        top5OutgoingNumbers.add(contact);
                        break;
                    }
                }
            }
        }
        orderTop5();

    }

    private static boolean existsContactInTopList(Contact contact) {
        for (Contact contact1 : top5OutgoingNumbers) {
            if (contact1.equals(contact)) {
                return true;
            }
        }
        return false;
    }

    private static boolean addContact() {

        System.out.println("Write name: ");
        String name = sc.nextLine();

        if (!name.isEmpty()) {

            System.out.println("Write phone number: ");
            String phoneNumber = sc.nextLine();

            if (!phoneNumber.isEmpty()) {

                if (validatePhoneNumber(phoneNumber)) {
                    phoneBook.add(new Contact(name, phoneNumber, 0));
                    return true;
                } else {
                    System.out.println("The given number is incorrect. Try again...");
                    return false;
                }

            } else {
                System.out.println("Phone number is empty. You have to insert a valid phone number. Try again...");
                return false;
            }

        } else {
            System.out.println("Name is empty. You have to insert a valid name. Try again...");
            return false;
        }

    }

    private static boolean removeInTop5IfExistsAfterRemoveInPhoneBook(Contact contact) {

        for (Contact contact1 : top5OutgoingNumbers) {
            if (contact1.equals(contact)) {
                top5OutgoingNumbers.remove(contact1);
                return true;
            }
        }
        return false;

    }

    private static boolean validatePhoneNumber(String phoneNumber) {

        String regex = "(\\+359|00359|0)(87|88|89)([2-9]{1})([0-9]{6})";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(phoneNumber).matches();

    }

    private static Contact extractContact(String line) {

        String[] pair = line.split(",");

        if (validatePhoneNumber(pair[1])) {
            return new Contact(pair[0], pair[1], 0);
        } else {
            System.out.println("This number is INCORRECT: " + pair[0] + " " + pair[1]);
            return null;
        }


    }

    private static class Contact implements Comparable<Contact> {

        private String name;
        private String phoneNumber;
        private int outCalls;

        private Contact(String name, String phoneNumber, int outCalls) {
            this.name = name;
            this.phoneNumber = phoneNumber;
            this.outCalls = outCalls;
        }

        @Override
        public int compareTo(Contact contact) {
            return this.name.compareToIgnoreCase(contact.name);
        }


    }


}
