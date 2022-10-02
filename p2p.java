import java.io.*;
import java.util.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;
import java.time.*;
import java.net.InetAddress;

public class p2p extends UnicastRemoteObject implements p2pServerInterface
{
	private volatile ArrayList<Peer> peers;
	private volatile HashMap<Peer, LocalDateTime> timers;


    //arrumar
	public p2p() throws RemoteException   
	{
		peers = new ArrayList<>();
		timers = new HashMap<>();
		new p2pServerTimer(timers, peers).start();
	}

   

	public static void main(String[] args) throws IOException 
	{
		
        if (args.length != 1 || args.length != 3) 
		{
            System.out.println(args[0]);
			System.out.println("Usage: java p2p Server <server_ip> || java p2p Peer <server_address> <local_address> <localport>");     
			System.exit(1);
		}


        //fazer condicionais pra modo server e pra modo peer
        if(args[0].equals("Server")){
            server(args[1]);
        }else if(args[0].equals("Peer")){
            peer(args[1], args[2], args[3]);
        }
		
	}

    public static synchronized void server(String server_ip){
        try 
		{
			System.setProperty("java.rmi.server.hostname", server_ip);
			LocateRegistry.createRegistry(52369);
			System.out.println("java RMI registry created.");
		} 
		catch (RemoteException e) 
		{
			System.out.println("java RMI registry already exists.");
		}

		try 
		{
			String server = "rmi://" + server_ip + ":52369/server_if";
			Naming.rebind(server, new p2p());
			System.out.println("Server is ready.");
		} 
		catch (Exception e) 
		{
			System.out.println("Serverfailed: " + e);
		}
    }
	
	public synchronized void heartbeat(InetAddress source, int port) 
	{
		System.out.println("heartbeat from " + source + ":" + port);
		Peer currentPeer = null;
		for (Peer p : peers)
		{
			if (p.address.equals(source) && p.port == port)
			{
				currentPeer = p;
			}
		}
		try
		{
			if (currentPeer == null)
			{
				throw (new Exception("peer not found"));
			}
			timers.replace(currentPeer, LocalDateTime.now());
		}
		catch (Exception e)
		{
			System.out.println("Source for heartbeat does not exist in registered peers");
		}
	}

	public synchronized void registerResource(InetAddress source, int port, String resourceName, String resourceHash)
	{ 
		System.out.println("Resource " + resourceName + " from " + source + " on port " + port + " with hash " + resourceHash);
		Peer currentPeer = null;
		for (Peer p : peers)
		{
			if (p.address.equals(source) && p.port == port)
			{
				currentPeer = p;
			}
		}

		if (currentPeer == null)
		{
			currentPeer = new Peer();
			currentPeer.address = source;
			currentPeer.port = port;
			peers.add(currentPeer);
			timers.put(currentPeer, LocalDateTime.now());
		}
		
		if (!currentPeer.resources.containsKey(resourceName))
		{
			currentPeer.resources.put(resourceName, resourceHash);
		}
		
		System.out.println(peers);
	}

	// Retornar lista de peers
	public synchronized ArrayList<String> listResources(String nomeRecurso)
	{
		System.out.println("Resource list request received");
		ArrayList<String> result = new ArrayList<String>();

		for(Peer p : peers) 

		{
			for(String recurso : p.resources.keySet()) 
			{
				if(nomeRecurso != null) {
					if(recurso.contains(nomeRecurso)) 
					{
						result.add(p.toString());
						break;
					}
				} 
				else
				{
					result.add(p.toString());
					break;
				}
			}
		}

		System.out.println(result);
		return result;
	}


    public static synchronized void peer(String server_address, String local_address, String localport) throws IOException{
        
        String remoteHostName = server_address;
        String connectLocation = "rmi://" + remoteHostName + ":52369/server_if";

        p2pServerInterface serverIf = null;
        try 
        {
            System.out.println("Connecting to server at : " + connectLocation);
            serverIf = (p2pServerInterface) Naming.lookup(connectLocation);
        } 
        catch (Exception e) 
        {
            System.out.println ("Client failed: ");
            e.printStackTrace();
        }

        String resourceDirectory = "arquivos";
        InetAddress localAddress = InetAddress.getByName(local_address);
        int port = Integer.parseInt(localport);

        new p2pPeerThread(localAddress, port, serverIf).start();
        new p2pPeerHeartbeat(localAddress, port, serverIf).start();
        new p2pPeerClient(localAddress, port, serverIf, resourceDirectory).start();
       
    }


}
