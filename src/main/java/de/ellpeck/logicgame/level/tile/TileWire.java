package de.ellpeck.logicgame.level.tile;

import de.ellpeck.logicgame.level.Level;
import de.ellpeck.logicgame.render.TextureSheet;
import de.ellpeck.logicgame.util.Colors;
import de.ellpeck.logicgame.util.Direction;

public class TileWire extends LogicTile{

    public final int color;
    private boolean isEnabled;

    public TileWire(Level level, int x, int y, int color){
        super(level, x, y);
        this.color = color;
    }

    @Override
    public void render(TextureSheet sheet, float renderX, float renderY, float scale){
        int renderColor = this.isEnabled ? this.color : Colors.multiply(this.color, 0.5F);

        sheet.get(1, 0).draw(renderX, renderY, scale, scale, renderColor);

        for(Direction dir : Direction.ADJACENT){
            if(this.level.isInBounds(this.x+dir.x, this.y+dir.y)){
                LogicTile tile = this.level.getTile(this.x+dir.x, this.y+dir.y);
                if(tile != null && tile.connectsToWire(this.x, this.y, this.color)){
                    sheet.get(getSheetPos(dir), 0).draw(renderX, renderY, scale, scale, renderColor);
                }
            }
            else if(this.level.getIOInfo(this.x, this.y) != null){
                sheet.get(getSheetPos(dir), 0).draw(renderX, renderY, scale, scale, renderColor);
            }
        }
    }

    private static int getSheetPos(Direction dir){
        switch(dir){
            case DOWN:
                return 2;
            case LEFT:
                return 3;
            case UP:
                return 4;
            case RIGHT:
                return 5;
            default:
                return -1;
        }
    }

    @Override
    public boolean connectsToWire(int wireX, int wireY, int wireColor){
        return wireColor == this.color;
    }

    @Override
    public boolean isFullTile(){
        return false;
    }
}
