import com.sun.security.ntlm.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

/**
 * 服务端
 */
public class UpgradeServer {

    //定义一个集合用来存放 监听到的客户端socket
    public static ArrayList<Socket> socketList = new ArrayList<Socket>();

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            //新建一个服务端ServerSocket,端口号为8888
            serverSocket = new ServerSocket(8888);
            System.out.println("等待客户端连接!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            Socket socket = null;
            while (true) {
                try {
                    //监听客户端的连接
                    socket = serverSocket.accept();
                    System.out.println("["+new Date()+"]客户端 " + socket.getInetAddress().getHostAddress() + " 连接成功！");

                    //开启客户端接收信息线程
                    new Thread(new ReceiveThreat(socket)).start();
                    //开始客户端发送信息线程
                    new Thread(new SendThreat(socket)).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

/**
 * 接收信息线程
 */
class ReceiveThreat implements Runnable {
    Socket socket;
    BufferedInputStream bis;
    public ReceiveThreat(Socket socket) {
        super();
        this.socket = socket;
        try {
            //获取socket的输入流
            bis = new BufferedInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        //获取指令,操作后台，通知发送线程

        //一次最多读取1KB的内容
        byte[] b=new byte[1024];
        //实际读取的字节数
        int length = 0 ;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        while (true){
            try {
                length=bis.read(b);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //读完就结束死循环
            if(!(length!=-1)) break;
            //未读完则继续存入容器
            out.write(b,0,length);
        }
        byte[] orderIn=out.toByteArray();
        System.out.println(orderIn.length);
    }
}
/**
 * 发送信息线程
 */
class SendThreat implements Runnable {
    Socket socket;
    //使用BufferedWriter流来向客户端发送信息
    BufferedOutputStream bio;

    public SendThreat(Socket socket) {
        super();
        this.socket = socket;
        try {
            //接收socket的字节输出流，用OutputStreamWriter把字节输出流转化为字符流，再传给PrintWriter
            bio = new BufferedOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        while (true) {
            //监听状态，进行发送

        }
    }
}