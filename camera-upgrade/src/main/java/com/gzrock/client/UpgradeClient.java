package com.gzrock.client;

import com.gzrock.server.InteractionUtil;
import com.gzrock.server.UpgradeServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.CharBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class UpgradeClient {

    public static Log logger = LogFactory.getLog(UpgradeServer.class);

    public static final Object locked = new Object();
    public static final BlockingQueue<String> queue = new ArrayBlockingQueue<String>(
            1024 * 100);

    class SendThread extends Thread {

        public Log logger = LogFactory.getLog(UpgradeServer.class);

        private Socket socket;
        OutputStreamWriter writer;
        private boolean writeable=false;

        public SendThread(Socket socket) {
            try {
                this.socket = socket;
                writer = new OutputStreamWriter(socket.getOutputStream());
                writeable=true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while (writeable) {
                try {
                    byte[] msg = getMsg();
                    if (msg.length > 0) {
                        writer.write(new String(msg));
                        writer.flush();
                      //  writeable=false;
                    }
                }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
        public byte[] getMsg() throws InterruptedException{
            Thread.sleep(1000);
            StringBuffer data=new StringBuffer();
            data.append("A99762000000013").append("\n")
                .append("U5820HCA").append("\n")
                .append("app:")
                .append(500).append("\n");
            return InteractionUtil.getBytesWithoutFile(InteractionUtil.CMD_UPGRADE_RESULT_REQUEST,data);
        }
    }

    class ReceiveThread extends Thread{
        private Socket socket;
        InputStreamReader reader;
        private boolean readable=false;

        public ReceiveThread(Socket socket) {
            try {
                this.socket = socket;
                reader = new InputStreamReader(socket.getInputStream());
                readable=true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while(readable){
                try {
                    CharBuffer charBuffer = CharBuffer.allocate(8192);
                    int index = -1;
                    while((index=reader.read(charBuffer))!=-1){
                        charBuffer.flip();
                        logger.info("client:"+charBuffer.toString());
                    }
                   // readable=false;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void start() throws UnknownHostException, IOException{
        Socket socket = new Socket("127.0.0.1",8888);
        new SendThread(socket).start();
        new ReceiveThread(socket).start();
    }
    public static void main(String[] args) throws UnknownHostException, IOException {
        new UpgradeClient().start();
    }
}
