package com.gzrock.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Data
@Builder
@AllArgsConstructor
@Accessors(chain = true)
@Slf4j
public class DeviceWfiGetsRouting {
    private String deviceId;
    private String software;
}
