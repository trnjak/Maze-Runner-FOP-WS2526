package Maze.runner.game;


import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

import java.io.*;
import java.util.ArrayList;

/**  i m going to use this class to read whatever map i can get as a file
    the idea is acutally basic each line in the file will be written like so
    x,y=z where x and y are the coordinates in the Map and the z refers to which type
    exactly are we addressing ,it can be an enemy , a player ,a wall ,or a trap
 **/
public abstract class LoadingMap {
    /**
    discuss the fact of creating an array here or a list because
    we are going later to make some walls spone automatically and
    I think a linked list would be better in terms of speed of
    execution of the statement
     **/
    private static AssetManager assetManager;
    public ArrayList<Gameclass> load(String GameFile) throws IOException {
        /** i took a look at the Chat here and it proposed this code to me
        to be able to read the file at first and get all exception but i won t
        use it i ll stick with what we studied i ll place the chatcode here for everyone
        so that everyone can taake a look at it and maybe learn something new

        Properties props = new Properties(); try { props.load(Gdx.files.internal(filePath).reader());
        }
         catch (Exception e) { throw new RuntimeException("Failed to load map: " + filePath, e); }
         **/


        ArrayList<Gameclass> objects = new ArrayList<Gameclass>();
        /** in this list i m going to store all of the elements that
         * are written in the file
         */


        /** I created here just a temporal file so that i can try the code and avoid
        Exceptions while working
         **/

        /** later we can specify the i-file path when it s already ready
         * i didnt have enough time i could ve had implemented one by hand
         **/
        BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(GameFile)));
        writer.write("3,4=1"+"\n"
        +"3,1=2"+"\n"+"8,9=3");
        writer.close();




        try( BufferedReader reader = new BufferedReader(new FileReader(GameFile))) {
            String line;
            while((line=reader.readLine())!=null){
                String[] componenets = line.split("=");
                String position = componenets[0];
                String type = componenets[1];

                String[] coordinate = position.split(",");
                int x = Integer.parseInt(coordinate[0]);
                int y = Integer.parseInt(coordinate[1]);
                int typeID = Integer.parseInt(type);

                Gameclass obj = determine(typeID, x, y);
                if (obj != null) {
                    objects.add(obj);

                }
                reader.close();
            }

                /**now through what I have just wrote i m able
                 * to read each line of the lines in the file
                 * containig the map and i m able to identify
                 * for each line what does that actual line refer to
                 * in terms of object like enemy or wall or ;;;
                 * and where exactly it is located
                 **/


            }catch(IOException e) {
                e.printStackTrace();
            }
            return objects;


        }

        public static Gameclass determine(int typeId,int x,int y){
              return   switch (typeId){
                  case 0->{
                      Texture WallTexture = assetManager.get("basictiles.png",Texture.class);
                      yield new Wall(x,y,WallTexture);

                  }
                  case 1->{
                      Texture EnemyTexture = assetManager.get("mobs.png",Texture.class);

                      yield new Enemy(x,y,EnemyTexture);
                  }
                  case 2->{
                      Texture PlayerTexture = assetManager.get("character.png",Texture.class);
                      yield new Player(x,y,PlayerTexture);
                  }
                  case 3->{
                      Texture TrapTexture = assetManager.get("things.png",Texture.class);
                      yield new Trap(x,y,TrapTexture);
                  }

                  default -> null;


            };
            /** I did those classes for instance but i can add the others as
             * soon as I finish these here
             */
        }




    }






