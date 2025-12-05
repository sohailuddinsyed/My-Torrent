import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class PeerServer extends Thread {

    private PeerDetails curr_peer;
    private HashMap<Integer, PeerDetails> neighbors_list;
    private ArrayList<Integer> previous_neighbors_ids;
    private Logger logger;

    // Peer server constructor 
    public PeerServer(PeerDetails curr_peer, HashMap<Integer, PeerDetails>  neighbors_list, ArrayList<Integer>  previous_neighbors_ids, Logger logger) {
        this.curr_peer              = curr_peer;
        this.neighbors_list         = neighbors_list;
        this.previous_neighbors_ids = previous_neighbors_ids;
        this.logger                 = logger;
    }
    public void run() {
        System.out.println("The server is running.");
        ServerSocket listener = null;

        // listen and accept connection requests
        try {
            listener = new ServerSocket(curr_peer.peer_port);
            while(true) {
                // Start a handler for the incoming connection
                new Handler(listener.accept()).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                listener.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * A handler thread class.  Handlers are spawned from the listening
     * loop and are responsible for dealing with a single client's requests.
     */
    private class Handler extends Thread {
        private byte[] hand_shake_rcv;    //message received from the client
        private String MESSAGE;    //uppercase message send to the client
        private Socket connection;
        private DataInputStream in;	//stream read from the socket
        private DataOutputStream out;    //stream write to the socket

        public Handler(Socket connection) {
            this.connection     = connection;
            this.hand_shake_rcv = new byte[32];
        }

        public void run() {
            try{
                //initialize Input and Output streams
                out = new DataOutputStream(connection.getOutputStream());
                out.flush();
                in = new DataInputStream(connection.getInputStream());
                try{
                    while(true)
                    {
                        // Receive handshake from client
                        int msg_len = in.read(hand_shake_rcv);
                        System.out.println(msg_len + "bytes HS recvd at server");
                        String client_peer_id = new String(hand_shake_rcv).substring(28);
                        logger.log("is connected from Peer " + client_peer_id);

                        // If Handshake verification gets field, then break
                        if (!HandShake.VerifyHandShakeMessage(hand_shake_rcv))
                            break;

                        // Send handshake to client
                        HandShake hand_shake_msg = new HandShake(Integer.parseInt(client_peer_id));
                        Utils.sendMessage(hand_shake_msg.BuildHandshakeMessage(), out);
                        System.out.println("Server send handshake");

                        // Send bitfield to client
                        Message bit_field_message = new Message(curr_peer.bitfield_piece_index.size()/8, (byte)5, curr_peer.bitfield_piece_index.toByteArray());
                        System.out.println(bit_field_message.BuildMessageByteArray());
                        Utils.sendMessage(bit_field_message.BuildMessageByteArray(), out);
                    }
                }
                catch(Exception classnot){
                    System.err.println("Data received in unknown format");
                }
            }
            catch(IOException ioException){
                System.out.println("Disconnect with Client ");
            }
            finally{
                //Close connections
                try{
                    in.close();
                    out.close();
                    connection.close();
                }
                catch(IOException ioException){
                    System.out.println("Disconnect with Client ");
                }
            }
        }

        //send a message to the output stream
        public void sendMessage(String msg)
        {
            try{
                out.writeBytes(msg);
                out.flush();
                System.out.println("Send message: " + msg + " to Client ");
            }
            catch(IOException ioException){
                ioException.printStackTrace();
            }
        }

    }

}
