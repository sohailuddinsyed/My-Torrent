import java.io.*;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

public class FileHandler {
    private static String file_name;
    private peerProcess host_peer;
    private byte[][] file_pieces;
    private Integer piece_size;
    public FileHandler(peerProcess host_peer) {
        file_name = host_peer.peer_id.toString() + "/" + host_peer.config_params.get("FileName");
        this.host_peer = host_peer;
        piece_size = Integer.parseInt(host_peer.config_params.get("PieceSize"));
        file_pieces = new byte[host_peer.no_of_pieces][piece_size];
    }

    // Method to convert file into pieces based on the configuration details
    public void ConvertFileToPieces() throws IOException {
        // If host doesn't have a file, then return
        if (!host_peer.host_details.has_file)
            return;
        File file = new File(file_name);
        byte[] file_data_in_bytes = Files.readAllBytes(file.toPath());

        int file_bytes_index = 0;

        // Creating byte array for each piece and copying contents from file
        for (int i = 0; i < host_peer.no_of_pieces; i++) {
            byte[] piece = new byte[piece_size];
            //Cannot use below because last part may not be same as piece_size
            //byte[] piece = Arrays.copyOfRange(file_data_in_bytes, i*piece_size, piece_size*(i+1));
            for (int j = 0; j < piece_size && file_bytes_index<file_data_in_bytes.length; j++) {
                piece[j] = file_data_in_bytes[file_bytes_index++];
            }
            file_pieces[i] = piece;
        }
    }

    // Return the piece for a given index
    public byte[] GetPiece(int index) {
        return file_pieces[index];
    }

    // Write the received piece on a given index
    public void SetPiece(int index, byte[] data) {
        if (host_peer.host_details.bitfield_piece_index.get(index))
            return;
        file_pieces[index] = data;
        host_peer.host_details.bitfield_piece_index.set(index, true);
    }

    // Build the file from pieces once all the pieces are received
    public void BuildFile() throws IOException {
        File file = new File(file_name);
        FileOutputStream file_stream = new FileOutputStream(file);

        for (int i = 0; i < host_peer.no_of_pieces; i++) {
            file_stream.write(file_pieces[i]);
        }
        file_stream.close();
    }
}
