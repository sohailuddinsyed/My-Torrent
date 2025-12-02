import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;

public class peerProcess {

    private static int peer_id, peer_port;
    private static HashMap<String, String> config_params = new HashMap<>();
    private static HashMap<Integer, PeerDetails> neighbors_list = new HashMap<>();
    private static ArrayList<Integer> previous_neighbors_ids = new ArrayList<Integer>();
    private static boolean has_file = false;
    private static boolean is_first_peer = false;
    public peerProcess(int id) {
        peer_id = id;
    }

    public void ReadCommonCfg() {
        try {
            String line;
            String[] line_split;
            BufferedReader file = new BufferedReader(new FileReader("Common.cfg"));
            while((line = file.readLine()) != null) {
                line_split = line.split(" ");
                config_params.put(line_split[0], line_split[1]);
            }
        }
        catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    public void PrintConfigParams() {
        System.out.println(config_params.get("NumberOfPreferredNeighbors"));
        System.out.println(config_params.get("UnchokingInterval"));
        System.out.println(config_params.get("OptimisticUnchokingInterval"));
        System.out.println(config_params.get("FileName"));
        System.out.println(config_params.get("FileSize"));
        System.out.println(config_params.get("PieceSize"));
    }

    public void ReadPeerInfoCfg() {
        try {
            String line;
            String[] line_split;
            boolean found_peer = false;

            BufferedReader file = new BufferedReader(new FileReader("PeerInfo.cfg"));

            while((line = file.readLine()) != null) {
                line_split = line.split(" ");
                if (!found_peer && Integer.valueOf(line_split[0]) == peer_id) {
                    found_peer = true;
                    peer_port = Integer.valueOf(line_split[2]);
                    has_file = line_split[3].equals("1");
                    System.out.println(line_split[3]);
                    System.out.println(has_file);
                } else {
                    if (!found_peer)
                        previous_neighbors_ids.add(Integer.valueOf(line_split[0]));
                    PeerDetails peer_details = new PeerDetails(line);
                    neighbors_list.put(Integer.valueOf(line_split[0]), peer_details);
                }
            }
        }
        catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    public static void main(String args[]) {
        peerProcess peer = new peerProcess(Integer.valueOf(args[0]));
        peer.ReadCommonCfg();
        peer.ReadPeerInfoCfg();
        PeerClient peer_client = new PeerClient(peer_id, neighbors_list, previous_neighbors_ids);
    }
}
