package com.gzrock.server;

import com.gzrock.data.DeviceUpgradeRecord;
import com.gzrock.data.DeviceUtil;
import com.gzrock.data.DeviceWfiGetsRouting;
import com.gzrock.data.ExceptionUtil;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 交互操作
 */
@Getter
@Setter
@Builder
@Accessors(chain = true)
@Slf4j
public class InteractionUtil {

    /**
     * 2.2.1 获取设备版本信息(client)
     */
    public static final Integer CMD_VERSION_REQUEST = 0x5000;
    /**
     * 2.2.2 返回设备版本信息(server)
     */
    public static final Integer CMD_VERSION_RESPONSE = 0x5001;
    /**
     * 2.2.3 请求升级文件(client)
     */
    public static final Integer CMD_UPGRADE_REQUEST = 0x5002;
    /**
     * 2.2.4 应答升级文件请求(server)
     */
    public static final Integer CMD_UPGRADE_RESPONSE = 0x5003;
    /**
     * 2.2.5 传送升级文件(server)
     */
    public static final Integer CMD_UPGRADE_PACK_TRANSFER = 0xF000;
    /**
     * 2.2.6 接收文件应答(client)
     */
    public static final Integer CMD_UPGRADE_PACK_RECEIVE_REQUEST = 0xF001;
    /**
     * 2.2.7 查询升级结果(client)
     */
    public static final Integer CMD_UPGRADE_RESULT_REQUEST = 0x5004;
    /**
     * 2.2.8 升级结果查询的回复(server)
     */
    public static final Integer CMD_UPGRADE_RESULT_RESPONSE = 0x5005;
    /**
     * 客户端不下载文件标识
     */
    public static final String undownload = "none";
    /**
     * 客户端文件接收完整
     */
    public static final String RECEIVE_FILE_COMPLETED = "result:ok";
    /**
     * 客户端文件md5校验失败
     */
    public static final String MD5_VALIDATION_FAILED = "fault_md5";
    /**
     * 客户端指令(k):(v)服务端动作
     */
    private static final Map<Integer, String> cmdMethodMap = new HashMap<Integer, String>();
    /**
     * 文件路径
     */
    public static String filePath;
    /**
     * 文件名(命名规范:504.app.C_800.tar.bz2,504版本号开头)
     */
    public static String FILE_NAME;
    /**
     * 文件对象
     */
    public static File FILE;
    /**
     * 升级包最新版本号(暂定数字)
     */
    public static Integer  currentVersion;
    /**
     * 是否下载文件
     */
    public static boolean download = true;
    /**
     * 类反射对象
     */
    private static Class clazz = null;
    /**
     * 当前应答指令
     */
   // private  static String CUR_RESP_CMD="";
    /**
     * 当前升级设备ID
     */
  //  private  static String CUR_DEVICE_ID ="";
    /**
     * 当前设备上报的版本
     */
   // private  static String CUR_OLD_VERSION="";
    /**
     * 当前最新升级包版本号
     */
    //private  static String CUR_NEW_VERSION="";

    private static Map<String,String[]> deviceQueue=new HashMap<>();
    /**
     * 初始化数据
     */
    static {
        cmdMethodMap.put(CMD_VERSION_REQUEST, "pushDeviceVersionInfo");
        cmdMethodMap.put(CMD_UPGRADE_REQUEST, "answerUpgrdeRequest");
        cmdMethodMap.put(CMD_UPGRADE_PACK_RECEIVE_REQUEST, "packReceiveAckHandle");
        cmdMethodMap.put(CMD_UPGRADE_RESULT_REQUEST, "answerUpgradeResultRequest");
        //获取反射对象clazz
        try {
            clazz = Class.forName("com.gzrock.server.InteractionUtil");
        } catch (ClassNotFoundException e) {
            log.info( ExceptionUtil.getStackTraceString(e));
        }
    }

    /**
     * 根据客户端发送的指令进行动作
     *
     * @param cmdHex:十六进制指令
     * @param busiData:业务数据(不包含LENGTH和COMMAND)
     */
    public static byte[] actionByCmd(Integer cmdHex, StringBuffer busiData){
        int num = 0;
        for (Integer key : cmdMethodMap.keySet()) {
            if (key.equals(cmdHex)) {
                num++;
            }
        }
        if (num == 0) {
            throw new RuntimeException("升级协议无此指令!" + cmdHex);
        }
        if (null == busiData || 0 == busiData.toString().length()) {
            throw new RuntimeException("业务数据获取有误!");
        }
        String methodName = cmdMethodMap.get(cmdHex);
        byte[] result = null;
        try {
            result = (byte[]) clazz.getMethod(methodName, Integer.class, StringBuffer.class)
                    .invoke(clazz.newInstance(), cmdHex, busiData);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException e) {
            log.info( ExceptionUtil.getStackTraceString(e));
        }
        return result;
    }

