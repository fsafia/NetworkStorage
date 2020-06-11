package network.storage.client;

import io.netty.channel.ChannelHandlerContext;
import network.storage.common.Comand;
import network.storage.common.ProtoComand;
import network.storage.common.ProtoFileSender;

import java.io.IOException;

public class ProtoComandClient extends ProtoComand {
    public Controller c;

    public ProtoComandClient(Controller c) {
        this.c = c;
    }
    @Override
    public void executСomand(ProtoFileSender protoFileSender, byte cmd, String msgString, ChannelHandlerContext ctx) {
        if (cmd == Comand.AUTH_OK || cmd == Comand.AUTH_NOT_OK) {
            try {
                c.authResponse(cmd, msgString);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (cmd == Comand.SERVER_STORAGE_LiST) {
            c.updateServerStorage(msgString);
        }

    }
}
