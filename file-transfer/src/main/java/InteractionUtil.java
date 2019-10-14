
import bean.BusiData;
import bean.Header;
import bean.UpgradeMsg;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 交互操作
 */
@Getter
@Setter
@Builder
@Accessors(chain = true)
public class InteractionUtil {

    /**
     * 2.2.1 获取设备版本信息(client)
     *
     */
    public static final Integer CMD_VERSION_REQUEST=0x5000;
    /**
     * 2.2.2 返回设备版本信息(server)
     */
    public static final Integer CMD_VERSION_RESPONSE=0x5001;
    /**
     * 2.2.3 请求升级文件(client)
     */
    public static final Integer CMD_UPGRADE_REQUEST=0x5002;
    /**
     * 2.2.4 应答升级文件请求(server)
      */
    public static final Integer CMD_UPGRADE_RESPONSE=0x5003;
    /**
     * 2.2.5 传送升级文件(server)
     */
    public static final Integer CMD_UPGRADE_PACK_TRANSFER=0xF000;
    /**
     * 2.2.6 接收文件应答(client)
     */
    public static final Integer CMD_UPGRADE_PACK_RECEIVE_REQUEST=0xF001;
    /**
     * 2.2.7 查询升级结果(client)
     */
    public static final Integer CMD_UPGRADE_RESULT_REQUEST=0x5004;
    /**
     * 2.2.8 升级结果查询的回复(server)
     */
    public static final Integer CMD_UPGRADE_RESULT_RESPONSE=0x5005;
    /**
     * 客户端指令(k):(v)服务端动作
     */
    private static final Map<Integer,String> cmdMethodMap=new HashMap<Integer, String>();

    private static Class clazz=null;
    /**
     * 升级包路径
     */
    public static String filePath="";
    /**
     * 升级包名
     */
    public static String fileName="";

