import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.BitSet;

public class PeerDetails {
    public String hostname;
    public Boolean has_file;
    public int peer_id, peer_port;
    public BitSet bitfield_piece_index;
    Socket socket;
    ObjectOutputStream out;
    ObjectInputStream in;

    public PeerDetails(String line) {
        String[] line_split = line.split(" ");
        try {
            peer_id   = Integer.parseInt(line_split[0]);
            hostname  = line_split[1];
            peer_port = Integer.parseInt(line_split[2]);
            has_file  = line_split[3].equals("1");
        }
        catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }
}
