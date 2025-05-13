The compiled plugin can be found at https://www.spigotmc.org/resources/customworldgeneration.124556/
How to use this plugin

Each file containing terrain/material data should be a 4D list of points, the first 2 dimensions are the rows and columns of the image, the 2nd 2 dimensions are each of images as a chunk (16x16)

To use the plugin:
    In the server folder --> put the plugin in the plugins folder

    In the bukkit.yml copy and paste this at the bottom of the file
        worlds:
          world:
            generator: CustomWorldGeneration
        aliases: now-in-commands.yml

    In the server folder, make a folder called static
        The folder needs to contain a minimum of 3 items
            FileStructure.json
            Elevation file(s) (json files)
            MaterialsFiles folder (containing Material Files (json files))


    The files of structure of the folder

    NOTE: If you are using the pixel painter [https://alexanthology.com/CustomWorldGen/index.php] - only the FileStructure.json is relevant
    NOTE: The Elevation files and Material files need to have the same name
    NOTE: This plugin does not currently access negative indices, the bottom left is at 0,0

    The FileStructure.json file should contain the following
    It should contain the number of chunks in a single direction contained in a file (it will be assumed to be a square) {FileChunkSize:X}
    For example, if a file is 4 chunks by 4 chunks --> FileChunkSize:4

    Another parameter stores how many files in the X and Z direction {files_on_x_axis:numFiles}
        so if there are 4 files {files_on_x_axis:2}


    It should also contain a json structure holding which file to use given a 'file coordinate' in an X_Z fashion {"FileStructure":{"0_0":X,}
    {"FileStructure":{"0_0":file1,"0_1":file2,"1_0":file3}}

    Put together --> {FileChunkSize:X,files_on_x_axis:numFiles,"FileStructure:"{"0_0":file1,"0_1":file2,"1_0":file3}}



    Elevation Files
        The elevation files are 4 dimensional file - the first 2 dimensions are rows and columns of chunks
        each chunk is a 2 dimensional (rows and columns) array of heights (Integers)
        The top left chunk is the top left of the world, the top left block in each chunk is the top left of each chunk

   Material Files
        Material files are constructed the exact same way as the elevation files
        but instead of integers it is strings, the names of blocks (ex: DIRT, SAND, WATER, etc...)

