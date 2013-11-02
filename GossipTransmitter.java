import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;


public class GossipTransmitter extends TimerTask {
    Timer transmitterTimer = null;
    ConcurrentHashMap<String, TableEntry> membTable = null;
	TableEntry selfEntry = null;
    DatagramSocket socket = null;

	public GossipTransmitter(ConcurrentHashMap<String, TableEntry> membTable, TableEntry selfEntry, DatagramSocket socket) {
		this.membTable = membTable;
		this.selfEntry = selfEntry;
		this.socket = socket;
	}

	public void start() {
		transmitterTimer = new Timer();
		transmitterTimer.schedule(this, 0, Constants.TRANSMITTER_PERIOD);
	}

	public void stop() {
		transmitterTimer.cancel();
	}

	@Override
	public void run() {
		//lock.lock();
		//System.out.println("Transmitter: Running");
		
		ArrayList<TableEntry> tE = getReservoir();
		
		if (tE == null) {
			//lock.unlock();
			return;
		} else {
			// Increment heart beat as there is at least one member.
			selfEntry.incHrtBeat();
		}

		//Loop over the members of the randomized list.
		//System.out.println("--------------------------");
		for (TableEntry entry: tE) {
			try {
				//System.out.println("Tx Gossip to " + entry.id);
				SendMembList(entry);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				continue;
			}
		}
		//System.out.println("Transmitter: Done");
		//lock.unlock();
	}

	private ArrayList<TableEntry> getReservoir() {
    	// Lock already taken, safe to change memTable.
    	ArrayList<TableEntry> tE = null;
    	int k = 0, size = 0;

    	
    	tE = new ArrayList<TableEntry>(10);
    	
		for(TableEntry entry : membTable.values()) {
			if (entry == selfEntry) {
				continue;
			} else if (entry.hasFailed) {
				continue;
			} else {
				tE.add(entry);
			}
		}

    	size = tE.size();
		if (size < 1) {
			tE = null;
			return tE;
    	} else if (size == 1) {
    		return tE;
    	}
    	// The array may have size elements, but there are a total of size + 1 nodes.
    	k = (int)Math.floor(Math.sqrt(size + 1));

    	ArrayList<TableEntry> subList = new ArrayList<TableEntry>(10);
    	//System.out.println("Sublist size: " + subList.size());

        Random ra = new Random();
    	int randomNumber;
    	TableEntry temp = null;
    	//System.out.println("Size to send: " + k + "Out of: " + size);
    	for (int i = k; i < size; i ++) {
    		randomNumber = (int)ra.nextInt(i + 1);
        	//System.out.println("Random Number: " + randomNumber);
       		if (randomNumber < k) {
    			//swap
    			temp = tE.get(i);
    			tE.set(i, tE.get(randomNumber));
    			tE.set(randomNumber, temp);
    		}
    	}

        subList.addAll(tE.subList(0, k));
    	//System.out.println("Sublist size: " + subList.size());
        return subList;
    }

	public synchronized void SendMembList(TableEntry entry) throws UnknownHostException {
		String[] dataItems = entry.id.split("___");
		
		InetAddress address = null;
		address = InetAddress.getByName(dataItems[0]);
		int port = Integer.parseInt(dataItems[1]);
        Gson gson = new Gson();
        MarshalledServerData mR = new MarshalledServerData(membTable);
		String tx = gson.toJson(mR);
        byte[] outbuf = tx.getBytes();
		DatagramPacket sendpacket = new DatagramPacket(outbuf, outbuf.length, address, port);
		send(sendpacket);

    }

	public synchronized void send(DatagramPacket packet) {
		try {
			socket.send(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
