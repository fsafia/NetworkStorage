import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
//-------------------отправка файла 1.txt-----------------------------------
//        String f = "network_storage-client/1.txt";
//        File file = new File(f);
//        System.out.println(file.exists());
//
//        byte comand  = 1;
//        System.out.println("номер команды " + comand); // номер команды
//
//        System.out.println("длина имени файла " + f.length()); // длина имени файла
//
//        System.out.println("название файла " + f.getBytes());  //название файла);
//
//        long size = file.length();
//        System.out.println("размер файла " + size); //размер файла
//------------------------------------------------------------------------------

        try{
            Socket socket = new Socket("localhost", 8189);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            Scanner in = new Scanner(socket.getInputStream());


            //  отправлять команды в другом классе

            ClientWant clientWant = new ClientWant(socket, out, in);
            clientWant.toDo((byte) 1, "network_storage-client/clToServ.txt");
            clientWant.toDo((byte) 1, "network_storage-client/1.txt");

//-------------------------------------------------------
//            out.write(comand); // номер команды
//
//            out.writeInt(f.length());  // длина имени файла
//            out.write(f.getBytes());  //название файла
//            out.writeLong(size);  //размер файла
//            FileInputStream fis = new FileInputStream(f);
//            int x ;
//            while ((x = fis.read()) > 0) {
//                out.write(x);  //отправка текста файла
//            }
//----------------------------------------------------------------------------
//            out.write(new byte[]{11, 21, 31});
//            String s = in.nextLine();
//            System.out.println("A: " + s);
//            fis.close();------------------------------------
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
