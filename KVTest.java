import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class KVTest {
	KVClientAPI kvC = null;
	KVClientResponse response = null;
	static volatile List<Long> latencies = null;
	static long start=8;

	public KVTest(String host, String port) {
		kvC = new KVClientAPI(host, port);
		latencies = new ArrayList<Long>(1000000);
	}
	
	public void Testcase1() {
		long time, diff1, key;
		response = kvC.lookup(0+"", 1);
		for (key = 0; key < 10000; key++) {
			time = System.currentTimeMillis();
			response = kvC.lookup(key+"", 1);
			diff1 = System.currentTimeMillis() - time;
			latencies.add(diff1);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//genHistogram();
	}
	
	public void Testcase2() {
		long time, diff1, key;
		//response = kvC.lookup(0);
		for (key =0; key < 1000; key++) {
			time = System.currentTimeMillis();
			response = kvC.lookup((long)(Math.random()*10000.0)+"",1);
			diff1 = System.currentTimeMillis() - time;
			latencies.add(diff1);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//genHistogram();
	}

	public void genHistogram() {
		//System.out.println("Get histogram");
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
			System.out.println(val);
			min += bucketsize;
		}
	}

	public void show() {
		System.out.println(kvC.showAll().kV.length);
	}
	
	public void Testcase3(int level, int st) {
		
		long time, diff1, key;
		response = kvC.lookup(0+"", level);

		/*
		 
		 latencies = new ArrayList<Long>(1000000);

		for (key = st+0; key < st+250; key++) {
			time = System.currentTimeMillis();
			response = kvC.lookup(key*2, level);
			diff1 = System.currentTimeMillis() - time;
			if(response.Status==false)
				System.out.println("Failure for key : " + key);
			latencies.add(diff1);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		//System.out.println("Latencies size : "+ latencies.size());
		/*
		
		System.out.println("************************************** || Histogram for read ||**************************************");
				
		genHistogram();
		
		latencies = new ArrayList<Long>(1000000);
		*/
		for (key = st+0; key < st+250; key++) {
			time = System.currentTimeMillis();
			response = kvC.modify((key*2)+"", (key+1000)+"" ,level);
			diff1 = System.currentTimeMillis() - time;
			latencies.add(diff1);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/*
		System.out.println("************************************** || Histogram for write(update) ||**************************************");

		genHistogram();*/
	}
	
	void insertKeys(int level){
		//insert 1000 keys
		long key;
		for (key = 0; key < 1000; key++) {
			response = kvC.insert(key+"", key+"", level);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	void removeKeys(int level){
		//remove all 1000 keys
		long key;

		for (key = 0; key < 1000; key++) {
			response = kvC.delete(key+"", level);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}
	
	public static void main(String[] args) {
		KVTest kvT = new KVTest("ubuntu", "1124");
		//kvT.insertKeys(1);

		Thread th1 = new Thread(new Runnable() {			
			@Override
			public void run() {
				KVTest kvT = new KVTest("ubuntu", "1124");
					kvT.Testcase3(3,250);		
				
			}
		});
		th1.start();
		Thread th2 = new Thread(new Runnable() {			
			@Override
			public void run() {
				KVTest kvT = new KVTest("ubuntu", "1125");
					kvT.Testcase3(3,0);		
				
			}
		});
		th2.start();
		try {
			th1.join();
			th2.join();
			while(latencies.size()!=500){
				Thread.sleep(1000L);
			}
			kvT.genHistogram();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*for(;start<10;start++) {
			kvT.Testcase1();
			//kvT.Testcase2();
			kvT.show();
			kvT = new KVTest("ubuntu", "1125");
			kvT.show();
			kvT = new KVTest("ubuntu", "1126");
			kvT.show();
			kvT = new KVTest("ubuntu", "1127");
			kvT.show();
		}*/
	}
}
