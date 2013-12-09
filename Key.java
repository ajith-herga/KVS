import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;


public class Key implements Comparable<Key> {
	String key = null;
	String keyHash = null;
	long timestamp = 0;
	public Key(String key, long timestamp) {
		MessageDigest md;
		this.key = key;
		this.timestamp = timestamp;
		try {
			md = MessageDigest.getInstance("SHA-256");
		    keyHash = Hex.encodeHexString(md.digest((key + "").getBytes()));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public boolean canReplace(Key o) {
		return (o.timestamp >= this.timestamp);
	}
	@Override
	public int compareTo(Key o) {
		return keyHash.compareTo(o.keyHash);
	}
	
	@Override
	public int hashCode(){
		return keyHash.hashCode();
	}

	@Override
	public boolean equals(Object other){
		return ((other instanceof Key) && this.key.equals(((Key)other).key)); 
	}

}
