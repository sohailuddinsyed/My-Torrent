import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

public class PeerClient extends Thread{

    private PeerDetails curr_peer;
    private HashMap<Integer, PeerDetails> neighbors_list;
    private ArrayList<Integer> previous_neighbors_ids;
    public PeerClient(PeerDetails curr_peer, HashMap<Integer, PeerDetails>  neighbors_list, ArrayList<Integer>  previous_neighbors_ids) {
        this.curr_peer              = curr_peer;
        this.neighbors_list         = neighbors_list;
        this.previous_neighbors_ids = previous_neighbors_ids;
    }

    public void run() {
        // Creating Client object for all the neighbors that requires TCP Connection
        for (int id: previous_neighbors_ids) {
            new Client(neighbors_list.get(id)).start();
        }
    }

    public class Client extends Thread{
        Socket requestSocket;           //socket connect to the server
        ObjectOutputStream out;         //stream write to the socket
        ObjectInputStream in;          //stream read from the socket
        String message;                //message send to the server
        String MESSAGE;                //capitalized message read from the server
        PeerDetails neighbor_peer;

        public Client(PeerDetails peer_details) {
            this.neighbor_peer = peer_details;
        }

        public void run() {
            try {
                //create a socket to connect to the server
                requestSocket        = new Socket(neighbor_peer.hostname, neighbor_peer.peer_port);

                System.out.println("Connected to " + neighbor_peer.hostname + "in port " + neighbor_peer.peer_port);
                //initialize inputStream and outputStream
                out = new ObjectOutputStream(requestSocket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(requestSocket.getInputStream());

                neighbor_peer.socket = requestSocket;
                neighbor_peer.out    = out;
                neighbor_peer.in     = in;

                HandShake hand_shake = new HandShake(curr_peer.peer_id);
                HelperMethods.sendMessage(hand_shake.BuildHandshakeMessage(), out);

                while (true) {
                    // HandShake message received and verified
                    byte[] hand_shake_rcv = (byte[]) in.readObject();
                    if (hand_shake.VerifyHandShakeMessage(hand_shake_rcv, neighbor_peer.peer_id))
                        break;
                }

                Message bit_field_message = new Message(0, (byte)5, curr_peer.bitfield_piece_index.toByteArray());
                HelperMethods.sendMessage(bit_field_message.BuildMessageByteArray(), out);

                P2PMessageHandler message_handler = new P2PMessageHandler(curr_peer, neighbor_peer);
                message_handler.MessageListener();


//                Message bit_field_rcv = new Message(0, (byte)5, in.readAllBytes());
//                boolean interested = bit_field_rcv.HandleBitFieldMessage(bitfield_piece_index);
//                if (interested) {
//                    Message interested_msg = new Message(0, (byte)2, new byte[0]);
//                    sendMessage(interested_msg.BuildMessageByteArray());
//                } else {
//                    Message not_interested_msg = new Message(0, (byte)2, new byte[0]);
//                    sendMessage(not_interested_msg.BuildMessageByteArray());
//                }

            } catch (ConnectException e) {
                System.err.println("Connection refused. You need to initiate a server first.");
            } catch (UnknownHostException unknownHost) {
                System.err.println("You are trying to connect to an unknown host!");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } catch (ClassNotFoundException e) {
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
                out.writeObject(msg);
                out.flush();
            }
            catch(IOException ioException){
                ioException.printStackTrace();
            }
        }

//        void sendMessage(byte[] msg)
//        {
//            try{
//                //stream write the message
//                out.writeObject(msg);
//                out.flush();
//            }
//            catch(IOException ioException){
//                ioException.printStackTrace();
//            }
//        }
    }
}
