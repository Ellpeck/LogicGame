package de.ellpeck.logicgame.renderer;

import de.ellpeck.logicgame.Main;
import de.ellpeck.logicgame.util.Colors;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import java.nio.Buffer;
import java.nio.FloatBuffer;

public class Renderer implements IDisposable{

    private final VertexBufferObject vbo;
    private final FloatBuffer vertices;

    private ShaderProgram defaultProgram;
    private ShaderProgram program;
    private int vertexAmount;
    private int componentCounter;
    private boolean isDrawing;
    private Texture texture;
    private int backgroundColor;

    private float rotationCenterX;
    private float rotationCenterY;
    private float rotation;
    private float sinRot;
    private float cosRot;

    private float translationX;
    private float translationY;
    private float scaleX;
    private float scaleY;

    private boolean mirroredHor;
    private boolean mirroredVert;

    private int lastFlushes;
    private int flushCounter;

    public Renderer(){
        this.vbo = new VertexBufferObject(false);
        this.vertices = MemoryUtil.memAllocFloat(Main.vertexCache);

        this.vbo.data(this.vertices.capacity()*Float.BYTES);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void setDefaultProgram(ShaderProgram defaultProgram){
        this.defaultProgram = defaultProgram;
        this.setProgram(null);
    }

    public void setProgram(ShaderProgram program){
        if(program == null){
            program = this.defaultProgram;
        }

        if((this.program == null) != (program == null) || this.program.getId() != program.getId()){
            if(this.isDrawing){
                this.flush();
            }

            this.program = program;
        }
    }

    public void setTexture(Texture texture){
        if((this.texture == null) != (texture == null) || this.texture.getId() != texture.getId()){
            if(this.isDrawing){
                this.flush();
            }

            this.texture = texture;

            if(this.texture != null){
                this.texture.bind();
            }
        }
    }

    public void addTexturedRegion(Texture texture, float x, float y, float x2, float y2, float x3, float y3, float x4, float y4, float srcX, float srcY, float srcX2, float srcY2, int filter){
        this.setTexture(texture);

        float u = (srcX+texture.getRenderOffsetX())/texture.getTextureWidth();
        float v = (srcY+texture.getRenderOffsetY())/texture.getTextureHeight();
        float u2 = (srcX2+texture.getRenderOffsetX())/texture.getTextureWidth();
        float v2 = (srcY2+texture.getRenderOffsetY())/texture.getTextureHeight();

        if(this.mirroredHor){
            float temp = u2;
            u2 = u;
            u = temp;
        }
        if(this.mirroredVert){
            float temp = v2;
            v2 = v;
            v = temp;
        }

        this.program.getProcessor().addTexturedRegion(this, texture, x, y, x2, y2, x3, y3, x4, y4, u, v, u2, v2, filter, filter, filter, filter);
    }

    public void addTriangle(float x1, float y1, float x2, float y2, float x3, float y3, int color1, int color2, int color3, float u1, float v1, float u2, float v2, float u3, float v3){
        this.program.getProcessor().addTriangle(this, x1, y1, x2, y2, x3, y3, color1, color2, color3, u1, v1, u2, v2, u3, v3);
    }

    public void addVertex(float x, float y, int color, float u, float v){
        float theX;
        float theY;

        if(this.rotation != 0F){
            if(this.rotationCenterX != 0F){
                x -= this.rotationCenterX;
                theX = this.rotationCenterX+x*this.cosRot-y*this.sinRot;
            }
            else{
                theX = x*this.cosRot-y*this.sinRot;
            }

            if(this.rotationCenterY != 0F){
                y -= this.rotationCenterY;
                theY = this.rotationCenterY+x*this.sinRot+y*this.cosRot;
            }
            else{
                theY = x*this.sinRot+y*this.cosRot;
            }

        }
        else{
            theX = x;
            theY = y;
        }

        if(this.translationX != 0F){
            theX += this.translationX;
        }
        if(this.translationY != 0F){
            theY += this.translationY;
        }

        if(this.scaleX != 1F){
            theX *= this.scaleX;
        }
        if(this.scaleY != 1F){
            theY *= this.scaleY;
        }

        this.program.getProcessor().addVertex(this, theX, theY, color, u, v);
    }

    public Renderer put(float f){
        if(this.isDrawing){
            if(this.vertices.remaining() < this.program.getComponentsPerVertex()*3 && this.vertexAmount%3 == 0){
                this.flush();
            }

            this.vertices.put(f);

            this.componentCounter++;
            if(this.componentCounter >= this.program.getComponentsPerVertex()){
                this.vertexAmount++;
                this.program.getProcessor().onVertexCompleted(this);

                this.componentCounter = 0;
            }

            return this;
        }
        else{
            throw new RuntimeException("Can't add vertices to a renderer while it's not drawing!");
        }
    }

    public void begin(){
        if(!this.isDrawing){
            ((Buffer)this.vertices).clear();
            this.vertexAmount = 0;
            this.flushCounter = 0;

            this.resetTransformation();

            this.isDrawing = true;

            this.program.getProcessor().onBegin(this);
        }
        else{
            throw new RuntimeException("Can't begin a renderer that is already drawing!");
        }
    }

    public void end(){
        if(this.isDrawing){
            this.flush();

            this.isDrawing = false;

            this.lastFlushes = this.flushCounter;
            this.flushCounter = 0;

            this.program.getProcessor().onEnd(this);
        }
        else{
            throw new RuntimeException("Can't end a renderer that isn't drawing!");
        }
    }

    public void flush(){
        if(this.vertexAmount > 0){
            ((Buffer)this.vertices).flip();
            this.program.bind();

            this.vbo.subData(this.vertices);
            this.program.draw(this.vertexAmount);

            ((Buffer)this.vertices).clear();
            this.vertexAmount = 0;

            this.flushCounter++;

            this.program.getProcessor().onFlush(this);
        }
    }

    public void rotate(float angle){
        this.setRotation(this.rotation+angle);
    }

    public void setRotation(float angle){
        this.rotation = angle%360F;

        double rads = Math.toRadians(this.rotation);
        this.sinRot = (float)Math.sin(rads);
        this.cosRot = (float)Math.cos(rads);
    }

    public void setRotationCenter(float x, float y){
        this.rotationCenterX = x;
        this.rotationCenterY = y;
    }

    public float getRotationCenterX(){
        return this.rotationCenterX;
    }

    public float getRotationCenterY(){
        return this.rotationCenterY;
    }

    public void translate(float x, float y){
        this.setTranslation(this.translationX+x, this.translationY+y);
    }

    public void setTranslation(float x, float y){
        this.translationX = x;
        this.translationY = y;
    }

    public void scale(float x, float y){
        this.setScale(this.scaleX*x, this.scaleY*y);
    }

    public void setScale(float x, float y){
        this.scaleX = x;
        this.scaleY = y;
    }

    public void mirror(boolean hor, boolean vert){
        this.setMirrored(hor != this.mirroredHor, vert != this.mirroredVert);
    }

    public void setMirrored(boolean hor, boolean vert){
        this.mirroredHor = hor;
        this.mirroredVert = vert;
    }

    public void resetTransformation(){
        this.setRotation(0F);
        this.setTranslation(0F, 0F);
        this.setScale(1F, 1F);
        this.setMirrored(false, false);
        this.setRotationCenter(0F, 0F);
    }

    public float getRotation(){
        return this.rotation;
    }

    public float getTranslationX(){
        return this.translationX;
    }

    public float getTranslationY(){
        return this.translationY;
    }

    public float getScaleX(){
        return this.scaleX;
    }

    public float getScaleY(){
        return this.scaleY;
    }

    public boolean isMirroredHor(){
        return this.mirroredHor;
    }

    public boolean isMirroredVert(){
        return this.mirroredVert;
    }

    public ShaderProgram getProgram(){
        return this.program;
    }

    public Texture getTexture(){
        return this.texture;
    }

    @Override
    public void dispose(){
        MemoryUtil.memFree(this.vertices);
        this.vbo.dispose();
    }

    public int getFlushes(){
        return this.lastFlushes;
    }

    public void addEmptyRect(float x, float y, float width, float height, int color){
        this.addEmptyRect(x, y, width, height, 1F, color);
    }

    public void addEmptyRect(float x, float y, float width, float height, float lineWidth, int color){
        this.addFilledRect(x, y, width-lineWidth, lineWidth, color);
        this.addFilledRect(x+lineWidth, y+height-lineWidth, width-lineWidth, lineWidth, color);
        this.addFilledRect(x+width-lineWidth, y, lineWidth, height-lineWidth, color);
        this.addFilledRect(x, y+lineWidth, lineWidth, height-lineWidth, color);
    }

    public void addFilledRect(float x, float y, float width, float height, int color){
        this.addTriangle(x, y, x, y+height, x+width, y, color, color, color, 0F, 0F, 0F, 0F, 0F, 0F);
        this.addTriangle(x+width, y, x, y+height, x+width, y+height, color, color, color, 0F, 0F, 0F, 0F, 0F, 0F);
    }

    public void backgroundColor(int color){
        if(this.backgroundColor != color){
            GL11.glClearColor(Colors.getR(color), Colors.getG(color), Colors.getB(color), Colors.getA(color));
            this.backgroundColor = color;
        }
    }

    public FloatBuffer getVertices(){
        return this.vertices;
    }

    public int getVertexAmount(){
        return this.vertexAmount;
    }
}