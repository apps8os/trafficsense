import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class getCoords {

	String listOfStopIds[] ={"E2223", "E2226", "E2204", "E2215", "E2024", "E2022","E2310","E2332","1014","1233"};

	
	public void getCoords() throws Exception{
		
	
		for(int i=0; i < listOfStopIds.length;i++ ){
			URL url = new URL("http://api.reittiopas.fi/hsl/prod/?user=jussi&pass=infinity&request=stop&code=" + 
						listOfStopIds[i] +"&p=00000001&format=text");
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			String inputline = in.readLine();
			Pattern pattern = Pattern.compile(":\"(..)(.*),(..)(.*)\"");
			Matcher matcher = pattern.matcher(inputline);
			if (matcher.find()) {
			    System.out.println("\"test\",\""+matcher.group(1)+"."+matcher.group(2)+","+matcher.group(3)+"."+matcher.group(4)+"\"");
			    
			}
			
		}
		System.out.println("Finished");
	}
	
		public static void main(String [] args){
			getCoords get = new getCoords();
			try {
				get.getCoords();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		
	
	
	
	
	
	
}
