package network.storage.client;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import network.storage.common.Comand;
import network.storage.common.ProtoFileSender;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;


public class Controller {
//    @FXML
//    TextArea textArea;

    @FXML
    TextField textFieldClient, textFieldServer;

    @FXML
    HBox workPanel;
    @FXML
    HBox authPanel;
    @FXML
    TextField loginField, signupLoginField, signupNickField;
    @FXML
    PasswordField passwordField, signupPasswordField;
    @FXML
    ListView<String> localStorage, serverStorage;



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
            //
            authPanel.setVisible(true);
            authPanel.setManaged(true);
            workPanel.setVisible(false);
            workPanel.setManaged(false);

//            authPanel.setVisible(true);
//            authPanel.setManaged(true);
//            workPanel.setVisible(false);
//            workPanel.setManaged(false);
//            localStorage.setVisible(false);
//            localStorage.setManaged(false);
//            serverStorage.setVisible(false);
//            serverStorage.setManaged(false);
        } else {
            //
            authPanel.setVisible(false);
            authPanel.setManaged(false);
            workPanel.setVisible(true);
            workPanel.setManaged(true);


//            authPanel.setVisible(false);
//            authPanel.setManaged(false);
//            workPanel.setVisible(true);
//            workPanel.setManaged(true);
//            localStorage.setVisible(true);
//            localStorage.setManaged(true);
//            serverStorage.setVisible(true);
//            serverStorage.setManaged(true);

            localStorage.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    textFieldClient.clear();
                    textFieldClient.setText(localStorage.getSelectionModel().getSelectedItem());
                }
            });

            serverStorage.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    textFieldServer.clear();
                    textFieldServer.setText(serverStorage.getSelectionModel().getSelectedItem());
                }
            });
        }
    }

    public void connect() throws InterruptedException {
        CountDownLatch networkStarter = new CountDownLatch(1);
        new Thread(() -> {
            network = new Network(this);
            network.start(networkStarter);
        }).start();
        networkStarter.await();
        channel = network.currentChannel;
        protoFileSender = new ProtoFileSender(channel/*, "1client-storage"*/);
    }


    public void tryToAuth(ActionEvent actionEvent) throws IOException, InterruptedException {
        if (channel == null ){
            connect();
        }

        String authString = loginField.getText() + " " + passwordField.getText();
        protoFileSender.sendComand(Comand.TRY_TO_AUTH, authString, future -> {
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
        loginField.clear();
        passwordField.clear();
    }

    public void tryToSignup(ActionEvent actionEvent) throws InterruptedException, IOException {
        if (channel == null ){
            connect();
        }
        String signupString = signupLoginField.getText() + " " + signupPasswordField.getText() + " " + signupNickField.getText();
        protoFileSender.sendComand(Comand.TRY_TO_SIGNUP, signupString, null);
        signupLoginField.clear();
        signupPasswordField.clear();
        signupNickField.clear();
    }

    public void sendMsg (ActionEvent actionEvent) {

    }

    public void deleteRemoteFile (ActionEvent actionEvent) throws IOException {
        protoFileSender.sendComand(Comand.DELETE_FILE_FromServer, textFieldServer.getText(), null);
        textFieldServer.clear();
    }

    public void deleteLocalFile (ActionEvent actionEvent) throws IOException {
        Files.delete(Paths.get("1client-storage", textFieldClient.getText()));
        textFieldClient.clear();
        updateLocalStorage();
    }

    public void sendFileToServer (ActionEvent actionEvent) throws IOException {
        protoFileSender.sendFile(Paths.get("1client-storage", textFieldClient.getText()), null);
        textFieldClient.clear();
    }
    public void downloadToClient (ActionEvent actionEvent) throws IOException {
        protoFileSender.sendComand(Comand.DOWNLOAD_FILE_TO_CLIENT, textFieldServer.getText(), null);
        textFieldServer.clear();
    }

    public void authResponse(byte comand, String response) throws IOException {
        if (comand == Comand.AUTH_OK) {
            setAuthorized(true);
            updateLocalStorage();
        }
        if (comand == Comand.AUTH_NOT_OK) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Подключиться не удалось!");
                alert.setHeaderText(null);// Header Text: null
                alert.setContentText(response);
                alert.showAndWait();
            });
        }
    }
    public void updateServerStorage(String fileList) {
        String[] files = fileList.split("   ");
        Platform.runLater(() -> {
            serverStorage.getItems().clear();
            for (int i = 0; i < files.length; i++) {
                serverStorage.getItems().add(files[i]);
            }
        });
    }
    public void updateLocalStorage() {
        List<String> fileList = null;
        try {
            fileList = Files.list(Paths.get("1client-storage"))
                    .filter(p -> !Files.isDirectory(p))
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        localStorage.getItems().clear();
        for (String f: fileList ) {
            localStorage.getItems().add(f);
        }
    }

    public String selectItemFromLocalStorage() {
        return localStorage.getSelectionModel().getSelectedItem();
    }

    public void Dispose(){
        System.out.println("Отправляем сообщение на сервер о завершении работы");
        if(channel != null ){
            protoFileSender.sendClose(Comand.CLIENT_CLOSE);
        }
    }
}
