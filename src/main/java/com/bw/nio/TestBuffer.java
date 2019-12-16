/*
 * All rights Reserved, Designed By baowei
 *
 * 注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目的
 */
package com.bw.nio;

import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * @author yan.zhang
 * @date 2019/5/24 22:36
 */
public class TestBuffer {

    /**
     * 1.缓冲区（buffer）：在java nio中负责数据的存取，缓冲区底层就是数组实现，用于存储不同类型的数据
     * 根据数据类型不同（boolean除外） 提供了对应类型的数据缓冲区
     * ByteBuffer CharBuffer ShortBuffer IntBuffer LongBuffer FloatBuffer DoubleBuffer
     * <p>
     * 上述缓冲区的管理方式基本一致  通过allocate()获取一个缓冲区
     * <p>
     * 2.缓冲区存取数据的两个核心方法
     * put():存入数据到缓冲区中
     * get():获取缓冲区的数据
     * <p>
     * <p>
     * 3.Class Buffer 基本属性
     * private int mark = -1;
     * private int position = 0;  位置，表示缓冲区中正在操作数据的位置
     * private int limit;      界限，标识缓冲区可以操作数据的大小（limit 后数据不能读写操作）
     * private int capacity;  容量，缓冲区中最大存储数据的容量，一旦声明不能改变
     * 必须满足条件  position <= limit <= capacity
     * <p>
     * <p>
     * 5.直接缓冲区和非直接缓冲区
     * 非直接缓冲区：allocate（）方法分配缓冲区，将缓冲区建立在JVM之上
     * 直接缓冲区：通过allocateDirect()方法分配直接缓冲区，将缓冲区建立在物理内存中，可以提高效率
     */


    @Test
    public void test1() {
        //1.分配指定大小的缓冲区
        ByteBuffer bf = ByteBuffer.allocate(1024);
        System.out.println("--------------allocate()-------------");
        System.out.println(bf.position());
        System.out.println(bf.limit());
        System.out.println(bf.capacity());


        //2.put()存入数据到缓冲区中
        System.out.println("--------------put()-------------");
        String str = "abcdef";
        bf.put(str.getBytes());
        System.out.println(bf.position());
        System.out.println(bf.limit());
        System.out.println(bf.capacity());

        //3.切换成读取数据模式  position归零，limit变为上一次position位置
        System.out.println("--------------flip()-------------");
        bf.flip();
        System.out.println(bf.position());
        System.out.println(bf.limit());
        System.out.println(bf.capacity());

        //4.利用get()读取缓冲区数据
        System.out.println("--------------get()-------------");
        byte[] dst = new byte[bf.limit()];
        bf.get(dst);
        System.out.println(new String(dst, 0, dst.length));
        System.out.println(bf.position());
        System.out.println(bf.limit());
        System.out.println(bf.capacity());

        //5.利用rewind()读取缓冲区数据
        System.out.println("--------------rewind()-------------");
        //重新回到读取模式
        bf.rewind();
        System.out.println(bf.position());
        System.out.println(bf.limit());
        System.out.println(bf.capacity());

        //6 .利用clear()清空缓冲区，但是缓冲区中的数据依然存在，但是属于被遗忘状态
        System.out.println("--------------clear()-------------");
        bf.clear();
        System.out.println(bf.position());
        System.out.println(bf.limit());
        System.out.println(bf.capacity());

        //即使clear数据依然存在
        System.out.println((char) bf.get());


    }

    @Test
    public void test2() {
        //创建直接缓冲区
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        //判断是否是直接缓冲区
        System.out.println(byteBuffer.isDirect());
    }
}
