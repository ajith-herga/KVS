import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class KVClientRequestServer extends Thread {
	Socket clientSocket = null; 
	ServerSocket serverSocket = null;
	List<Worker> workers = new ArrayList<Worker>();
	ConcurrentHashMap<String, TableEntry> membTable;
	BufferedWriter bw;
	TableEntry selfEntry;
	GossipTransmitter txObj = null;
	KVStore kvStore = null;
	List<ICommand> redirectCommands = null;
	
	public KVClientRequestServer(ConcurrentHashMap<String, TableEntry> membTable, BufferedWriter bw, TableEntry selfEntry,
			GossipTransmitter txObj, KVStore kvStore, List<ICommand> redirectCommands) {
		
    	for (int i = 1124; i < 1500; i ++) {
			try {
				serverSocket = new ServerSocket(i);
			} 
			catch (IOException e) {
			    System.err.printf("Could not listen on port: %d, Trying %d\n", i, i+1);
			    continue;
			}
			break;
		}

    	try {
			String hostname = InetAddress.getLocalHost().getHostName();
			String localKVPort = Integer.toString(serverSocket.getLocalPort());
			System.out.println("KVServer running on host: "+ hostname + " port: " + localKVPort);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(-1);
		}
    	this.bw = bw;
    	this.selfEntry = selfEntry;
    	this.membTable = membTable;
    	this.kvStore = kvStore;
		this.redirectCommands = redirectCommands;
		this.txObj = txObj;
	}

	public class Worker extends Thread {
		
		Socket clientSocket;
		PrintWriter out = null;
		BufferedReader in = null;
		ICommand clientCommand = null;
		
		public Worker(Socket clientSocket) {
		    this.clientSocket = clientSocket;
			try {
				out = new PrintWriter(clientSocket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			} catch (IOException e) {
				e.printStackTrace();
				// Going to return anyway
			}		
		}
		
		public synchronized void send(String json) {
			if (out != null) {
				out.println(json);
			}
		}
		
		public synchronized void closeClient() {
			if (out != null)
				out.close();
				out = null;
			try {
				if (in != null) 
					in.close();
					in = null;
				if (clientSocket != null)
					clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
				//Going to return anyway
			}
		}

		public void run() {
			String inputLine;
			try {
				if ((inputLine = in.readLine()) != null) {
					//System.out.printf("KVWorker: Received %s\n", inputLine);
					Gson gson = new Gson();
					Type dataType = new TypeToken<MarshalledClientData>(){}.getType();
					MarshalledClientData marshelledData = gson.fromJson(inputLine,dataType);
					KVData data = marshelledData.data;
					if(data.command.equals(KVCommands.SHOWALL)){
						clientCommand = new DirectLocalShowCommand(data, this, kvStore);
						clientCommand.execute();
						return;
					}
					
					TableEntry destHostEntry = 	HashUtility.findMachineForKey(membTable,data.key.keyHash);
					if (destHostEntry == selfEntry) {
						clientCommand = new DirectLocalCommand(this, data, kvStore);
						clientCommand.execute();
					} else {
						clientCommand = new RemoteCommand(this, destHostEntry, txObj, selfEntry, data);
						synchronized(redirectCommands) {
							redirectCommands.add(clientCommand);
						}
						clientCommand.execute();
					}					    
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
    
	public void run() {
		while (true) {
			try {
			    clientSocket = serverSocket.accept();
			} 
			catch (IOException e) {
			    System.out.printf("Accept failed for main socket %d", serverSocket.getLocalPort());
			    System.exit(-1);
			}
			//System.out.println("KvAcceptor: Got Connection");
			Worker worker = new Worker(clientSocket);
			worker.start();
			workers.add(worker);
		}
	}

}