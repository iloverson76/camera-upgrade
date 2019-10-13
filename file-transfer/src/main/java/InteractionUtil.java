
import bean.BusiData;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 交互操作
 */
@Getter
@Setter
@Slf4j
public class InteractionUtil {

    /**
     * 2.2.1 获取设备版本信息(client)
     *
     */
    public static final String CMD_VERSION_REQUEST="00005000";
    /**
     * 2.2.2 返回设备版本信息(server)
     */
    public static final String CMD_VERSION_RESPONSE="00005001";
    /**
     * 2.2.3 请求升级文件(client)
     */
    public static final String CMD_UPGRADE_REQUEST="00005002";
    /**
     * 2.2.4 应答升级文件请求(server)
      */
    public static final String CMD_UPGRADE_RESPONSE="00005003";
    /**
     * 2.2.5 传送升级文件(server)
     */
    public static final String CMD_UPGRADE_PACK_TRANSFER="0000F000";
    /**
     * 2.2.6 接收文件应答(client)
     */
    public static final String CMD_UPGRADE_PACK_RECEIVE_REQUEST="0000F001";
    /**
     * 2.2.7 查询升级结果(client)
     */
    public static final String CMD_UPGRADE_RESULT_REQUEST="00005004";
    /**
     * 2.2.8 升级结果查询的回复(server)
     */
    public static final String CMD_UPGRADE_RESULT_RESPONSE="00005005";
    /**
     * 客户端指令(k):(v)服务端动作
     */
    private static final Map<String,String> cmdMethodMap=new HashMap<String, String>();

    static {
        cmdMethodMap.put(CMD_VERSION_REQUEST,"pushDeviceVersionInfo");
        cmdMethodMap.put(CMD_UPGRADE_REQUEST,"answerUpgrdeRequest");
       // cmdMethodMap.put(CMD_UPGRADE_PACK_TRANSFER,"transferPack");//回复客户端升级请求后,不用等客户端再回复,紧接着客户端主动发送文件(另外写方法插入交互过程中)
        cmdMethodMap.put(CMD_UPGRADE_PACK_RECEIVE_REQUEST,"packReceiveAckHandle");
        cmdMethodMap.put(CMD_UPGRADE_RESULT_REQUEST,"answerUpgradeResultRequest");
    }

    /**
     * 根据客户端发送的指令进行动作
     * @param cmdHex : 十六进制指令
     * @param busiData:业务数据(不包含LENGTH和COMMAND)
     */
    public static void actionByCmd(String cmdHex, StringBuffer busiData) {

        String methodName= cmdMethodMap.get(cmdHex);

        log.info ( ">>>开始执行动作:"+methodName );

        try {
            Class.forName ( "InteractionUtil" ).getMethod ( methodName ).invoke ( cmdHex,busiData );
        } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public FileInputStream uploadFile(String dir,String fileName){

        log.info(">>>开始获取文件{},{}",dir,fileName);

        FileInputStream fis=null;
        try {
            fis= new FileInputStream(new File ( dir+fileName ));
        } catch (FileNotFoundException e) {
            e.printStackTrace ();
        }
        return fis;
    }

    /**
     * 文件流转字节数组
     * @param fis
     * @return
     */
    public byte[] getFileToByte(FileInputStream fis) {
       log.info ( ">>>开始文件转字节" );
        byte[] by=null;
        ByteArrayOutputStream bytestream=null;
        try {
            bytestream = new ByteArrayOutputStream();
            byte[] bb = new byte[1024];
            int ch;
            ch = fis.read(bb);
            while (ch != -1) {
                bytestream.write(bb, 0, ch);
                ch = fis.read(bb);
            }
            bytestream.flush ();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        log.info(">>>完成");
        return  bytestream.toByteArray();
    }

    //2.2.1 获取设备版本信息(client)

    /**
     * 2.2.2 返回设备版本信息(server)
     * @param cmd
     */
    public void pushDeviceVersionInfo(String cmd,StringBuffer sb) {


    }

    //2.2.3 请求升级文件(client)

    /**
     * 2.2.4 应答升级文件请求(server)
     * @param cmd
     */
    public void answerUpgrdeRequest(String cmd,StringBuffer sb){

    }

    /**
     *  2.2.5 传送升级文件(server)
     * @param cmd
     */
    public void transferPack(String cmd){


    }

    //2.2.6 接收文件应答(client)|服务端的处理方法,不用给客户端反馈
    public void packReceiveAckHandle(String cmd){
        //不用给客户端返回去
    }

    //2.2.7 查询升级结果(client)

    /**
     * 2.2.8 升级结果查询的回复(server)
     * @param cmd
     */
    public void answerUpgradeResultRequest(String cmd,StringBuffer sb){

    }

    /**
     * 校验设备版本
     */
    public void validateVersion(String cmd,StringBuffer sb){

    }

    /**
     * 文件的十六进制md5值
     * @param in
     * @return
     */
    private String caculateMD5(FileInputStream in){
        String md5="";
        try {
           md5 = DigestUtils.md5Hex(in);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return md5;
    }

    /**
     *
     * @param b
     * @return
     */
    public static String bytesToHexString(byte[] b) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            sb.append(String.format("%02x", b[i]));
        }
        return sb.toString();
    }

    /**
     * 拆包
     * @param sb
     * @return
     */
    public static BusiData unpackData(StringBuffer sb){

        return null;
    }

    /**
     * 封包
     * @param data
     * @return
     */
    public static StringBuffer packData(BusiData data){

        return null;
    }

    public static void main(String[] args){
    }


}