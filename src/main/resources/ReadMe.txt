How to use this plugin

Each File should be a 4D list of points, the first 2 dimensions are the rows and columns of the image, the 2nd 2 dimensions are each of images as a chunk (16x16)

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
            MaterialsFiles folder


    The files of structure of the folder



To load more than 1 file, the FileLoader(String Name_Of_MultiFile_Structure_file); must be used
the Name_Of_MultiFile_Structure_file should be a json file but the actual string is just the name without the .json extension
THe FileStructure.json file should contain the following
It should contain the number of chunks in a single direction contained in a file (it will be assumed to be a square) {FileChunkSize:X}
For example, if a file is 4 chunks by 4 chunks --> FileChunkSize:4

Another parameter stores how many files in the X and Z direction {files_on_x_axis:numFiles}
    so if there are 4 files {files_on_x_axis:2}


It should also contain a json structure holding which file to use given a 'file coordinate' in an X_Z fashion {"FileStructure":{"0_0":X,}
{"FileStructure":{"0_0":file1,"0_1":file2,"1_0":file3}}

Put together --> {FileChunkSize:X,files_on_x_axis:numFiles,"FileStructure:"{"0_0":file1,"0_1":file2,"1_0":file3}}
