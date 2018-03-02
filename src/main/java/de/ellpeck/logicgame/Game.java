package de.ellpeck.logicgame;

import de.ellpeck.logicgame.level.Level;
import de.ellpeck.logicgame.render.AssetLoader;
import de.ellpeck.logicgame.render.Renderer;
import de.ellpeck.logicgame.render.engine.ShaderProgram;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;

public class Game{

    private final GLFWErrorCallback callback = new GLFWErrorCallback(){
        @Override
        public void invoke(int error, long description){
            new RuntimeException("GLFW Error: "+GLFWErrorCallback.getDescription(description)).printStackTrace();
        }
    };
    private long windowId;
    public boolean isRunning = true;
    private int width;
    private int height;

    public Renderer renderer;
    public ShaderProgram program;

    public Level currentLevel;

    public void init(){
        GLFW.glfwSetErrorCallback(this.callback);

        if(!GLFW.glfwInit()){
            throw new IllegalStateException("Unable to inialize GLFW");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);

        this.windowId = GLFW.glfwCreateWindow(Main.width, Main.height, "Logic Game", MemoryUtil.NULL, MemoryUtil.NULL);
        if(this.windowId == MemoryUtil.NULL){
            GLFW.glfwTerminate();
            throw new RuntimeException("Unable to create window");
        }

        GLFWVidMode mode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        GLFW.glfwSetWindowPos(this.windowId, mode.width()/2-Main.width/2, mode.height()/2-Main.height/2);

        GLFW.glfwMakeContextCurrent(this.windowId);
        GL.createCapabilities();
        this.getWindowSize();

        /*try{
            String[] icons = new String[]{"16x16.png", "32x32.png", "128x128.png"};
            GLFWImage.Buffer imageBuffer = GLFWImage.malloc(icons.length);

            for(int i = 0; i < icons.length; i++){
                Texture texture = new Texture(AssetManager.getResourceAsStream("assets/rockbottom/tex/icon/"+icons[i]));
                imageBuffer.position(i).width(texture.getTextureWidth()).height(texture.getTextureHeight()).pixels(texture.getPixelData());
            }

            imageBuffer.position(0);
            GLFW.glfwSetWindowIcon(this.windowId, imageBuffer);
            imageBuffer.free();
        }
        catch(Exception e){
            RockBottomAPI.logger().log(Level.WARNING, "Couldn't set game icon", e);
        }*/

        GLFW.glfwShowWindow(this.windowId);
        GLFW.glfwPollEvents();

        this.renderer = new Renderer();

        this.program = AssetLoader.loadShaderProgram("/shaders/default.vert", "/shaders/default.frag");
        this.program.setDefaultValues(this.width, this.height);
        this.renderer.setDefaultProgram(this.program);

        /*try{
            ITexture tex = new Texture(AssetManager.getResourceAsStream("/assets/rockbottom/tex/intro/loading.png"));
            tex.draw(0, 0, this.width, this.height);
            GLFW.glfwSwapBuffers(this.windowId);
        }
        catch(Exception e){
            RockBottomAPI.logger().log(Level.WARNING, "Couldn't render loading screen image", e);
        }*/

        GLFW.glfwSetWindowSizeCallback(this.windowId, new GLFWWindowSizeCallback(){
            @Override
            public void invoke(long window, int width, int height){
                Game.this.onResize();
            }
        });

        Registry.init();

        this.currentLevel = Level.parse(AssetLoader.loadJson("/levels/level_1.json"));
    }

    public void updateTickless(){
        if(GLFW.glfwWindowShouldClose(this.windowId)){
            this.isRunning = false;
        }
        else{
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            this.renderer.begin();

            if(this.currentLevel != null){
                this.currentLevel.renderer.render();
            }

            this.renderer.end();

            GLFW.glfwSwapBuffers(this.windowId);
            GLFW.glfwPollEvents();
        }
    }

    public void updateTicked(){
        if(this.currentLevel != null){
            this.currentLevel.update();
        }
    }

    protected void getWindowSize(){
        MemoryStack stack = MemoryStack.stackPush();
        IntBuffer width = stack.mallocInt(1);
        IntBuffer height = stack.mallocInt(1);

        GLFW.glfwGetFramebufferSize(this.windowId, width, height);

        this.width = width.get();
        this.height = height.get();
        stack.pop();
    }

    protected void onResize(){
        this.getWindowSize();
        GL11.glViewport(0, 0, this.width, this.height);
        this.program.updateProjection(this.width, this.height);
    }

    public void shutdown(){
        AssetLoader.dispose();

        if(this.windowId != MemoryUtil.NULL){
            GLFW.glfwDestroyWindow(this.windowId);
            Callbacks.glfwFreeCallbacks(this.windowId);
        }

        GLFW.glfwTerminate();
        this.callback.free();
    }

}
