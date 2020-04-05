package com.bw.nio.niodemo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class NioServer {
    public static void main(String[] args) throws IOException {
        eventDriver();
    }

    private static Selector selector;

    private static void eventDriver() throws IOException {

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        selector = Selector.open();

        serverSocketChannel.socket().bind(new InetSocketAddress(6666));

        serverSocketChannel.configureBlocking(false);
        //channel注册到selector 关注OP_ACCEPT事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        //等待客户端连接
        while (true) {
            //没有事件发生
            if (selector.select(1000) == 0) {
                System.out.println("server wait...");
                continue;
            }
            //有关注事件发生
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            if (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                //ready to accept a new socket connection.
                if (selectionKey.isAcceptable()) {
                    //block method
                    SocketChannel socketChannel = serverSocketChannel.accept();

                    socketChannel.configureBlocking(false);
                    //socketChannel注册到selector,关注事件为READ事件,并分配buffer
                    socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));

                    System.out.println("客户端连接成功" + socketChannel.hashCode());
                }

                //读事件  
                if (selectionKey.isReadable()) {
                    //获取到selectionKey对应的channel
                    SocketChannel channel = (SocketChannel) selectionKey.channel();

                    ByteBuffer buffer = (ByteBuffer) selectionKey.attachment();

                    int index = channel.read(buffer);

                    System.out.println("from client " + new String(buffer.array(), 0, index));


                    /**
                     * 第二部分
                     */
                    readData(selectionKey);
                }
                //处理key后remove,防止重复处理
                iterator.remove();
            }

        }
    }

    /**
     * 读取客户端消息
     */
    private static void readData(SelectionKey key) {
        //取出 key关联的channel
        SocketChannel attChannel;
        try {
            attChannel = (SocketChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int count = attChannel.read(buffer);
            //如果读取到数据
            if (count > 0) {
                String msg = new String(buffer.array());
                System.out.println("from client: " + msg);

                //向其他客户端处理数据
                sent2Other(msg, attChannel);
            }

        } catch (IOException e) {

        }
    }

    private static void sent2Other(String msg, SocketChannel self) throws IOException {
        System.out.println("消息转发中");

        Iterator<SelectionKey> iterator = selector.keys().iterator();
        while (iterator.hasNext()) {

            Channel targetChannel = iterator.next().channel();

            //排除自己 和 serverSocket
            if (targetChannel instanceof SocketChannel && targetChannel != self) {
                SocketChannel dest = (SocketChannel) targetChannel;
                ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
                //buffer数据写入channel
                dest.write(buffer);
            }

        }
    }


}
