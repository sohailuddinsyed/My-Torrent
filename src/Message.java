import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Message {
    private Integer message_length;
    private Byte message_type;
    private byte[] message_payload;

    // Message constructor to initialize message fields
    // TODO: message_length should be of type byte[]?
    public Message(MessageType message_type, byte[] message_payload) {
        this.message_type = MakeMessageType(message_type);
        this.message_payload = message_payload;
        // 4-byte message length specifies the message length in bytes. It does not include the 
        // length of the message length field itself
        this.message_length = message_payload.length;
    }

    // Message constructor to initialize message fields from a byte array
    public Message(byte[] message) {       
        this.message_length  = ByteBuffer.wrap(Arrays.copyOfRange(message, 0, 4)).getInt();
        this.message_type    = message[4];
        this.message_payload = ByteBuffer.wrap(Arrays.copyOfRange(message, 5, 5 + message_length)).array();
    }

    // Returns a byte array of the message
    public byte[] BuildMessageByteArray() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            try {
                // Writing message_length as a byte[] of size 4 since Integer is truncated to 1 byte in Java
                byte[] messg_len = new byte[4];
                messg_len = ByteBuffer.allocate(4).putInt(message_length).array();
                buffer.writeBytes(messg_len);
                buffer.write(message_type);
                buffer.writeBytes(message_payload);
            } catch (Exception e) {
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

    public byte MakeMessageType(MessageType message_type) {
        switch(message_type) {
            case CHOKE: return (byte) 0;
            case UNCHOKE: return (byte) 1;
            case INTERESTED: return (byte) 2;
            case NOTINTERESTED: return (byte) 3;
            case HAVE: return (byte) 4;
            case BITFIELD: return (byte) 5;
            case REQUEST: return (byte) 6;
            case PIECE: return (byte) 7;
            default: return (byte) 10;
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