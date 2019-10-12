package bean;

import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
public class BusiData {

    /**
     * 设备ID
     * 值类型：用值"xxxx"直接表示，不是"deviceId:xxxx"
     */
    private String deviceId;
    /**
     * 设备型号：值类型
     */
    private String deviceType;
    /**
     * k：v 形式 (默认 )
     */
    private String app;
    private String fw;
    private String kernel;
    private String uboot;
    private String error;
    private String startpos;
    private String dstApp;
    private String dstFw;
    private String dstKernel;
    private String dstUboot;
    private String none;
    private String result;
    private String md5;

    /**
     * 二进制文件流：值类型
     */
    private byte[] fileData;

}
