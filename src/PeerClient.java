import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

// PeerClient is responsible to establish TCP Connections with previous peers
public class PeerClient extends Thread{

    private PeerDetails curr_peer;
    private HashMap<Integer, PeerDetails> neighbors_list;
    private ArrayList<Integer> previous_neighbors_ids;
    private Logger logger;

    public PeerClient(PeerDetails curr_peer, HashMap<Integer, PeerDetails>  neighbors_list, ArrayList<Integer>  previous_neighbors_ids, Logger logger) {
        this.curr_peer              = curr_peer;
        this.neighbors_list         = neighbors_list;
        this.previous_neighbors_ids = previous_neighbors_ids;
        this.logger                 = logger;
    }

    public void run() {
        // Creating Client object for all the neighbors that requires TCP Connection
        for (int id: previous_neighbors_ids) {
            new Client(neighbors_list.get(id)).start();
        }
    }

    public class Client extends Thread{
        Socket requestSocket;           //socket connect to the server
        DataOutputStream out;         //stream write to the socket
        DataInputStream in;          //stream read from the socket
        String message;                //message send to the server
        String MESSAGE;                //capitalized message read from the server
        PeerDetails neighbor_peer;

        public Client(PeerDetails peer_details) {
            this.neighbor_peer = peer_details;
        }

        public void run() {
            try {
                //create a socket to connect to the Peerserver og neighbors
                requestSocket = new Socket(neighbor_peer.hostname, neighbor_peer.peer_port);

                logger.log("makes a connection to Peer " + neighbor_peer.peer_id);
                //initialize inputStream and outputStream
                out = new DataOutputStream(requestSocket.getOutputStream());
                out.flush();
                in = new DataInputStream(requestSocket.getInputStream());

                // Save the socket, out and in details in neighbor_peer object to use it later
                neighbor_peer.socket = requestSocket;
                neighbor_peer.out    = out;
                neighbor_peer.in     = in;

                // Create a handshake object with current peer id, build the handshake message
                // and send it to the neighbor
                HandShake hand_shake = new HandShake(curr_peer.peer_id);
                Utils.sendMessage(hand_shake.BuildHandshakeMessage(), out);

                while (true) {
                    // Wait for HandShake message to be received and verified
                    byte[] hand_shake_rcv = (byte[]) in.readAllBytes();
                    if (hand_shake.VerifyHandShakeMessage(hand_shake_rcv, neighbor_peer.peer_id))
                        break;
                }

                // Once HandShake is completed, create a bit field message and send it to the neighbor
                Message bit_field_message = new Message(curr_peer.bitfield_piece_index.size()/8, (byte)5, curr_peer.bitfield_piece_index.toByteArray());
                Utils.sendMessage(bit_field_message.BuildMessageByteArray(), out);

                // Create a P2PMessageHandler for each of the TCP Connections which will be responsible
                // to listen and handle all type of messages
                P2PMessageHandler message_handler = new P2PMessageHandler(curr_peer, neighbor_peer);
                message_handler.MessageListener();

            } catch (ConnectException e) {
                System.err.println("Connection refused. You need to initiate a server first.");
            } catch (UnknownHostException unknownHost) {
                System.err.println("You are trying to connect to an unknown host!");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                //Close connections
                try {
                    in.close();
                    out.close();
                    requestSocket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
        //send a message to the output stream
        void sendMessage(String msg)
        {
            try{
                //stream write the message
                out.writeBytes(msg);
                out.flush();
            }
            catch(IOException ioException){
                ioException.printStackTrace();
            }
        }
    }
}
