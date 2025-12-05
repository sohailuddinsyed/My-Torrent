import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

// Class to maintain Helper Methods that can be used by other classes
public class Utils {

    // To send a message of type byte[] through a socket's DataOutputStream
    static void sendMessage(byte[] msg, DataOutputStream out)
    {
        try{
            //stream write the message
            out.write(msg);
            out.flush();
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
    }

    // Check if curr_peer is interested in the received bitfield index
    public static boolean CheckInterestInIndex(PeerDetails curr_peer, PeerDetails neighbor_peer, int bitfield_index) {
        return !curr_peer.bitfield_piece_index.get(bitfield_index) && 
        neighbor_peer.bitfield_piece_index.get(bitfield_index);
    }

    // Check if curr_peer is interested in any bitfield index
    public static boolean CheckInterest(PeerDetails curr_peer, PeerDetails neighbor_peer) {
        int bit_field_size = curr_peer.bitfield_piece_index.size();
        // compare the entire bitfield 
        for(int i = 0; i < bit_field_size; i++) {
            if(!curr_peer.bitfield_piece_index.get(i) && neighbor_peer.bitfield_piece_index.get(i)) {
                return true;
            }
        }
        return false;
    }
}
