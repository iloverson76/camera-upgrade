package com.gzrock.server;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

import static com.sun.corba.se.impl.orbutil.ORBUtility.bytesToInt;

/**
 * 服务端
 */
@Slf4j
public class UpgradeServer {

    /**
     * 参数校验
     *
     * @param args
     */
    private static void validationParams(String[] args) {
        if (null == args[1] || "".equals(args[1])) {//还要有格式校验
            try {
                throw new Exception("文件路径不能为空");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //文件名匹配正则校验
        if (null == args[2] || "".equals(args[2]) || !args[2].contains("tar.bz2")) {
            try {
                throw new Exception("文件名不能为空并且文件名必须符合此格式:[504.app.C_800.tar.bz2]");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 文件路径和名称校验
     *
     * @param filePath
     * @param fileName
     */
    private static void uploadFile(String filePath, String fileName) {
        InteractionUtil.filePath = filePath;
        InteractionUtil.fileName = fileName;
        InteractionUtil.currentVersion = Integer.valueOf(fileName.split("\\.")[0]);
        InteractionUtil.file = new File(filePath + fileName);
        if (null == InteractionUtil.file) {
            try {
                throw new Exception("升级文件不存在!请重新检查!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        log.info("升级包:" + filePath + fileName);
    }

    /**
     * 入口
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        validationParams(args);
        uploadFile(args[1], args[2]);

        ServerSocket serverSocket = null;
        try {
            //新建一个服务端ServerSocket,端口号为8888
            serverSocket = new ServerSocket(Integer.valueOf(args[0]));
            log.info("等待客户端连接!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                //监听客户端的连接
                Socket socket = serverSocket.accept();
                log.info("[" + new Date() + "]客户端 " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + " 连接成功！");

                //开启客户端接收信息线程
                new Thread(new ReceiveThread(socket)).start();
                //开始客户端发送信息线程
                // new Thread(new SendThreat(socket)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

/**
 * 接收信息线程
 */
class ReceiveThread implements Runnable {

    public static Log log = LogFactory.getLog(ReceiveThread.class);
    private static boolean running = false;
    private static boolean readable = false;
    private Socket socket;
    private BufferedInputStream reader;
    private BufferedOutputStream writer = null;
    private InteractionUtil interactionUtil = null;

    public ReceiveThread(Socket socket) {
        super();
        this.socket = socket;
        try {
            //获取socket的输入流
            reader = new BufferedInputStream(socket.getInputStream());
            writer = new BufferedOutputStream(socket.getOutputStream());
            running = true;
            readable = true;
            interactionUtil = new InteractionUtil();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (running) {
            //LENGTH字段总长(字节)
            final int lengthSize = 4;
            //command字段总长(字节)
            final int cmdSize = 4;
            //data字段：业务数据总长(字节)=bytesToInt(lengthSize)
            int busiDataSize = 0;
            //每次读step个字节(对应读次序的字段的字节数)
            int step = 0;
            //读次序(协议规定)
            int LENGTH_SEQ = 1;
            int CMD_SEQ = 2;
            int BUSIDATA_SEQ = 3;
            //十六进制指令
            int cmdHex = 0;
            //当前读到的字节总数
            int totalReadBytes = 0;
            //报文数据总长(字节)
            int totalSize = 0;
            int seq = 0;
            //解释报文
            while (readable) {
                log.info("......server thread reading.....");
                seq++;
                try {
                    if (seq == LENGTH_SEQ) {
                        step = lengthSize;
                        log.info(">>>[seq:" + seq + "]" + "]开始读取LENGTH");
                    } else if (seq == CMD_SEQ) {
                        step = cmdSize;
                        log.info(">>>[seq:" + seq + "]" + "] 开始读取COMMAND");
                    } else if (seq == BUSIDATA_SEQ) {
                        step = Math.abs(busiDataSize);//可能有符号位,负数
                        log.info(">>>[seq:" + seq + "]" + "] 开始读取DATA");
                    }

                    //实际每次读到的字符数
                    byte[] buf = new byte[step];
                    int readBytes = reader.read(buf, 0, buf.length);
                    totalReadBytes += readBytes;

                    StringBuffer sb = new StringBuffer();
                    String str = new String(buf);
                    sb.append(str);

                    //报文总长：按网络字节序发送和接收
                    if (totalReadBytes == lengthSize) {
                        busiDataSize = bytesToInt(buf, 0);
                        log.info("->busiDataSize:" + busiDataSize);
                        totalSize = lengthSize + cmdSize + busiDataSize;
                        log.info("->报文总长：" + totalSize);
                        continue;//这段逻辑只运行一次：首次读的时候
                    }

                    //获取指令
                    if (seq == CMD_SEQ) {
                        cmdHex = InteractionUtil.byteArray2Int(buf);
                        log.info("-> COMMAND[Hex]:0x" + Integer.toHexString(cmdHex));
                    }

                    //报文总长等于当前已读字节数的时候，表示已经读完
                    if (totalSize == totalReadBytes) {
                        log.info(">>>报文接收完鸟<<<");
                        actionByCmd(cmdHex, sb);
                        break;
                    }
                } catch (Exception e) {
                    readable = false;
                    assert writer != null;
                    try {
                        writer.close();

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    assert reader != null;
                    try {
                        reader.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    } finally {
                        assert writer != null;
                        try {
                            writer.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        assert reader != null;
                        try {
                            reader.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 根据指令进行动作
     *
     * @param cmd
     * @param sb
     * @return
     */
    private void actionByCmd(int cmd, StringBuffer sb) {
        byte[] sendBytes = null;
        try {

            sendBytes = InteractionUtil.actionByCmd(cmd, sb);

            //2.2.6 接收文件应答 0xF001 result:ok 后客户端重启设备,关闭服务端的socket和线程
            if (cmd == InteractionUtil.CMD_UPGRADE_PACK_RECEIVE_REQUEST) {
                closeConnection(writer, reader);
                return;
            }

            //请求升级文件
            if (cmd == InteractionUtil.CMD_UPGRADE_REQUEST) {
                String downloadMark = sb.toString().split("\n")[2].split(":")[1];
                if (InteractionUtil.undownload.equals(downloadMark)) return;//客户端不下载文件
                //先应答升级文件请求
                sendBytesToClient(writer, sendBytes);
                //接着传送升级文件
                sendBytesToClient(writer, InteractionUtil.transferFile(InteractionUtil.CMD_UPGRADE_PACK_TRANSFER));
                return;
            }

            sendBytesToClient(writer, sendBytes);

            //如果没有可升级版本,通知设备后要主动断开这次连接的socket
            log.info(">>>sendBytes length:" + sendBytes.length);
            byte[] buf = new byte[sendBytes.length - 8];
            System.arraycopy(sendBytes, 8, buf, 0, sendBytes.length - 8);
            String resultData = new String(buf);
            log.info(">>>resultData:" + resultData);
            if (resultData.equals("none")) {
                closeForNoUpgradeVersion(writer, reader);
            }
            //设备重启后,发送查询升级结果的指令,重新建立连接
            //2.2.8 升级结果查询的回复 0x5005 result:ok 后,服务端关闭socket和线程,整个升级过程全部结束
            if (cmd == InteractionUtil.CMD_UPGRADE_RESULT_REQUEST) {
                closeConnection(writer, reader);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送信息给客户端
     *
     * @param writer
     * @param sendBytes
     */
    private void sendBytesToClient(BufferedOutputStream writer, byte[] sendBytes) {
        log.info(">>>开始发送报文");
        try {
            writer.write(sendBytes);
            writer.flush();
            log.info(" ^_^发送成功,本次交互完成鸟^_^");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 客户端关闭后,服务端也关闭
     *
     * @param out
     * @param in
     */
    private void closeConnection(BufferedOutputStream out, BufferedInputStream in) {
        try {
            //不断发送指令,判断客户端是否已关闭,
            while (true) {
                socket.sendUrgentData(0xFF);
            }
        } catch (IOException e) {
            log.info(">客户端链路已关闭");
            close(out, in);
            readable = false;
            running = false;
        }
    }

    private void close(BufferedOutputStream out, BufferedInputStream in) {
        assert out != null;
        try {
            out.close();
        } catch (IOException e) {
            log.info(">>>服务端输出流关闭异常");
        }
        assert in != null;
        try {
            in.close();
        } catch (IOException e) {
            log.info(">>>服务端输入流关闭异常");
        } finally {
            assert out != null;
            try {
                out.close();
                log.info(">>>服务端输出流已关闭");
            } catch (IOException e) {
                log.info(">>>服务端输出流关闭异常");
            }
            assert in != null;
            try {
                in.close();
                log.info(">>>服务端输入流已关闭");
                log.info(">>>本次升级完成,开始等待下次升级...");
            } catch (IOException e) {
                log.info(">>>服务端输入流关闭异常");
            }
        }
    }

    private void closeForNoUpgradeVersion(BufferedOutputStream out, BufferedInputStream in) {
        assert out != null;
        try {
            out.close();
            readable = false;
        } catch (IOException e) {
            log.info(">>>没有可升级版本,输出流关闭");
        }
        assert in != null;
        try {
            in.close();
            running = false;
            log.info(">>>没有可升级版本,输入流关闭");
        } catch (IOException e) {
            log.info(">>>没有可升级版本,输入流关闭异常");
        } finally {
            assert out != null;
            try {
                out.close();
            } catch (IOException e) {
                log.info(">>>服务端输出流关闭异常");
            }
            assert in != null;
            try {
                in.close();
            } catch (IOException e) {
                log.info(">>>服务端输入流关闭异常");
            }
        }
    }
}