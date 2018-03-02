package de.ellpeck.logicgame.level.tile;

import de.ellpeck.logicgame.level.Level;

public class TileWire extends LogicTile{

    public final int color;

    public TileWire(Level level, int x, int y, int color){
        super(level, x, y);
        this.color = color;
    }
}
