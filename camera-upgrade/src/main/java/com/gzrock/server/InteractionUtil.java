package com.gzrock.server;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
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
    public static Log logger = LogFactory.getLog(InteractionUtil.class);
    /**
     * 文件路径
     */
    public static String filePath;
    /**
     * 文件名(命名规范:504.app.C_800.tar.bz2,504版本号开头)
     */
    public static String fileName;
    /**
     * 文件对象
     */
    public static File file;
    /**
     * 升级包最新版本号(暂定数字)
     */
    public static Integer currentVersion;
    /**
     * 是否下载文件
     */
    public static boolean download = true;
    /**
     * 类反射对象
     */
    private static Class clazz = null;

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
            e.printStackTrace();
        }
    }

    /**
     * 根据客户端发送的指令进行动作
     *
     * @param cmdHex                           : 十六进制指令
     * @param busiData:业务数据(不包含LENGTH和COMMAND)
     */
    public static byte[] actionByCmd(Integer cmdHex, StringBuffer busiData) throws Exception {
        int num = 0;
        for (Integer key : cmdMethodMap.keySet()) {
            if (key.equals(cmdHex)) {
                num++;
            }
        }
        if (num == 0) {
            throw new Exception("指令不在协议范围内!" + cmdHex);
        }
        if (null == busiData || 0 == busiData.toString().length()) {
            throw new Exception("业务数据获取有误!");
        }
        String methodName = cmdMethodMap.get(cmdHex);
        byte[] result = null;
        try {
            result = (byte[]) clazz.getMethod(methodName, Integer.class, StringBuffer.class)
                    .invoke(clazz.newInstance(), cmdHex, busiData);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
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
        logger.info(">返回设备版本信息-请求指令[" + Integer.toHexString(cmd) + "] 应答指令[" +  Integer.toHexString(CMD_VERSION_RESPONSE) + "]");
        String origStr = sb.toString();
        String[] origStrArr = origStr.split("\n");
        byte[] valRet = validateVersion(Integer.valueOf(origStrArr[2].split(":")[1]));
        if (null != valRet) return valRet;
        StringBuffer dataBuf = new StringBuffer();
        dataBuf.append(origStrArr[0])
                .append("\n")
                .append(origStrArr[1])
                .append("\n")
                .append("app")
                .append(":")
                .append(fileName)//升级版本号要大于客户端传过来的版本号501>500
                .append("\n");
        return getBytesWithoutFile(CMD_VERSION_RESPONSE, dataBuf);
    }

    //2.2.3 请求升级文件(client)

    /**
     * 2.2.5 传送升级文件(server)
     *
     * @param cmd
     */
    public static byte[] transferFile(Integer cmd) {
        logger.info(">开始传送文件,应答指令[" + Integer.toHexString(cmd) + "]");
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
        logger.info(">接收文件应答-请求指令[" + Integer.toHexString(cmd) + "]");
        String resultStr = sb.toString();
        if (resultStr.contains(RECEIVE_FILE_COMPLETED)) {
            return RECEIVE_FILE_COMPLETED.getBytes();
        } else if (resultStr.contains(MD5_VALIDATION_FAILED)) {
            try {
                throw new Exception("文件md5校验失败");
            } catch (Exception e) {
                e.printStackTrace();
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
        logger.info(">返回报文,不包含文件字节流");
        byte[] resultByte = null;
        StringBuffer retSb = new StringBuffer();
        try {
            Integer dataLength = dataBuf.toString().getBytes().length;
            byte[] lengthByte = intToByteArray(dataLength);
            byte[] cmdByte = intToByteArray(cmd);
            byte[] dataByte = dataBuf.toString().getBytes();
            resultByte = getCommonBytes(lengthByte, cmdByte, dataByte);
        } catch (Exception e) {
            e.printStackTrace();
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
        logger.info(">返回文件字节流报文");
        byte[] resultByte = null;
        try {
            Integer dataLength = fileBytes.length;
            byte[] lengthByte = intToByteArray(dataLength);
            byte[] cmdByte = intToByteArray(cmd);
            resultByte = getCommonBytes(lengthByte, cmdByte, fileBytes);
        } catch (Exception e) {
            e.printStackTrace();
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
        if ("" == filePath || "" == fileName) {
            logger.info(">文件路径或文件名不存在!");
            return null;
        }
        logger.info(">开始获取文件" + filePath + fileName);
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
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
        logger.info(">文件输入流转字节数组");
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return bytestream.toByteArray();
    }

    /**
     * 校验设备版本
     */
    public static byte[] validateVersion(Integer deviceVersion) {
        int currentVersion = Integer.valueOf(fileName.split("\\.")[0]);
        if (currentVersion <= deviceVersion) {
            logger.info("没有新版本");
            StringBuffer sb = new StringBuffer();
            return getBytesWithoutFile(CMD_VERSION_RESPONSE, sb.append("none"));
        }
        logger.info("有可升级版本:" + currentVersion);
        return null;
    }

    /**
     * byte数组转int类型的对象
     *
     * @param b
     * @return
     */
    public static int byteArray2Int(byte[] b) {
        logger.info(">字节转整数");
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
        logger.info(">整数转字节");
        int byteNum = (40 - Integer.numberOfLeadingZeros(integer < 0 ? ~integer : integer)) / 8;
        byte[] byteArray = new byte[4];
        for (int n = 0; n < byteNum; n++)
            byteArray[3 - n] = (byte) (integer >>> (n * 8));
        return byteArray;
    }

    /**
     * 2.2.4 应答升级文件请求(server)
     *
     * @param cmd
     */
    public byte[] answerUpgrdeRequest(Integer cmd, StringBuffer sb) {
        logger.info(">应答升级文件请求-请求指令[" + Integer.toHexString(cmd) + "]应答指令[" + Integer.toHexString(CMD_UPGRADE_RESULT_RESPONSE) + "]");
        String origStr = sb.toString();
        String[] origStrArr = origStr.split("\n");
        if ("none".equalsIgnoreCase(origStrArr[2].split(":")[1])) {
            download = false;//客户端不下载
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
        return getBytesWithoutFile(CMD_UPGRADE_RESULT_RESPONSE, dataBuf);
    }

    /**
     * 2.2.8 升级结果查询的回复(server)
     *
     * @param cmd
     */
    public byte[] answerUpgradeResultRequest(Integer cmd, StringBuffer dataBuf) {
        logger.info(">应答升级文件请求-请求指令[" + Integer.toHexString(cmd) + "]应答指令[" + Integer.toHexString(CMD_UPGRADE_RESULT_RESPONSE) + "]");
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info(">计算文件md5值:" + md5);
        return md5;
    }
}