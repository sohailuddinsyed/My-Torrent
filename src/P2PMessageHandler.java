import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.BitSet;

public class P2PMessageHandler {
    PeerDetails curr_peer;
    PeerDetails neighbor_peer;
    public P2PMessageHandler(PeerDetails curr_peer, PeerDetails neighbor_peer) {
        this.curr_peer     = curr_peer;
        this.neighbor_peer = neighbor_peer;
    }

    public void HandleBitFieldMessage(Message message) {
        BitSet peer_bitset = new BitSet(message.GetMessageLength() * 8); // 8 bits in a byte
        byte[] message_payload = message.GetMessagePayload();

        for (int i = 0; i < message.GetMessageLength(); i++) {
            for (int j = 0; j < 8; j++) {
                if ((message_payload[i] & (1 << j)) != 0) {
                    peer_bitset.set(i * 8 + j);
                }
            }
        }
        BitSet copy = (BitSet) curr_peer.bitfield_piece_index.clone();
        copy.andNot(peer_bitset);

        byte msg_type = !copy.isEmpty() ? (byte)2 : (byte)3;

        // Make third argument in Message as None and avoid sending third argument?
        Message msg = new Message(0, msg_type, new byte[0]);
        HelperMethods.sendMessage(msg.BuildMessageByteArray(), neighbor_peer.out);
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

    public void HandleHaveMessage() {
        // To-do
    }

    public void HandleRequestMessage() {
        // To-do
    }

    public void HandlePieceMessage() {
        // To-do
    }

    public void MessageListener() throws IOException, ClassNotFoundException {
        ObjectInputStream in = neighbor_peer.in;
        MessageType msg_type;
        while (true) {
            Message message_received = new Message((byte[]) in.readObject());
            msg_type = message_received.GetMessageType();

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
                    HandleHaveMessage();
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
