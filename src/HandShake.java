import java.nio.charset.StandardCharsets;

public class HandShake {
    private static String handshake_header;
    private String zero_bits;
    private String peer_id;

    public HandShake(Integer id) {
        peer_id          = String.valueOf(id);
        handshake_header = "P2PFILESHARINGPROJ";
        zero_bits        = "0000000000";
    }

    // Builds Handshake message and returns in a byte array format
    public byte[] BuildHandshakeMessage() {
        String handshake_msg = handshake_header + zero_bits + peer_id;
        return handshake_msg.getBytes();
    }

    // Verifies if the received handshake message has a valid handshake header and peer ID
    public boolean VerifyHandShakeMessage(byte[] handshake_msg, int id) {
        String msg = new String(handshake_msg);
        return msg.substring(0, 18).equals(handshake_header) &&
                msg.substring(28).equals(String.valueOf(id));
    }

    // Verifies if the received handshake message has a valid handshake header in case of PeerServer
    public boolean VerifyHandShakeMessage(byte[] handshake_msg) {
        String msg = new String(handshake_msg);
        return msg.substring(0, 18).equals(handshake_header);
    }
}
