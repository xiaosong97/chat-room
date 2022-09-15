import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * This is a handler for a chat-room user.
 *
 * @author qusong
 * @Date 2022/9/14
 **/
public class ChatUserHandler implements Runnable {
    private final User user;
    private final HashMap<UUID, User> onSiteUser;
    private final HashMap<UUID, UUID> chatSession;
    private final HashMap<String, UUID> nameIdMap;
    private final HashMap<UUID, Deque<String>> messageQ;
    private boolean exited = false;


    public ChatUserHandler(User user, HashMap<UUID, User> onSiteUser, HashMap<UUID, UUID> chatSession, HashMap<String, UUID> nameIdMap, HashMap<UUID, Deque<String>> messageQ) {
        this.user = user;
        this.onSiteUser = onSiteUser;
        this.chatSession = chatSession;
        this.nameIdMap = nameIdMap;
        this.messageQ = messageQ;
    }

    @Override
    public void run() {
        Socket incoming = user.getSocket();
        try (InputStream inStream = incoming.getInputStream();
             OutputStream outStream = incoming.getOutputStream();
             Scanner in = new Scanner(inStream, "UTF-8");
             PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(outStream, "UTF-8"), true)) {
            // server said one sentence, then server read one sentence from client
            String response = "Welcome to chat room, please input your name first, or you can Enter '@Exit' to disconnect at any time.";
            String request = "";
            while (!exited) {
                if (!response.equals("")) {
                    out.println(response);
                    response = "";
                }
                else if (messageQ.get(user.getUserId()) != null) {
                    System.out.println("I'm " + user.getName() + ", id=" + user.getUserId());
                    Deque<String> msgQ = messageQ.get(user.getUserId());
                    String receiveMsg = msgQ.removeFirst();
                    out.println(receiveMsg);
                }
                System.out.println("begin in.hasNextLine");
                if (in.hasNextLine()) {
                    request = in.nextLine();
                    System.out.println("request: " + request);
                    if (user.getName().equals("")) {
                        if (isValidName(request)) {
                            user.setName(request);
                            nameIdMap.put(user.getName(), user.getUserId());
                            System.out.println("User " + user.getName() + ": " + user.getUserId() + " login!"); // for debug
                            response = "Welcome " + user.getName() + ", now you can enter @List to see which one to talk?";
                        } else {
                            response = "Invalid name, please try another name: ";
                        }
                    } else {
                        System.out.println("handleRequest " + request);
                        response = handleRequest(request);
                    }
                }
                System.out.println("after in.hasNextLine " + response);
            }
            System.out.println("incoming.close()");
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
                    response = "From " + user.getName() + ": " + msg;
                    System.out.println(response);
                    Deque<String> msgQ = messageQ.computeIfAbsent(toId, k -> new LinkedList<>());
                    msgQ.addLast(response);
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
//            System.out.println("name is empty!");  // for debug
            return false;
        }
        // invalid name, because @ is a command starter
        if (name.trim().startsWith("@")) {
//            System.out.println("'@' is a command starter, please don't start from '@'");  // for debug
            return false;
        }
        if (isRepeatedName(name)) {
//            System.out.println("There is already a user named " + name);  // for debug
            return false;
        }
        return true;
    }

    private boolean isRepeatedName(String name) {
        UUID id = nameIdMap.get(name);
        if (id == null) return false;
        System.out.println("!onSiteUser.containsKey(id): " + !onSiteUser.containsKey(id));
        return onSiteUser.containsKey(id);
    }
}
