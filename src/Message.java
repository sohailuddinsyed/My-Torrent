import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Message {
    private Integer message_length;
    private Byte message_type;
    private byte[] message_payload;

    // Message constructor to initialize message fields
    public Message(int message_length, byte message_type, byte[] message_payload) {
        this.message_length = message_length;
        this.message_type = message_type;
        this.message_payload = message_payload;
    }

    // Message constructor to initialize message fields from a byte array
    public Message(byte[] message) {
        String msg = new String(message);
        this.message_length  = Integer.getInteger(msg.substring(0, 4));
        this.message_type    = message[4];
        this.message_payload = msg.substring(5).getBytes();
    }

    // Returns a byte array of the message
    public byte[] BuildMessageByteArray() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            try {
                buffer.write(message_length);
                buffer.write(message_type);
                buffer.write(message_payload);
            } catch (IOException e) {
                e.printStackTrace();
            }
        return buffer.toByteArray();
    }

    // Return the type of the message as an enum
    public MessageType GetMessageType() {
        switch(message_type) {
            case 0: return MessageType.CHOKE;
            case 1: return MessageType.UNCHOKE;
            case 2: return MessageType.INTERESTED;
            case 3: return MessageType.NOTINTERESTED;
            case 4: return MessageType.HAVE;
            case 5: return MessageType.BITFIELD;
            case 6: return MessageType.REQUEST;
            case 7: return MessageType.PIECE;
            default: return MessageType.UNKNOWN;
        }
    }

    // Return the message length field
    public Integer GetMessageLength() {
        return message_length;
    }

    // Return the message payload field
    public byte[] GetMessagePayload() {
        return message_payload;
    }
}