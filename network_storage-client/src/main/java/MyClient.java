import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

//public class MyClient {
//    public static void main(String[] args) {
////***************1 вариант программы
//        try{
//            Socket socket = new Socket("localhost", 8189);
//            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
//            Scanner in = new Scanner(socket.getInputStream());
//            out.write(new byte[]{11, 21, 31});
//            String x = in.nextLine();
//            System.out.println("A: " + x);
//            in.close();
//            out.close();
//            socket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } // ********конец 1 вариант

//        try{  //*********2 вариант программы
//            Socket socket = new Socket("localhost", 8189);
//            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
//            Scanner in = new Scanner(socket.getInputStream());
//
//
//            //  отправлять команды в другом классе
//
//            ClientWant clientWant = new ClientWant(socket, out, in);
////            clientWant.toDo((byte) 1, "network_storage-client/clToServ.txt");
////            clientWant.toDo((byte) 1, "network_storage-client/1.txt");
////
//// //           clientWant.toDo((byte) 2, "network_storage-client/1.txt");
////            clientWant.toDo((byte) 2, "network_storage-client/clToServ.txt");
////            clientWant.toDo((byte) 1, "network_storage-client/2.txt");
//            clientWant.toDo((byte) 3, "network_storage-client/2.txt", "network_storage-client/3.txt");
//            clientWant.toDo((byte) 1, "network_storage-client/3.txt");
//
//            in.close();
//            out.close();
//            socket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }//**********конец 2 варианта
//
//    }
//}

public class MyClient {
    public static void main(String[] args) {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();                    // (1)
            b.group(workerGroup);                             // (2)
            b.channel(NioSocketChannel.class);                // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new OutgoingMessageHandler(), new IncomingMessageHandler());
                }
            });

            // Start the client.
            ChannelFuture f = b.connect("localhost", 8189).sync();   // (4)

            f.channel().closeFuture().sync();

        } catch ( InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }

    }
}
