package vibecode.karootheme.ui;

public class MapColors {
    private int forestColor;
    private int farmColor;
    private int scrubColor;
    private int grassColor;
    private int riverColor;

    public MapColors(int forestColor, int farmColor, int scrubColor, int grassColor, int riverColor) {
        this.forestColor = forestColor;
        this.farmColor = farmColor;
        this.scrubColor = scrubColor;
        this.grassColor = grassColor;
        this.riverColor = riverColor;
    }

    public int getForestColor() {
        return forestColor;
    }

    public void setForestColor(int forestColor) {
        this.forestColor = forestColor;
    }

    public int getFarmColor() {
        return farmColor;
    }

    public void setFarmColor(int farmColor) {
        this.farmColor = farmColor;
    }

    public int getScrubColor() {
        return scrubColor;
    }

    public void setScrubColor(int scrubColor) {
        this.scrubColor = scrubColor;
    }

    public int getGrassColor() {
        return grassColor;
    }

    public void setGrassColor(int grassColor) {
        this.grassColor = grassColor;
    }

    public int getRiverColor() {
        return riverColor;
    }

    public void setRiverColor(int riverColor) {
        this.riverColor = riverColor;
    }
}
