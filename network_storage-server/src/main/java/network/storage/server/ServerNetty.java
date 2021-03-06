package network.storage.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import network.storage.common.ProtoHandler;

public class ServerNetty {
    public void run()  throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();     // Пул потоков для обработки подключений клиентов
        EventLoopGroup workerGroup = new NioEventLoopGroup();   // пул потоков для обработки сетевых подключений
        ProtoComandServer protoComandServer = new ProtoComandServer("1server-storage");
        ProtoHandler protoHandler = new ProtoHandler("1server-storage", protoComandServer);
        protoHandler.setOnReceivedCallback(() -> {
            protoComandServer.sendServerStorageList();
        });
        try {
            AuthService.connect(); //подключение к БД
            // Cоздание настроек сервера
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)  // указание пулов потоков для работы
                    .channel(NioServerSocketChannel.class) // указание канала для подключения новых клиентов
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(protoHandler);
                        }
                    });
            ChannelFuture f = b.bind(8189).sync(); // запуск прослушивания порта 8189 для подключения клиентов
            f.channel().closeFuture().sync(); // ожидание завершения работы сервера
        } finally {
            AuthService.disconnect();
            workerGroup.shutdownGracefully();  // закрытие пула
            bossGroup.shutdownGracefully();    //закрытие пула
        }
    }

    public static void main(String[] args) throws Exception{
        new ServerNetty().run();
    }
}
