package de.ellpeck.logicgame.level.tile;

import de.ellpeck.logicgame.level.Level;
import de.ellpeck.logicgame.render.TextureSheet;

public abstract class LogicTile{

    public final Level level;
    public final int x;
    public final int y;

    public LogicTile(Level level, int x, int y){
        this.level = level;
        this.x = x;
        this.y = y;
    }

    public abstract void render(TextureSheet sheet, float renderX, float renderY, float scale);

    public boolean connectsToWire(int wireX, int wireY, int wireColor){
        return false;
    }

    public boolean isFullTile(){
        return true;
    }
}
