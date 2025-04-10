package alex_petmecky.customworldgeneration;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.org.apache.bcel.internal.generic.ACONST_NULL;
import sun.tools.jconsole.JConsole;

//import static alex_petmecky.customworldgen.StaticVariables.MULTI_FILE_PATH;
import static alex_petmecky.customworldgeneration.StaticVariables.MULTI_FILE_PATH;


public class FileLoader {
    public JsonArray elevation_data;
    public CustomWorldGeneration plugin;

    private JsonObject fileStructure;
    private int chunksPerFile;
    private int filesOnXAxis;

    //private boolean[] needToLoad;
    //private boolean[] isLoaded;
    private ArrayList<Boolean> needToLoad; //= new ArrayList<>();
    private ArrayList<Boolean> isLoaded; //= new ArrayList<>();
    HashMap<String,String> savedMappings = new HashMap<>();

    private JsonArray bottomLeft;
    private JsonArray bottomRight;
    private JsonArray topLeft;
    private JsonArray topRight;

    private JsonArray currentChunk;
    //hashmap to track how many chunks are loaded in a single file, if 0 unload file
    // 0_0->1

    private HashMap<String,Integer> currNeeded = new HashMap<>();

    private HashMap<String,JsonArray> loadedChunks = new HashMap<>();
    public FileLoader(CustomWorldGeneration plugin){
        //this.plugin = plugin;

        //String loc = new String("plugins/alex_petmecky.customworldgen/elevation_files/test.json");
        String loc = new String("./plugins/alex_petmecky.customworldgen/test.json");

        //;
        //File file = new File(loc);

        //File file = new File(plugin.getDataFolder().getAbsolutePath()+"/test.json");
        //File file = new File(String.valueOf(this.getClass().getResourceAsStream("test.json")));
        //File file = new File(String.valueOf(plugin.getResource("test.json")));
        File file = new File("./static/test.json");
        try{
            String content = new String(Files.readAllBytes(Paths.get(file.toURI())));
            //JSONObject jsonObject = new JSONObject(content);
            JsonElement jsonElement = JsonParser.parseString(content);
            //System.out.println(jsonArray.get(0));
            //JSONArray image = jsonArray;

            this.elevation_data = jsonElement.getAsJsonArray();
            //JSONArray top_left_chunck = (JSONArray) image.get(0);
            //System.out.println(top_left_chunck);
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public FileLoader(String Name_Of_MultiFile_Structure_file){
        String fname = MULTI_FILE_PATH+Name_Of_MultiFile_Structure_file+".json";
        File file = new File(fname);
        try{
            String content = new String(Files.readAllBytes(Paths.get(file.toURI())));
            //JSONObject jsonObject = new JSONObject(content);
            JsonElement jsonElement = JsonParser.parseString(content);
            this.chunksPerFile = jsonElement.getAsJsonObject().get("FileChunkSize").getAsInt();
            this.fileStructure = jsonElement.getAsJsonObject().get("FileStructure").getAsJsonObject();

            this.filesOnXAxis = jsonElement.getAsJsonObject().get("files_on_x_axis").getAsInt();
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

        //this.needToLoad = new boolean[fileStructure.size()];
        //this.isLoaded = new boolean[fileStructure.size()];

        //Arrays.fill(needToLoad,false);
        //Arrays.fill(isLoaded,false);
        //this.needToLoad.ensureCapacity(fileStructure.size());
        //this.isLoaded.ensureCapacity(fileStructure.size());
        this.needToLoad = new ArrayList<Boolean>(Collections.nCopies(fileStructure.size(), Boolean.FALSE));
        this.isLoaded = new ArrayList<Boolean>(Collections.nCopies(fileStructure.size(), Boolean.FALSE));
        System.out.println("needToLoadSIZE_INIT: "+this.needToLoad.size());
        //Collections.fill(this.needToLoad,Boolean.FALSE);
        //Collections.fill(this.isLoaded,Boolean.FALSE);
    }

    public JsonArray loadChunks(int chunkX, int chunkZ){
        System.out.println("needToLoadSIZE: "+this.needToLoad.size());
        int quadrantX = chunkX / this.chunksPerFile;
        int quadrantZ = chunkZ / this.chunksPerFile;
        System.out.println("QX: "+quadrantX);
        System.out.println("QZ: "+quadrantZ);

        //needs to be tested
        //int index = (quadrantX * fileStructure.size()) + ((fileStructure.size()-1)-quadrantZ);
        //int index = (quadrantX * this.filesOnXAxis) + ((fileStructure.size()-1)-quadrantZ);

        int index = ((fileStructure.size()-1)-quadrantZ) - (quadrantX * this.filesOnXAxis);
        //savedMapping
        System.out.println("INDEX: "+index);
        System.out.println("isLoadedSize: "+this.isLoaded.size());
        if(index > this.isLoaded.size()-1 || index < 0) {
            System.out.println("INDEX LARGER OR SMALLER THAN ISLOADEDSIZE");
            return null;
        }
        String file_index = quadrantX+"_"+quadrantZ;
        System.out.println("FILE_INDEX: "+file_index);

        //int tempX_pos = quadrantX + 1;
        //int tempX_neg = quadrantX - 1;
        //int tempZ_pos = quadrantZ + 1;
        //int tempZ_neg = quadrantZ - 1;

        int[] tempVals = new int[4];
        tempVals[0] = quadrantX + 1;
        tempVals[1] = quadrantX - 1;
        tempVals[2] = quadrantZ + 1;
        tempVals[3] = quadrantZ - 1;
        /*
        for(int i = 0; i < 2; i++){
            for(int j = 0; j < 2; j++){

                int deleteIndex = (tempVals[i] * this.filesOnXAxis) + ((fileStructure.size()-1)-tempVals[j+2]);
                if(deleteIndex<0 || deleteIndex >= this.isLoaded.size()-1){
                    //return null;
                    break;
                }
                if(this.isLoaded.get(deleteIndex)==true){
                    this.isLoaded.set(deleteIndex,false);
                    String deleteQuadFile = i+"_"+j;
                    this.unloadFile(deleteQuadFile);
                }
            }
            //int newIndex = (quadrantX * this.filesOnXAxis) + ((fileStructure.size()-1)-quadrantZ);
        }
        */


        if(this.isLoaded.get(index) == true){
            return this.loadedChunks.get(file_index);
        }else{
            String fileName = String.valueOf(fileStructure.get(file_index));
            JsonArray currentImg = this.loadFile(fileName,file_index);
            this.isLoaded.set(index,true);
            return currentImg;
        }

        /*
        * file_position = savedMappings.get(String.valueOf(i));
                if(this.isLoaded.get(i)){
                    //unload the file
                    loadedChunks.remove(file_position);

                }else{
                    //load the file
                    String fileName = String.valueOf(fileStructure.get(file_position));
                    //loadedChunks.put(file_position,)
                    this.loadFile(fileName,file_position);
        * */

    }

    public void LoadMultipleChunks(int chunkX, int chunkZ){
        System.out.println("needToLoadSIZE: "+this.needToLoad.size());
        int quadrantX = chunkX / this.chunksPerFile;
        int quadrantZ = chunkZ / this.chunksPerFile;

        //needs to be tested
        //int index = (quadrantX * fileStructure.size()) + ((fileStructure.size()-1)-quadrantZ);
        int index = (quadrantX * this.filesOnXAxis) + ((fileStructure.size()-1)-quadrantZ);
        //savedMapping

        String file_index = quadrantX+"_"+quadrantZ;
        if(this.isLoaded.get(index) == true){
            //return this.loadedChunks.get(file_index);
        }else{

        }

        for(int i=0; i<this.needToLoad.size();i++){
            boolean actionNeeded = (this.needToLoad.get(i) ^ this.isLoaded.get(i));
            // & (!this.isLoaded[i])
            String file_position;
            if(actionNeeded){
                file_position = savedMappings.get(String.valueOf(i));
                if(this.isLoaded.get(i)){
                    //unload the file
                    loadedChunks.remove(file_position);

                }else{
                    //load the file
                    String fileName = String.valueOf(fileStructure.get(file_position));
                    //loadedChunks.put(file_position,)
                    this.loadFile(fileName,file_position);
                    this.isLoaded.set(i,Boolean.TRUE);
                }

            }
        }





        System.out.println("INDEX_:"+index);
        int scaledX = chunkX - (this.chunksPerFile*quadrantX);
        int scaledZ = chunkX - (this.chunksPerFile*quadrantZ);

        boolean loadedLeft = false;
        boolean loadedRight = false;
        boolean loadedTop = false;
        boolean loadedBottom = false;

        int loadX = 0;
        int loadZ = 0;
        // if 2 are loaded, load the corner file connecting them

        int newX = quadrantX;
        int newZ = quadrantZ;

        //int index = (quadrantX * fileStructure.size()) + ((fileStructure.size()-1)-quadrantZ);
        if (this.chunksPerFile - scaledX < 20){
            //load right file
            System.out.println("Load RIGHT FILE");
            newX = quadrantX + 1;
            loadX = 1;

            //index = (newX * fileStructure.size()) + ((fileStructure.size()-1)-quadrantZ);
            index = (newX * this.filesOnXAxis) + ((fileStructure.size()-1)-quadrantZ);
            System.out.println("INDEX_:"+index);
            /*
            if(index <= needToLoad.length-1){
                loadedRight = true;
                this.needToLoad[index] = true;

            }
            */
            if(index <= needToLoad.size()-1){
                System.out.println("Loading Right at Index: "+index);
                loadedRight = true;
                //this.needToLoad[index] = true;
                this.needToLoad.set(index,Boolean.TRUE);
            }


            //this.needToLoad[index] = true;



        }else if (scaledX < 20){
            //load left file
            System.out.println("Load left file");
            loadX = -1;

            newX = quadrantX - 1;
            /*
            if(newX >= 0){
                loadedLeft = true;
                //index = (newX * fileStructure.size()) + ((fileStructure.size()-1)-quadrantZ);
                index = (newX * this.filesOnXAxis) + ((fileStructure.size()-1)-quadrantZ);
                System.out.println("INDEX_:"+index);
                this.needToLoad[index] = true;
            }
            */
            System.out.println("NEW_X_: "+newX);
            if(newX >= 0){
                loadedLeft = true;
                //index = (newX * fileStructure.size()) + ((fileStructure.size()-1)-quadrantZ);
                index = (newX * this.filesOnXAxis) + ((fileStructure.size()-1)-quadrantZ);
                //System.out.println("INDEX_:"+index);
                System.out.println("Loading Left at Index: "+index);
                this.needToLoad.set(index,Boolean.TRUE);
            }
        }

        if(this.chunksPerFile - scaledZ <20){
            //load top file

            newZ = quadrantZ + 1;
            //index = (quadrantX * fileStructure.size()) + ((fileStructure.size()-1)-newZ);
            index = (quadrantX * this.filesOnXAxis) + ((fileStructure.size()-1)-newZ);
            System.out.println("INDEX TO LOAD TOP FILE: "+index);
            //this.needToLoad[index] = true;
            this.needToLoad.set(index,Boolean.TRUE);

            if(loadedLeft || loadedRight){
                //this.needToLoad[];
                System.out.println("Load Top Corner File");
                //index = (newX * fileStructure.size()) + ((fileStructure.size()-1)-newZ);
                index = (newX * this.filesOnXAxis) + ((fileStructure.size()-1)-newZ);
                System.out.println("INDEX_:"+index);

                /*
                if(index <= needToLoad.length-1){
                    this.needToLoad[index] = true;
                }
                */
                if(index <= needToLoad.size()-1){
                    this.needToLoad.set(index,Boolean.TRUE);
                }



            }

        }else if(scaledZ < 20){
            // load bottom file
            System.out.println("Load Bottom File");

            newZ = quadrantZ - 1;
            loadZ = -1;

            if(newZ >=0){
                //index = (quadrantX * fileStructure.size()) + ((fileStructure.size()-1)-newZ);
                index = (quadrantX * this.filesOnXAxis) + ((fileStructure.size()-1)-newZ);
                //this.needToLoad[index] = true;
                this.needToLoad.set(index,Boolean.TRUE);

                if(loadedLeft || loadedRight){
                    System.out.println("Load Bottom Corner File");
                    //this.needToLoad[];
                    //index = (newX * fileStructure.size()) + ((fileStructure.size()-1)-newZ);
                    index = (newX * this.filesOnXAxis) + ((fileStructure.size()-1)-newZ);
                    //this.needToLoad[index] = true;
                    this.needToLoad.set(index,Boolean.TRUE);


                }
            }



        }



        //boolean[] needToLoad =new boolean[fileStructure.size()];
        //Arrays.fill(needToLoad,false);
        /*
        for(int i=0; i<this.needToLoad.length;i++){
            System.out.print(this.needToLoad[i]);
            System.out.print(this.isLoaded[i]);

        }
        */
        for(int i=0; i<this.needToLoad.size();i++){
            System.out.print(this.needToLoad.get(i));
            System.out.print(this.isLoaded.get(i));

        }

        /*
        for(int i=0; i<this.needToLoad.length;i++){
            boolean actionNeeded = (this.needToLoad[i] ^ this.isLoaded[i]);
            // & (!this.isLoaded[i])
            String file_position;
            if(actionNeeded){
                file_position = savedMappings.get(String.valueOf(i));
                if(this.isLoaded[i]){
                    //unload the file
                    loadedChunks.remove(file_position);

                }else{
                    //load the file
                    String fileName = String.valueOf(fileStructure.get(file_position));
                    //loadedChunks.put(file_position,)
                    this.loadFile(fileName,file_position);
                    this.isLoaded[i] = true;
                }

            }
        }
        */

        for(int i=0; i<this.needToLoad.size();i++){
            boolean actionNeeded = (this.needToLoad.get(i) ^ this.isLoaded.get(i));
            // & (!this.isLoaded[i])
            String file_position;
            if(actionNeeded){
                file_position = savedMappings.get(String.valueOf(i));
                if(this.isLoaded.get(i)){
                    //unload the file
                    loadedChunks.remove(file_position);

                }else{
                    //load the file
                    String fileName = String.valueOf(fileStructure.get(file_position));
                    //loadedChunks.put(file_position,)
                    this.loadFile(fileName,file_position);
                    this.isLoaded.set(i,Boolean.TRUE);
                }

            }
        }






        /*
        if(this.chunksPerFile - scaledZ <20){
            // load top file
            loadedTop = true;
            newZ = quadrantZ + 1;
            loadZ = 1;


            if(loadedRight || loadedLeft){

                String fileStructKey = String.valueOf(newX)+"_"+String.valueOf(newZ);
                String fileName = fileStructure.get(fileStructKey).getAsString();
                this.loadFile(fileName,fileStructKey);
            }
            //may only need 1?
            //if(loadedLeft){
            //    String fileStructKey = String.valueOf(newX)+"_"+String.valueOf(newZ);
            //    String fileName = fileStructure.get(fileStructKey).getAsString();
            //    this.loadFile(fileName,fileStructKey);
            //}
        }

        if(scaledZ < 20){
            // load bottom file
            loadedBottom = true;
            newZ = quadrantZ - 1;
            loadZ = -1;
        }

        if(loadX != 0 && loadZ  != 0){
            //load a corner chunk
            String fileName;
            String corner_Key = String.valueOf(newX)+"_"+String.valueOf(newZ);
            fileName = fileStructure.get(corner_Key).getAsString();
            this.loadFile(fileName,corner_Key);



            String north_south_Key = String.valueOf(chunkX)+"_"+String.valueOf(newZ);
            fileName = fileStructure.get(north_south_Key).getAsString();
            this.loadFile(fileName,north_south_Key);

            String west_east_key = String.valueOf(newX)+"_"+String.valueOf(chunkZ);
            fileName = fileStructure.get(west_east_key).getAsString();
            this.loadFile(fileName,west_east_key);


        }else if(loadX != 0 || loadZ !=0){
            String cornerKey = String.valueOf(newX)+"_"+String.valueOf(newZ);
            String fileName = fileStructure.get(cornerKey).getAsString();
            this.loadFile(fileName,cornerKey);
        }


        String fileStructKey = String.valueOf(newX)+"_"+String.valueOf(newZ);
        String fileName = fileStructure.get(fileStructKey).getAsString();
        this.loadFile(fileName,fileStructKey);
        */

    }

    private JsonArray loadFile(String fileName,String fileCoordinateKey ){
        fileName = fileName.replace("\"","");
        String fname = MULTI_FILE_PATH+fileName;
        File file = new File(fname);
        try{
            String content = new String(Files.readAllBytes(Paths.get(file.toURI())));
            //JSONObject jsonObject = new JSONObject(content);
            JsonElement jsonElement = JsonParser.parseString(content);

            JsonArray currentLoad =  jsonElement.getAsJsonArray();
            //String key = String.valueOf(quadrantX)+"_"+String.valueOf(quadrantZ);

            this.loadedChunks.put(fileCoordinateKey,currentLoad);
            return currentLoad;

        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    private void unloadFile(String fileCoordinateKey){
        if(this.loadedChunks.containsKey(fileCoordinateKey)){
            this.loadedChunks.remove(fileCoordinateKey);//this removes it in place, but it does return the removed value
        }
    }

    public JsonArray getImg(int chunkX,int chunkZ){
        int quadrantX = chunkX / this.chunksPerFile;
        int quadrantZ = chunkZ / this.chunksPerFile;

        //needs to be tested
        //int index = (quadrantX * fileStructure.size()) + ((fileStructure.size()-1)-quadrantZ);

        int scaledX = chunkX - (this.chunksPerFile*quadrantX);
        int scaledZ = chunkX - (this.chunksPerFile*quadrantZ);

        String key = quadrantX+"_"+quadrantZ;
        System.out.println(this.loadedChunks);
        JsonArray img = this.loadedChunks.get(key);
        return img;


    }

    public int getChunksPerFile() {
        return chunksPerFile;
    }
    public int getFilesOnXAxis(){
        return filesOnXAxis;
    }
}
