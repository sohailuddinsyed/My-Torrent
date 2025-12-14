import java.io.*;
import java.util.*;

import static java.lang.System.exit;

public class peerProcess {

    public Integer peer_id; // Current Host's peer id
    public PeerDetails host_details; // To store current peer details as PeerDetails object
    public HashMap<String, String> config_params; // Stores the Common.cfg parameters
    public HashMap<Integer, PeerDetails> neighbors_list; // All Neighbors stored as hashmap
    public ArrayList<Integer> previous_neighbors_ids; // List of Neighbors listed before current peer
    public HashMap<Integer, Boolean> neighbors_interested_in_host;
    public HashMap<Integer, Boolean> choked_by_neighbors;
    public HashMap<Integer, Boolean> unchoked_by_host;
    public HashMap<Integer, Integer> neighbor_downloads;
    public Set<Integer> requested_indices;
    private static PeerClient peer_client;
    private static PeerServer peer_server;
    private static SelectNeighbors select_neighbors;
    public Logger logger;
    public Integer no_of_pieces;
    public FileHandler file_handler;

    public peerProcess(int id) {
        peer_id                      = id;
        config_params                = new HashMap<>();
        neighbors_list               = new HashMap<>();
        previous_neighbors_ids       = new ArrayList<>();
        neighbors_interested_in_host = new HashMap<>();
        choked_by_neighbors          = new HashMap<>();
        unchoked_by_host             = new HashMap<>();
        requested_indices            = new HashSet<>();
        neighbor_downloads           = new HashMap<>();
        logger                       = new Logger(peer_id.toString());
        file_handler                 = new FileHandler(this);
    }

    // Method to read common.cfg and store values in a hashmap
    public void ReadCommonCfg() {
        try {
            String line;
            String[] line_split;
            BufferedReader file = new BufferedReader(new FileReader("Common.cfg"));
            while((line = file.readLine()) != null) {
                line_split = line.split(" ");
                // Store as a hashmap with key as parameter name and value as parameter's value
                config_params.put(line_split[0], line_split[1]);
            }
            file.close();
        }
        catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    // Method to read PeerInfo.cfg and store the peer information as PeerDetails object in a hashmap
    public void ReadPeerInfoCfg() {
        try {
            String line;
            int p_id;
            boolean found_peer = false;

            BufferedReader file = new BufferedReader(new FileReader("PeerInfo.cfg"));

            while((line = file.readLine()) != null) {
                // Peer information stored as PeerDetails object
                PeerDetails peer_details = new PeerDetails(line);
                p_id = Integer.parseInt(line.split(" ")[0]);
                if (!found_peer && p_id == peer_id) {
                    found_peer = true;
                    host_details  = peer_details;
                } else {
                    // Append previous_neighbors_ids only until we find current peer
                    if (!found_peer)
                        previous_neighbors_ids.add(p_id);
                    // All the neighbors information is stored in a hashmap
                    neighbors_list.put(p_id, peer_details);
                }
                choked_by_neighbors.put(peer_id, true);
                unchoked_by_host.put(peer_id, false);
            }
            file.close();
        }
        catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    // Method to Set the bit fields based on the file size and piece size
    public void SetBitField() {
        int file_size  = Integer.parseInt(config_params.get("FileSize"));
        int piece_size = Integer.parseInt(config_params.get("PieceSize"));
        no_of_pieces   = (int) Math.ceil((double)file_size/piece_size);
        BitSet bitfield_piece_index = new BitSet(no_of_pieces);

        // Sets all bit values to 1 if has_file is true else 0
        for(int i = 0; i < no_of_pieces; i++) {
            bitfield_piece_index.set(i, host_details.has_file);
        }
        host_details.bitfield_piece_index = bitfield_piece_index;
    }

    // file_handler handles converting file to pieces, copying and fetching pieces, constructing file from pieces
    public void HandleFile() throws IOException {
        if (host_details.has_file)
            file_handler.ConvertFileToPieces();
    }

    public static void main(String args[]) throws IOException {
        if (args.length < 1) {
            System.out.println("No arguments Passed. Exiting the Program");
            exit(1);
        }
        peerProcess peer = new peerProcess(Integer.parseInt(args[0]));
        // Read Common.cfg file
        peer.ReadCommonCfg();
        // Read PeerInfo.cfg file
        peer.ReadPeerInfoCfg();
        peer.SetBitField();
        peer.HandleFile();
        
        // Creating PeerClient and PeerServer object
        peer_client = new PeerClient(peer);
        peer_server = new PeerServer(peer);
        select_neighbors = new SelectNeighbors(peer);
        
        peer_client.start();
        peer_server.start();
        select_neighbors.start();
    }
}
