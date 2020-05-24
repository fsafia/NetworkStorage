package network.storage.client;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


public class Controller {
    @FXML
    TextArea textArea;

    @FXML
    TextField textField;

    @FXML
    HBox bottomPanel;
    @FXML
    HBox upperPanel;
    @FXML
    TextField loginField, signupLoginField, signupNickField;
    @FXML
    PasswordField passwordField, signupPasswordField;
    @FXML
    ListView<String> clientsList, clientsBlockList;
    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    final String IP_ADRES  = "localhost";
    final int PORT = 8189;
    private boolean isAuthorized;

    public void setAuthorized(boolean isAuthorized){
        this.isAuthorized = isAuthorized;
        if(!isAuthorized){
            textArea.clear();
            upperPanel.setVisible(true);
            upperPanel.setManaged(true);
            bottomPanel.setVisible(false);
            bottomPanel.setManaged(false);
            clientsList.setVisible(false);
            clientsList.setManaged(false);
            clientsBlockList.setVisible(false);
            clientsBlockList.setManaged(false);
        } else {
            textArea.clear();
            upperPanel.setVisible(false);
            upperPanel.setManaged(false);
            bottomPanel.setVisible(true);
            bottomPanel.setManaged(true);
            clientsList.setVisible(true);
            clientsList.setManaged(true);
            clientsBlockList.setVisible(true);
            clientsBlockList.setManaged(true);
        }
    }


    public void connect() {
        try {
            socket = new Socket(IP_ADRES,PORT);
            in = new DataInputStream(socket.getInputStream()); //инициализируем потоки
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                    try {
                        while (true){
                            String str = in.readUTF();
                            if (str.equals("/authok")){
                                setAuthorized(true);
                                break;
                            } else {
//                                for (TextArea o : textAreas){
//                                    o.appendText(str + "\n");
//                                }
                               textArea.appendText(str + "\n");
                            }
                        }


                        while (true){
                            String str = in.readUTF();
                            if (str.startsWith("/")){
                                if (str.equals("/serverClosed")) break;
                                if (str.startsWith("/clientslist ")){
                                    String[] tokens = str.split(" ");
                                    Platform.runLater(() -> {
                                        clientsList.getItems().clear();
                                        for (int i = 1; i < tokens.length; i++) {
                                            clientsList.getItems().add(tokens[i]);
                                        }
                                    });
                                }
                                if (str.startsWith("/blacklist ")){
                                    String[] tokens = str.split(" ");
                                    Platform.runLater(() -> {
                                        clientsBlockList.getItems().clear();
                                        for (int i = 1; i < tokens.length; i++) {
                                            clientsBlockList.getItems().add(tokens[i]);
                                        }
                                    });
                                }
                            }else{
                                textArea.appendText(str + "\n");
                            }


                        }
                    }catch (IOException e) {
                        e.printStackTrace();
                    }finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        setAuthorized(false);
                    }
                }
            ).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(ActionEvent actionEvent) {
        try {
            out.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Dispose(){
        System.out.println("Отправляем сообщение на сервер о завершении работы");
        try {
            if (out != null){
                out.writeUTF("/end");
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void tryToAuth(ActionEvent actionEvent){
        if (socket == null || socket.isClosed()){
            connect();
        }
        try {
            out.writeUTF("/auth " + loginField.getText() + " " + passwordField.getText());
            loginField.clear();
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void tryToSignup(ActionEvent actionEvent){
        if (socket == null || socket.isClosed()){
            connect();
        }
        try {
            out.writeUTF("/signup " + signupLoginField.getText() + " " + signupPasswordField.getText() + " " + signupNickField.getText());
            signupLoginField.clear();
            signupPasswordField.clear();
            signupNickField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
