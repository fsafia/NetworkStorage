package network.storage.server;

import io.netty.channel.ChannelHandlerContext;
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
    public String storage;

    public ProtoComandServer(String storage) {
        this.storage = storage;
    }

    @Override
    public void executСomand(ProtoFileSender protoFileSender, byte comand, String msgString, ChannelHandlerContext ctx) {
        this.protoFileSender = protoFileSender;
        if (comand == Comand.TRY_TO_SIGNUP || comand == Comand.TRY_TO_AUTH) {
            Authorization authorization = new Authorization(protoFileSender, comand, msgString, storage);
            authorization.executAutorized();
            nick = authorization.getNick();
        }
        switch (comand) {
            case Comand.TRY_TO_AUTH:
                if (nick != null) { //авторизация прошла успешно
                    sendServerStorageList();
                }
                break;
            case Comand.CLIENT_CLOSE:
                if (nick != null) {
                    AuthService.getUserList().remove(nick);
                }
                protoFileSender.getChannel().close();
                ctx.close();
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
        try {
            String serverStorageList = Files.list(Paths.get(storage, nick))
                    .filter(p -> !Files.isDirectory(p))
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.joining("   ", "", ""));

            protoFileSender.sendComand(Comand.SERVER_STORAGE_LiST, serverStorageList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
