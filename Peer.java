import java.net.*;
import java.util.HashMap;

public class Peer
{
    public InetAddress address;
    public int port;
    public HashMap<String, String> resources;

    public Peer()
    {
        resources = new HashMap<>();
    }

    public Peer(InetAddress address, int port, HashMap<String, String> resources)
    {
        this.address = address;
        this.port = port;
        this.resources = resources;
    }

    @Override
    public String toString() {
        String result = "";

        result += address + " " + port + " ->\n";

        for(String key : resources.keySet()) {
            result += key + "\tHash: " + resources.get(key) + "\n";
        }

        return result;
    }

    public String name() {
        return address + " on port " + port;
    }
}