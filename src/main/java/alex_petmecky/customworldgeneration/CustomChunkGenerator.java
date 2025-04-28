package alex_petmecky.customworldgeneration;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.io.Console;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

//https://www.spigotmc.org/threads/how-to-create-a-custom-world-generator.545616/
//https://gis.stackexchange.com/questions/230237/looking-for-the-simplest-way-to-get-elevation-for-a-give-wgs84-coordinate
public class CustomChunkGenerator extends ChunkGenerator {

    //public final FastNoiseLite terrainNoise = new FastNoiseLite();
    //public final FastNoiseLite detailNoise = new FastNoiseLite();
    public CustomWorldGeneration plugin;
    public FileLoader fileLoader;
    public int cpf;



    public CustomChunkGenerator(CustomWorldGeneration plugin){
        this.plugin = plugin;
        //fileLoader = new FileLoader(this.plugin);
        fileLoader = new FileLoader("FileStructure");
        this.cpf = fileLoader.getChunksPerFile();
    }
    @Override
    public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData){


        //System.out.println("Starting Load For X: "+chunkX+" Z: "+chunkZ);
        if(chunkX < 0 || chunkZ < 0){
            //to prevent indexing less than 0 if the chunk is less than 0
            //System.out.println("Chunk Exited For X: "+chunkX+" Z: "+chunkZ);
            return;
        }


        //System.out.println("ChunkX: "+String.valueOf(chunkX)+" ChunkZ: "+String.valueOf(chunkZ));

        JsonArray image = fileLoader.loadChunks(chunkX,chunkZ);

        JsonArray materialData = fileLoader.getMaterialImage(chunkX,chunkZ);
        if (materialData == null){

        }
        if(image == null){
            System.out.println("NULL Chunk Exited For X: "+chunkX+" Z: "+chunkZ);
            return;
        }

        //load the whole image








        int scaledZ = chunkZ;
        if(chunkZ >= this.cpf){
            //int quadrantZ = chunkZ / this.chunksPerFile;
            scaledZ = chunkZ - this.cpf * (chunkZ / this.cpf);
        }
        int scaledX = chunkX;
        if(chunkX >= this.cpf){
            //int quadrantZ = chunkZ / this.chunksPerFile;
            scaledX = chunkX - this.cpf * (chunkX / this.cpf);
        }






        if(image.size() -(1+scaledZ) < 0){
            //make sure we are not entering negative indecies
            //System.out.println("Chunk? Exited For X: "+chunkX+" Z: "+scaledZ);
            return;
        }
        JsonArray z_chunk_data = (JsonArray) image.get(image.size() - (1+scaledZ));
        JsonArray z_material_chunk = null;
        if(materialData != null){
            z_material_chunk = (JsonArray) materialData.get(image.size() - (1+scaledZ));

        }


        if(scaledX > z_chunk_data.size()-1){
            //System.out.println("Chunk@ Exited For X: "+chunkX+" Z: "+chunkZ);
            return;
        }
        JsonArray final_chunk_data = (JsonArray) z_chunk_data.get(scaledX);
        JsonArray final_material_data = null;
        if(materialData !=null){
            final_material_data = (JsonArray) z_material_chunk.get(scaledX);
        }



        //System.out.println("FINAL CHUNK DATA FOR: "+"X: "+chunkX+" Z: "+chunkZ);

        for (int z = 15; z >= 0; z--){
            for (int x = 0; x < 16; x++){

                int max_height = (int) final_chunk_data.get((15-z)).getAsJsonArray().get(x).getAsInt();
                String final_material = null;

                if(materialData != null){
                    final_material = (String) final_material_data.get((15-z)).getAsJsonArray().get(x).getAsString();

                }

                boolean flag = false;
                if (max_height == -32768){
                    max_height = 0;
                    flag = true;
                    //Material setblock = Material.WATER;
                }

                Material f_mat=null;
                if (materialData !=null){
                    f_mat = Material.getMaterial(final_material);
                }

                //switch ()
                if (f_mat == null){
                    f_mat = Material.WHITE_WOOL;
                }

                for (int y = chunkData.getMinHeight(); y <= max_height; y++){
                    if(y == chunkData.getMinHeight()){
                        chunkData.setBlock(x, y, z,Material.BEDROCK);
                    }else{
                        if(flag){
                            chunkData.setBlock(x, y, z, f_mat);
                        }else if (max_height < -30) {
                            chunkData.setBlock(x,y,z,f_mat);
                        }else {
                            chunkData.setBlock(x, y, z, f_mat);
                        }

                    }



                }
            }
        }
        //System.out.println("Chunk Loaded For X: "+chunkX+" Z: "+chunkZ);

        String key = scaledX+"_"+scaledZ;
        if(fileLoader.remainingChunks.containsKey(key)){
            int remaining = (fileLoader.remainingChunks.get(key)) - 1;
            if (remaining <= 0){
                fileLoader.unloadFile(key);
            }
        }


    }


}
