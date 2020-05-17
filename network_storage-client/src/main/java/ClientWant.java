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
            case 2:
                deletFileOnServer(pathname);
                break;

        }
    }

    public void toDo (byte numberComands, String pathnameOld, String pathnameNew ) {
        switch (numberComands) {
            case 3:
                renameFileOnServer(pathnameOld, pathnameNew );
        }
    }

    private void sendFileToServer(String pathname) {//"network_storage-client/clToServ.txt"
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
            fis.close();//-----------------------------------проверить добавление строчки
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deletFileOnServer(String pathname){
        byte comand = 2;  // номер команды для удаления файлов
        File file = new File(pathname);

        System.out.println("файл " + pathname + " существует? - " + file.exists());

        String fileName = file.getName(); // clToServ.txt
        try {
            out.write(comand); // номер команды
            out.writeInt(fileName.length());  // длина имени файла
            out.write(fileName.getBytes());  //название файла
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void renameFileOnServer(String pathnameOld, String pathnameNew) {
        byte comand = 3;  // номер команды для переименования файла на сервере
        File fileOld = new File(pathnameOld);
        String fileNameOld = fileOld.getName(); // clToServ.txt

        File fileNew = new File(pathnameNew);
        String fileNameNew = fileNew.getName(); // clToServ.txt

        try {
            out.write(comand); // номер команды
            out.writeInt(fileNameOld.length());  // длина имени файла
            out.write(fileNameOld.getBytes());  //название файла
            out.writeInt(fileNameNew.length());
            out.write(fileNameNew.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
