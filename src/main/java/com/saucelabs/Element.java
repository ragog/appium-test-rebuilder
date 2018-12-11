package com.saucelabs;

public class Element {

    private String id;
    private String name;
    private String strategy;
    private String strategyValue;

    public Element(String id, String name, String strategy, String strategyValue) {
        this.id = id;
        this.name = name;
        this.strategy = strategy;
        this.strategyValue = strategyValue;
    }

    public String getName() {
        return name;
    }

    public String getStrategy() {
        return strategy;
    }

    public String getStrategyValue() {
        return strategyValue;
    }

    public String getId() {
        return id;
    }
}