    //2.2.1 获取设备版本信息(client)

    /**
     * 2.2.2 返回设备版本信息(server)
     *
     * @param cmd
     */
    public static byte[] pushDeviceVersionInfo(Integer cmd, StringBuffer sb) {
        log.info(">返回设备版本信息-请求指令[0x" + Integer.toHexString(cmd) + "] 应答指令[0x" +  Integer.toHexString(CMD_VERSION_RESPONSE) + "]");
        log.info("接收DATA:"+"\n"+sb.toString());
        String origStr = sb.toString();
        String[] origStrArr = origStr.split("\n");
        Integer deviceVersion=Integer.valueOf(origStrArr[2].split(":")[1]);
        String deviceId= origStrArr[0];
        log.info("设备当前版本:"+deviceVersion);
        String[] infoArr=new String[2];
        infoArr[0]=String.valueOf(deviceVersion);
        deviceQueue.put(deviceId,infoArr);
        byte[] valRet = validateVersion(deviceVersion,deviceId);
        if (null != valRet){ return valRet;}
        StringBuffer dataBuf = new StringBuffer();
        dataBuf.append(deviceId)
                .append("\n")
                .append(origStrArr[1])
                .append("\n")
                .append("app")
                .append(":")
                .append(FILE_NAME)
                .append("\n");
        log.info("应答报文:"+"\n"+dataBuf.toString());
        Date beginTime=new Date();
        log.info(">>>创建升级记录到数据库 beginTime["+beginTime+"]");
        DeviceUtil.builder().build().createUpgradeResultBegin(
                DeviceUpgradeRecord.builder()
                        .imei(deviceId)
                        .beginTime(beginTime)
                        .oldVersion(String.valueOf(deviceVersion))
                        .build()
        );
        return getBytesWithoutFile(CMD_VERSION_RESPONSE, dataBuf);
    }

    //2.2.3 请求升级文件(client)

    /**
     * 2.2.5 传送升级文件(server)
     *
     * @param cmd
     */
    public static byte[] transferFile(Integer cmd) {
        log.info(">开始传送文件,应答指令[0x" + Integer.toHexString(cmd) + "]");
        byte[] fileBytes = fileToByte(uploadFile());
        return getBytesWithFile(cmd, fileBytes);
    }

    /**
     * 2.2.6 接收文件应答(client)|服务端的处理方法,不用发送回客户端
     *
     * @param cmd
     * @param sb
     * @return
     */
    public static byte[] packReceiveAckHandle(Integer cmd, StringBuffer sb) {
        log.info(">接收文件应答-请求指令[0x" + Integer.toHexString(cmd) + "]");
        log.info("接收DATA:"+"\n"+sb.toString());
        String resultStr = sb.toString();
        if (resultStr.contains(RECEIVE_FILE_COMPLETED)) {
            String curDeviceId= sb.toString().split("\n")[0];
            String[] infoArr=deviceQueue.get(curDeviceId);
            String curOldVersion=infoArr[0];
            String curNewVersion=infoArr[1];

            DeviceUtil.builder().build().createUpgradeResultEnd(
                    DeviceUpgradeRecord.builder()
                            .endTime(new Date())
                            .newVersion(curNewVersion)
                            .upgradeResult(0)
                            .imei(curDeviceId)
                            .build()
            );
            log.info(">>>客户端已成功下载升级包,结果已记录到数据库表[wp.wp_device_upgrade_record]");
            //开始异步查询升级结果
            new Thread( QueryUpgradeResult.builder()
                    .deviceId(curDeviceId)
                    .oldVersion(curOldVersion)
                    .newVersion(curNewVersion)
                    .build()).start();
            //返回
            return RECEIVE_FILE_COMPLETED.getBytes();
        } else if (resultStr.contains(MD5_VALIDATION_FAILED)) {
            try {
                throw new RuntimeException("文件md5校验失败");
            } catch (Exception e) {
                log.info( ExceptionUtil.getStackTraceString(e));
            }
        }
        return null;
    }

