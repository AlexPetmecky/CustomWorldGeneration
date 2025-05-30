package alex_petmecky.customworldgeneration;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.io.Console;
import java.util.Random;
import java.util.logging.Level;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
//import org.json.*;
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
        //terrainNoise.SetFrequency(0.001f);
        //detailNoise.SetFrequency(0.05f);
        System.out.println("Starting Load For X: "+chunkX+" Z: "+chunkZ);
        if(chunkX < 0 || chunkZ < 0){
            //to prevent indexing less than 0 if the chunk is less than 0
            System.out.println("Chunk Exited For X: "+chunkX+" Z: "+chunkZ);
            return;
        }


        System.out.println("ChunkX: "+String.valueOf(chunkX)+" ChunkZ: "+String.valueOf(chunkZ));
        //fileLoader.LoadMultipleChunks(chunkX,chunkZ);

        //JsonArray image = fileLoader.getImg(chunkX,chunkZ);
        JsonArray image = fileLoader.loadChunks(chunkX,chunkZ);

        if(image == null){
            System.out.println("NULL Chunk Exited For X: "+chunkX+" Z: "+chunkZ);
            return;
        }




        //JsonArray image = fileLoader.elevation_data;



        //if(image.size() -(1+chunkX) < 0){
        //make sure we are not entering negative indecies
        //    return;
        //}

        //JSONArray x_chunk_data = (JSONArray) image.get(image.length() -(1+chunkX));
        //JSONArray final_chunk_data =  (JSONArray) x_chunk_data.get(chunkZ);
        /*
        JsonArray x_chunk_data = (JsonArray) image.get(image.size() -(1+chunkX));
        if(chunkZ > x_chunk_data.size()-1){
            return;
        }*/
        // JsonArray final_chunk_data =  (JsonArray) x_chunk_data.get(chunkZ);//err


        //TEST THE LINES BELOW
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
            System.out.println("Chunk? Exited For X: "+chunkX+" Z: "+scaledZ);
            return;
        }
        JsonArray z_chunk_data = (JsonArray) image.get(image.size() - (1+scaledZ));

        if(scaledX > z_chunk_data.size()-1){
            System.out.println("Chunk@ Exited For X: "+chunkX+" Z: "+chunkZ);
            return;
        }
        JsonArray final_chunk_data = (JsonArray) z_chunk_data.get(scaledX);
        System.out.println("FINAL CHUNK DATA FOR: "+"X: "+chunkX+" Z: "+chunkZ);
        //System.out.println(final_chunk_data);



        /*
        if(image.size() -(1+chunkZ) < 0){
            //make sure we are not entering negative indecies
            System.out.println("Chunk? Exited For X: "+chunkX+" Z: "+chunkZ);
            return;
        }
        JsonArray z_chunk_data = (JsonArray) image.get(image.size() - (1+chunkZ));

        if(chunkX > z_chunk_data.size()-1){
            System.out.println("Chunk@ Exited For X: "+chunkX+" Z: "+chunkZ);
            return;
        }
        JsonArray final_chunk_data = (JsonArray) z_chunk_data.get(chunkX);
        System.out.println("FINAL CHUNK DATA FOR: "+"X: "+chunkX+" Z: "+chunkZ);
        System.out.println(final_chunk_data);

        */
        for (int z = 15; z >= 0; z--){
            for (int x = 0; x < 16; x++){
                //int max_height = (int) final_chunk_data.getJSONArray(z).get(x);
                //int max_height = (int) final_chunk_data.getJSONArray(z).get(x);
                //JsonArray row = final_chunk_data.get(z).getAsJsonArray();

                //int max_height = (int) final_chunk_data.get((z)).getAsJsonArray().get(x).getAsInt();
                //int max_height = (int) final_chunk_data.get(x).getAsJsonArray().get(z).getAsInt();
                //int max_height = row.getAsInt()

                int max_height = (int) final_chunk_data.get((15-z)).getAsJsonArray().get(x).getAsInt();

                boolean flag = false;
                if (max_height == -32768){
                    max_height = 0;
                    flag = true;
                    //Material setblock = Material.WATER;
                }
                for (int y = chunkData.getMinHeight(); y <= max_height; y++){
                    if(y == chunkData.getMinHeight()){
                        chunkData.setBlock(x, y, z,Material.BEDROCK);
                    }else{
                        if(flag){
                            chunkData.setBlock(x, y, z, Material.WATER);
                        }else if (max_height < -30) {
                            chunkData.setBlock(x,y,z,Material.DIRT);
                        }else {
                            chunkData.setBlock(x, y, z, Material.STONE);
                        }
                        /*
                        if (max_height <= 0 && max_height >= -30){
                            chunkData.setBlock(x, y, z, Material.WATER);
                        } else if (max_height < -30) {
                            chunkData.setBlock(x,y,z,Material.DIRT);
                        }else {
                            chunkData.setBlock(x, y, z, Material.STONE);
                        }
                        */
                    }



                }
            }
        }
        System.out.println("Chunk Loaded For X: "+chunkX+" Z: "+chunkZ);

/*
        for (int z = (chunkZ+1)*16; z >= chunkZ * 16; z--){
            for (int x = (chunkX)*16; x <=  (chunkX+1) *16; x++){
                JSONArray row = final_chunk_data.getJSONArray(0)
                int max_height =
                for (int y = chunkData.getMinHeight(); y <= chunkData.getMaxHeight(); y++){

                }
            }
        }

*/

      /*
        for( int y = chunkData.getMinHeight();y < 130 && y  < chunkData.getMaxHeight();y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    //float noise2 = (terrainNoise.GetNoise(x + (chunkX * 16), z + (chunkZ * 16)) * 2) + (detailNoise.GetNoise(x + (chunkX * 16), z + (chunkZ * 16)) / 10);
                    float noise2 = (terrainNoise.GetNoise(x, z) * 2) + (detailNoise.GetNoise(x, z) / 10);
                    if(65+(30 * noise2) > y){
                        chunkData.setBlock(x, y, z, Material.STONE);
                    } else if (y < 62) {
                        chunkData.setBlock(x,y,z,Material.DIRT);
                    }


                }
            }
        }
        */
    }


}
