
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

import static com.sun.corba.se.impl.orbutil.ORBUtility.bytesToInt;

/**
 * 服务端
 */
@Slf4j
public class UpgradeServer {

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            //新建一个服务端ServerSocket,端口号为8888
            serverSocket = new ServerSocket(8888);
            log.info ( "等待客户端连接!" );
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

        /*判断客户端socket未关闭&&(如果关闭了,服务端也要关闭,升级完成或者客户端异常)=>长连接要搞心跳监控对端状态*/
        while(true/*running*/){

            //当前读到的字节总数
            int totalReadBytes=0;
            //LENGTH字段总长(字节)
            int lengthSize=4;
            //command字段总长(字节)
            int cmdSize=4;
            //data字段：业务数据总长(字节)=bytesToInt(lengthSize)
            int busiDataSize=0;
            //报文数据总长(字节)
            int totalSize=0;
            //每次读step个字节(对应读次序的字段的字节数)
            int step=0;
            StringBuffer sb = new StringBuffer();
            //读次序
            int seq=0;
            int lengthSeq=1;
            int cmdSeq=2;
            int dataSeq=3;
            //十六进制指令
            String cmdHex="";

            /*客户端没有数据发送过来,read一直在等待阻塞 | break 之后还会进来,然后继续等待客户端数据。。。。循环往复*/
            while (true) {
                seq++;
                try {
                    if(seq==lengthSeq){
                        step=lengthSize;
                        System.out.println(">>>[seq:"+seq+"]"+"["+new Date()+"]开始读取LENGTH");
                    }else if(seq==cmdSeq){
                        step=cmdSize;
                        System.out.println( ">>>[seq:"+seq+"]"+"["+new Date()+"] 开始读取COMMAND");
                    }else if(seq==dataSeq){
                        step=busiDataSize;
                        System.out.println( ">>>[seq:"+seq+"]"+"["+new Date()+"] 开始读取DATA");
                    }//封装出去

                    byte[] buf = new byte[step];
                    //实际每次读到的字符数
                    int readBytes=bis.read(buf,0,buf.length);/*调试技巧：如果debug线程消失,但是程序还在运行,就是阻塞了*/
                    totalReadBytes+=readBytes;

                    String str = new String(buf);
                    sb.append(str);
                    System.out.println(str);

                    //获取指令
                    if(seq==cmdSeq){
                        cmdHex=InteractionUtil.bytesToHexString(buf);
                        System.out.println("-> COMMAND[Hex]:"+cmdHex);
                    }

                    //报文总长：按网络字节序发送和接收
                    if(totalReadBytes==lengthSize){
                        busiDataSize= bytesToInt(buf,0);
                        System.out.println("->busiDataSize:"+busiDataSize);
                        totalSize=lengthSize+cmdSize+busiDataSize;
                        System.out.println("->报文总长："+totalSize);
                        continue;//这段逻辑只运行一次：首次读的时候
                    }
                    //报文总长等于当前已读字节数的时候，表示已经读完
                    if(totalSize==totalReadBytes) {
                        System.out.println("["+new Date()+">>>报文接收完鸟<<<");

                        //读完报文后并且在退出前,根据指令和客户端交互[可能有bug,while的条件不好判断]
                        //client发送的数据包都有data,server发送的不一定
                        //如何与写线程交互?
                        InteractionUtil.actionByCmd(cmdHex,sb);

                        //这个退出条件很关键,暂时先放这里调试,
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
/**
 * 发送信息线程
 */
class SendThreat implements Runnable {
    Socket socket;
    //使用BufferedWriter流向客户端发送信息
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