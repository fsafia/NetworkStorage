import java.io.File;

public class ClientWant {

    public ClientWant(){}

    public void toDo (int numberComands, String fileName) {
        switch (numberComands) {
            case 1:
                sendFileToServer(fileName);
                break;
        }
    }

    private void sendFileToServer(String pathname) {
        int numberComands = 1;  // номер команды для отправки файлов
        File file = new File(pathname);
        if (!file.exists()) {
            System.out.println("Файла  с именем " + pathname + " не существует.");
            return;//???
        }
        String name = file.getName(); //имя файла с расширением
        int lengthName = name.length(); // длина имени

    }
}
