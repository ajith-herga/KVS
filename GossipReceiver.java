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
	List<ICommand> replicaCommands = null;

	public GossipReceiver(ConcurrentHashMap<String, TableEntry> membTable, BufferedWriter bw, 
			TableEntry selfEntry, 
			DatagramSocket socket, GossipTransmitter txObj, 
			KVStore kvStore, 
			List<ICommand> redirectCommands, List<ICommand> replicaCommands) {
		this.membTable = membTable;
		this.bw = bw;
		this.selfEntry = selfEntry;
		this.socket = socket;
		this.txObj = txObj;
		this.kvStore = kvStore;
		this.redirectCommands = redirectCommands;
		this.replicaCommands = replicaCommands;
	}

	private boolean processPacket(DatagramPacket packet) {
		String rx = new String(packet.getData(), 0, packet.getLength());
		Gson gson = new Gson();
		Type collectionType = new TypeToken<MarshalledServerData>(){}.getType();
		MarshalledServerData mR = gson.fromJson(rx,collectionType);
		if (mR.membTable == null) {
		    System.out.println("Recieve: Got " + rx);
		}
		if (mR.membTable != null) {
			ConcurrentHashMap<String, TableEntry> mT = mR.membTable; 
	        mergeMembTable(mT);
		} else if (mR.query != null) {
			IndirectLocalCommand inDir = new IndirectLocalCommand(mR.query, kvStore, txObj, membTable, selfEntry, replicaCommands);
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
				IndirectLocalCommandNoCallback inDir = new IndirectLocalCommandNoCallback(data, kvStore, txObj, membTable, selfEntry, replicaCommands);
				inDir.execute();
			}
		} else if (mR.replicaBulkReq != null) {
			ReplicaIndirectBulkCommand repl = new ReplicaIndirectBulkCommand(mR.replicaBulkReq, kvStore, txObj);
		} else if (mR.replicaBulkReply != null) {
			System.out.println("Inside" + replicaCommands.size());
			ICommand toRemove = null;
			for (ICommand comm: replicaCommands) {
				if (mR.replicaBulkReply.milisPrimary == comm.getId()) {
					comm.callback(null);
					toRemove = comm;
					break;
				}
			}
			if (toRemove != null) {
				replicaCommands.remove(toRemove);
			} else {
				try {
					bw.write("Wrong answer to us for time " + mR.replicaBulkReply.milisPrimary);
					bw.newLine();
					bw.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}			
		} else if (mR.replicaQuery !=null) {
			ReplicaIndirectCommand replInDir = new ReplicaIndirectCommand(mR.replicaQuery, kvStore, txObj);
			replInDir.execute();
		} else if (mR.replicaReply != null) {
			System.out.println("Inside" + replicaCommands.size());
			ICommand toRemove = null;
			for (ICommand comm: replicaCommands) {
				if (mR.replicaReply.milisPrimary == comm.getId()) {
					comm.callback(mR.replicaReply.data);
					toRemove = comm;
					break;
				}
			}
			if (toRemove != null) {
				replicaCommands.remove(toRemove);
			} else {
				try {
					bw.write("Wrong answer to us for" + mR.replicaReply.data.key);
					bw.newLine();
					bw.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}


	public void mergeMembTable(ConcurrentHashMap<String, TableEntry> temp) {
		long currentTime = System.currentTimeMillis();
		TableEntry[] sendoldReplicas = null;
		TableEntry[] sendnewReplicas = null;
		for (TableEntry entry: temp.values()) {
			if (entry.hasFailed) {
				continue;
			}
			if (membTable.containsKey(entry.id)) {
				//System.out.println("Known Machine");
				TableEntry oldEntry = membTable.get(entry.id);
				if (entry.checkLeave()) {
					sendoldReplicas = HashUtility.findReplicaforMachine(membTable, selfEntry.hashString);
				}
				
				if (oldEntry.cmpAndUpdateHrtBeat(entry.hrtBeat, currentTime)) {
					sendnewReplicas = HashUtility.findReplicaforMachine(membTable, selfEntry.hashString);
	    			sendKeysToNewReplica(sendnewReplicas, sendoldReplicas);
					try {
						bw.write(entry.id + ": Left   at " + new Date(currentTime));
						bw.newLine(); 
						bw.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}else{
				//Find my replicas before adding the new guy. This is useful for two things:
				// 1. find the keys my replicas are not supposed to have.
				// 2. These are also my old replicas in case this guy becomes my new designated replica.
				
				//System.out.println("New Entry: " + entry.id);
				if (entry.hrtBeat >= 1) {
					if (entry.hrtBeat < 5) {
						sendoldReplicas = HashUtility.findReplicaforMachine(membTable, selfEntry.hashString);
					}
				}
				membTable.put(entry.id, entry);
				entry.updateTime();
				
				
				if (entry.hrtBeat == 1) {
					// Only the first to be introduced need to send all of his
					// table to dest, rest will be taken up by dest in Tx,
					// as the rest of the members are not waiting more than
					// dest can reach some form of gossip to them.
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
				
				if (entry.hrtBeat >= 1) {
					if (entry.hrtBeat < 5) {
						try {
							sendnewReplicas = HashUtility.findReplicaforMachine(membTable, selfEntry.hashString);
							System.out.println("Joined: " + entry.id);
							KVData[] replicaData = kvStore.getKVDataForMachine(entry, KVCommands.DELETEKV);
							KVData[] destData = kvStore.getKVDataForMachine(entry, KVCommands.INSERTKV);
							MoveKeysToNewDest check = new MoveKeysToNewDest(sendoldReplicas,entry, selfEntry, txObj, kvStore, replicaCommands, replicaData, destData);
							check.execute();
							//Callback for check called by object within check.
							SwapKVwithinReplicas swap = new SwapKVwithinReplicas(sendoldReplicas, sendnewReplicas, txObj, kvStore, selfEntry, replicaCommands);
							swap.execute();
							//Callback for swap called by object within check.
							
							bw.write(entry.id + ": Joined at " + new Date(currentTime));
							bw.newLine();
							bw.flush();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	

	private void sendKeysToNewReplica(TableEntry[] newReplicas, TableEntry[] oldReplicas) {
		
		if (newReplicas == null || oldReplicas == null) {
			return;
		}
		TableEntry[] SrcDest = new TableEntry[2];
		if (newReplicas[0] == oldReplicas[0]) {
			if (newReplicas[1] != oldReplicas[1]) {
				SrcDest[0] = oldReplicas[1];
				SrcDest[1] = newReplicas[1];
			}
		} else if (newReplicas[1] == oldReplicas[1]) {
			if (newReplicas[0] != oldReplicas[0]) {
				SrcDest[0] = oldReplicas[0];
				SrcDest[1] = newReplicas[0];
			}
		} else if (newReplicas[1] == oldReplicas[0]) {
			if (newReplicas[0] != oldReplicas[1]) {
				SrcDest[0] = oldReplicas[1];
				SrcDest[1] = newReplicas[0];
			}
		} else if (newReplicas[0] == oldReplicas[1]) {
			if (newReplicas[1] != oldReplicas[0]) {
				SrcDest[0] = oldReplicas[0];
				SrcDest[1] = newReplicas[1];
			}
		}
		
		if (SrcDest[0] == null) {
			System.out.println("No difference in locality");
			return;
		} else {
		    KVData[] dataAdd = kvStore.getKVDataForMachine(selfEntry, KVCommands.INSERTKV);
		    if (dataAdd == null) {
		    	return;
		    }
		    ReplicaBulkAddOrDelRemote replicaDest = new ReplicaBulkAddOrDelRemote(SrcDest[1], selfEntry, txObj, dataAdd, null);
		    replicaDest.execute();
		    //No call back will be called as you are not there in the replicalist.
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
