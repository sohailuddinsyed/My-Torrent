import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

public class PeerClient extends Thread{

    private int peer_id;
    private HashMap<Integer, PeerDetails> neighbors_list;
    private ArrayList<Integer> previous_neighbors_ids;
    public PeerClient(int peer_id, HashMap<Integer, PeerDetails>  neighbors_list, ArrayList<Integer>  previous_neighbors_ids) {
        this.peer_id = peer_id;
        this.neighbors_list = neighbors_list;
        this.previous_neighbors_ids = previous_neighbors_ids;
    }

    public void run() {
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
        PeerDetails peer_details;

        public Client(PeerDetails peer_details) {
            this.peer_details = peer_details;
        }

        public void run() {
            try {
                //create a socket to connect to the server
                requestSocket = new Socket(peer_details.hostname, peer_details.peer_port);
                peer_details.socket = requestSocket;
                System.out.println("Connected to " + peer_details.hostname + "in port " + peer_details.peer_port);
                //initialize inputStream and outputStream
                out = new ObjectOutputStream(requestSocket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(requestSocket.getInputStream());

                HandShake hand_shake = new HandShake(peer_details.peer_id);
                sendMessage(hand_shake.BuildHandshakeMessage());

                byte[] hand_shake_rcv = in.readAllBytes();

                while (true) {
                    // HandShake message received and verified
                    if (hand_shake.VerifyHandShakeMessage(hand_shake_rcv, peer_details.peer_id))
                        break;
                }
            } catch (ConnectException e) {
                System.err.println("Connection refused. You need to initiate a server first.");
            } catch (UnknownHostException unknownHost) {
                System.err.println("You are trying to connect to an unknown host!");
            } catch (IOException ioException) {
                ioException.printStackTrace();
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

        void sendMessage(byte[] msg)
        {
            try{
                //stream write the message
                //writeObject or just write?
                out.writeObject(msg);
                out.flush();
            }
            catch(IOException ioException){
                ioException.printStackTrace();
            }
        }
    }
}
