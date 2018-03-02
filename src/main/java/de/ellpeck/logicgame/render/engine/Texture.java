package de.ellpeck.logicgame.render.engine;

import com.google.common.io.ByteStreams;
import de.ellpeck.logicgame.Main;
import de.ellpeck.logicgame.util.Colors;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class Texture implements IDisposable{

    public static int binds;

    private static final int[] BOUND_TEXTURES = new int[TextureBank.BANKS.length];
    private static TextureBank activeBank = TextureBank.BANK_1;

    private final int id;
    private int textureWidth;
    private int textureHeight;
    private int renderWidth;
    private int renderHeight;
    private int renderOffsetX;
    private int renderOffsetY;
    private ByteBuffer pixelData;

    public Texture(int textureWidth, int textureHeight, ByteBuffer data){
        this(GL11.GL_NEAREST);
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.pixelData = data;

        this.init();
    }

    public Texture(InputStream stream) throws Exception{
        this(GL11.GL_NEAREST);
        this.load(stream);
    }

    public Texture(int filter){
        this.id = GL11.glGenTextures();

        this.param(GL11.GL_TEXTURE_MIN_FILTER, filter);
        this.param(GL11.GL_TEXTURE_MAG_FILTER, filter);
    }

    private Texture(int id, int textureWidth, int textureHeight, ByteBuffer pixelData){
        this.id = id;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.pixelData = pixelData;
    }

    public Texture getSubTexture(int x, int y, int width, int height){
        Texture sub = new Texture(this.getId(), this.textureWidth, this.textureHeight, this.pixelData);

        sub.renderOffsetX = this.renderOffsetX+x;
        sub.renderOffsetY = this.renderOffsetY+y;
        sub.renderWidth = width;
        sub.renderHeight = height;

        return sub;
    }

    public void draw(float x, float y){
        this.draw(x, y, 1F);
    }

    public void draw(float x, float y, float scale){
        this.draw(x, y, this.renderWidth*scale, this.renderHeight*scale);
    }

    public void draw(float x, float y, float width, float height){
        this.draw(x, y, width, height, Colors.WHITE);
    }

    public void draw(float x, float y, float width, float height, int filter){
        this.draw(x, y, x+width, y+height, 0, 0, this.renderWidth, this.renderHeight, filter);
    }

    public void draw(float x, float y, float x2, float y2, float srcX, float srcY, float srcX2, float srcY2){
        this.draw(x, y, x2, y2, srcX, srcY, srcX2, srcY2, Colors.WHITE);
    }

    public void draw(float x, float y, float x2, float y2, float srcX, float srcY, float srcX2, float srcY2, int filter){
        this.draw(x, y, x, y2, x2, y2, x2, y, srcX, srcY, srcX2, srcY2, filter);
    }

    public void draw(float x, float y, float x2, float y2, float x3, float y3, float x4, float y4, float srcX, float srcY, float srcX2, float srcY2, int filter){
        Main.game.renderer.addTexturedRegion(this, x, y, x2, y2, x3, y3, x4, y4, srcX, srcY, srcX2, srcY2, filter);
    }

    public int getTextureColor(int x, int y){
        int offset = (x+this.renderOffsetX+((y+this.renderOffsetY)*this.textureWidth))*4;
        return Colors.rgb(this.pixelData.get(offset), this.pixelData.get(offset+1), this.pixelData.get(offset+2), this.pixelData.get(offset+3));
    }

    private void load(InputStream stream) throws Exception{
        if(stream == null){
            throw new IOException("Missing input stream! The texture might not be at the location it's supposed to be");
        }

        byte[] input = ByteStreams.toByteArray(stream);
        ByteBuffer data = BufferUtils.createByteBuffer(input.length);
        data.put(input);
        ((Buffer)data).flip();

        MemoryStack stack = MemoryStack.stackPush();
        IntBuffer width = stack.mallocInt(1);
        IntBuffer height = stack.mallocInt(1);

        this.pixelData = STBImage.stbi_load_from_memory(data, width, height, stack.mallocInt(1), 4);
        if(this.pixelData == null){
            throw new IOException("Failed to load texture:\n"+STBImage.stbi_failure_reason());
        }

        this.textureWidth = width.get();
        this.textureHeight = height.get();

        this.renderWidth = this.textureWidth;
        this.renderHeight = this.textureHeight;

        stack.pop();

        this.init();
    }

    public static void activateTextureBank(TextureBank bank){
        if(activeBank != bank){
            GL13.glActiveTexture(GL13.GL_TEXTURE0+bank.ordinal());
            activeBank = bank;
        }
    }

    public void bind(TextureBank bank, boolean revertAfterBind){
        TextureBank last = activeBank;

        activateTextureBank(bank);
        this.bind();

        if(revertAfterBind){
            activateTextureBank(last);
        }
    }

    public void bind(){
        if(BOUND_TEXTURES[activeBank.ordinal()] != this.id){
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.id);
            BOUND_TEXTURES[activeBank.ordinal()] = this.id;

            binds++;
        }
    }

    public void unbind(TextureBank bank, boolean revertAfterUnbind){
        TextureBank last = activeBank;

        activateTextureBank(bank);
        this.unbind();

        if(revertAfterUnbind){
            activateTextureBank(last);
        }
    }

    public void unbind(){
        if(BOUND_TEXTURES[activeBank.ordinal()] == this.id){
            unbindCurrentBank();
        }
    }

    public static void unbindCurrentBank(){
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        BOUND_TEXTURES[activeBank.ordinal()] = -1;
    }

    public static void unbindAllBanks(){
        TextureBank last = activeBank;

        for(int i = 0; i < BOUND_TEXTURES.length; i++){
            if(BOUND_TEXTURES[i] >= 0){
                activateTextureBank(TextureBank.BANKS[i]);
                unbindCurrentBank();
            }
        }

        activateTextureBank(last);
    }

    private void init(){
        this.bind();
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, this.textureWidth, this.textureHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, this.pixelData);
    }

    public void param(int param, int value){
        this.bind();
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, param, value);
    }

    public int getId(){
        return this.id;
    }

    public int getTextureWidth(){
        return this.textureWidth;
    }

    public int getTextureHeight(){
        return this.textureHeight;
    }

    public ByteBuffer getPixelData(){
        return this.pixelData;
    }

    @Override
    public void dispose(){
        this.unbind();
        GL11.glDeleteTextures(this.id);
    }

    public int getRenderWidth(){
        return this.renderWidth;
    }

    public int getRenderHeight(){
        return this.renderHeight;
    }

    public int getRenderOffsetX(){
        return this.renderOffsetX;
    }

    public int getRenderOffsetY(){
        return this.renderOffsetY;
    }
}
