import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class PeerServer extends Thread {

    private int peer_id, peer_port;
    private HashMap<Integer, PeerDetails> neighbors_list;
    public PeerServer(int peer_id, int peer_port, HashMap<Integer, PeerDetails>  neighbors_list) {
        this.peer_id = peer_id;
        this.peer_port = peer_port;
        this.neighbors_list = neighbors_list;
    }
    public void run() {
        System.out.println("The server is running.");
        ServerSocket listener = null;
        try {
            listener = new ServerSocket(peer_port);
            while(true) {
                new Handler(listener.accept(), peer_id).start();
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
    private static class Handler extends Thread {
        private String message;    //message received from the client
        private String MESSAGE;    //uppercase message send to the client
        private Socket connection;
        private ObjectInputStream in;	//stream read from the socket
        private ObjectOutputStream out;    //stream write to the socket
        private int peer_id;


        public Handler(Socket connection, int peer_id) {
            this.connection = connection;
            this.peer_id    = peer_id;
        }

        public void run() {
            try{
                //initialize Input and Output streams
                out = new ObjectOutputStream(connection.getOutputStream());
                out.flush();
                in = new ObjectInputStream(connection.getInputStream());
                try{
                    while(true)
                    {
                        //receive the message sent from the client
                        message = (String)in.readObject();
                        //show the message to the user
                        System.out.println("Receive message: " + message + " from client ");
                        //Capitalize all letters in the message
                        MESSAGE = message.toUpperCase();
                        //send MESSAGE back to the client
                        sendMessage(MESSAGE);
                    }
                }
                catch(ClassNotFoundException classnot){
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
                out.writeObject(msg);
                out.flush();
                System.out.println("Send message: " + msg + " to Client ");
            }
            catch(IOException ioException){
                ioException.printStackTrace();
            }
        }

    }

}
