import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class KVTest {
	KVClientAPI kvC = null;
	KVClientResponse response = null;
	List<Long> latencies = null;
	public KVTest(String[] args) {
		kvC = new KVClientAPI(args[0], args[1]);
		latencies = new ArrayList<Long>(1000000);
	}
	
	public void Testcase1() {
		long time, diff1, key;
		response = kvC.lookup(0, 1);
		for (key = 0; key < 10000; key++) {
			time = System.currentTimeMillis();
			response = kvC.lookup(key, 1);
			diff1 = System.currentTimeMillis() - time;
			latencies.add(diff1);
		}
		genHistogram();
	}
	
	public void genHistogram() {
		System.out.println("Get histogram");
		Collections.sort(latencies);
		latencies = latencies.subList(5, latencies.size() - 5);
		int numBuck = 12;
		float min = latencies.get(0);
		float max = latencies.get(latencies.size() - 1);
		float bucketsize = (max - min)/numBuck;
		System.out.println(bucketsize);
		long bucket[] = new long[numBuck + 1];
		for (long lat: latencies) {
			int index = (int)((lat - min)/bucketsize);
			bucket[index]++;
		}
		
		for (long val: bucket) {
			System.out.println(val + "," + min + "," + (min + bucketsize));
			min += bucketsize;
		}
	}
	
	public static void main(String[] args) {
		KVTest kvT = new KVTest(args);
		kvT.Testcase1();
	}
}