    static {
        cmdMethodMap.put(CMD_VERSION_REQUEST,"pushDeviceVersionInfo");
        cmdMethodMap.put(CMD_UPGRADE_REQUEST,"answerUpgrdeRequest");
       // cmdMethodMap.put(CMD_UPGRADE_PACK_TRANSFER,"transferPack");//回复客户端升级请求后,不用等客户端再回复,紧接着客户端主动发送文件(另外写方法插入交互过程中)
        cmdMethodMap.put(CMD_UPGRADE_PACK_RECEIVE_REQUEST,"packReceiveAckHandle");
        cmdMethodMap.put(CMD_UPGRADE_RESULT_REQUEST,"answerUpgradeResultRequest");

        if(null!=UpgradeServer.filePath&&null!=UpgradeServer.fileName){
            filePath=UpgradeServer.filePath;
            fileName=UpgradeServer.fileName;
        }
        try {
            clazz=Class.forName("InteractionUtil");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据客户端发送的指令进行动作
     * @param cmdHex : 十六进制指令
     * @param busiData:业务数据(不包含LENGTH和COMMAND)
     */
    public static byte[] actionByCmd(Integer cmdHex, StringBuffer busiData) throws Exception {
        int num=0;
        for (Integer key : cmdMethodMap.keySet()) {
            if (key.equals(cmdHex)) {
                num++;
            }
        }
        if(num==0){
            throw new Exception("指令不在协议范围内!");
        }
        if(null==busiData||0==busiData.toString().length()){
            throw new Exception("业务数据获取有误!");
        }
        String methodName= cmdMethodMap.get(cmdHex);
        byte[] result=null;
        try {
            result= (byte[]) clazz.getMethod(methodName,Integer.class,StringBuffer.class)
                    .invoke(clazz.newInstance(),cmdHex,busiData);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return result;
    }
    /**
     * 获取文件
     * @param dir
     * @param fileName
     * @return
     */
    public FileInputStream uploadFile(String dir,String fileName){
        System.out.println(">>>开始获取文件"+fileName);
        dir=InteractionUtil.filePath;fileName=InteractionUtil.fileName;
        FileInputStream in=null;
        try {
            in= new FileInputStream(new File(dir+fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println(">>>成功");
        return in;
    }

    /**
     * 文件输入流转字节数组
     * @param in
     * @return
     */
    public byte[] FileToByte(FileInputStream in){
        System.out.println(">>>开始转换文件");

        ByteArrayOutputStream bytestream=null;
        try {
            bytestream = new ByteArrayOutputStream();
            byte[] bb = new byte[2048];
            int ch;
            ch = in.read(bb);
            while (ch != -1) {
                bytestream.write(bb, 0, ch);
                ch = in.read(bb);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return bytestream.toByteArray();
    }


    //2.2.1 获取设备版本信息(client)

    /**
     * 2.2.2 返回设备版本信息(server)
     * @param cmd
     */
    public static byte[] pushDeviceVersionInfo(Integer cmd, StringBuffer sb) {
        System.out.println(">>>返回设备版本信息-请求指令"+cmd+"应答指令[+"+CMD_VERSION_RESPONSE+"]");
        String origStr=sb.toString();
        String[] origStrArr=origStr.split("\n");
        StringBuffer dataSb=new StringBuffer();
        dataSb.append(origStrArr[0])
                .append("\n")
                .append(origStrArr[1])
                .append("\n")
                .append("app:501.app.C_800.tar.bz2")//升级版本号要大于客户端传过来的版本号501>500
                .append("\n");
        //.append(fileName)

       /* A99762000000013
        U5886HAA
        app:395.app.C_800.tar.bz2(format:"app",appValue,"app",custemTypeValue)
        */
        return getBytesWithoutFile(dataSb);
    }

    //2.2.3 请求升级文件(client)

    /**
     * 2.2.4 应答升级文件请求(server)
     * @param cmd
     */
    public byte[] answerUpgrdeRequest(Integer cmd,StringBuffer sb){
        System.out.println(">>>应答升级文件请求-请求指令"+cmd+"应答指令[+"+CMD_UPGRADE_RESPONSE+"]");
       // CMD_UPGRADE_REQUEST;
       // CMD_UPGRADE_RESPONSE;
        /*
        COMMAND: 0x5003
        DATA:设备ID\n设备型号\n字段名:字段值\n...字段名:字段值\n
        传送文件类型：
        字段名：app/fw/kernel/uboot
        字段值：ok/fault(表示没有找到下载包)/errorpos(错误的起始位)
        文件校验值类型：
        字段名：md5
        字段值:  md5值 (小写)*/
        /*
        A99762000000013
        U5886HAA
        app:ok
        md5:870b58e7d9256cbc36782664998361e5
        */
        String origStr=sb.toString();
        String[] origStrArr=origStr.split("\n");
        StringBuffer dataSb=new StringBuffer();
        dataSb.append(origStrArr[0])
                .append("\n")
                .append(origStrArr[1])
                .append("\n")
                .append("app")
                .append(":")
                .append("ok")//先写死
                .append("\n")
                .append("md5")
                .append(":")
                .append("870b58e7d9256cbc36782664998361e5")
                .append("\n");
        return getBytesWithoutFile(dataSb);
    }

    /**
     * 返回报文,不包含文件流
     * @param dataSb
     * @return
     */
    private static byte[] getBytesWithoutFile(StringBuffer dataSb) {
        byte[] resultByte=null;
        StringBuffer retSb=new StringBuffer();
        try {
            Integer dataLength=dataSb.toString().getBytes().length;
            byte[] lengthByte=intToByteArray(dataLength);
            byte[] cmdByte=intToByteArray(CMD_VERSION_RESPONSE);
            byte[] dataByte=dataSb.toString().getBytes();
            resultByte=new byte[lengthByte.length+cmdByte.length+dataByte.length];
            System.arraycopy(lengthByte,0,resultByte,0,lengthByte.length);
            System.arraycopy(cmdByte,0,resultByte,lengthByte.length,cmdByte.length);
            System.arraycopy(dataByte,0,resultByte,lengthByte.length+cmdByte.length,dataByte.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultByte;
    }

    /**
     *  2.2.5 传送升级文件(server)
     * @param cmd
     */
    public void transferPack(Integer cmd){

    }

    //2.2.6 接收文件应答(client)|服务端的处理方法,不用给客户端反馈
    public void packReceiveAckHandle(Integer cmd){
        //不用给客户端返回去
    }

    //2.2.7 查询升级结果(client)

    /**
     * 2.2.8 升级结果查询的回复(server)
     * @param cmd
     */
    public void answerUpgradeResultRequest(Integer cmd,StringBuffer sb){

    }

    /**
     * 校验设备版本
     */
    public void validateVersion(Integer cmd,StringBuffer sb){

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
     * byte数组转int类型的对象
     * @param b
     * @return
     */
    public static int byteArray2Int(byte[] b) {
        return ((b[0] & 0xFF) << 24)
                | ((b[1] & 0xFF) << 16)
                | ((b[2] & 0xFF) <<  8)
                | ((b[3] & 0xFF) <<  0);
    }
    /**
     * int转byte数组
     * 高位在前
     * @param integer
     * @return
     */
    public static byte[] intToByteArray( Integer integer) {
        int byteNum = (40 -Integer.numberOfLeadingZeros (integer < 0 ? ~integer : integer))/ 8;
        byte[] byteArray = new byte[4];

        for (int n = 0; n < byteNum; n++)
            byteArray[3 - n] = (byte) (integer>>> (n * 8));

        return byteArray;
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