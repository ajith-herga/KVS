import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

public class TableEntry implements Comparable<TableEntry> {
	String id = null;
	String hashString = null;
	long hrtBeat, jiffies;
	boolean hasFailed;
	long deadCount = Constants.DEADCOUNT;
	
	TableEntry(String id, long hrtBeat){
		this.id = id;
		this.hrtBeat = hrtBeat;
		jiffies = System.currentTimeMillis();
		hasFailed = false;
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-256");
			hashString = Hex.encodeHexString(md.digest(id.getBytes()));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

	}
	
	public synchronized void updateTime(){
		this.jiffies = System.currentTimeMillis();
	}
	
	public synchronized void incHrtBeat() {
		this.hrtBeat++;
		updateTime();
  	}
	
	public synchronized boolean cmpAndUpdateHrtBeat(long hrtBeat, long currentTime) {
		if (hrtBeat == 0) {
			hasFailed = true;
			System.out.println("Machine marked as Left : "+ id);
			return true;
		}
		if (hrtBeat <= this.hrtBeat || hasFailed) {
			return false;
		}
		//System.out.println("Changing heartbeat");
		this.hrtBeat = hrtBeat;
		this.jiffies = currentTime;
		return false;
	}
	
	public synchronized int timerCheck(long currentTime) {
		if (!hasFailed && currentTime > jiffies + Constants.TIMEOUT) {
			hasFailed = true;
			System.out.println("Machine marked as Failed : "+ id);
			return 0;
		}
		if (hasFailed) {
			//System.out.println("Deadcount: "+ deadCount);
			if (deadCount == 0) {
				return 1;
			} else {
				deadCount--;
			}
		}
		return -1;
	}

	@Override
	public int compareTo(TableEntry o) {
		return this.hashString.compareTo(o.hashString);
	}
}
