
public class KVServer {
	public static void main(String[] args) {
		//System.out.println("Main: Begin");
		final GrepServer grepServ = new GrepServer();
		System.out.println("Server: Port Acquired");
		grepServ.startrun();
		final GossipServer gossipServer = new GossipServer(args, grepServ.localPort);
		System.out.println("Server:started ");		
		Runtime.getRuntime().addShutdownHook(new Thread(){
			@Override
			public void run(){
				System.out.println("Server: Trying to stop ");		
				gossipServer.shutdown();
				grepServ.shutdown();
			}
		});
		System.out.println("Server: Shutdown hook attached");		
		//System.out.println("Main: Done");
		while(true){
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				System.out.println("Main interrupted!");
				e.printStackTrace();
			}
		}
	}
}
