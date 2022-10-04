import java.io.*;
import java.net.*;
import java.rmi.RemoteException;

public class p2pPeerHeartbeat extends Thread {
	protected InetAddress localAddress = null;
	protected int port;
	p2pServerInterface serverIf;

	public p2pPeerHeartbeat(InetAddress localAddress, int port, p2pServerInterface serverIf) throws IOException 
	{
		this.localAddress = localAddress;
		this.port = port;
		this.serverIf = serverIf;
	}

	public void run() 
	{
		while (true) 
		{
			try 
			{
				serverIf.heartbeat(localAddress, port);
				Thread.sleep(10000);
			} 
			catch(InterruptedException e) 
			{
				
			}
			catch (RemoteException e)
			{
				e.printStackTrace();
			}
		}
	}
}
