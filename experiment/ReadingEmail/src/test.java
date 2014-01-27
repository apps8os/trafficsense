





public class test {
	public static void main(String[] args){
		GmailReader reader = new GmailReader();
		try {
			reader.initMailbox("trafficsense.aalto@gmail.com","ag47)h(58P");
		} catch (GmailReader.EmailException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Email email=null;
		try {
			email = reader.getNextEmail();
			email= reader.getNextEmail();
		} catch (GmailReader.EmailException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(email.toString());
	}
}
