package com.meroapp.stayhome.data;

public class CovidData{
    private String totalCase;
    private String isolation;
    private String quarantined;
    private String positive;
    private String totalDeaths;
    private String totalRecovered;

    public CovidData() {
    }

    public String getTotalCase() {
        return totalCase;
    }

    public void setTotalCase(String totalCase) {
        this.totalCase = totalCase;
    }

    public String getIsolation() {
        return isolation;
    }

    public void setIsolation(String isolation) {
        this.isolation = isolation;
    }

    public String getQuarantined() {
        return quarantined;
    }

    public void setQuarantined(String quarantined) {
        this.quarantined = quarantined;
    }

    public String getPositive() {
        return positive;
    }

    public void setPositive(String positive) {
        this.positive = positive;
    }

    public String getTotalDeaths() {
        return totalDeaths;
    }

    public void setTotalDeaths(String totalDeaths) {
        this.totalDeaths = totalDeaths;
    }

    public String getTotalRecovered() {
        return totalRecovered;
    }

    public void setTotalRecovered(String totalRecovered) {
        this.totalRecovered = totalRecovered;
    }
}
