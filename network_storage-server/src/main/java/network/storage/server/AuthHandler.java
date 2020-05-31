package network.storage.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import network.storage.common.Comand;
import network.storage.common.ProtoFileSender;
import network.storage.common.ProtocolLogPass;

import java.io.IOException;

public class AuthHandler extends ChannelInboundHandlerAdapter {
    private boolean authOk = false;
    private String nick;
    ProtoFileSender protoFileSender;
    ProtocolLogPass protocolLogPass = new ProtocolLogPass();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (authOk) {
            ctx.fireChannelRead(msg);
        } else {
            protoFileSender = new ProtoFileSender(ctx.channel());
            ByteBuf bufMsg = (ByteBuf) msg;
            byte cmd = bufMsg.readByte();

            if (cmd == Comand.CLIENT_CLOSE) { //если user не авторизовался и закрыл соединение
                ctx.close();
                return;
            }

            String msgString = getMsgFromClient(cmd, ctx, bufMsg); // приходит сообщение в виде: log1_pass1
            String log = msgString.split(" ")[0];
            String pass = msgString.split(" ")[1];

            //авторизация
            if (cmd == Comand.TRY_TO_AUTH) {
                nick = AuthService.getNickByLoginAndPass(log, pass);
                if (chekLogAndPass(log, pass, nick)) {
                    authOk = true;
                    AuthService.getUserList().add(nick);
                }
            }

            // регистрация
            if (cmd == Comand.TRY_TO_SIGNUP) {
                nick = msgString.split(" ")[2];
                //проверка уникальности логин  и nick
                if(checkUniqueLogAndNick(log, pass, nick)) {
                    authOk = true;
                    AuthService.getUserList().add(nick);
                    System.out.println(AuthService.getUserList());
                } else {
                    nick = null;
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    public String getMsgFromClient(byte comand, ChannelHandlerContext ctx, ByteBuf bufMsg) throws Exception {
        protocolLogPass.executeComand(comand, ctx, bufMsg);
        return protocolLogPass.msgString;
    }

    private boolean chekLogAndPass(String log, String pass, String nick) throws IOException {
        if (nick != null) {
            if (isNickInUserList(nick)) {
                protoFileSender.sendComand(Comand.AUTH_NOT_OK, "Пользователь с " + nick + " уже авторизован! ", null);
                return false;
            } else {
                protoFileSender.sendComand(Comand.AUTH_OK, nick, null);//отправить ответ клиенту авторизован
                return true;
            }
        } else {
            protoFileSender.sendComand(Comand.AUTH_NOT_OK, "Неверный логин, пароль!", null);
            return false;
        }
    }

    private boolean checkUniqueLogAndNick(String log, String pass, String nickNew) throws IOException {
        if (AuthService.isLoginUnique(log)) {
            if (AuthService.isNickUnique(nickNew)) {
                AuthService.addUser(log, pass, nickNew);
                protoFileSender.sendComand(Comand.AUTH_OK, nick, null);//отправить ответ клиенту авторизован
                return true;
            } else {
                protoFileSender.sendComand(Comand.AUTH_NOT_OK, "Пользователь с " + nickNew + " уже существует!", null);
                return false;
            }
        } else {
            protoFileSender.sendComand(Comand.AUTH_NOT_OK, "Пользователь с " + log + " уже существует!", null);
            return false;
        }
    }

    public boolean isNickInUserList(String nick) {
        for (String u: AuthService.getUserList()) {
            if (nick == u) {
                return true;
            }
        }
        return false;
    }

    public boolean getAuthOk() {
        return authOk;
    }

    public void setAuthOk(boolean authOk) {
        this.authOk = authOk;
    }

    public String getNick() {
        return nick;
    }
}
