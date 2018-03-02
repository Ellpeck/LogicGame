package de.ellpeck.logicgame;

public final class Main{

    private static final int INTERVAL = 1000/32;
    public static int width = 1280;
    public static int height = 720;
    public static int vertexCache = 65536;
    public static int fpsAverage;
    public static int tpsAverage;
    public static Game game;

    public static void main(String[] args){
        game = new Game();

        try{
            game.init();

            long lastPollTime = 0;
            int tpsAccumulator = 0;
            int fpsAccumulator = 0;

            long lastDeltaTime = System.currentTimeMillis();
            int deltaAccumulator = 0;

            while(game.isRunning){
                long time = System.currentTimeMillis();

                int delta = (int)(time-lastDeltaTime);
                lastDeltaTime = time;

                deltaAccumulator += delta;
                if(deltaAccumulator >= INTERVAL){
                    long updates = deltaAccumulator/INTERVAL;
                    for(int i = 0; i < updates; i++){
                        game.updateTicked();
                        tpsAccumulator++;

                        deltaAccumulator -= INTERVAL;
                    }
                }

                game.updateTickless();
                fpsAccumulator++;

                if(time-lastPollTime >= 1000){
                    tpsAverage = tpsAccumulator;
                    fpsAverage = fpsAccumulator;

                    tpsAccumulator = 0;
                    fpsAccumulator = 0;

                    lastPollTime = time;
                }

                try{
                    Thread.sleep(1);
                }
                catch(InterruptedException ignored){
                }
            }
        }
        finally{
            game.shutdown();
        }
    }
}
