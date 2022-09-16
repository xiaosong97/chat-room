import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The client agent
 *
 * @author qusong
 * @Date 2022/9/14
 **/
public class Client {
    Logger logger = Logger.getLogger("Client");
    private Socket socket;
    private Scanner in;
    private BufferedReader reader;
    private PrintWriter out;

    public void chatRequest() throws IOException {
        try {
            socket = new Socket("localhost", Server.CHAT_SERVER_PORT);
            in = new Scanner(socket.getInputStream(), "UTF-8");
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            Thread read = new Thread(() -> {
                logger.info("begin read thread.");
                while (true) {
                    try {
                        String msg = null;
                        while (true) {
//                            msg = in.nextLine();
                            msg = reader.readLine();
                            if (msg != null && msg.length() > 0) {
                                logger.info("receive msg: " + msg);
                                System.out.println(msg);
                                if (msg.equalsIgnoreCase("From Server: bye")) {
                                    logger.info("end read thread.");
                                    close();
                                    System.exit(0);
                                }
                                break;
                            }
                        }
                    } catch (NoSuchElementException | IOException e) {
                        e.printStackTrace();
                        close();
                        System.exit(-2);
                    }
                }
            });
            read.start();

            Thread write = new Thread(() -> {
                Scanner inUser = new Scanner(System.in);
                logger.info("begin write thread.");
                while (inUser.hasNextLine()) {
                    String request = inUser.nextLine();
                    out.println(request);
                    logger.info("write request: " + request);
                    if (request.equalsIgnoreCase("@Exit")) {
                        close();
                        break;
                    }
                }
                logger.info("end write thread.");
            });
            write.start();
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
    }

    private void close() {
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        try {
            client.chatRequest();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
