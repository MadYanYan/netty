/*
 * All rights Reserved, Designed By baowei
 *
 * 注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目的
 */
package com.bw.nio.channel;

/**
 * @author yan.zhang
 * @date 2019/6/6 21:35
 */


import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;


/**
 * 1.通道（channel）：用于源节点与目标节点的连接，在java NIO中负责缓冲区中数据的传输，通道不存储数据，配合缓冲区操作
 * 2.java.nio.channels.Channel接口：
 * |--FileChannel
 * |--SocketChannel       TCP
 * |--ServerSocketChannel    TCP
 * |--DatagramChannel       UDP
 * <p>
 * 3.
 * 获取通道   对支持通道的类提供了getChannel()方法
 * 1）本地IO操作： FileInputStream/FileOutPutStream
 * RandomAccessFile
 * 网络IO：
 * Socket/ServerSocket/DatagramSocket
 * <p>
 * 2）JDK 1.7后  NIO  针对各个通道提供了静态方法open()
 * 3)JDK1.7  NIO的Files工具类 newByteChannel()
 * <p>
 * 4.通道之间的数据传输
 * 底层使用到零拷贝
 * transferFrom()
 * transferTo()
 * <p>
 * 5.分散()与聚集
 * 分散读取：将通道中的数据分散到多个缓冲区中
 * 聚集写入：将多个缓冲区中的数据聚集到通道中
 */


public class TestChannel_01 {

    @Test
    public void test1() throws IOException {
        //利用通道复制
        FileInputStream fis = new FileInputStream("C:\\Users\\Lenovo\\Desktop\\宝唯\\图片\\docker.PNG");
        FileOutputStream fos = new FileOutputStream("C:\\Users\\Lenovo\\Desktop\\宝唯\\图片\\docker_copy .PNG");

        //获取通道
        FileChannel inChannel = fis.getChannel();
        FileChannel outChannel = fos.getChannel();

        //缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        //将通道中的数据存入缓冲区
        while ((inChannel.read(buffer)) != -1) {
            //缓冲区的数据写入通道
            buffer.flip();//切换成读取数据模式
            outChannel.write(buffer);//将缓冲区数据写入通道
            buffer.clear();//清空缓冲区 position,limit复位，否则position = limit 进入死循环
        }

        outChannel.close();
        inChannel.close();
        fos.close();
        fis.close();
    }


    @Test
    public void test2() throws IOException {
        //JDK1.7之后open方法
        FileChannel readChannel = FileChannel.open(Paths.get("C:\\Users\\Lenovo\\Desktop\\宝唯\\图片\\docker.PNG"), StandardOpenOption.READ);

        FileChannel writeChannel = FileChannel.open(Paths.get("C:\\Users\\Lenovo\\Desktop\\宝唯\\图片\\3.PNG"), StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);//CREATE_NEW代表文件不存在创建，存在报错

        //mappedByteBuffer 可以让文件直接在内存（堆外内存）修改
        //创建直接缓冲区  缓冲区在物理内存中 ，操作系统级别的修改，性能较高，无需将文件copy到堆内存
        //参数1：读写模式  参数2：修改的起始位置 参数3：映射到内存的大小（不是索引位置），即将文件映射到内存的大小
        MappedByteBuffer inMapBuffer = readChannel.map(FileChannel.MapMode.READ_ONLY, 0, readChannel.size());
        MappedByteBuffer outMapBuffer = writeChannel.map(FileChannel.MapMode.READ_WRITE, 0, readChannel.size());

        //直接对缓冲区数据读取操作  inMapBuffer  --->  outMapBuffer
        byte[] bytes = new byte[inMapBuffer.limit()];
        System.out.println(inMapBuffer.limit());
        inMapBuffer.get(bytes);
        outMapBuffer.put(bytes);

        readChannel.close();
        writeChannel.close();
    }

    @Test
    public void test3() throws IOException {
        //通道之间的数据传输
        FileChannel readChannel = FileChannel.open(Paths.get("C:\\Users\\Lenovo\\Desktop\\宝唯\\图片\\docker.PNG"),
                StandardOpenOption.READ);

        FileChannel writeChannel = FileChannel.open(Paths.get("C:\\Users\\Lenovo\\Desktop\\宝唯\\图片\\3.PNG"),
                StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);

//        readChannel.transferTo(0, readChannel.size(), writeChannel);
        //或者
        writeChannel.transferFrom(readChannel, 0, readChannel.size());
        readChannel.close();
        writeChannel.close();
    }

    @Test
    public void test4() throws IOException {
        int a = 17;
        int b = 8;

//      res = a % b == 0 ? a / b : a / b + 1;

        double ceil = Math.ceil((double) a / b);
        System.out.println((int) ceil);

    }


}
