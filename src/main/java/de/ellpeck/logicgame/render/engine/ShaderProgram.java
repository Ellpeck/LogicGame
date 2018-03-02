package de.ellpeck.logicgame.render.engine;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

import java.util.HashMap;
import java.util.Map;

public class ShaderProgram implements IDisposable{

    private static int boundProgram;
    private static ShaderProgram boundProgramRef;

    private final int id;
    private final VertexArrayObject vao;
    private final Map<String, Integer> attributeLocations = new HashMap<>();
    private final Map<String, Integer> uniformLocations = new HashMap<>();
    private int componentsPerVertex;
    private int attributeOffset;
    private VertexProcessor processor;

    public ShaderProgram(Shader vertex, Shader fragment){
        this.id = GL20.glCreateProgram();
        this.vao = new VertexArrayObject();

        GL20.glAttachShader(this.id, vertex.getId());
        GL20.glAttachShader(this.id, fragment.getId());

        this.setVertexProcessing(8, new VertexProcessor());
    }

    public void setDefaultValues(int width, int height){
        this.bindFragmentDataLocation("fragColor", 0);
        this.link();
        this.bind();

        this.pointVertexAttribute("position", 2);
        this.pointVertexAttribute("color", 4);
        this.pointVertexAttribute("texCoord", 2);

        this.setUniform("model", new Matrix4f());
        this.setUniform("view", new Matrix4f());
        this.setUniform("texImage", 0);

        this.updateProjection(width, height);
    }

    public void updateProjection(int width, int height){
        this.setUniform("projection", new Matrix4f().ortho(0F, width, height, 0F, -1F, 1F));
    }

    public void bindFragmentDataLocation(String name, int location){
        GL30.glBindFragDataLocation(this.id, location, name);
    }

    public void link(){
        GL20.glLinkProgram(this.id);

        if(GL20.glGetProgrami(this.id, GL20.GL_LINK_STATUS) != GL11.GL_TRUE){
            throw new RuntimeException("Couldn't compile shader program:\n"+GL20.glGetProgramInfoLog(this.id));
        }
    }

    public void bind(){
        if(boundProgram != this.id){
            boundProgram = this.id;
            boundProgramRef = this;

            GL20.glUseProgram(this.id);
            this.vao.bind();

            for(int i : this.attributeLocations.values()){
                GL20.glEnableVertexAttribArray(i);
            }
        }
    }

    public int getAttributeLocation(String name){
        this.bind();
        return this.attributeLocations.computeIfAbsent(name, s -> GL20.glGetAttribLocation(this.id, s));
    }

    public int getUniformLocation(String name){
        this.bind();
        return this.uniformLocations.computeIfAbsent(name, s -> GL20.glGetUniformLocation(this.id, s));
    }

    public void pointVertexAttribute(String name, int size){
        int location = this.getAttributeLocation(name);
        GL20.glVertexAttribPointer(location, size, GL11.GL_FLOAT, false, this.componentsPerVertex*Float.BYTES, this.attributeOffset);
        this.attributeOffset += size*Float.BYTES;

        GL20.glEnableVertexAttribArray(location);
    }

    public void setUniform(String name, Matrix4f matrix){
        int location = this.getUniformLocation(name);
        MemoryStack stack = MemoryStack.stackPush();
        GL20.glUniformMatrix4fv(location, false, matrix.get(stack.mallocFloat(4*4)));
        stack.pop();
    }

    public void setUniform(String name, int value){
        GL20.glUniform1i(this.getUniformLocation(name), value);
    }

    public void setUniform(String name, float f){
        GL20.glUniform1f(this.getUniformLocation(name), f);
    }

    public void setUniform(String name, float x, float y){
        GL20.glUniform2f(this.getUniformLocation(name), x, y);
    }

    public void setUniform(String name, float x, float y, float z){
        GL20.glUniform3f(this.getUniformLocation(name), x, y, z);
    }

    public void unbind(){
        if(boundProgram == this.id){
            for(int i : this.attributeLocations.values()){
                GL20.glDisableVertexAttribArray(i);
            }

            this.vao.unbind();
            GL20.glUseProgram(0);

            boundProgram = -1;
            boundProgramRef = null;
        }
    }

    public static void unbindAll(){
        if(boundProgram >= 0){
            boundProgramRef.unbind();
        }
    }

    public int getId(){
        return this.id;
    }

    public void setVertexProcessing(int componentsPerVertex, VertexProcessor processor){
        this.componentsPerVertex = componentsPerVertex;
        this.processor = processor;
    }

    public int getComponentsPerVertex(){
        return this.componentsPerVertex;
    }

    public VertexProcessor getProcessor(){
        return this.processor;
    }

    public void draw(int amount){
        this.vao.draw(amount);
    }

    @Override
    public void dispose(){
        this.unbind();
        GL20.glDeleteProgram(this.id);
    }
}
