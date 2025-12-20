import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    private final String log_file_name;
    private final String peer_name;

    public Logger(String peer) {
        peer_name = peer;
        // Required log file name
        String curr_dir = System.getProperty("user.dir");
        String file_name = curr_dir + "/logs/log_peer_" + peer + ".log";

        try {
            FileOutputStream fos = new FileOutputStream(file_name, true);
            PrintStream ps = new PrintStream(fos);
            System.setErr(ps);
        } catch (Exception e) {
            e.printStackTrace();
        }

        log_file_name = file_name;
        try {
            File file = new File(log_file_name);
            if (!file.createNewFile()) {
                // Reset old logs
                FileOutputStream fileOutputStream = new FileOutputStream(log_file_name);
                fileOutputStream.close();
            }
        } catch (IOException e) {
            System.out.println("An error occurred while initilaizing logger");
            e.printStackTrace();
        }
    }

    public void log(String log_string) {
        try {
            // Add date to each log statement
            Date currentTime = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
            String formattedTime = dateFormat.format(currentTime);
            String content = formattedTime + ": Peer " + peer_name + " " + log_string + "\n";
            System.out.print(content);

            // Write logs to file log_peer_[peer_id].log
            FileOutputStream fileOutputStream = new FileOutputStream(log_file_name, true);
            byte[] bytes = content.getBytes();
            fileOutputStream.write(bytes);
            fileOutputStream.close();
        } catch (Exception e) {
            System.out.println("An error occurred while logging");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Logger logger = new Logger("1001");
        logger.log("This is a test log");
        logger.log("More test log");
    }
}