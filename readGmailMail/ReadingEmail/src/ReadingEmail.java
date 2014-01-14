import java.util.*;
import javax.mail.*;

//reads email
public class ReadingEmail {
    public static void main(String[] args) {
        Properties props = new Properties();
        //set protocol
        props.setProperty("mail.store.protocol", "imaps");
        try {
            Session session = Session.getInstance(props, null);
            Store store = session.getStore();
            //connect to gmails imap server and logs in with given credentials
            store.connect("imap.gmail.com", "trafficsense.aalto@gmail.com", "ag47)h(58P");
            //specify the folder from which to get email
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            //get last message
            Message msg = inbox.getMessage(inbox.getMessageCount());
            //get the senders email address
            Address[] in = msg.getFrom();
            for (Address address : in) {
                System.out.println("From:" + address.toString());
            }
            //imap email protocol allows for multiple email parts
            //Here we assume just one text part to the email
            Multipart mp = (Multipart) msg.getContent();
            BodyPart bp = mp.getBodyPart(0);
            System.out.println("Send Date:" + msg.getSentDate());
            System.out.println("Subject:" + msg.getSubject());
            System.out.println("Content:\n" + bp.getContent()); 
        } catch (Exception mex) {
            mex.printStackTrace();
        }
    }
}