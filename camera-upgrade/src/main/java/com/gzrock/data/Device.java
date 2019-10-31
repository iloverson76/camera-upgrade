package com.gzrock.data;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @Date 2019/11/1 9:43
 * @Created by chp
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Device {

    /*private String versionmcu;
    private String qrcodeId;
    private Integer verson;
    private Integer networkStatus;
    private Date loginLastTime;
    private String wpMemberInfoId;
    private String nickName;
    private String phone;
    private Integer deviceStatus;*/

    private String deviceId;
    private String token;
}
