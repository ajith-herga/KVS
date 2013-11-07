

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.UnknownHostException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class KVClient {

    Requester requester;
	/* 
     * The requester thread gets the host/port information. 
     * thread tires to connect to the remote server.
     */
	private class Requester extends Thread {
		KVData query;
		Socket sock = null;
        String hostName = null, portOne = null;

        public Requester(KVData query, String _hostName, String _portOne) {
            hostName = _hostName;
            portOne = _portOne;
			//System.out.printf("Client: Construct: Reg Servers: %s %s\n",
			                  //hostName, portOne);
            this.query = query;
            try {
	            sock = new Socket(hostName, Integer.parseInt(portOne));
	        } catch (UnknownHostException e) {
	            System.err.println("Don't know about host: "+ hostName + " " + portOne);
	            return;
	        } catch (IOException e) {
	            System.err.println("No socket for " + hostName + " " + portOne);
	            return;
	        }
			//System.out.println("Client: Construct Done");
        }

		@Override
		public void run() {
			//System.out.println("Client: Thread Start Send");
			PrintWriter out = null;
	        BufferedReader in = null;
	        String servLine = null;
	        if (sock == null) {
	        	return;
	        }
	        try {
	            out = new PrintWriter(sock.getOutputStream(), true);
	            in = new BufferedReader(new InputStreamReader(
	                                        sock.getInputStream()));
	        } catch (IOException e) {
	            System.err.println("No Buffered I/O for " + hostName + " " + portOne);
	            return;
	        }

	        Gson gson = new Gson();
        	MarshalledClientData mcD = new MarshalledClientData(query);
    		String tx = gson.toJson(mcD);
    		out.println(tx);

	        try {

	        	if ((servLine = in.readLine()) != null) {
					Type dataType = new TypeToken<MarshalledClientData>(){}.getType();
					MarshalledClientData marshalledData = gson.fromJson(servLine,dataType);

					if (marshalledData.showData!=null) {
						if(marshalledData.showData.length == 0){
							System.out.println("No keys at this server");						
							return;
						}
					} else if ((marshalledData.data!=null && marshalledData.data.code==StatusCode.FAILURE) || marshalledData.data==null) {
						System.out.println("Operation Unsuccessful");
						return;
					}

					switch(query.command) {
						case LOOKUPKV:
							System.out.println("Value: " + marshalledData.data.value.toString());
							break;
						case DELETEKV:
						case MODIFYKV:
							System.out.println("Old Value: " + marshalledData.data.value.toString());
						case INSERTKV:
							System.out.println("Operation successful");
							break;
						case SHOWALL:
							for(KVData data :  marshalledData.showData){
								System.out.println("Key: " + data.key.key + " Value: " + data.value.toString());
							}
							break;
						default:
							System.out.println("Invalid command");
					}
	        	}
	        } catch (IOException e) {
	            System.err.println("Couldnot read from Buffered I/O " + hostName + " " + portOne);
	            return;
	        }
			//System.out.println("Client: Thread Finish Send");
		}

	}
	
	public void startrun(String[] args) {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String input = "";
		try {
			while(!(input = br.readLine().trim()).equalsIgnoreCase("quit")){
				KVCommands command = null;
				KVData data = null;
				String[] commandAndKV = input.split(" ", 2);

				if(commandAndKV.length==1 && commandAndKV[0].equalsIgnoreCase("show")){
				
					command = KVCommands.SHOWALL;
					data = new KVData(command, null, null, System.currentTimeMillis(),StatusCode.FAILURE);					
				
				} else if (commandAndKV.length==2) {
					
					if(commandAndKV[0].equalsIgnoreCase("insert") || commandAndKV[0].equalsIgnoreCase("modify")){
						
						String[] kV = commandAndKV[1].split(" ", 2);
						long key = 0;
						if(kV.length!=2){
							System.out.println("Invalid input. Enter again or quit");
							continue;
						} else {
							try{
								key = Long.parseLong(kV[0]);
							} catch(Exception e) {
								System.out.println("Invalid key. Enter again or quit");
								continue;							
							}
							if(commandAndKV[0].equalsIgnoreCase("insert")) {
								command = KVCommands.INSERTKV;
							} else {
								command = KVCommands.MODIFYKV;
							}
							data = new KVData(command, key, kV[1], System.currentTimeMillis(),StatusCode.FAILURE);
						}
					} else if(commandAndKV[0].equalsIgnoreCase("lookup") || commandAndKV[0].equalsIgnoreCase("delete")){
						long key = 0;
						try{
								key = Long.parseLong(commandAndKV[1]);
						} catch(Exception e) {
							System.out.println("Invalid key. Enter again or quit");
							continue;							
						}
						if(commandAndKV[0].equalsIgnoreCase("lookup")) {
							command = KVCommands.LOOKUPKV;
						} else {
							command = KVCommands.DELETEKV;
						}
						data = new KVData(command, key, null, System.currentTimeMillis(),StatusCode.FAILURE);
					} else {
						System.out.println("Invalid input. Enter again or quit");
						continue;				
					}
				} else {
					System.out.println("Invalid input. Enter again or quit");
					continue;				
				}
				
			    requester = new Requester(data, args[0], args[1]);
				requester.start();
				joinThreads();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 	}

	public void joinThreads() {
		try {
			requester.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		KVClient client = new KVClient();
		client.startrun(args);
	}

}