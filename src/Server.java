import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * The server
 *
 * @author qusong
 * @Date 2022/9/14
 **/
public class Server {
    public static final int CHAT_SERVER_PORT = 9189;
    private HashMap<UUID, User> onSiteUser = new HashMap<>();
    private HashMap<UUID, UUID> chatSession = new HashMap<>();
    private HashMap<String, UUID> nameIdMap = new HashMap<>();
    private final HashMap<UUID, Deque<String>> messageQ = new HashMap<>();
    private ExecutorService pool = Executors.newFixedThreadPool(10);

    public void runServer() {
        try (ServerSocket s = new ServerSocket(CHAT_SERVER_PORT)) {
            while (true) {
                User user = new User(s.accept());
                onSiteUser.put(user.getUserId(), user);
                ChatUserHandler handler = new ChatUserHandler(user, onSiteUser, chatSession, nameIdMap, messageQ);
                pool.submit(handler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.runServer();
    }

}
