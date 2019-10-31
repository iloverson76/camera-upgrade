package com.gzrock.data;

import com.baomidou.mybatisplus.toolkit.CollectionUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.yaml.snakeyaml.Yaml;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @Date 2019/11/1 9:29
 * @Created by chp
 */
@Data
@Builder
@AllArgsConstructor
@Accessors(chain = true)
@Slf4j
public class DeviceInfo {

    private static DriverManagerDataSource dataSource;
    private static JdbcTemplate jdbcTemplate;

    private static String url;

    private static String userName;

    private static String password;

    static{
        initDataSource();
    }

    private static void initDataSource(){
        Yaml yaml = new Yaml();
        Map<String, Object> ret = (Map<String, Object>) yaml.load(DeviceInfo.class
                .getClassLoader().getResourceAsStream("application.yaml"));
        LinkedHashMap spring= (LinkedHashMap) ret.get("spring");
        LinkedHashMap obj= (LinkedHashMap) spring.get("datasource");
        url= (String) obj.get("url");
        userName= (String) obj.get("username");
        password= (String) obj.get("password");
    }

    private static JdbcTemplate  getJdbcTemplate(){
        dataSource=new DriverManagerDataSource();
        dataSource.setUrl(url);
        dataSource.setUsername(userName);
        dataSource.setPassword(password);
        return new JdbcTemplate(dataSource);
    }

    /**
     * 获取3518在线设备ID
     *
     * @return
     */
    public List<String> getOnlineDeviceIds3518() {
        String sql = "select e.DeviceID from \n" +
                "(select a.versionmcu,a.DeviceID,b.id as qrcodeId,ifnull(a.verson,'109') as\n" +
                "\t\tverson,ifnull(a.network_status,1) as\n" +
                "\t\tnetwork_status,a.login_last_time,d.wp_member_info_id,ifnull(c.nickName,'') as nickName, ifnull(c.phone,'') as phone,d.deviceStatus\n" +
                "\t\tfrom wp_device_alldevice_info a\n" +
                "\t\tleft join wp_qrcode_info b on b.imei=a.DeviceID\n" +
                "\t\tleft join wp_device_info d on d.deviceId=a.DeviceID\n" +
                "\t\tleft join wp_member_info c on c.wp_member_info_id=d.wp_member_info_id\n" +
                "\t\twhere 1=1 and a.network_status=0 and a.device_type=1\n" +
                ") e\n" +
                "where e.verson>=210";
        List<Device> devices= getJdbcTemplate().query(sql, new DeviceRowMapper());
        List<String> deviceIds=new ArrayList<>(devices.size());
        if(CollectionUtils.isEmpty(devices)){
           //return Collections.EMPTY_LIST;
        }
        log.info("在线3518设备=====================↓");
        devices.forEach(device -> {
            String deviceId=device.getDeviceId();
            deviceIds.add(deviceId);
            log.info(deviceId);
        });
        deviceIds.add("868334033322417");
        log.info("在线3518设备=====================↑");
        return deviceIds;
    }
    /**
     * 获取在线用户token
     *
     * @return
     */
    public String getOnlineMemberToken() {
        String sql="select token from wp_member_info where token is not null limit 0,1";
        Device device= getJdbcTemplate().queryForObject(sql, new TokenRowMapper());
        String token=device.getToken();
        log.info(">>>在线用户token:"+token);
        //return token;
        return "6ebb3376-8241-4da3-9a43-16a482c7f82d";
    }
}

class DeviceRowMapper implements RowMapper<Device> {
    //rs为返回结果集，以每行为单位封装着
    public Device mapRow(ResultSet rs, int rowNum) throws SQLException {
       return new Device().builder().deviceId(rs.getString("DeviceId")).build();
    }
}
class TokenRowMapper implements RowMapper<Device> {
    //rs为返回结果集，以每行为单位封装着
    public Device mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Device().builder().token(rs.getString("token")).build();
    }
}