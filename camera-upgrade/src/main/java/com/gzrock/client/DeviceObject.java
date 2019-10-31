package com.gzrock.client;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @Date 2019/10/31 17:30
 * @Created by chp
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DeviceObject {
    //设备返回结果
    //{"MessageType":"CtrlParamResponse",
    // "ResultCode":0,"Body":{"DeviceId":"868334033322417","DstInfo":{"SvrId":"123455","Session":"987654"},"DeviceParam":{"CMDType":628,"result":0}}}
    @JSONField(name = "MessageType")
    private String MessageType;
    @JSONField(name = "ResultCode")
    private String ResultCode;
}
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
class Body{
    @JSONField(name = "DeviceId")
    private String DeviceId;
    @JSONField(name = "destInfo")
    private DstInfo destInfo;
    @JSONField(name = "deviceParam")
    private DeviceParam deviceParam;
}
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
class DstInfo{
    @JSONField(name = "SvrId")
    private String SvrId;
    @JSONField(name = "Session")
    private String Session;
}
@Data@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
class DeviceParam{
    @JSONField(name = "CMDType")
    private String CMDType;
    @JSONField(name = "result")
    private String result;
}