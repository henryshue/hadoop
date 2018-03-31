package cn.szse.i.flume.domain;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "config", ignoreUnknownFields = false)
@PropertySource("classpath:config/monitor-config.properties")
@Data
@Component
public class ConfigBean {

  private String ipaddress;
  private String logtype;
  private String port;
  private String dealyperiod;
  private String username;
  private String password;

  public String getIpaddress() {
    return ipaddress;
  }

  public void setIpaddress(String ipaddress) {
    this.ipaddress = ipaddress;
  }

  public String getLogtype() {
    return logtype;
  }

  public void setLogtype(String logtype) {
    this.logtype = logtype;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public String getDealyperiod() {
    return dealyperiod;
  }

  public void setDealyperiod(String dealyperiod) {
    this.dealyperiod = dealyperiod;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

}
