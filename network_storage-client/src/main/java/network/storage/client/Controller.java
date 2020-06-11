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
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;


public class Controller {
    @FXML
    TextField textFieldClient, textFieldServer, textFieldnewNameClient, textFieldnewNameServer;
    @FXML
    HBox workPanel;
    @FXML
    VBox authPanel, panelRenameFileClient, panelRenameFileServer;
    @FXML
    TextField loginField, signupLoginField, signupNickField;
    @FXML
    PasswordField passwordField, signupPasswordField;
    @FXML
    ListView<String> localStorage, serverStorage;
    @FXML
    Button buttonRenameFileClient, buttonRenameFileServer;

    private boolean isAuthorized;
    private Network network ;
    private ChannelHandlerContext context;
    private Channel channel;
    ProtoFileSender protoFileSender;
    private String oldNameClient, oldNameServer;
    private String newNameClient, newNameServer;

    public boolean getIsAuthorized() {
        return isAuthorized;
    }

    public void setAuthorized(boolean isAuthorized, Network network){
        this.isAuthorized = isAuthorized;
        if(!isAuthorized){
            authPanel.setVisible(true);
            authPanel.setManaged(true);
            workPanel.setVisible(false);
            workPanel.setManaged(false);
        } else {
            authPanel.setVisible(false);
            authPanel.setManaged(false);
            workPanel.setVisible(true);
            workPanel.setManaged(true);

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
            network.setReceivedCallback(() -> {
                updateLocalStorage();
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
        channel = network.getCurrentChannel();
        protoFileSender = network.protoFileSender;
    }


    public void tryToAuth(ActionEvent actionEvent) throws IOException, InterruptedException {
        if (channel == null ){
            connect();
        }

        String authString = loginField.getText() + " " + passwordField.getText();
        protoFileSender.sendComand(Comand.TRY_TO_AUTH, authString/*, null*/);
        loginField.clear();
        passwordField.clear();
    }

    public void tryToSignup(ActionEvent actionEvent) throws InterruptedException, IOException {
        if (channel == null ){
            connect();
        }
        String signupString = signupLoginField.getText() + " " + signupPasswordField.getText() + " " + signupNickField.getText();
        protoFileSender.sendComand(Comand.TRY_TO_SIGNUP, signupString/*, null*/);
        signupLoginField.clear();
        signupPasswordField.clear();
        signupNickField.clear();
    }

    public void deleteRemoteFile (ActionEvent actionEvent) throws IOException {
        if (isFileNotSelected(textFieldServer, "Выберите файл для удаления.")) {
            return;
        }
        protoFileSender.sendComand(Comand.DELETE_FILE_FromServer, textFieldServer.getText()/*, null*/);
        textFieldServer.clear();
    }

    public void deleteLocalFile (ActionEvent actionEvent) throws IOException {
        if (isFileNotSelected(textFieldClient, "Выберите файл для удаления.")) {
            return;
        }
        Files.delete(Paths.get("1client-storage", textFieldClient.getText()));
        textFieldClient.clear();
        updateLocalStorage();
    }

    public void sendFileToServer (ActionEvent actionEvent) throws IOException {
        if (isFileNotSelected(textFieldClient, "Выберите файл для отправки.")) {
            return;
        }
        protoFileSender.sendFile(Paths.get("1client-storage", textFieldClient.getText()), null);
        textFieldClient.clear();
    }
    public void downloadToClient (ActionEvent actionEvent) throws IOException {
        if (isFileNotSelected(textFieldServer, "Выберите файл для отправки.")) {
            return;
        }
        protoFileSender.sendComand(Comand.DOWNLOAD_FILE_TO_CLIENT, textFieldServer.getText()/*, null*/);
        textFieldServer.clear();
    }

    public void authResponse(byte comand, String response) throws IOException {
        if (comand == Comand.AUTH_OK) {
            setAuthorized(true, network);
            updateLocalStorage();
        }
        if (comand == Comand.AUTH_NOT_OK) {
            Platform.runLater(() -> {
//                Alert alert = new Alert(Alert.AlertType.INFORMATION);
//                alert.setTitle("Подключиться не удалось!");
//                alert.setHeaderText(null);// Header Text: null
//                alert.setContentText(response);
//                alert.showAndWait();

                Alert alert = new Alert(Alert.AlertType.INFORMATION, response, ButtonType.OK);
                alert.setHeaderText(null);
                alert.showAndWait();
            });
        }
    }

    public void updateServerStorage(String fileList) {
        if (fileList.equals("empty")) {
            Platform.runLater(() -> {
                serverStorage.getItems().clear();
            });
        } else {String[] files = fileList.split("   ");
            Platform.runLater(() -> {
                serverStorage.getItems().clear();
                for (int i = 0; i < files.length; i++) {
                    serverStorage.getItems().add(files[i]);
                }
            });
        }


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

    public void renameLocalFile(ActionEvent actionEvent) {
        if (isFileNotSelected(textFieldClient, "Выберите файл для переименования.")) {
            return;
        }
        oldNameClient = textFieldClient.getText();
        renamePanelAclive(buttonRenameFileClient , panelRenameFileClient);
    }

    public void renameClientOk(ActionEvent actionEvent) throws IOException {
        if (isFileNotSelected(textFieldnewNameClient, "Введите новое имя файла.")) {
            return;
        }
        newNameClient = textFieldnewNameClient.getText();
        Files.move(Paths.get("1client-storage",oldNameClient), Paths.get("1client-storage", newNameClient));
        Alert alert = new Alert(Alert.AlertType.INFORMATION, oldNameClient+ " успешно переименован в " + newNameClient, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
        updateLocalStorage();
        renamePanelInAclive(buttonRenameFileClient, panelRenameFileClient);
        textFieldClient.clear();
        textFieldnewNameClient.clear();
    }

    private boolean isFileNotSelected(TextField textField, String info) {
        if (textField.getText().equals("")) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, info, ButtonType.OK);
            alert.setHeaderText(null);
            alert.showAndWait();
            return true;
        } else return false;
    }


    private void renamePanelAclive(Button button, VBox renamePanel) {
        button.setManaged(false);
        button.setVisible(false);
        renamePanel.setVisible(true);
        renamePanel.setManaged(true);
    }
    private void renamePanelInAclive(Button button, VBox renamePanel) {
        button.setManaged(true);
        button.setVisible(true);
        renamePanel.setVisible(false);
        renamePanel.setManaged(false);
    }

    public void cancelClient(ActionEvent actionEvent) {
        renamePanelInAclive(buttonRenameFileClient, panelRenameFileClient);
        textFieldClient.clear();
    }

    public void Dispose() {
        System.out.println("Отправляем сообщение на сервер о завершении работы");
        try {
            if(channel != null ){
                protoFileSender.sendComand(Comand.CLIENT_CLOSE, "close");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void renameRemoteFile(ActionEvent actionEvent) {
        if (isFileNotSelected(textFieldServer, "Выберите файл для переименования.")) {
            return;
        }
        oldNameServer = textFieldServer.getText();
        renamePanelAclive(buttonRenameFileServer, panelRenameFileServer);
    }

    public void renameServerOk(ActionEvent actionEvent) throws IOException {
        if (isFileNotSelected(textFieldnewNameServer, "Введите новое имя файла.")) {
            return;
        }
        newNameServer = textFieldnewNameServer.getText();
        String msgServerRename = oldNameServer + "   " + newNameServer;
        protoFileSender.sendComand(Comand.RENAME_FILE_FromServer, msgServerRename/*, null*/);
        renamePanelInAclive(buttonRenameFileServer, panelRenameFileServer);
        textFieldServer.clear();
        textFieldnewNameServer.clear();
    }
    public void cancelServer(ActionEvent actionEvent) {
        renamePanelInAclive(buttonRenameFileServer, panelRenameFileServer);
        textFieldServer.clear();
    }

}
