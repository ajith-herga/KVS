import java.net.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.io.*;


public class GossipServer {
    DatagramSocket udpSocket = null;
	InetSocketAddress selfInetSock = null;
    GossipTransmitter txObj = null;
    GossipReceiver rxObj = null;
    GossipTimeOutManager toObj = null;
    ConcurrentHashMap<String, TableEntry> membTable = null;
	TableEntry selfEntry = null;
	BufferedWriter bw = null;
	KVClientRequestServer kvcRx = null;
	KVStore kvStore = null;
	List<ICommand> redirectCommands = null;
	List<ICommand> replicaCommands = null;
	
    public GossipServer(String[] args, String localPort) {
    	long currentTime = System.currentTimeMillis();
    	InetAddress localInet = null;
		try {
			localInet = InetAddress.getLocalHost();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			System.out.println("Could not get local host, kill");
			System.exit(0);
		}
    	for (int i = 1024; i < 1500; i++) {
		    try {
				udpSocket = new DatagramSocket(i, localInet);
			} catch (SocketException e) {
			    System.err.printf("Could not listen on port: %d, Trying %d\n", i, i+1);
				continue;
			}
		    break;
    	}



    	selfInetSock = new InetSocketAddress(udpSocket.getLocalAddress(), udpSocket.getLocalPort());
    	
    	String id = selfInetSock.getHostName() + "___" + 
    	            	selfInetSock.getPort() + "___" + currentTime;
    	selfEntry = new TableEntry(id, 0);
    	membTable = new ConcurrentHashMap<String, TableEntry>();
    	membTable.put(id, selfEntry);
    	System.out.println(selfEntry.id);
    	if( args != null && args.length != 0 && args[0] != null ){
    		TableEntry contact = new TableEntry(args[0], 1);
        	membTable.put(contact.id, contact);
    	}
    	
    	try {
			bw = new BufferedWriter(new FileWriter("/tmp/testlog_" + localPort, false));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
    	
    	
    	kvStore = new KVStore(bw,membTable);
    	redirectCommands = Collections.synchronizedList(new LinkedList<ICommand>());
    	replicaCommands = Collections.synchronizedList(new LinkedList<ICommand>());
    	txObj = new GossipTransmitter(membTable, selfEntry, udpSocket);
    	txObj.start();
    	rxObj = new GossipReceiver(membTable, bw, selfEntry, udpSocket, txObj, kvStore, redirectCommands, replicaCommands);
    	rxObj.start();
    	toObj = new GossipTimeOutManager(membTable, bw, selfEntry);
    	toObj.start();
		kvcRx = new KVClientRequestServer(membTable, bw, selfEntry, txObj, kvStore, redirectCommands, replicaCommands);
		kvcRx.start();
	}


	public void shutdown() {
		
		toObj.cancel();
		rxObj.interrupt();
		System.out.println("Shutting down Gossip");
		selfEntry.hrtBeat = 0;
		try {
			System.out.println("Leaving " + selfEntry.id);
			for (TableEntry entry: membTable.values()) {
				if (entry != selfEntry && !entry.hasFailed) {
					txObj.SendMembList(entry);
				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		//membTable.remove(selfEntry.id);
		TableEntry newDest = HashUtility.findMachineForKey(membTable, selfEntry.hashString);
		KVData[] replicaData = kvStore.getKVDataForMachine(selfEntry, KVCommands.DELETEKV);
		KVData[] destData = kvStore.getKVDataForMachine(selfEntry, KVCommands.INSERTKV);
		TableEntry[] sendoldReplicas = HashUtility.findReplicaforMachine(membTable, selfEntry.hashString);
		MoveKeysToNewDest check = new MoveKeysToNewDest(sendoldReplicas,newDest, selfEntry, txObj, kvStore, replicaCommands, replicaData, destData);
		check.execute();
		txObj.cancel();
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		
	}
}
