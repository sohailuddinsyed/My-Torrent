import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Random;

public class SelectNeighbors extends Thread {
    private peerProcess host_peer;

    // SelectNeighbors constructor 
    public SelectNeighbors(peerProcess host_peer) {
        this.host_peer = host_peer;
    }
    public void run() {
        // Read the time interval 'P' and preferred neighbors 'K'
        float p = Float.parseFloat(host_peer.config_params.get("UnchokingInterval"));
        int k = Integer.parseInt(host_peer.config_params.get("NumberOfPreferredNeighbors"));
        try {
            while(true) {
                // If there is atleast one interested neighbor proceed to select min(k, interested)
                if(host_peer.neighbors_interested_in_host != null) {

                    // Random generator for the case when host has file
                    Random random = new Random();
                    boolean host_has_file = host_peer.host_details.has_file;

                    // Store all interested neighbors in a list
                    ArrayList<Integer> interested_neighs = new ArrayList<>();
                    
                    // Find all interested neighbors
                    for(Map.Entry<Integer, Boolean> entry : host_peer.neighbors_interested_in_host.entrySet()) {
                        int peer_id = entry.getKey();
                        boolean interested = entry.getValue();
                        if(interested) 
                            interested_neighs.add(peer_id);
                    }

                    // Calculate download rate for all neighbors
                    int interested_count = interested_neighs.size();
                    
                    float[][] download_rates = new float[interested_count][2];

                    // If host has file, download rate is randomly assigned else computed
                    int i = 0;
                    for(int peer_id: interested_neighs) {
                        download_rates[i][0] = peer_id;
                        download_rates[i][1] = host_has_file ?
                        random.nextInt(1000) :
                        host_peer.neighbor_downloads.getOrDefault(peer_id, 0)/p;
                        i++;
                    }
                    
                    // Sort on download rates in descending order
                    Arrays.sort(download_rates, Comparator.comparing(pair -> pair[1], Comparator.reverseOrder()));

                    // Unchoke top 'k' peers and choke remaining
                    for(i = 0; i < interested_count; i++) {
                        int peer_id = (int) download_rates[i][0];
                        host_peer.unchoked_by_host.put(peer_id, (i < k) ? true : false);
                    }

                    // Generate log
                    String unchoke_list = "";
                    for(Map.Entry<Integer, Boolean> entry : host_peer.unchoked_by_host.entrySet()) {
                        int peer_id = entry.getKey();
                        boolean unchoked = entry.getValue();
                        if(unchoked) 
                            unchoke_list += " " + peer_id;
                    }
                    if(unchoke_list.length() > 0)
                        host_peer.logger.log("has the preferred neighbors" + unchoke_list);
                    
                    // Clear downloads to record new values for the next interval
                    host_peer.neighbor_downloads.clear();
                    
                    // Sleep for interval 'p'
                    Thread.sleep((int)p * 1000);
                }
            }
        } catch(InterruptedException e) {
            System.out.println("Exception in SelectNeighbors, maybe sleep()");
            e.printStackTrace();
        }
    }
}
