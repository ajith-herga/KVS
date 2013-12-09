import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;


public class MovieSearch {

	KVClientAPI kvC = null;
	
	public MovieSearch(String host, String port) {
		kvC = new KVClientAPI(host, port);
	}

	public void findMovies(String word){
		KVClientResponse res = kvC.lookup(word.toLowerCase(), 1); 
		if(res.Status)
			System.out.println(res.kV.Value);
		else
			System.out.println("No movies found for keyword");
	}
	
	public void populateStore(){
        File inputFile  = new File("input.txt");  // File to read from.
        try {
			BufferedReader inputReader = new BufferedReader(new FileReader(inputFile));
			String inputLine = "", movieTitle = "", str= "";
			String keywords[];
			HashSet<String> allTitles = new HashSet<String>();
			while ((inputLine = inputReader.readLine())!=null){
				int end = inputLine.substring(1).indexOf('"') + 1;
				movieTitle = inputLine.substring(1, end);
				if(allTitles.contains(movieTitle))
					continue;
				allTitles.add(movieTitle);
				System.out.print("\n"+movieTitle+" - ");
				str = movieTitle.replaceAll("[!?,:]", "");
				keywords = str.split("\\s+");
				for(String word:keywords){
					word = word.toLowerCase();
					System.out.print(word+",");
					KVClientResponse response = kvC.lookup(word, 1);
					Thread.sleep(10L);
					String list = "";
					if(response.Status){
						list = (String)response.kV.Value + "," ;
					}
					String newValue = list + "\""+ movieTitle + "\"";
					response = kvC.insert(word, newValue, 1);
					if(!response.Status){
						System.out.println("Error Occured");
					}
					Thread.sleep(10L);
				}
			}
			System.out.println(kvC.lookup("murder", 1).kV.Value);
			inputReader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		MovieSearch m = new MovieSearch(args[0], args[1]);
		m.populateStore();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String input = "";
		try {
			while(!(input = br.readLine().trim()).equalsIgnoreCase("quit")){
				m.findMovies(input);
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
