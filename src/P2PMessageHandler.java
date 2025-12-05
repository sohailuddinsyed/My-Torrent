import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.BitSet;

public class P2PMessageHandler {
    PeerDetails curr_peer; // Current host
    PeerDetails neighbor_peer; // Neighbor peer to which the current TCP is established

    public P2PMessageHandler(PeerDetails curr_peer, PeerDetails neighbor_peer) {
        this.curr_peer     = curr_peer;
        this.neighbor_peer = neighbor_peer;
    }

    // Method to handle BitField Message received from Neighbor
    public void HandleBitFieldMessage(Message message) {
        // Create an empty bitset of same length as payload
        BitSet peer_bitset                 = new BitSet(message.GetMessagePayload().length * 8); // 8 bits in a byte
        // Retrieve the message payload and store it in neighbor_peer object
        byte[] message_payload             = message.GetMessagePayload();
        neighbor_peer.bitfield_piece_index = BitSet.valueOf(message_payload);

        // Set the peer bitfield from bitfield index payload
        for (int i = 0; i < message.GetMessageLength(); i++) {
            // Parse each byte of the bitfield message
            for (int j = 0; j < 8; j++) {
                if ((message_payload[i] & (1 << j)) != 0) {
                    peer_bitset.set(i * 8 + j);
                }
            }
        }
        // Compare with current host's bitfield_piece_index
        BitSet copy = (BitSet) curr_peer.bitfield_piece_index.clone();
        // Below will set the bit fields as one if there's any missing piece available in neighbor
        copy.andNot(peer_bitset);

        // Send Interested if the above result is not empty else send NotInterested message
        byte msg_type = !copy.isEmpty() ? (byte)2 : (byte)3;

        // Make third argument in Message as None and avoid sending third argument?
        Message msg = new Message(0, msg_type, new byte[0]);
        Utils.sendMessage(msg.BuildMessageByteArray(), neighbor_peer.out);
    }

    public void HandleChokeMessage() {
        // To-do
    }

    public void HandleUnChokeMessage() {
        // To-do
    }

    public void HandleInterestedMessage() {
        // To-do
    }

    public void HandleNotInterestedMessage() {
        // To-do
    }

    // Handler for 'have' message type
    public void HandleHaveMessage(Message message_received) {
        // Set the bitfield index received from the neighbor_peer
        int bitfield_index = Integer.parseInt(message_received.GetMessagePayload().toString());
        neighbor_peer.bitfield_piece_index.set(bitfield_index);

        // Check if interested
        boolean send_interested = Utils.CheckInterestInIndex(curr_peer, neighbor_peer, bitfield_index);
        if(send_interested) {
            // If interested send 'interested' message type
            Message interest_msg = new Message(0, (byte)2, new byte[0]);
            Utils.sendMessage(interest_msg.BuildMessageByteArray(), neighbor_peer.out);
            } else {
            // If not interested send 'not interested' message type
            Message not_interested_msg = new Message(0, (byte)3, new byte[0]);
            Utils.sendMessage(not_interested_msg.BuildMessageByteArray(), neighbor_peer.out);
            }
    }

    public void HandleRequestMessage() {
        // To-do
    }

    public void HandlePieceMessage() {
        // To-do
    }

    public void MessageListener() throws IOException {
        DataInputStream in = neighbor_peer.in;
        MessageType msg_type;
        while (true) {
            // Receive message and retrieve the message type
            Message message_received = new Message((byte[]) in.readAllBytes());
            msg_type = message_received.GetMessageType();

            // Take Action based on message type received
            switch(msg_type) {
                case CHOKE: {
                    HandleChokeMessage();
                    break;
                }
                case UNCHOKE: {
                    HandleUnChokeMessage();
                    break;
                }
                case INTERESTED: {
                    HandleInterestedMessage();
                    break;
                }
                case NOTINTERESTED: {
                    HandleNotInterestedMessage();
                    break;
                }
                case HAVE: {
                    HandleHaveMessage(message_received);
                    break;
                }
                case BITFIELD: {
                    HandleBitFieldMessage(message_received);
                    break;
                }
                case REQUEST: {
                    HandleRequestMessage();
                    break;
                }
                case PIECE: {
                    HandlePieceMessage();
                    break;
                }
                default: ;
            }
        }
    }
}
