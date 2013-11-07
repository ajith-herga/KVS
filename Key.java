import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;


public class Key implements Comparable<Key> {
	long key = 0;
	String keyHash = null;
	public Key(long key) {
		MessageDigest md;
		this.key = key;
		try {
			md = MessageDigest.getInstance("SHA-256");
		    keyHash = Hex.encodeHexString(md.digest((key + "").getBytes()));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
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
		return ((other instanceof Key) && this.key==((Key)other).key); 
	}

}
