import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;

public class P2PMessageHandler {
    peerProcess host_peer; // Current host
    PeerDetails neighbor_peer; // Neighbor peer to which the current TCP is established
    Boolean chocked_by_host;

    public P2PMessageHandler(peerProcess host_peer, PeerDetails neighbor_peer) {
        this.host_peer     = host_peer;
        this.neighbor_peer = neighbor_peer;
        this.chocked_by_host = true;
    }

    // Method to handle BitField Message received from Neighbor
    public void HandleBitFieldMessage(Message message) {
        // Create an empty bitset of same length as payload
        BitSet peer_bitset                 = new BitSet(message.GetMessagePayload().length * 8); // 8 bits in a byte
        // Retrieve the message payload and store it in neighbor_peer object
        byte[] message_payload             = message.GetMessagePayload();
        boolean interested                 = false;

        // Set the peer bitfield from bitfield index payload
        for (int i = 0; i < message.GetMessageLength(); i++) {
            // Parse each byte of the bitfield message
            for (int j = 0; j < 8; j++) {
                if ((message_payload[i] & (1 << j)) != 0) {
                    peer_bitset.set(i * 8 + j);
                    // If the peer neighbor has any bits host does not have, flag interested to true
                    if(peer_bitset.get(i * 8 + j) && !host_peer.host_details.bitfield_piece_index.get(i * 8 + j)) {
                        interested = true;
                    }
                }
            }
        }
        // Set neighbor bit field
        neighbor_peer.bitfield_piece_index = peer_bitset;

        // Send Interested if the above result is not empty else send NotInterested message
        MessageType msg_type = interested ? MessageType.INTERESTED : MessageType.NOTINTERESTED;

        // Make third argument in Message as None and avoid sending third argument?
        Message msg = new Message(msg_type, new byte[1]);
        Utils.sendMessage(msg.BuildMessageByteArray(), neighbor_peer.out);
    }

    public void HandleChokeMessage() {
        host_peer.choked_by_neighbors.put(neighbor_peer.peer_id, true);
    }

    public void HandleUnChokeMessage() {
        host_peer.choked_by_neighbors.put(neighbor_peer.peer_id, false);
        // Gets next interested index and sends request message
        int interested_index = Utils.GetInterestIndex(host_peer, neighbor_peer);
        if (interested_index != -1) {
            Message msg = new Message(MessageType.REQUEST, ByteBuffer.allocate(4).putInt(interested_index).array());
            Utils.sendMessage(msg.BuildMessageByteArray(), neighbor_peer.out);
            host_peer.requested_indices.add(interested_index);
        }
    }

    public void HandleInterestedMessage() {
        host_peer.neighbors_interested_in_host.put(neighbor_peer.peer_id, true);
    }

    public void HandleNotInterestedMessage() {
        host_peer.neighbors_interested_in_host.put(neighbor_peer.peer_id, false);
    }

    // Handler for 'have' message type
    public void HandleHaveMessage(Message message_received) {
        // Set the bitfield index received from the neighbor_peer
        int bitfield_index = Integer.parseInt(message_received.GetMessagePayload().toString());
        neighbor_peer.bitfield_piece_index.set(bitfield_index);

        // Check if interested
        boolean send_interested = Utils.CheckInterestInIndex(host_peer.host_details, neighbor_peer, bitfield_index);

        MessageType msg_type = send_interested ? MessageType.INTERESTED : MessageType.NOTINTERESTED;

        Message msg = new Message(msg_type, new byte[0]);
        Utils.sendMessage(msg.BuildMessageByteArray(), neighbor_peer.out);
    }

    public void HandleRequestMessage(Message message_received, int index) {
        // Don't send data when neighbor is choked
        // TODO: We set requested_indices as true when a request is sent, this should set back to false in this case
        if (!host_peer.unchoked_by_host.get(neighbor_peer.peer_id))
            return;

        // Below should not happen as the request is received only if the host has required piece
        if (!host_peer.host_details.bitfield_piece_index.get(index))
            return;

        // Get the requested index in byte format
        byte[] message_payload = message_received.GetMessagePayload();

        // Pull the required piece
        byte[] requested_piece = host_peer.file_handler.GetPiece(index);

        // Below is to concatenate index in byte format and the piece
        byte[] data_payload = new byte[requested_piece.length + message_payload.length];

        ByteBuffer buffer = ByteBuffer.wrap(data_payload);
        buffer.put(message_payload);
        buffer.put(requested_piece);

        Message msg = new Message(MessageType.PIECE, buffer.array());
        Utils.sendMessage(msg.BuildMessageByteArray(), neighbor_peer.out);
    }

