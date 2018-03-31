package cn.szse.i.flume.api.query;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.stereotype.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import cn.szse.i.flume.domain.ConfigBean;
import cn.szse.i.flume.domain.MetricsBean;

@Controller
@EnableAutoConfiguration
@EnableConfigurationProperties(ConfigBean.class)
@RestController
public class MetricsController extends SpringBootServletInitializer {

  private static MetricsAnalyze metricsAnalyze = null;
  private String ip1;
  private String ip2;
  private String ip3;
  private String ip4;

  @Autowired
  private ConfigBean configBean;

  private void setFlumeConfig() {
    String ipaddress = configBean.getIpaddress();
    String logTypeStr = configBean.getLogtype();
    String portStr = configBean.getPort();
    String delayPeriod = configBean.getDealyperiod();

    ip1 = ipaddress.split(",")[0];
    ip2 = ipaddress.split(",")[1];
    ip3 = ipaddress.split(",")[2];
    ip4 = ipaddress.split(",")[3];

    if (null == metricsAnalyze) {
      metricsAnalyze = new MetricsAnalyze();
      metricsAnalyze.run_analyze(ipaddress, logTypeStr, portStr, delayPeriod);
    }
  }

  @RequestMapping("sys/index")
  public ModelAndView page() {

    setFlumeConfig();

    List<MetricsBean> metricsList = metricsAnalyze.getMetricsListA(0);

    ModelAndView mav = new ModelAndView("sys/index");
    mav.addObject("ip1", ip1 + "(nei-c)");
    mav.addObject("ip2", ip2 + "(wai-c)");
    mav.addObject("ip3", ip3 + "(jy-c)");
    mav.addObject("ip4", ip4 + "(nei-a)");
    mav.addObject("metricsList", metricsList);
    return mav;
  }

  @RequestMapping("sys/index2")
  public ModelAndView page2() {
    setFlumeConfig();

    List<MetricsBean> metricsList = metricsAnalyze.getMetricsListA(1);

    ModelAndView mav = new ModelAndView("sys/index2");
    mav.addObject("ip1", ip1 + "(nei-c)");
    mav.addObject("ip2", ip2 + "(wai-c)");
    mav.addObject("ip3", ip3 + "(jy-c)");
    mav.addObject("ip4", ip4 + "(nei-a)");
    mav.addObject("metricsList", metricsList);
    return mav;
  }

  @RequestMapping("sys/index3")
  public ModelAndView page3() {
    setFlumeConfig();

    List<MetricsBean> metricsList = metricsAnalyze.getMetricsListA(2);

    ModelAndView mav = new ModelAndView("sys/index3");
    mav.addObject("ip1", ip1 + "(nei-c)");
    mav.addObject("ip2", ip2 + "(wai-c)");
    mav.addObject("ip3", ip3 + "(jy-c)");
    mav.addObject("ip4", ip4 + "(nei-a)");
    mav.addObject("metricsList", metricsList);
    return mav;
  }

  @RequestMapping("sys/index4")
  public ModelAndView page4() {
    setFlumeConfig();

    List<MetricsBean> metricsList = metricsAnalyze.getMetricsListB(0);

    ModelAndView mav = new ModelAndView("sys/index4");
    mav.addObject("ip1", ip1 + "(nei-c)");
    mav.addObject("ip2", ip2 + "(wai-c)");
    mav.addObject("ip3", ip3 + "(jy-c)");
    mav.addObject("ip4", ip4 + "(nei-a)");
    mav.addObject("metricsList", metricsList);
    return mav;
  }

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
    // TODO Auto-generated method stub
    builder.sources(this.getClass());
    return super.configure(builder);
  }

  public static void main(String[] args) {
    // TODO Auto-generated method stub
    SpringApplication.run(MetricsController.class, args);
  }

}