    /**
     * 返回报文,不包含文件字节流
     *
     * @param dataBuf
     * @return
     */
    public static byte[] getBytesWithoutFile(Integer cmd, StringBuffer dataBuf) {
        log.info(">返回报文,不包含文件字节流");
        byte[] resultByte = null;
        StringBuffer retSb = new StringBuffer();
        try {
            Integer dataLength = dataBuf.toString().getBytes().length;
            byte[] lengthByte = intToByteArray(dataLength);
            byte[] cmdByte = intToByteArray(cmd);
            byte[] dataByte = dataBuf.toString().getBytes();
            resultByte = getCommonBytes(lengthByte, cmdByte, dataByte);
        } catch (Exception e) {
            log.info( ExceptionUtil.getStackTraceString(e));
        }
        return resultByte;
    }

    //2.2.7 查询升级结果(client)

    /**
     * 返回文件字节流报文
     *
     * @param cmd
     * @param fileBytes
     * @return
     */
    private static byte[] getBytesWithFile(Integer cmd, byte[] fileBytes) {
        log.info(">返回文件字节流报文");
        byte[] resultByte = null;
        try {
            Integer dataLength = fileBytes.length;
            byte[] lengthByte = intToByteArray(dataLength);
            byte[] cmdByte = intToByteArray(cmd);
            resultByte = getCommonBytes(lengthByte, cmdByte, fileBytes);
        } catch (Exception e) {
            log.info( ExceptionUtil.getStackTraceString(e));
        }
        return resultByte;
    }

    /**
     * 组装报文字节流
     *
     * @param lengthByte
     * @param cmdByte
     * @param dataByte
     * @return
     */
    public static byte[] getCommonBytes(byte[] lengthByte, byte[] cmdByte, byte[] dataByte) {
        byte[] resultByte;
        resultByte = new byte[lengthByte.length + cmdByte.length + dataByte.length];
        System.arraycopy(lengthByte, 0, resultByte, 0, lengthByte.length);
        System.arraycopy(cmdByte, 0, resultByte, lengthByte.length, cmdByte.length);
        System.arraycopy(dataByte, 0, resultByte, lengthByte.length + cmdByte.length, dataByte.length);
        return resultByte;
    }

    /**
     * 获取文件
     *
     * @return
     */
    public static FileInputStream uploadFile() {
        if ("" == filePath || "" == FILE_NAME) {
            log.info(">文件路径或文件名不存在!");
            return null;
        }
        log.info(">开始获取文件" + filePath + FILE_NAME);
        FileInputStream in = null;
        try {
            if(null==FILE){
                throw new RuntimeException("升级文件流不存在!");
            }
            in = new FileInputStream(FILE);
        } catch (FileNotFoundException e) {
            log.info( ExceptionUtil.getStackTraceString(e));
        }
        return in;
    }

    /**
     * 文件输入流转字节数组
     *
     * @param in
     * @return
     */
    public static byte[] fileToByte(FileInputStream in) {
        log.info(">文件输入流转字节数组");
        ByteArrayOutputStream bytestream = null;
        try {
            bytestream = new ByteArrayOutputStream();
            byte[] bb = new byte[2048];
            int ch;
            ch = in.read(bb);
            while (ch != -1) {
                bytestream.write(bb, 0, ch);
                ch = in.read(bb);
            }
        } catch (Exception e) {
            log.info( ExceptionUtil.getStackTraceString(e));
        }
        return bytestream.toByteArray();
    }

    /**
     * 校验设备版本
     */
    public static byte[] validateVersion(Integer deviceVersion,String deviceId) {
        int newVersion = Integer.valueOf(FILE_NAME.split("\\.")[0]);
        if (newVersion <= deviceVersion) {
            log.info(">>>没有新版本");
            StringBuffer sb = new StringBuffer();
            return getBytesWithoutFile(CMD_VERSION_RESPONSE, sb.append("none"));
        }
        log.info("有可升级版本:" + newVersion);
        String[] infoArr=deviceQueue.get(deviceId);
        infoArr[1]=String.valueOf(newVersion);
        return null;
    }

    /**
     * byte数组转int类型的对象
     *
     * @param b
     * @return
     */
    public static int byteArray2Int(byte[] b) {
        log.info(">字节转整数");
        return ((b[0] & 0xFF) << 24)
                | ((b[1] & 0xFF) << 16)
                | ((b[2] & 0xFF) << 8)
                | ((b[3] & 0xFF) << 0);
    }

    /**
     * int转byte数组
     * 高位在前
     *
     * @param integer
     * @return
     */
    public static byte[] intToByteArray(Integer integer) {
        log.info(">整数转字节");
        int byteNum = (40 - Integer.numberOfLeadingZeros(integer < 0 ? ~integer : integer)) / 8;
        byte[] byteArray = new byte[4];
        for (int n = 0; n < byteNum; n++){
            byteArray[3 - n] = (byte) (integer >>> (n * 8));
        }
        return byteArray;
    }

