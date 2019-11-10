package com.gzrock.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@Accessors(chain = true)
@Slf4j
public class DeviceUpgradeRecord {
    private String imei;
    private Date beginTime;
    private Date endTime;
    private String oldVersion;
    private String newVersion;
    private Integer upgradeResult;
}
