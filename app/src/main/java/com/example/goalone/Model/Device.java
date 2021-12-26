package com.example.goalone.Model;

import java.util.ArrayList;

public class Device {

    private String macAddress;
    private long lastIdentifiedTime;
    private Threat threatLevel;
    private ArrayList<Integer> rssis;
    private double averageDistance;
    private String user = "UnKnown";
    private String uName = "UnKnown";
    private Threat maxThreat;

    public Device() {
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public Threat getMaxThreat() {
        return maxThreat;
    }

    public Device(String user, String macAddress, long lastIdentifiedTime, Threat threatLevel) {
        this.user = user;
        this.macAddress = macAddress;
        this.lastIdentifiedTime = lastIdentifiedTime;
        this.threatLevel = threatLevel;
        this.averageDistance = 0;
        rssis = new ArrayList<Integer>();
        this.maxThreat = threatLevel;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }


    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public long getLastIdentifiedTime() {
        return lastIdentifiedTime;
    }

    public void setLastIdentifiedTime(long lastIdentifiedTime) {
        this.lastIdentifiedTime = lastIdentifiedTime;
    }

    public Threat getThreatLevel() {
        return threatLevel;
    }

    public void setThreatLevel(Threat threatLevel) {
        maxThreat = (maxThreat.getValue() < threatLevel.getValue()) ? threatLevel : maxThreat;
        this.threatLevel = threatLevel;
    }

    public enum Threat {
        NONE(0),
        LEVEL1(1),
        LEVEL2(2),
        LEVEL3(3),
        ;
        int value;

        Threat(int val) {
            this.value = val;
        }

        public int getValue() {
            return value;
        }
    }

    public ArrayList<Integer> getRssis() {
        return rssis;
    }

    public void addRssi(Integer rssi) {
        rssis.add(rssi);
    }

    public double getAverageDistance() {
        return averageDistance;
    }

    public void setAverageDistance(double averageDistance) {
        System.out.println("Average distance: " + Integer.toString((int) averageDistance));
        this.averageDistance = averageDistance;
        if (this.averageDistance < 1) setThreatLevel(Threat.LEVEL3);
        else if (this.averageDistance < 2) setThreatLevel(Threat.LEVEL2);
        else setThreatLevel(Threat.LEVEL1);
    }
}
