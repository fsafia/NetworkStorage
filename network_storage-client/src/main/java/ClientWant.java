import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ClientWant {
    Socket socket;
    DataOutputStream out;
    Scanner in;

    public ClientWant(Socket socket, DataOutputStream out, Scanner in) {
        this.socket = socket;
        this.out = out;
        this.in = in;
    }

    public void toDo (byte numberComands, String pathname) {
        switch (numberComands) {
            case 1:
                sendFileToServer(pathname);
                break;
        }
    }

    private void sendFileToServer(String pathname) {
        byte comand = 1;  // номер команды для отправки файлов
        File file = new File(pathname);

        System.out.println("файл " + pathname + " существует? - " + file.exists());

        String fileName = file.getName(); // clToServ.txt
        try {
            out.write(comand); // номер команды
            out.writeInt(fileName.length());  // длина имени файла
            out.write(fileName.getBytes());  //название файла
            out.writeLong(file.length());  //размер файла
            FileInputStream fis = new FileInputStream(pathname);
            int x ;
            while ((x = fis.read()) > 0) {
                out.write(x);  //отправка текста файла
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
