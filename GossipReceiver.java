import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class GossipReceiver extends Thread {

    ConcurrentHashMap<String, TableEntry> membTable = null;
	BufferedWriter bw = null;
	TableEntry selfEntry = null;
    DatagramSocket socket = null;
    GossipTransmitter txObj = null;

	public GossipReceiver(ConcurrentHashMap<String, TableEntry> membTable, BufferedWriter bw, TableEntry selfEntry, 
			DatagramSocket socket, GossipTransmitter txObj) {
		this.membTable = membTable;
		this.bw = bw;
		this.selfEntry = selfEntry;
		this.socket = socket;
		this.txObj = txObj;
	}

	private boolean processPacket(DatagramPacket packet) {
		String rx = new String(packet.getData(), 0, packet.getLength());
		//System.out.println("Recieve: Got " + rx);
		Gson gson = new Gson();
		Type collectionType = new TypeToken<HashMap<String,TableEntry>>(){}.getType();
		HashMap<String, TableEntry> temp = gson.fromJson(rx,collectionType);
        mergeMembTable(temp);
		return false;
	}


	public void mergeMembTable(HashMap<String, TableEntry> temp) {
		long currentTime = System.currentTimeMillis();
		for (TableEntry entry: temp.values()) {
			if (entry.hasFailed) {
				continue;
			}
			if (membTable.containsKey(entry.id)) {
				//System.out.println("Known Machine");
				TableEntry oldEntry = membTable.get(entry.id);
				if (oldEntry.cmpAndUpdateHrtBeat(entry.hrtBeat, currentTime)) {
	    			try {
						bw.write(entry.id + ": Left   at " + new Date(currentTime));
						bw.newLine(); 
						bw.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}else{
				//System.out.println("New Entry: " + entry.id);
				membTable.put(entry.id, entry);
				entry.updateTime();
				if (entry.hrtBeat >= 1) {
					if (entry.hrtBeat < 5) {
						try {
							System.out.println("Joined: " + entry.id);
							bw.write(entry.id + ": Joined at " + new Date(currentTime));
							bw.newLine();
							bw.flush();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					if (entry.hrtBeat > 1) {
						//Spaghetti code.
						return;
					}
				}
				// Only the first to be introduced need to send all of his
				// table to joinee, rest will be taken up by joinee in Tx,
				// as the rest of the members are not waiting more than
				// joinee can reach some form of gossip to them.
				try {
					//lock.lock();
					// This was added to take care of corner case when first member joins
					// and the intro itself is the first join.
					selfEntry.hrtBeat++;
					selfEntry.incHrtBeat();
					//System.out.println("Tx Intro to " + entry.id);
					txObj.SendMembList(entry);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} finally {
					//lock.unlock();
				}
			}
		}
	}
	@Override
	public void run() {
		//System.out.println("Recieve: Running");
		// TODO Auto-generated method stub
		while(true) {	
			byte[] buf = new byte[4096];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			try {
				socket.receive(packet);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(processPacket(packet)) {
				//System.out.println("Recieve: Done");
				break;
			}
		}
	}
}
