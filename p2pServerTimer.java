//import java.net.*;
import java.time.*;
import java.util.*;

public class p2pServerTimer extends Thread
{
    private HashMap<Peer, LocalDateTime> timers;
    private ArrayList<Peer> peers;
    public p2pServerTimer(HashMap<Peer, LocalDateTime> timers, ArrayList<Peer> peers)
    {
        this.timers = timers;
        this.peers = peers;
    }

    public void run()
    {
        while (true)
        {
            for (Peer peer : timers.keySet())
            {
                if (timers.get(peer).isBefore(LocalDateTime.now().minusSeconds(30)))
                {
                    System.out.println(peer.name() + " timed out");
                    timers.remove(peer);
                    peers.remove(peer);
                }
            }

            try
            {
                Thread.sleep(5000);
            }
            catch (InterruptedException e)
            {
                //do nothing
            }
        }
    }
}