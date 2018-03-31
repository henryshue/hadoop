package cn.szse.i.flume.domain;

public class MetricsBean {

  String agentname;

  String listenin;

  long allin;

  long allout;

  long inrate;

  long outrate;

  long capacity;

  long channelsize;

  String sinkto;

  long takesuccess;

  long drainsuccess;

  public String getAgentname() {
    return agentname;
  }

  public void setAgentname(String agentname) {
    this.agentname = agentname;
  }

  public String getListenin() {
    return listenin;
  }

  public void setListenin(String listenin) {
    this.listenin = listenin;
  }

  public long getAllin() {
    return allin;
  }

  public void setAllin(long allin) {
    this.allin = allin;
  }

  public long getAllout() {
    return allout;
  }

  public void setAllout(long allout) {
    this.allout = allout;
  }

  public float getInrate() {
    return inrate;
  }

  public void setInrate(long inrate) {
    this.inrate = inrate;
  }

  public float getOutrate() {
    return outrate;
  }

  public void setOutrate(long outrate) {
    this.outrate = outrate;
  }

  public long getCapacity() {
    return capacity;
  }

  public void setCapacity(long capacity) {
    this.capacity = capacity;
  }

  public long getChannelsize() {
    return channelsize;
  }

  public void setChannelsize(long channelsize) {
    this.channelsize = channelsize;
  }

  public String getSinkto() {
    return sinkto;
  }

  public void setSinkto(String sinkto) {
    this.sinkto = sinkto;
  }

  public long getTakesuccess() {
    return takesuccess;
  }

  public void setTakesuccess(long takesuccess) {
    this.takesuccess = takesuccess;
  }

  public long getDrainsuccess() {
    return drainsuccess;
  }

  public void setDrainsuccess(long drainsuccess) {
    this.drainsuccess = drainsuccess;
  }

}
