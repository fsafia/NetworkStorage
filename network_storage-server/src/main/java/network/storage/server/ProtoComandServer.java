package network.storage.server;

import network.storage.common.Comand;
import network.storage.common.ProtoComand;
import network.storage.common.ProtoFileSender;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class ProtoComandServer extends ProtoComand {
    ProtoFileSender protoFileSender;

    public ProtoComandServer(String storage) {
        this.storage = storage;
    }

    public String storage;
//    String nick;

    @Override
    public void executСomand(ProtoFileSender protoFileSender, byte comand, String msgString) {
        this.protoFileSender = protoFileSender;
        if (comand == Comand.TRY_TO_SIGNUP || comand == Comand.TRY_TO_AUTH) {
            Authorization authorization = new Authorization(protoFileSender, comand, msgString, storage);
            authorization.executAutorized();
            nick = authorization.getNick();
        }
        switch (comand) {
            case Comand.TRY_TO_AUTH:
//                String n = nick;
                if (nick != null) { //авторизация прошла успешно
                    sendServerStorageList();
                }
                break;
            case Comand.CLIENT_CLOSE:
                if (nick != null) {
                    AuthService.getUserList().remove(nick);
                }
                protoFileSender.getChannel().close();
                return;

            case Comand.DELETE_FILE_FromServer:
                try {
                    Files.delete(Paths.get(storage,nick,msgString));
                    System.out.println("Файл " + (Paths.get(storage,nick,msgString).toString()) + " удален");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                sendServerStorageList();
                break;
            case Comand.RENAME_FILE_FromServer:
                try {
                    String fileOldName = msgString.split("   ")[0];
                    String fileNewName = msgString.split("   ")[1];
                    Path storagePathOld = Paths.get(storage, nick, fileOldName);
                    Path storagePathNew = Paths.get(storage, nick, fileNewName);
                    Files.move(storagePathOld, storagePathNew);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                sendServerStorageList();
                break;
            case Comand.DOWNLOAD_FILE_TO_CLIENT:
                try {
                    protoFileSender.sendFile(Paths.get(storage, nick,msgString), null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }

    }


    public void sendServerStorageList() {
//        if (!Files.exists(Paths.get(storage,nick))) {// если еще нет каталога с nick
//            return;
//        }
        try {
            List<String> fileList = null;
            fileList = Files.list(Paths.get(storage, nick))
                    .filter(p -> !Files.isDirectory(p))
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toList());
            StringBuffer sb = new StringBuffer();
            for (String file : fileList ) {
                sb = sb.append(file + "   ");
            }
//            String serverStorageList = Files.list(Paths.get(storage, nick))
//                    .filter(p -> !Files.isDirectory(p))
//                    .map(p -> p.getFileName().toString())
//                    .collect(Collectors.joining("   ", "", ""));

            protoFileSender.sendComand(Comand.SERVER_STORAGE_LiST, sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
