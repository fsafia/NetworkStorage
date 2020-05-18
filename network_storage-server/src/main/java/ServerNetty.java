import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class ServerNetty {
    public void run()  throws Exception {   /////////////////////////throws Exception??????????????????????????????????7
        // пул потоков для обработки подключенией клиентов
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // пул потоков для обработки сетевых подключений
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            // Cоздание настроек сервера
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)  // указание пулов потоков для работы
                    .channel(NioServerSocketChannel.class) // указание канала для подключения новых клиентов
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new StringToByteBufHandler(), new FirstHahdler(), new SecondHandler(), new GatewayHandler(), new FinalHandler());
                        }
                    });
            ChannelFuture f = b.bind(8189).sync(); // запуск прослушивания порта 8189 для подключения клиентов
            f.channel().closeFuture().sync(); // ожидание завершения работы сервера
        } finally {
            workerGroup.shutdownGracefully();  // закрытие пула
            bossGroup.shutdownGracefully();    //закрытие пула
        }
    }

    public static void main(String[] args) throws Exception{
        new ServerNetty().run();
    }
}
