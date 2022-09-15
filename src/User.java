import java.net.Socket;
import java.util.UUID;

/**
 * The user bean
 *
 * @author qusong
 * @Date 2022/9/14
 **/
public class User {
    private final UUID userId;
    private String name;
    private final Socket socket;

    public User(String name, Socket socket) {
        this.userId = UUID.randomUUID();
        this.name = name;
        this.socket = socket;
    }

    public User(Socket socket) {
        this.userId = UUID.randomUUID();
        this.socket = socket;
        this.name = "";
    }

    public UUID getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", socket=" + socket +
                '}';
    }
}
