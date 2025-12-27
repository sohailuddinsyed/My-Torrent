import java.io.*;
import java.nio.file.Files;

public class FileHandler {
    private static String file_name;
    private peerProcess host_peer;
    private byte[][] file_pieces;
    private Integer piece_size;
    private Integer file_size;
    public FileHandler(peerProcess host_peer) {
        file_name = "peer_" + host_peer.peer_id.toString() + "/" + host_peer.config_params.get("FileName");
        this.host_peer = host_peer;
        piece_size = Integer.parseInt(host_peer.config_params.get("PieceSize"));
        file_size  = Integer.parseInt(host_peer.config_params.get("FileSize"));
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

        for (int i = 0; i < host_peer.no_of_pieces; i++) {
            // Determine the size of the current piece
            int currentPieceSize = piece_size;
            
            // Adjust size for the last piece if file size isn't a perfect multiple of piece size     
            if (i == host_peer.no_of_pieces - 1 && file_size % piece_size != 0) {
                currentPieceSize = file_size % piece_size;
            }
        
            byte[] piece = new byte[currentPieceSize];
            // Copying contents from the file to the current piece
            for (int j = 0; j < currentPieceSize && file_bytes_index < file_data_in_bytes.length; j++) {
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
            if(i == host_peer.no_of_pieces - 1) {
                file_stream.write(file_pieces[i], 0, file_size % piece_size);
            } else {
                file_stream.write(file_pieces[i]);
            }
        }
        file_stream.close();    
    }
}
