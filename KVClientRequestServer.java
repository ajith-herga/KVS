import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class KVClientRequestServer extends Thread {
	Socket clientSocket = null; 
	ServerSocket serverSocket = null;
	List<Worker> workers = new ArrayList<Worker>();
    
	KVClientRequestServer(ServerSocket KVClientSock) {
		this.serverSocket = KVClientSock;
	}

	public class Worker extends Thread {
		
		Socket clientSocket;
		
		public Worker(Socket clientSocket) {
		    this.clientSocket = clientSocket;
		}
		
		public void run() {
			PrintWriter out = null;
			BufferedReader in = null;
			try {
				String inputLine;
				out = new PrintWriter(clientSocket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				if ((inputLine = in.readLine()) != null) {
					//System.out.printf("KVWorker: Received %s\n", inputLine);
					if (inputLine.startsWith("grep ")) {
						
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				// Going to return anyway
			} finally {
				if (out != null)
					out.close();
				try {
					if (in != null) 
						in.close();
					if (clientSocket != null)
						clientSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
					//Going to return anyway
				}
			}
		}
	}
    
	public void run() {
		while (true) {
			try {
			    clientSocket = serverSocket.accept();
			} 
			catch (IOException e) {
			    System.out.printf("Accept failed for main socket %d", serverSocket.getLocalPort());
			    System.exit(-1);
			}
			//System.out.println("KvAcceptor: Got Connection");
			Worker worker = new Worker(clientSocket);
			worker.start();
			workers.add(worker);
		}
	}

}

