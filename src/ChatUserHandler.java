import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

/**
 * This is a handler for a chat-room user.
 *
 * @author qusong
 * @Date 2022/9/14
 **/
public class ChatUserHandler implements Runnable {
    Logger logger = Logger.getLogger("ChatUserHandler");
    private final User user;
    private final HashMap<UUID, User> onSiteUser;
    private final HashMap<UUID, UUID> chatSession;
    private final HashMap<String, UUID> nameIdMap;
    private boolean exited = false;


    public ChatUserHandler(User user, HashMap<UUID, User> onSiteUser, HashMap<UUID, UUID> chatSession, HashMap<String, UUID> nameIdMap) {
        this.user = user;
        this.onSiteUser = onSiteUser;
        this.chatSession = chatSession;
        this.nameIdMap = nameIdMap;
    }

    @Override
    public void run() {
        logger.info("current thread is " + Thread.currentThread().getName());
        Socket incoming = user.getSocket();
        try (InputStream inStream = incoming.getInputStream();
             OutputStream outStream = incoming.getOutputStream();
             Scanner in = new Scanner(inStream, "UTF-8");
             PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(outStream, "UTF-8"), true)) {
            // server said one sentence, then server read one sentence from client
            String response = "Welcome to chat room, please input your name first, or you can Enter '@Exit' to disconnect at any time.";
            String request = "";
            logger.info("exited is " + exited);
            while (!exited) {
                if (!response.equals("")) {
                    out.println(response);
                    response = "";
                }
                if (in.hasNextLine()) {
                    request = in.nextLine();
                    logger.info("read request: " + request);
                    if (user.getName().equals("")) {
                        if (isValidName(request)) {
                            user.setName(request);
                            nameIdMap.put(user.getName(), user.getUserId());
                            logger.info("User " + user.getName() + ": " + user.getUserId() + " login!");
                            response = "Welcome " + user.getName() + ", now you can enter @List to see which one to talk?";
                        } else {
                            response = "Invalid name, please try another name: ";
                        }
                    } else {
                        logger.info("handleRequest " + request);
                        response = handleRequest(request);
                    }
                }
            }
            logger.info("incoming.close()");
            incoming.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String handleRequest(String request) {
        if (request == null || request.equals("")) return "";
        else if (request.startsWith("@")) {
            return "From Server: " + handleCommand(request);
        } else {
            return handleMsg(request);
        }
    }

    private String handleMsg(String msg) {
        String response = "";
        if (!chatSession.containsKey(user.getUserId())) {
            response = "From Server: You have not specify who you want to talk! Please Enter @To <userName> to specify you want to talk!";
        } else {
            try {
                // transfer msg to To user
                UUID toId = chatSession.get(user.getUserId());
                if (onSiteUser.containsKey(toId)) {
                    User toUser = onSiteUser.get(toId);
                    response = "From " + user.getName() + ": " + msg;
                    logger.info("transfer response: [" + response + "] to " + toUser.getName());
                    Socket toSocket = toUser.getSocket();
                    // TODO: socket 只能被一处持有？这里写了第一次后，再次调用会报 SocketException: Socket is closed
                    try {
                        PrintWriter out = new PrintWriter(new OutputStreamWriter(toSocket.getOutputStream(), "UTF-8"), true);
                        out.println(response);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    response = "";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return response;
    }

    private String handleCommand(String command) {
        String cmd = command.substring(1);
        String response = "Invalid command!";
        if (cmd.equalsIgnoreCase("exit")) {
            exited = true;
            onSiteUser.remove(user.getUserId());
            response = "bye";
        } else if (cmd.equalsIgnoreCase("bye")) {
            UUID toId = chatSession.get(user.getUserId());
            if (toId == null || !onSiteUser.containsKey(toId)) {
                response = "you have not talking with someone";
            } else {
                String name = onSiteUser.get(toId).getName();
                response = "exit chat with " + name;
            }
            chatSession.remove(user.getUserId());
        } else if (cmd.equalsIgnoreCase("list")) {
            StringBuilder userNameList = new StringBuilder();
            int i = 0;
            for (User u : onSiteUser.values()) {
                if (i > 0) userNameList.append(", ").append(u.getName());
                else userNameList.append(u.getName());
                i++;
            }
            response = userNameList.toString();
        } else if (!cmd.isEmpty() && cmd.split(" ")[0].equalsIgnoreCase("to")) {
            try {
                String toName = cmd.split(" ")[1];
                UUID toId = nameIdMap.get(toName);
                if (toId == null || !onSiteUser.containsKey(toId)) {
                    response = "User " + toName + " does not exist!";
                } else {
                    chatSession.put(user.getUserId(), toId);
                    response = "You will chat with " + toName + " until you change to another user.";
                }
            } catch (Exception e) {
                response = "invalid format, please Enter @To <userName> to specify you want to talk!";
                e.printStackTrace();
            }
        }
        return response;
    }

    private boolean isValidName(String name) {
        if (name == null || name.length() == 0) {
            logger.info("name is empty!");
            return false;
        }
        // invalid name, because @ is a command starter
        if (name.trim().startsWith("@")) {
            logger.info("'@' is a command starter, please don't start from '@'");
            return false;
        }
        if (isRepeatedName(name)) {
            logger.info("There is already a user named " + name);
            return false;
        }
        return true;
    }

    private boolean isRepeatedName(String name) {
        UUID id = nameIdMap.get(name);
        if (id == null) return false;
        logger.info("!onSiteUser.containsKey(id): " + !onSiteUser.containsKey(id));
        return onSiteUser.containsKey(id);
    }
}
