import java.net.*;
import java.io.*;

public class PeerServer extends Thread {

    private peerProcess host_peer;
    private PeerDetails neighbor_peer;

    // Peer server constructor 
    public PeerServer(peerProcess host_peer) {
        this.host_peer = host_peer;
    }
    public void run() {
        System.out.println("The server is running....");
        ServerSocket listener = null;

        // listen and accept connection requests
        try {
            listener = new ServerSocket(host_peer.host_details.peer_port);
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
                        in.read(hand_shake_rcv);
                        Integer client_peer_id = Integer.valueOf(new String(hand_shake_rcv).substring(28));
                        
                        // If Handshake verification gets field, then break
                        HandShake hand_shake_msg = new HandShake(host_peer.peer_id);
                        if (!hand_shake_msg.VerifyHandShakeMessage(hand_shake_rcv))
                            break;
                        
                        host_peer.logger.log("is connected from Peer " + client_peer_id);
                        neighbor_peer = host_peer.neighbors_list.get(client_peer_id);
                        // Save the socket, out and in details in neighbor_peer object to use it later
                        neighbor_peer.socket = connection;
                        neighbor_peer.out    = out;
                        neighbor_peer.in     = in;

                        // host_peer.neighbors_list.put(neighbor_peer.peer_id, neighbor_peer);

                        // Send handshake to client
                        Utils.sendMessage(hand_shake_msg.BuildHandshakeMessage(), out);
                        
                        // If server has file, send bitfield to client
                        if(host_peer.host_details.has_file) {
                            Message bit_field_message = new Message(MessageType.BITFIELD, host_peer.host_details.bitfield_piece_index.toByteArray());
                            Utils.sendMessage(bit_field_message.BuildMessageByteArray(), out);
                        }
                        

                        // Create a P2PMessageHandler for each of the TCP Connections which will be responsible
                        // to listen and handle all type of messages
                        P2PMessageHandler message_handler = new P2PMessageHandler(host_peer, neighbor_peer);
                        message_handler.MessageListener();
                    }
                }
                catch(Exception classnot){
                    System.err.println("Data received in unknown format");
                    classnot.printStackTrace();
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
    }
}
