import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.BitSet;

public class PeerDetails {
    public String hostname; // hostname of the peer
    public Boolean has_file; // True if the peer has complete file else False
    public int peer_id, peer_port; // Peer ID and port of the peer
    public BitSet bitfield_piece_index; // BitField of the peer
    public ArrayList<Integer> latest_piece;
    Socket socket; // Socket through which current host connected to this peer
    DataOutputStream out; // Socket's DataOutputStream
    DataInputStream in; // Socket's DataInputStream

    // Stores the initial details pulled from peerInfo.cfg file
    public PeerDetails(String line) {
        String[] line_split = line.split(" ");
        try {
            peer_id   = Integer.parseInt(line_split[0]);
            hostname  = line_split[1];
            peer_port = Integer.parseInt(line_split[2]);
            has_file  = line_split[3].equals("1");
            latest_piece = new ArrayList<>();
        }
        catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }
}
