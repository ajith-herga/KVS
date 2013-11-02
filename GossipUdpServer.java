
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.io.*;


public class GossipUdpServer {
    DatagramSocket socket = null;
	InetSocketAddress selfInetSock = null;
	ServerSocket KVClientReqSock = null;
    GossipTransmitter txObj = null;
    GossipReceiver rxObj = null;
    GossipTimeOutManager toObj = null;
    ConcurrentHashMap<String, TableEntry> membTable = null;
	TableEntry selfEntry = null;
	BufferedWriter bw = null;
	KVClientRequestServer acceptor = null;

    public GossipUdpServer(String[] args, String localPort) {
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
				socket = new DatagramSocket(i, localInet);
			} catch (SocketException e) {
			    System.err.printf("Could not listen on port: %d, Trying %d\n", i, i+1);
				continue;
			}
		    break;
    	}

    	for (int i = 1124; i < 1500; i ++) {
			try {
			    KVClientReqSock = new ServerSocket(i);
			} 
			catch (IOException e) {
			    System.err.printf("Could not listen on port: %d, Trying %d\n", i, i+1);
			    continue;
			}
			break;
		}

    	try {
			String hostname = InetAddress.getLocalHost().getHostName();
			String localKVPort = Integer.toString(KVClientReqSock.getLocalPort());
			System.out.println("KVServer running on host: "+ hostname + " port: " + localKVPort);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(-1);
		}


    	selfInetSock = new InetSocketAddress(socket.getLocalAddress(), socket.getLocalPort());
    	
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
    	
    	txObj = new GossipTransmitter(membTable, selfEntry, socket);
    	txObj.start();
    	rxObj = new GossipReceiver(membTable, bw, selfEntry, socket, txObj);
    	rxObj.start();
    	toObj = new GossipTimeOutManager(membTable, bw, selfEntry);
    	toObj.start();
    	
	}


    /*
     * KvAcceptor thread listens for connections. On arrival of a connection, a worker is spawned,
     * and the socket is given to the connection.
     */

	public void startrun() {
		acceptor = new KVClientRequestServer(KVClientReqSock);
		acceptor.start();
	}



	public void shutdown() {
		toObj.cancel();
		txObj.cancel();
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
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		
	}
}
