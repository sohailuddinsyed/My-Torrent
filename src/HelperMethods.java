import java.io.IOException;
import java.io.ObjectOutputStream;

public class HelperMethods {
    static void sendMessage(byte[] msg, ObjectOutputStream out)
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
}
