

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class KVClient {

	KVClientAPI kvC = null;
	KVClientResponse response = null;
	KVClientShowResponse showResponse = null;
	/* 
     * The requester thread gets the host/port information. 
     * thread tires to connect to the remote server.
     */
	
	public KVClient(String[] args) {
		kvC = new KVClientAPI(args[0], args[1]);
	}

	public void startrun() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String input = "";
		try {
			while(!(input = br.readLine().trim()).equalsIgnoreCase("quit")){
				String[] commandAndKV = input.split(" ", 2);

				if(commandAndKV.length==1 && commandAndKV[0].equalsIgnoreCase("show")){
				
					showResponse = kvC.showAll();
					if (showResponse.Status) {
						System.out.println(showResponse.kV.length + " Keys");
						for(KeyAndValue kav: showResponse.kV) {
							System.out.println("Key " + kav.key + " Value " + kav.Value.toString());
						}
					}
				} else if (commandAndKV.length==2) {
					
					if(commandAndKV[0].equalsIgnoreCase("insert") || commandAndKV[0].equalsIgnoreCase("modify")){
						
						String[] kV = commandAndKV[1].split(" ", 2);
						long key = 0;
						int level = 1;
						if(kV.length!=2){
							System.out.println("Invalid input. Enter again or quit");
							continue;
						} else {
							String[] kVL = kV[0].split(":", 2);
							try{
								if (kVL.length == 2) {
									key = Long.parseLong(kVL[0]);
									level = Integer.parseInt(kVL[1]);
								} else {
									key = Long.parseLong(kV[0]);
								}
							} catch(Exception e) {
								System.out.println("Invalid key. Enter again or quit");
								continue;							
							}
							if(commandAndKV[0].equalsIgnoreCase("insert")) {
								response = kvC.insert(key, kV[1], level);
								if (response.Status) {
									System.out.println("Insert Sucessful");
								} else {
									System.out.println("Error: Operation Unsucessful");
								}
							} else {
								response = kvC.modify(key, kV[1], level);
								if (response.Status) {
									System.out.println("Modify Sucessful, Old value:" + response.kV.Value.toString());
								} else {
									System.out.println("Error: Operation Unsucessful");
								}
							}
						}
					} else if(commandAndKV[0].equalsIgnoreCase("lookup") || commandAndKV[0].equalsIgnoreCase("delete")){
						long key = 0;
						int level = 1;
						String[] kVL = commandAndKV[1].split(":", 2);
						try{
							if (kVL.length == 2) {
								key = Long.parseLong(kVL[0]);
								level = Integer.parseInt(kVL[1]);
							} else {
								key = Long.parseLong(commandAndKV[1]);
							}
						} catch(Exception e) {
							System.out.println("Invalid key. Enter again or quit");
							continue;							
						}
						if(commandAndKV[0].equalsIgnoreCase("lookup")) {
							response = kvC.lookup(key, level);
						} else {
							response = kvC.delete(key, level);
						}

						if (response.Status) {
							System.out.println("Sucess: Key " + response.kV.key + "Level " + " Value " + response.kV.Value.toString());
						} else {
							System.out.println("Error: Operation Unsucessful");
						}
					} else {
						System.out.println("Invalid input. Enter again or quit");
						continue;				
					}
				} else {
					System.out.println("Invalid input. Enter again or quit");
					continue;				
				}
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 	}

	public static void main(String[] args) {
		KVClient client = new KVClient(args);
		client.startrun();
	}

}