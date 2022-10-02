import java.io.*;
import java.nio.*;
import java.net.*;
import java.util.*;
import java.security.*;

public class p2pPeerClient extends Thread 
{
	protected DatagramSocket socket = null;
	protected InetAddress addr = null;
	protected byte[] response = new byte[32*1024];
	protected int port;
	/*-------------------------------------*/
	protected p2pServerInterface serverInterface;
	protected String resourceDirectory;

	public p2pPeerClient(InetAddress localAddress, int port, p2pServerInterface serverIf, String resourceDirectory) throws IOException {
		this.port = port + 101;
		this.addr = localAddress;
		socket = new DatagramSocket(this.port);
		this.resourceDirectory = "arquivos_receive";
		serverInterface = serverIf;
	}

	private enum inputType {
		LIST, LIST_SEARCH, FILE, INVALID;
	}

	public void run() 
	{
		BufferedReader obj = new BufferedReader(new InputStreamReader(System.in));
		String str = "";	

		while (true) 
		{
			inputType curType = inputType.INVALID;

			System.out.println("\n<list> <search_term>(optional)");
			System.out.println("\n<peer> <file_name> <peer_ip> <peer_port>");
			System.out.println("Example: list");
			System.out.println("Example: list HD");
			System.out.println("Example: peer file.txt <peer_ip> <port>");
			try 
			{
				str = obj.readLine();
				String vars[] = str.split("\\s");

				if(vars[0].equals("list")) 
				{
					if(vars.length == 1) 
					{
						curType = inputType.LIST;
					} 
					else 
					{
						curType = inputType.LIST_SEARCH;
					}
				} 
				else if(vars[0].equals("peer")) 
				{
					curType = inputType.FILE;
				}

				switch(curType) 
				{
					case LIST:
						ArrayList<String> recursos = serverInterface.listResources(null);
						System.out.println("-=-=-=-\nResources: \n");
						System.out.println(recursos);
						/*for(Peer p : recursos) {
							System.out.println(p.toString());
						}*/
						break;
					case LIST_SEARCH:
						ArrayList<String> recursosSearch = serverInterface.listResources(vars[1]);
						System.out.println("-=-=-=-\nResources with term \"" + vars[1] + "\": \n");
						System.out.println(recursosSearch);
						/*for(Peer p : recursosSearch) {
							System.out.println(p.toString());
						}*/
						break;
					case FILE:
						DatagramPacket fileRequest = new DatagramPacket(vars[1].getBytes(), vars[1].getBytes().length, InetAddress.getByName(vars[2]), Integer.parseInt(vars[3]));
						socket.setSoTimeout(10000);
						socket.send(fileRequest);


						DatagramPacket fileResponse = new DatagramPacket(response, response.length);
						socket.receive(fileResponse);

						String pathToFile = resourceDirectory + "/" + vars[1];
						File toWrite = new File(pathToFile);
						toWrite.createNewFile();

						FileOutputStream writer = new FileOutputStream(pathToFile);
						
						ByteBuffer fileBuffer = ByteBuffer.allocate(32*1024);
						fileBuffer.put(fileResponse.getData());
						fileBuffer.flip(); // need flip
						
						//get file size
						long fileSize = fileBuffer.getLong();
						
						//get file hash
						String fileHash = "";
						for(int i=0; i<32; i++) {
							fileHash += fileBuffer.getChar();
						}

						byte[] fileData = new byte[(int)fileSize];
						int aux = 0;
						int i = 0;
						int posicInicialData = fileBuffer.position();
						//get file data
						for(i = posicInicialData; i<posicInicialData+fileSize; i++) {
							//System.out.println(fileBuffer.position());
							fileData[aux] = fileBuffer.get();
							aux++;
						}

						MessageDigest md = MessageDigest.getInstance("MD5");
						md.update(fileData);
						byte[] digest = md.digest();
						
						StringBuilder result = new StringBuilder();
						for (byte aByte : digest) {
							result.append(String.format("%02X", aByte));
						}

						//System.out.println(result.toString());

						if(result.toString().equals(fileHash)) {
							System.out.println("File " + vars[1] + " written successfully");
						} else {
							System.out.println("File " + vars[1] + " hash check failed");
						}

						//write file
						writer.write(fileData);
						writer.close();
						break;
					case INVALID:
						System.out.println("\tError: Invalid \"" + vars[0] + "\" command\n");
						break;
					default:
				}

				/*addr = InetAddress.getByName(vars[2]);
				String str2 = vars[0] + " " + vars[1];
				resource = str2.getBytes();
				if (vars.length == 4) 
				{
					System.out.println("Sending message to peer on port " + vars[3]);
					peer_port = Integer.parseInt(vars[3]);
				} 
				else 
				{
					peer_port = 9000;
				}*/
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			catch (NoSuchAlgorithmException e)
			{
				e.printStackTrace();
			}
			
			//comentar? colocar if?
			/*try 
			{
				//packet = new DatagramPacket(resource, resource.length, addr, peer_port);
				socket.send(packet);
				
				while (true) 
				{
					try 
					{
						// obtem a resposta
						packet = new DatagramPacket(response, response.length);
						socket.setSoTimeout(500);
						socket.receive(packet);
						
						// mostra a resposta
						String resposta = new String(packet.getData(), 0, packet.getLength());
						System.out.println("recebido: " + resposta);
						//Salva o download
					} 
					catch (IOException e) 
					{
						break;
					}
				}
			} 
			catch (IOException e) 
			{
			}*/
		}
	}
}
