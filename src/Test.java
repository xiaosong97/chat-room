import java.util.Scanner;

/**
 * Test for Scanner
 *
 * @author qusong
 * @Date 2022/9/15
 **/
public class Test {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        if (in.hasNextLine()) {
            System.out.println(in.nextLine());
        }
    }
}
