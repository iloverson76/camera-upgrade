package com.gzrock.client;

import com.alibaba.fastjson.JSON;
import com.gzrock.server.UpgradeServer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

/**
 * @Date 2019/10/21 9:29
 * @Created by chp
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class JsonHttpClient {

    public static Log logger = LogFactory.getLog(JsonHttpClient.class);
    /**
     * json链路服务器
     */
    private static final String JSON_IP = "120.78.198.230";
    /**
     * 端口
     */
    private static final String JSON_PORT = "3668";
    /**
     * 3518在线设备ID
     */
    private String deviceId = "";
    /**
     * 在线用户token
     */
    private static String TOKEN = "";
    /**
     * 升级服务器IP
     */
    private static final String a_ipaddr = "47.107.243.212";
    /**
     * 升级服务器端口
     */
    private static final String un_port = "3518";
    /**
     * 在线3518设备ID
     */
    private static List<String> ONLINE_DEVICE_IDS;

    {
        ONLINE_DEVICE_IDS = getOnlineDeviceIds3518();
        TOKEN=getOnlineMemberToken();
    }

    public static void main(String[] args) {
        if(null==ONLINE_DEVICE_IDS){
            logger.info(">>没有在线3518设备!");
        }
        if(null==TOKEN){
            logger.info(">>>没有在线用户!");
        }
        ONLINE_DEVICE_IDS.forEach(id -> {
            new Thread(new UpgradrThread().builder()
                    .jsonIp(JSON_IP)
                    .jsonIp(JSON_PORT)
                    .deviceId(id)
                    .token(TOKEN)
                    .a_ipaddr(a_ipaddr)
                    .un_port(un_port).build()).start();
        });
    }

    /**
     * 获取3518在线设备ID
     *
     * @return
     */
    public static List<String> getOnlineDeviceIds3518() {

        return null;
    }

    /**
     * 获取在线用户token
     *
     * @return
     */
    public static String getOnlineMemberToken() {

        return "";
    }
}
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
class UpgradrThread implements Runnable{

    public static Log logger = LogFactory.getLog(JsonHttpClient.class);
    /**
     * json链路服务器
     */
    private String jsonIp = "";
    /**
     * 端口
     */
    private String jsonPort = "";
    /**
     * 3518在线设备ID
     */
    private String deviceId = "";
    /**
     * 在线用户token
     */
    private String token = "";
    /**
     * 升级服务器IP
     */
    private String a_ipaddr = "";
    /**
     * 升级服务器端口
     */
    private String un_port = "";
        @Override
        public void run() {
            startUpgrade(this.jsonIp,this.jsonIp,this.deviceId,this.token,this.a_ipaddr,this.un_port);
        }

        /**
         * 开始升级
         */
        private void startUpgrade(String targetIp, String targetPort,String deviceId,  String token,
                                  String a_ipaddr,String un_port) {

           targetIp="120.78.198.230";//json服务器
           targetPort="3668";//端口

           deviceId="868334033322417";//设备ID
           token = "55851367-8b24-44a5-9bbc-5d1a623cb8b2";//wp_member_info表取
           a_ipaddr="47.107.243.212";//升级文件服务器
           un_port="3518";//升级文件服务器端口

            String getRequestPathAndParams="http://"+targetIp+":"+targetPort+"/send_command?" +
                    "command=0x0273&deviceId="+deviceId+"&token="+token+"&a_ipaddr="+a_ipaddr+"&un_port="+un_port+"&cancelFlag=0";

            //get请求
            interfaceUtil(getRequestPathAndParams, "");

            //post请求
        /*interfaceUtil("http://172.83.28.221:7001/NSRTRegistration/test/add.do",
             "id=8888888&name=99999999");*/
        }

        /**
         * 调用对方接口方法
         *
         * @param path 对方或第三方提供的路径
         * @param data 向对方或第三方发送的数据，大多数情况下给对方发送JSON数据让对方解析
         */
        public static void interfaceUtil(String path, String data) {
            try {
                URL url = new URL(path);
                URLEncoder.encode(url.toString(),"UTF-8");
                //打开和url之间的连接
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                PrintWriter out = null;

                /**设置URLConnection的参数和普通的请求属性****start***/

                conn.setRequestProperty("accept", "*/*");
                conn.setRequestProperty("connection", "Keep-Alive");
                conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");

                /**设置URLConnection的参数和普通的请求属性****end***/

                //设置是否向httpUrlConnection输出，设置是否从httpUrlConnection读入，此外发送post请求必须设置这两个
                //最常用的Http请求无非是get和post，get请求可以获取静态页面，也可以把参数放在URL字串后面，传递给servlet，
                //post与get的 不同之处在于post的参数不是放在URL字串里面，而是放在http请求的正文内。
                conn.setDoOutput(true);
                conn.setDoInput(true);

                conn.setRequestMethod("GET");//GET和POST必须全大写
//            conn.setRequestMethod("POST");
                /**GET方法请求*****start*/
                /**
                 * 如果只是发送GET方式请求，使用connet方法建立和远程资源之间的实际连接即可；
                 * 如果发送POST方式的请求，需要获取URLConnection实例对应的输出流来发送请求参数。
                 * */
                conn.connect();

                /**GET方法请求*****end*/

                /***POST方法请求****start*/

           /* out = new PrintWriter(conn.getOutputStream());//获取URLConnection对象对应的输出流

            out.print(data);//发送请求参数即数据

            out.flush();//缓冲数据*/

                /***POST方法请求****end*/

                //获取URLConnection对象对应的输入流
                InputStream is = conn.getInputStream();
                //构造一个字符流缓存
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder result=new StringBuilder();
                String str="";
                while ((str = br.readLine()) != null) {
                    str = new String(str.getBytes(), "UTF-8");//解决中文乱码问题
                    result.append(str);
                    // logger.info(str);
                }
                //关闭流
                assert is !=null;
                is.close();
                //断开连接，最好写上，disconnect是在底层tcp socket链接空闲时才切断。如果正在被其他线程使用就不切断。
                //固定多线程的话，如果不disconnect，链接会增多，直到收发不出信息。写上disconnect后正常一些。
                conn.disconnect();
                String jsonResult=result.toString();

                //  Person newPerson = JSON.parseObject(jsonObject, Person.class);

                //设备返回结果
                //{"MessageType":"CtrlParamResponse",
                // "ResultCode":0,"Body":{"DeviceId":"868334033322417","DstInfo":{"SvrId":"123455","Session":"987654"},"DeviceParam":{"CMDType":628,"result":0}}}
                DeviceObject deviceObject;
                //230接口返回结果
                //{"ResultCode":2,"ResultDesc":"device is not online"}
                ForwardObject forwardObject;

                Object resultObject=null;
                if(jsonResult.contains("CMDType")){
                    resultObject =JSON.parseObject(jsonResult,DeviceObject.class);
                }else if (jsonResult.contains("ResultDesc")){
                    resultObject =JSON.parseObject(jsonResult,ForwardObject.class);
                }
                logger.info(resultObject.toString());
                logger.info("request complete!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }