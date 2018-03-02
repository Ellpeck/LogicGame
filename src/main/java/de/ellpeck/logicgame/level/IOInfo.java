package de.ellpeck.logicgame.level;

public class IOInfo{

    public final int x;
    public final int y;
    public final String name;
    public final boolean isInput;

    public IOInfo(int x, int y, String name, boolean isInput){
        this.x = x;
        this.y = y;
        this.name = name;
        this.isInput = isInput;
    }
}
