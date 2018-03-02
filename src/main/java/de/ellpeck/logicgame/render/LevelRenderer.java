package de.ellpeck.logicgame.render;

import de.ellpeck.logicgame.Game;
import de.ellpeck.logicgame.Registry;
import de.ellpeck.logicgame.level.Level;
import de.ellpeck.logicgame.level.tile.LogicTile;

public class LevelRenderer{

    private final Level level;

    public LevelRenderer(Level level){
        this.level = level;
    }

    public void render(Game game){
        float displayRatio = Math.min(game.getWidth()/16F, game.getHeight()/9F);
        float levelRatio = Math.max(this.level.width/16F, this.level.height/9F);
        float scale = displayRatio/levelRatio;

        float startX = (game.getWidth()-(this.level.width*scale))/2F;
        float startY = (game.getHeight()-(this.level.height*scale))/2F;

        for(int x = 0; x < this.level.width; x++){
            for(int y = 0; y < this.level.height; y++){
                LogicTile tile = this.level.getTile(x, y);

                if(tile == null || !tile.isFullTile()){
                    Registry.tileSheet.get(0, 0).draw(startX+x*scale, startY+y*scale, scale, scale);
                }

                if(tile != null){
                    tile.render(Registry.tileSheet, startX+x*scale, startY+y*scale, scale);
                }
            }
        }
    }
}
