import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class MyClient {
    public static void main(String[] args) {
//        try{
//            Socket socket = new Socket("localhost", 8189);
//            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
//            Scanner in = new Scanner(socket.getInputStream());
//            out.write(new byte[]{11, 21, 31});
//            String x = in.nextLine();
//            System.out.println("A: " + x);
//            in.close();
//            out.close();
//            socket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        String f = "1.txt";
        byte comand  = 1;
        String req = comand + f;
        byte[] sendFile = req.getBytes();

        try{
            Socket socket = new Socket("localhost", 8189);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            Scanner in = new Scanner(socket.getInputStream());
            //
            out.write(sendFile);
            //
            out.write(new byte[]{11, 21, 31});
            String x = in.nextLine();
            System.out.println("A: " + x);
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
