package alex_petmecky.customworldgeneration;

//3js for obj loading

//usd files


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
import static alex_petmecky.customworldgeneration.StaticVariables.MATERIAL_FILES;
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
    public HashMap<String,Integer> remainingChunks = new HashMap<>();

    private HashMap<String,JsonArray> loadedMaterials = new HashMap<>();

    public FileLoader(CustomWorldGeneration plugin){

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


        this.needToLoad = new ArrayList<Boolean>(Collections.nCopies(fileStructure.size(), Boolean.FALSE));
        this.isLoaded = new ArrayList<Boolean>(Collections.nCopies(fileStructure.size(), Boolean.FALSE));
//        System.out.println("needToLoadSIZE_INIT: "+this.needToLoad.size());

    }

    public JsonArray loadChunks(int chunkX, int chunkZ){
        //System.out.println("needToLoadSIZE: "+this.needToLoad.size());
        int quadrantX = chunkX / this.chunksPerFile;
        int quadrantZ = chunkZ / this.chunksPerFile;
        //System.out.println("QX: "+quadrantX);
        //System.out.println("QZ: "+quadrantZ);

        int index = ((fileStructure.size()-1)-quadrantZ) - (quadrantX * this.filesOnXAxis);
        //savedMapping
        //System.out.println("INDEX: "+index);
        //System.out.println("isLoadedSize: "+this.isLoaded.size());
        if(index > this.isLoaded.size()-1 || index < 0) {
            //System.out.println("INDEX LARGER OR SMALLER THAN ISLOADEDSIZE");
            return null;
        }
        String file_index = quadrantX+"_"+quadrantZ;
       // System.out.println("FILE_INDEX: "+file_index);


        int[] tempVals = new int[4];
        tempVals[0] = quadrantX + 1;
        tempVals[1] = quadrantX - 1;
        tempVals[2] = quadrantZ + 1;
        tempVals[3] = quadrantZ - 1;


        if(this.isLoaded.get(index) == true){
            return this.loadedChunks.get(file_index);
        }else{
            String fileName = String.valueOf(fileStructure.get(file_index));
            JsonArray currentImg = this.loadFile(fileName,file_index);

            //to check for null
            Integer material_load_status = this.loadMaterialFile(fileName,file_index);//loads the material file
            if(material_load_status != null){
                this.isLoaded.set(index,true);
            }


            return currentImg;
        }



    }
    /**
     * THIS MUST BE CALLED AFTER LOADCHUNKS
     * IT IS NOT SAFE AND ASSUMES THE MATERIAL FILES HAVE ALREADY BEEN LOADED
     * @return the material image.
     */
    public JsonArray getMaterialImage(int chunkX, int chunkZ){
        //
        int quadrantX = chunkX / this.chunksPerFile;
        int quadrantZ = chunkZ / this.chunksPerFile;
        String file_index = quadrantX+"_"+quadrantZ;
        return this.loadedMaterials.get(file_index);
    }

    public void LoadMultipleChunks(int chunkX, int chunkZ){
        //System.out.println("needToLoadSIZE: "+this.needToLoad.size());
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





        //System.out.println("INDEX_:"+index);
        int scaledX = chunkX - (this.chunksPerFile*quadrantX);
        int scaledZ = chunkX - (this.chunksPerFile*quadrantZ);

        boolean loadedLeft = false;
        boolean loadedRight = false;
        boolean loadedTop = false;
        boolean loadedBottom = false;


        // if 2 are loaded, load the corner file connecting them

        int newX = quadrantX;
        int newZ = quadrantZ;

        //int index = (quadrantX * fileStructure.size()) + ((fileStructure.size()-1)-quadrantZ);
        if (this.chunksPerFile - scaledX < 20){
            //load right file
            //System.out.println("Load RIGHT FILE");
            newX = quadrantX + 1;


            index = (newX * this.filesOnXAxis) + ((fileStructure.size()-1)-quadrantZ);


            if(index <= needToLoad.size()-1){

                loadedRight = true;
                //this.needToLoad[index] = true;
                this.needToLoad.set(index,Boolean.TRUE);
            }


            //this.needToLoad[index] = true;



        }else if (scaledX < 20){
            //load left file
            System.out.println("Load left file");


            newX = quadrantX - 1;

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
            index = (quadrantX * this.filesOnXAxis) + ((fileStructure.size()-1)-newZ);
            //System.out.println("INDEX TO LOAD TOP FILE: "+index);
            this.needToLoad.set(index,Boolean.TRUE);

            if(loadedLeft || loadedRight){

                //System.out.println("Load Top Corner File");
                index = (newX * this.filesOnXAxis) + ((fileStructure.size()-1)-newZ);
                //System.out.println("INDEX_:"+index);


                if(index <= needToLoad.size()-1){
                    this.needToLoad.set(index,Boolean.TRUE);
                }



            }

        }else if(scaledZ < 20){
            // load bottom file
            //System.out.println("Load Bottom File");

            newZ = quadrantZ - 1;


            if(newZ >=0){
                index = (quadrantX * this.filesOnXAxis) + ((fileStructure.size()-1)-newZ);
                this.needToLoad.set(index,Boolean.TRUE);

                if(loadedLeft || loadedRight){
                    //System.out.println("Load Bottom Corner File");

                    index = (newX * this.filesOnXAxis) + ((fileStructure.size()-1)-newZ);
                    //this.needToLoad[index] = true;
                    this.needToLoad.set(index,Boolean.TRUE);


                }
            }



        }



/*
        for(int i=0; i<this.needToLoad.size();i++){
            System.out.print(this.needToLoad.get(i));
            System.out.print(this.isLoaded.get(i));

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

                    loadedMaterials.remove(file_position);//may need to remove

                }else{
                    //load the file
                    String fileName = String.valueOf(fileStructure.get(file_position));
                    //loadedChunks.put(file_position,)
                    this.loadFile(fileName,file_position);

                    this.loadMaterialFile(fileName,file_position);//may need to remove

                    this.isLoaded.set(i,Boolean.TRUE);

                }

            }
        }







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
            int totalChunks = this.chunksPerFile * this.chunksPerFile;
            this.remainingChunks.put(fileCoordinateKey,totalChunks);
            return currentLoad;

        } catch (IOException e) {
            throw new RuntimeException(e);
            //return null;

        }
    }

    //need this to be nullable
    private Integer loadMaterialFile(String fileName,String fileCoordinateKey){
        fileName = fileName.replace("\"","");
        String fname = MATERIAL_FILES+fileName;
        File file = new File(fname);
        try{
            String content = new String(Files.readAllBytes(Paths.get(file.toURI())));
            JsonElement jsonElement = JsonParser.parseString(content);

            JsonArray currentLoad =  jsonElement.getAsJsonArray();

            this.loadedMaterials.put(fileCoordinateKey,currentLoad);

            return 1;

        } catch (IOException e) {
            return null;
            //throw new RuntimeException(e);

        }
    }

    public void unloadFile(String fileCoordinateKey){
        //System.out.println("CHUNK UNLOADED FOR: "+fileCoordinateKey);
        if(this.loadedChunks.containsKey(fileCoordinateKey)){
            this.loadedChunks.remove(fileCoordinateKey);//this removes it in place, but it does return the removed value

            this.loadedMaterials.remove(fileCoordinateKey);//may need to be removed
        }
    }

    public JsonArray getImg(int chunkX,int chunkZ){
        int quadrantX = chunkX / this.chunksPerFile;
        int quadrantZ = chunkZ / this.chunksPerFile;


        //int scaledX = chunkX - (this.chunksPerFile*quadrantX);
        //int scaledZ = chunkX - (this.chunksPerFile*quadrantZ);

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
