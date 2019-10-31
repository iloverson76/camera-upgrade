package com.gzrock.client;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * @Date 2019/10/31 17:30
 * @Created by chp
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ForwardObject {
    //230接口返回结果
    //{"ResultCode":2,"ResultDesc":"device is not online"}
    @JSONField(name = "ResultCode")
    private String ResultCode;
    @JSONField(name = "ResultDesc")
    private String ResultDesc;
}