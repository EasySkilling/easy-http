package com.easyhttp.demo.entity;


public class DietPlan {
    public String name;
    public String dietFunctionId;
    public String formulaIntro;
    public String healthValue;
    public String difficultyValue;
    public String costValue;
    public String totalValue;
    public String iconUrl;
    public String id;
    public String dietFunctionName;

    @Override
    public String toString() {
        return "DietPlanVO{" +
                "name='" + name + '\'' +
                ", dietFunctionId='" + dietFunctionId + '\'' +
                ", formulaIntro='" + formulaIntro + '\'' +
                ", healthValue='" + healthValue + '\'' +
                ", difficultyValue='" + difficultyValue + '\'' +
                ", costValue='" + costValue + '\'' +
                ", totalValue='" + totalValue + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", id='" + id + '\'' +
                ", dietFunctionName='" + dietFunctionName + '\'' +
                '}';
    }
}
