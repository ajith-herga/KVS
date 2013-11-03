import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;


public class Key implements Comparable<Key> {
	long key = 0;
	String keyHash = null;
	public Key(long key) {
		MessageDigest md;
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
	
}
