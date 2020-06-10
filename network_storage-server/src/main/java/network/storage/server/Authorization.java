package network.storage.server;

import io.netty.channel.ChannelHandlerContext;
import network.storage.common.Comand;
import network.storage.common.ProtoFileSender;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class Authorization {
    private String sorage;
    private ProtoFileSender protoFileSender;
    private byte comand;
    private String logPass;
    private String nick;
    public String getNick() {
        return nick;
    }
    public void setNick(String nick) {
        this.nick = nick;
    }

    public Authorization(ProtoFileSender protoFileSender, byte comand, String logPass, String storage) {
        this.protoFileSender = protoFileSender;
        this.comand = comand;
        this.logPass = logPass;
        this.sorage = storage;
    }

    public void executAutorized() {
        switch (comand) {
            case Comand.TRY_TO_AUTH:
                if (isLogAndPassValidate(logPass)) {
                    AuthService.getUserList().add(nick);
                }
                break;

            case Comand.TRY_TO_SIGNUP:
                if (isLogAndPassAndNickValidate(logPass)) {
                    AuthService.getUserList().add(nick);
                }
                break;
        }
    }

    private boolean isLogAndPassValidate(String logAndPass) {
        boolean rezult = false;
        String log = logAndPass.split(" ")[0];
        String pass = logAndPass.split(" ")[1];
        nick = AuthService.getNickByLoginAndPass(log, pass);
        try {
            if (nick != null) {

                if (isNickInUserList(nick)) {
                    protoFileSender.sendComand(Comand.AUTH_NOT_OK, "Пользователь с " + nick + " уже авторизован! "/*, null*/);
                    rezult = false;
                    nick = null;
                } else {
                    protoFileSender.sendComand(Comand.AUTH_OK, nick);    //отправить ответ клиенту авторизован
//                    protoFileSender.sendServerStorageList(Comand.SERVER_STORAGE_LiST, getServerStorageList("1server-storage"));
//                    sendServerStorageList(protoFileSender, Paths.get(storage, authHandler.nick));
                    rezult = true;
                }
            } else {
                protoFileSender.sendComand(Comand.AUTH_NOT_OK, "Неверный логин, пароль!"/*, null*/);
                rezult = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rezult;
    }


    public boolean isNickInUserList(String nick) {
        for (String u : AuthService.getUserList()) {
            if (nick.equals(u)) {
                return true;
            }
        }
        return false;
    }

    private boolean isLogAndPassAndNickValidate(String logPassNick) {
        boolean rezult = false;
        String log = logPassNick.split(" ")[0];
        String pass = logPassNick.split(" ")[1];
        nick = logPassNick.split(" ")[2];
        try {
            //проверка уникальности логин  и nick
            if (AuthService.isLoginUnique(log)) {
                if (AuthService.isNickUnique(nick)) {
                    AuthService.addUser(log, pass, nick);
                    protoFileSender.sendComand(Comand.AUTH_OK, nick/*, null*/);//отправить ответ клиенту авторизован
                    Files.createDirectory(Paths.get(sorage,nick));
                    rezult = true;
                } else {
                    protoFileSender.sendComand(Comand.AUTH_NOT_OK, "Пользователь с " + nick + " уже существует!"/*, null*/);
                    nick = null;
                    rezult = false;
                }
            } else {
                protoFileSender.sendComand(Comand.AUTH_NOT_OK, "Пользователь с " + log + " уже существует!"/*, null*/);
                nick = null;
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rezult;
    }
}
