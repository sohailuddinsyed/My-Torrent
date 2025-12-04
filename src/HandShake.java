public class HandShake {
    private String handshake_header;
    private String zero_bits;
    private String peer_id;

    public HandShake(Integer id) {
        peer_id          = String.valueOf(id);
        handshake_header = "P2PFILESHARINGPROJ";
        zero_bits        = "0000000000";
    }

    public byte[] BuildHandshakeMessage() {
        String handshake_msg = handshake_header + zero_bits + peer_id;
        return handshake_msg.getBytes();
    }

    public boolean VerifyHandShakeMessage(byte[] handshake_msg, int id) {
        String msg = new String(handshake_msg);
        return msg.substring(0, 18).equals(handshake_header) &&
                msg.substring(28).equals(String.valueOf(id));
    }
}
