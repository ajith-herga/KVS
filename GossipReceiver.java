import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class GossipReceiver extends Thread {

    ConcurrentHashMap<String, TableEntry> membTable = null;
	BufferedWriter bw = null;
	TableEntry selfEntry = null;
    DatagramSocket socket = null;
    GossipTransmitter txObj = null;
    KVStore kvStore = null;
	List<ICommand> redirectCommands = null;

	public GossipReceiver(ConcurrentHashMap<String, TableEntry> membTable, BufferedWriter bw, TableEntry selfEntry, 
			DatagramSocket socket, GossipTransmitter txObj, KVStore kvStore, List<ICommand> redirectCommands) {
		this.membTable = membTable;
		this.bw = bw;
		this.selfEntry = selfEntry;
		this.socket = socket;
		this.txObj = txObj;
		this.kvStore = kvStore;
		this.redirectCommands = redirectCommands;
	}

	private boolean processPacket(DatagramPacket packet) {
		String rx = new String(packet.getData(), 0, packet.getLength());
		//System.out.println("Recieve: Got " + rx);
		Gson gson = new Gson();
		Type collectionType = new TypeToken<MarshalledServerData>(){}.getType();
		MarshalledServerData mR = gson.fromJson(rx,collectionType);
		if (mR.membTable != null) {
			ConcurrentHashMap<String, TableEntry> mT = mR.membTable; 
	        mergeMembTable(mT);
		} else if (mR.query != null) {
			IndirectLocalCommand inDir = new IndirectLocalCommand(mR.query, kvStore, txObj);
			inDir.execute();
		} else if (mR.reply != null) {
			synchronized(redirectCommands) {
				ICommand toRemove = null;
				for (ICommand comm: redirectCommands) {
					if (mR.reply.id == comm.getId()) {
						comm.callback(mR.reply);
						toRemove = comm;
					}
				}
				if (toRemove != null) {
					redirectCommands.remove(toRemove);
				} else {
					try {
						bw.write("Wrong answer to us for" + mR.reply.key);
						bw.newLine();
						bw.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} 
		} else if (mR.bulkQuery != null) {
			for (KVData data: mR.bulkQuery) {
				IndirectLocalCommandNoCallback inDir = new IndirectLocalCommandNoCallback(data, kvStore);
				inDir.execute();
			}
		}
		return false;
	}


	public void mergeMembTable(ConcurrentHashMap<String, TableEntry> temp) {
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
							checkAndSendKeystoTableEntry(entry);
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
	private void checkAndSendKeystoTableEntry(TableEntry entry) {
		RemoteMoveBulkCommand rem = new RemoteMoveBulkCommand(entry, txObj, kvStore, false);
		rem.execute();
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
