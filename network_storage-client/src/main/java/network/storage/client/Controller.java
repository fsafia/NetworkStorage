package network.storage.client;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import network.storage.common.Comand;
import network.storage.common.ProtoFileSender;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;


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



    private boolean isAuthorized;
    private Network network ;
    private ChannelHandlerContext context;
    private Channel channel;
    ProtoFileSender protoFileSender;

    public boolean getIsAuthorized() {
        return isAuthorized;
    }

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


//    public void connect() {
//        try {
//            new Thread(() -> {
//                    try {
//                        while (true){
//                            String str = in.readUTF();
//                            if (str.equals("/authok")){
//                                setAuthorized(true);
//                                break;
//                            } else {
////                                for (TextArea o : textAreas){
////                                    o.appendText(str + "\n");
////                                }
//                               textArea.appendText(str + "\n");
//                            }
//                        }
//
//
//                        while (true){
//                            String str = in.readUTF();
//                            if (str.startsWith("/")){
//                                if (str.equals("/serverClosed")) break;
//                                if (str.startsWith("/clientslist ")){
//                                    String[] tokens = str.split(" ");
//                                    Platform.runLater(() -> {
//                                        clientsList.getItems().clear();
//                                        for (int i = 1; i < tokens.length; i++) {
//                                            clientsList.getItems().add(tokens[i]);
//                                        }
//                                    });
//                                }
//                                if (str.startsWith("/blacklist ")){
//                                    String[] tokens = str.split(" ");
//                                    Platform.runLater(() -> {
//                                        clientsBlockList.getItems().clear();
//                                        for (int i = 1; i < tokens.length; i++) {
//                                            clientsBlockList.getItems().add(tokens[i]);
//                                        }
//                                    });
//                                }
//                            }else{
//                                textArea.appendText(str + "\n");
//                            }
//
//
//                        }
//                    }catch (IOException e) {
//                        e.printStackTrace();
//                    }finally {
//                        try {
//                            socket.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        setAuthorized(false);
//                    }
//                }
//            ).start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

//    public void sendMsg(ActionEvent actionEvent) {
//        try {
//            out.writeUTF(textField.getText());
//            textField.clear();
//            textField.requestFocus();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void Dispose(){
//        System.out.println("Отправляем сообщение на сервер о завершении работы");
//        try {
//            if (out != null){
//                out.writeUTF("/end");
//            }
//        }catch (IOException e){
//            e.printStackTrace();
//        }
//    }
    public void connect() throws InterruptedException {
        CountDownLatch networkStarter = new CountDownLatch(1);
        new Thread(() -> {
            network = new Network(this);
            network.start(networkStarter);
        }).start();
        networkStarter.await();
    }
    public void tryToAuth(ActionEvent actionEvent) throws IOException, InterruptedException {
        if (channel == null ){
            connect();
            channel = network.currentChannel;
            protoFileSender = new ProtoFileSender(channel/*, "1client-storage"*/);
        }

        String authString = loginField.getText() + " " + passwordField.getText();
        protoFileSender.sendAuth(Comand.TRY_TO_AUTH,authString, future -> {
            if (!future.isSuccess()) {
                future.cause().printStackTrace();
                System.out.println("Log and Pass не передан");
//                Network.getInstance().stop();
            }
            if (future.isSuccess()) {
                System.out.println("Log and Pass успешно передан");
//                Network.getInstance().stop();
            }
        });


//        try {
//            out.writeUTF("/auth " + loginField.getText() + " " + passwordField.getText());
//            loginField.clear();
//            passwordField.clear();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }

    public void tryToSignup(ActionEvent actionEvent){
//        if (socket == null || socket.isClosed()){
//            connect();
//        }
//        try {
//            out.writeUTF("/signup " + signupLoginField.getText() + " " + signupPasswordField.getText() + " " + signupNickField.getText());
//            signupLoginField.clear();
            signupPasswordField.clear();
            signupNickField.clear();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }

    public void authResponse(byte comand, String response) {
        if (comand == Comand.AUTH_OK.getNumberComand()) {
            setAuthorized(true);
        }
        if (comand == Comand.AUTH_NOT_OK.getNumberComand()) {
            textArea.appendText(response + "\n");
        }
    }

    public void Dispose(){
        System.out.println("Отправляем сообщение на сервер о завершении работы");
//        try {
//            if (out != null){
//                out.writeUTF("/end");
//            }
//        }catch (IOException e){
//            e.printStackTrace();
//        }
    }
}