    public void HandlePieceMessage(Message message_received, int index) throws IOException {
        // Below should not happen as the piece is sent only with a request and
        // request for a given index is made to a single neighbor
        if (host_peer.host_details.bitfield_piece_index.get(index))
            return;

        // Copy the piece and set it in the respective index
        byte[] piece_payload = Arrays.copyOfRange(message_received.GetMessagePayload(), 4,
                message_received.GetMessageLength());

        host_peer.file_handler.SetPiece(index, piece_payload);

        // Check if all pieces received and build the file
        if (Utils.CheckAllPiecesReceived(host_peer.host_details.bitfield_piece_index, host_peer.no_of_pieces)) {
            host_peer.file_handler.BuildFile();
            // TODO: Check if host is not sending any data and terminate?
        }

    }

    public void SendUnChokedMessage() {
        Message msg = new Message(MessageType.UNCHOKE, new byte[1]);
        Utils.sendMessage(msg.BuildMessageByteArray(), neighbor_peer.out);
    }
    
    public void SendChokedMessage() {
        Message msg = new Message(MessageType.CHOKE, new byte[1]);
        Utils.sendMessage(msg.BuildMessageByteArray(), neighbor_peer.out);
    }

    public void MessageListener() throws IOException {
        DataInputStream in = neighbor_peer.in;
        MessageType msg_type;
        while (true) {
            if(chocked_by_host && host_peer.unchoked_by_host.getOrDefault(neighbor_peer.peer_id, false)) {
                chocked_by_host = false;
                SendUnChokedMessage();
            }
            if(!chocked_by_host && !host_peer.unchoked_by_host.get(neighbor_peer.peer_id)) {
                chocked_by_host = true;
                SendChokedMessage();
            }
            // Receive message and retrieve the message type
            if(in.available() != 0) {
                int bytes_available = in.available();
                byte[] recvd_message = new byte[bytes_available];
                in.read(recvd_message);
                
                Message message_received = new Message(recvd_message);
                msg_type = message_received.GetMessageType();
                
                // Take Action based on message type received
                switch(msg_type) {
                    case CHOKE: {
                        host_peer.logger.log("received " + msg_type.toString() + " message from Peer " + neighbor_peer.peer_id);
                        HandleChokeMessage();
                        break;
                    }
                    case UNCHOKE: {
                        host_peer.logger.log("received " + msg_type.toString() + " message from Peer " + neighbor_peer.peer_id);
                        HandleUnChokeMessage();
                        break;
                    }
                    case INTERESTED: {
                        host_peer.logger.log("received " + msg_type.toString() + " message from Peer " + neighbor_peer.peer_id);
                        HandleInterestedMessage();
                        break;
                    }
                    case NOTINTERESTED: {
                        host_peer.logger.log("received " + msg_type.toString() + " message from Peer " + neighbor_peer.peer_id);
                        HandleNotInterestedMessage();
                        break;
                    }
                    case HAVE: {
                        host_peer.logger.log("received " + msg_type.toString() + " message from Peer " + neighbor_peer.peer_id);
                        HandleHaveMessage(message_received);
                        break;
                    }
                    case BITFIELD: {
                        host_peer.logger.log("received " + msg_type.toString() + " message from Peer " + neighbor_peer.peer_id);
                        HandleBitFieldMessage(message_received);
                        break;
                    }
                    case REQUEST: {
                        int index = ByteBuffer.wrap(Arrays.copyOfRange(message_received.GetMessagePayload(), 0, 4)).getInt();
                        host_peer.logger.log("received " + msg_type.toString() + " (" + index + ") message from Peer " + neighbor_peer.peer_id);
                        HandleRequestMessage(message_received, index);
                        break;
                    }
                    case PIECE: {
                        int index = ByteBuffer.wrap(Arrays.copyOfRange(message_received.GetMessagePayload(), 0, 4)).getInt();
                        host_peer.logger.log("received " + msg_type.toString() + " (" + index + ") message from Peer " + neighbor_peer.peer_id);
                        HandlePieceMessage(message_received, index);
                        break;
                    }
                    default: ;
                }
            }
        }
    }
}
