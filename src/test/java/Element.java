import org.apache.commons.lang3.RandomStringUtils;

public class Element {

    private String id;
    private String name;
    private String strategy;
    private String strategyValue;

    public Element(String id, String strategy, String strategyValue) {
        this.id = id;
        this.name = RandomStringUtils.randomAlphabetic(8);
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
