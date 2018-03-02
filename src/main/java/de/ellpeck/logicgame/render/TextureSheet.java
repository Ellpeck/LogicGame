package de.ellpeck.logicgame.render;

import de.ellpeck.logicgame.render.engine.Texture;

public class TextureSheet{

    private final Texture[][] subTextures;

    public TextureSheet(Texture texture, int width, int height){
        this.subTextures = new Texture[width][height];

        int widthRatio = texture.getTextureWidth()/width;
        int heightRatio = texture.getTextureHeight()/height;

        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                this.subTextures[x][y] = texture.getSubTexture(x*widthRatio, y*widthRatio, widthRatio, heightRatio);
            }
        }
    }

    public Texture get(int x, int y){
        return this.subTextures[x][y];
    }
}
