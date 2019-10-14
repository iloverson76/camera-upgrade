package bean;

import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class BusiData implements Serializable {

    private static final long serialVersionUID = 223502723917074681L;
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
    private String appName="app";
    private String appValue;
    private String fwName="fw";
    private String kernelName="kernel";
    private String kernelValue;
    private String ubootName="uboot";
    private String ubootValue;
    private String errorName="error";
    private String errorValue;
    private String startposName="startpos";
    private String startposValue;
    private String dstAppName="destapp";
    private String destAppValue;
    private String dstFwName="destfw";
    private String destFwValue;
    private String dstKernel="destkernel";
    private String destKernelValue;
    private String dstUboot="uboot";
    private String destUbootValue;
    private String none;
    private String result;
    private String md5;
    private String fileName;

    /**
     * 二进制文件流：值类型
     */
    private byte[] fileData;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppValue() {
        return appValue;
    }

    public void setAppValue(String appValue) {
        this.appValue = appValue;
    }

    public String getFwName() {
        return fwName;
    }

    public void setFwName(String fwName) {
        this.fwName = fwName;
    }

    public String getKernelName() {
        return kernelName;
    }

    public void setKernelName(String kernelName) {
        this.kernelName = kernelName;
    }

    public String getKernelValue() {
        return kernelValue;
    }

    public void setKernelValue(String kernelValue) {
        this.kernelValue = kernelValue;
    }

    public String getUbootName() {
        return ubootName;
    }

    public void setUbootName(String ubootName) {
        this.ubootName = ubootName;
    }

    public String getUbootValue() {
        return ubootValue;
    }

    public void setUbootValue(String ubootValue) {
        this.ubootValue = ubootValue;
    }

    public String getErrorName() {
        return errorName;
    }

    public void setErrorName(String errorName) {
        this.errorName = errorName;
    }

    public String getErrorValue() {
        return errorValue;
    }

    public void setErrorValue(String errorValue) {
        this.errorValue = errorValue;
    }

    public String getStartposName() {
        return startposName;
    }

    public void setStartposName(String startposName) {
        this.startposName = startposName;
    }

    public String getStartposValue() {
        return startposValue;
    }

    public void setStartposValue(String startposValue) {
        this.startposValue = startposValue;
    }

    public String getDstAppName() {
        return dstAppName;
    }

    public void setDstAppName(String dstAppName) {
        this.dstAppName = dstAppName;
    }

    public String getDestAppValue() {
        return destAppValue;
    }

    public void setDestAppValue(String destAppValue) {
        this.destAppValue = destAppValue;
    }

    public String getDstFwName() {
        return dstFwName;
    }

    public void setDstFwName(String dstFwName) {
        this.dstFwName = dstFwName;
    }

    public String getDestFwValue() {
        return destFwValue;
    }

    public void setDestFwValue(String destFwValue) {
        this.destFwValue = destFwValue;
    }

    public String getDstKernel() {
        return dstKernel;
    }

    public void setDstKernel(String dstKernel) {
        this.dstKernel = dstKernel;
    }

    public String getDestKernelValue() {
        return destKernelValue;
    }

    public void setDestKernelValue(String destKernelValue) {
        this.destKernelValue = destKernelValue;
    }

    public String getDstUboot() {
        return dstUboot;
    }

    public void setDstUboot(String dstUboot) {
        this.dstUboot = dstUboot;
    }

    public String getDestUbootValue() {
        return destUbootValue;
    }

    public void setDestUbootValue(String destUbootValue) {
        this.destUbootValue = destUbootValue;
    }

    public String getNone() {
        return none;
    }

    public void setNone(String none) {
        this.none = none;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