    /**
     * 2.2.4 应答升级文件请求(server)
     *
     * @param cmd
     */
    public byte[] answerUpgrdeRequest(Integer cmd, StringBuffer sb) {
        log.info(">应答升级文件请求-请求指令[" + Integer.toHexString(cmd) + "]应答指令[" + Integer.toHexString(CMD_UPGRADE_RESPONSE) + "]");
        log.info("接收DATA:"+"\n"+sb.toString());
        String origStr = sb.toString();
        String[] origStrArr = origStr.split("\n");
        if ("none".equalsIgnoreCase(origStrArr[2].split(":")[1])) {
            //客户端不下载
            download = false;
        }
        StringBuffer dataBuf = new StringBuffer();
        dataBuf.append(origStrArr[0])
                .append("\n")
                .append(origStrArr[1])
                .append("\n")
                .append("app")
                .append(":")
                .append("ok")
                .append("\n")
                .append("md5")
                .append(":")
                .append(caculateMD5(uploadFile()))
                .append("\n");
        log.info("应答报文:"+"\n"+sb.toString());
        return getBytesWithoutFile(CMD_UPGRADE_RESPONSE, dataBuf);
    }

    /**
     * 2.2.8 升级结果查询的回复(server)
     *
     * @param cmd
     */
    public byte[] answerUpgradeResultRequest(Integer cmd, StringBuffer dataBuf) {
        log.info(">应答升级文件请求-请求指令[" + Integer.toHexString(cmd) + "]应答指令[" + Integer.toHexString(CMD_UPGRADE_RESULT_RESPONSE) + "]");
        log.info("接收DATA:"+"\n"+dataBuf.toString());
        String origStr = dataBuf.toString();
        String[] origStrArr = origStr.split("\n");
        StringBuffer dataSb = new StringBuffer();
        dataSb.append(origStrArr[0])
                .append("\n")
                .append(origStrArr[1])
                .append("\n")
                .append("result")
                .append(":")
                .append("ok")
                .append("\n");
        log.info("应答报文:"+"\n"+dataSb.toString());
        return getBytesWithoutFile(CMD_UPGRADE_RESULT_RESPONSE, dataSb);
    }

    /**
     * 文件的十六进制md5值
     *
     * @param in
     * @return
     */
    public String caculateMD5(FileInputStream in) {
        String md5 = "";
        try {
            md5 = DigestUtils.md5Hex(in);
        } catch (IOException e) {
            log.info( ExceptionUtil.getStackTraceString(e));
        }
        log.info(">计算文件md5值:" + md5);
        return md5;
    }
}

/**
 * 查询设备升级结果线程
 */
@Data
@Builder
@AllArgsConstructor
@Accessors(chain = true)
@Slf4j
class QueryUpgradeResult implements Runnable {
    private String deviceId;
    private String oldVersion;
    private String newVersion;
    static boolean running;

    static {
        running = true;
    }

    @Override
    public void run() {
        int seq = 0;
        int time=0;
        while (running) {
            try {
                log.info("开始第" + (++seq) + "次查询设备["+this.deviceId+"]升级结果");
                //五分钟后不再查询
                if(time>3*60000){
                    running=false;
                    log.info("APP端[摄像头wifi上线数据同步]接口没有获取到设备["+this.deviceId+"]升级重启后上报的版本号");
                    break;
                }
                time=time+30000;
                //30秒查询一次升级结果
                Thread.sleep(30000);
                String pushVersion = getUpgradeVersion(this.deviceId);
                log.info(">>>当前设备[" + this.deviceId + "]上报版本[" + pushVersion + "]");
                if(StringUtils.isNotEmpty(pushVersion)){
                    if (pushVersion.equals(this.newVersion)) {
                        log.info(">>>设备["+this.deviceId +"]升级成功!");
                    } else {
                        log.info("设备升级重启后上报不符合预期的版本["+pushVersion+"]");
                    }
                    running = false;
                    break;
                }
            } catch (InterruptedException e) {
                log.info( ExceptionUtil.getStackTraceString(e));
            }
        }
    }

    private String getUpgradeVersion(String deviceId) {
        DeviceWfiGetsRouting result=DeviceUtil.builder().build().getUpgradeVersion(deviceId);
        if(null==result){
            return "";
        }
        //"Software":"C_800.U5826HAA.010.509"
        String[] retStr=result.getSoftware().split("\\.");
        return retStr[retStr.length-1];
    }
}