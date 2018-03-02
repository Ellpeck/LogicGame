package de.ellpeck.logicgame.level.tile;

import de.ellpeck.logicgame.level.Level;

public class LogicTile{

    public final Level level;
    public final int x;
    public final int y;

    public LogicTile(Level level, int x, int y){
        this.level = level;
        this.x = x;
        this.y = y;
    }
}
