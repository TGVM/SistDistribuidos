import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.rmi.RemoteException;
import java.net.*;
import java.util.*;
import java.security.*;

public class p2pPeerThread extends Thread {
	protected DatagramSocket socket = null;
	protected InetAddress addr = null;
	protected byte[] response = new byte[32*1024];
	protected int port;
	protected String[] vars;
	p2pServerInterface serverInterface;
	String fileDirectory;

	public p2pPeerThread(InetAddress localAddress, int port, p2pServerInterface serverInterface) throws IOException 
	{
		addr = localAddress;
		this.port = port;
		socket = new DatagramSocket(port);
		this.serverInterface = serverInterface;
		fileDirectory = "arquivos";
	}

	public void run() 
	{
		HashMap<String,String> hashTable = new HashMap<>();
		try 
		{			
			//-=-= Criar hash para cada arquivo =-=-
			
			// Todos arquivos na pasta "arquivos"
			File file = new File(fileDirectory);
			String[] fileList = file.list();
		
			//Cria hash para cada arquivo
			for(String str : fileList) 
			{
				hashTable.put(str, generateFileHash(fileDirectory + "/" + str)); //if windows use "\\"
			}
			
			//Cria datagrama para cada arquivo
			for(String key : hashTable.keySet()) 
			{
				serverInterface.registerResource(addr, port, key, hashTable.get(key));
			}	
		}
			
		catch(RemoteException e)
		{
			e.printStackTrace();
		}

		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
		}
		
		while (true) 
		{
			try 
			{
				// obtem a resposta
				// Espera comunicação de outros peers
				DatagramPacket packet = new DatagramPacket(response, response.length);
				socket.setSoTimeout(500);
				socket.receive(packet);
				
				// mostra a resposta
				String data = new String(packet.getData(), 0, packet.getLength());
				System.out.println("/nfile request: " + data + " requested by " + packet.getAddress());
				//Envio do arquivo
				File file = new File(fileDirectory + "/" + data);		

				byte[] fileBytes = Files.readAllBytes(file.toPath());
				
				ByteBuffer buffer = ByteBuffer.allocate(32*1024);
				// write file length
				buffer.putLong(file.length());
				
				//write file hash
				for(int i=0; i<hashTable.get(data).length(); i++) {
					buffer.putChar(hashTable.get(data).charAt(i));
				}

				//write file data
				for(int i=0; i<fileBytes.length; i++) {
					buffer.put(fileBytes[i]);
				}

				socket.send(new DatagramPacket(buffer.array(), buffer.array().length, packet.getAddress(), packet.getPort()));
			}
			catch (FileNotFoundException e)
			{
				System.out.println("Requested file does not exist");
			} 

			catch (IOException e) 
			{
//				
			}
		}

	}


	// https://www.baeldung.com/java-md5
	//
	// https://mkyong.com/java/java-how-to-convert-bytes-to-hex/
	// 
	public String generateFileHash(String fileName) throws NoSuchAlgorithmException, IOException 
	{			
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(Files.readAllBytes(Paths.get(fileName)));
		byte[] digest = md.digest();
		
		StringBuilder result = new StringBuilder();
        for (byte aByte : digest) {
            result.append(String.format("%02X", aByte));
        }
        return result.toString();
			
	}
}