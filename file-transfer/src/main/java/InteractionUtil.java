
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * 交互操作
 */
//@Getter
//@Setter
public class InteractionUtil {

    /**
     * 2.2.1 获取设备版本信息(client)
     */
    public static final int VERSION_REQUEST=0x5000;

    /**
     * 2.2.2 返回设备版本信息(server)
     */
    public static final int VERSION_RESPONSE=0x5001;

    /**
     * 2.2.3 请求升级文件(client)
     */
    public static final int UPGRADE_REQUEST=0x5002;

    /**
     * 2.2.4 应答升级文件请求(server)
      */
    public static final int UPGRADE_RESPONSE=0x5003;

    /**
     * 2.2.5 传送升级文件(server)
     */
    public static final int UPGRADE_PACK_TRANSFER=0xF000;

    /**
     * 2.2.6 接收文件应答(client)
     */
    public static final int UPGRADE_PACK_RECEIVE_REQUEST=0xF001;

    /**
     * 2.2.7 查询升级结果(client)
     */
    public static final int UPGRADE_RESULT_REQUEST=0x5004;

    /**
     * 2.2.8 升级结果查询的回复(server)
     */
    public static final int UPGRADE_RESULT_RESPONSE=0x5005;

    public static int[] COMMANDS=null;
    
    static{
        Class clazz = null;
        try {
            clazz = Class.forName("InteractionUtil");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Field[] fields = clazz.getFields();

        for( Field field : fields ){
           // field.
        }
    }

    //获取文件
    public File[] uploadPackFromServer(String path){

        return null;
    }

    //2.2.1 获取设备版本信息(client)

    /**
     * 2.2.2 返回设备版本信息(server)
     * @param cmd
     */
    public void pushDeviceVersionInfo(int cmd){

    }

    //2.2.3 请求升级文件(client)

    /**
     * 2.2.4 应答升级文件请求(server)
     * @param cmd
     */
    public void answerUpgrdeRequest(int cmd){

    }

    /**
     *  2.2.5 传送升级文件(server)
     * @param cmd
     */
    public void transferPack(int cmd){

    }

    //2.2.6 接收文件应答(client)

    //2.2.7 查询升级结果(client)

    /**
     * 2.2.8 升级结果查询的回复(server)
     * @param cmd
     */
    public void answerUpgradeResultRequest(int cmd){

    }

    /**
     * 校验设备版本
     */
    public void validateVersion(){

    }

    /**
     * 加密码文件MD5
     * @param file
     */
    private void codeMD5(File file){

    }

    /**
     * 解码文件MD5
     * @param file
     */
    private void deCodeMD5(File file){

    }


}