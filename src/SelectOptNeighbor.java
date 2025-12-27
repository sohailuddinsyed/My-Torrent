import java.util.Map;
import java.util.Random;
import static java.lang.System.exit;

public class SelectOptNeighbor extends Thread{
    private peerProcess host_peer;

    // Constructor 
    public SelectOptNeighbor(peerProcess host_peer) {
        this.host_peer = host_peer;
    }

    public void run(){
        float m = Float.parseFloat(host_peer.config_params.get("OptimisticUnchokingInterval"));
        
        try {
            while(true) {
                // If there is atleast one interested neighbor proceed to select min(k, interested)
                if(host_peer.neighbors_interested_in_host != null) {

                    // Random generator for the case when host has file
                    Random random = new Random();
                    // boolean host_has_file = host_peer.host_details.has_file;
                    int max_rand=-1;
                    // Store opt neighbors 
                    int opt_neighbor=0;
                    
                    // Find all inetrested neighbors
                    for(Map.Entry<Integer, Boolean> entry : host_peer.neighbors_interested_in_host.entrySet()) {
                        int peer_id = entry.getKey();
                        boolean interested = entry.getValue();
                        if(interested){
                            int rand = random.nextInt(1000);
                            // Increasing priority if peer is not already optimistically selected
                            if(peer_id != host_peer.opt_neighbor){
                                rand = rand + 1000;
                                // Increasing priority if peer is choked
                                if(!host_peer.unchoked_by_host.get(peer_id)){
                                    rand = rand + 1000;
                                }
                            }
                            if (rand > max_rand){
                                opt_neighbor = peer_id;
                                max_rand = rand;
                            }
                        }
                    }
                    
                    // Setting optimistic neighbor
                    host_peer.opt_neighbor = opt_neighbor;
                    if(opt_neighbor != 0)
                        host_peer.logger.log("has the optimistically unchocked neighbor " + opt_neighbor);

                    // Terminate once host checks the file count
                    if(host_peer.host_details.has_file && host_peer.completed_peer_files >= host_peer.neighbors_list.size()){
                        exit(0);         
                    }


                    // Sleep for interval 'm'
                    Thread.sleep((int)m * 1000);
                }
            }
        } catch(InterruptedException e) {
            System.out.println("Exception in SelectOptNeighbor, maybe sleep()");
            e.printStackTrace();
        }
    }
    
}
