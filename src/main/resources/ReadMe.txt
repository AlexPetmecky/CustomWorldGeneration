To load more than 1 file, the FileLoader(String Name_Of_MultiFile_Structure_file); must be used
the Name_Of_MultiFile_Structure_file should be a json file but the actual string is just the name without the .json extension
it should contain the number of chunks in a single direction contained in a file  (it will be assumed to be a square) and another parameter storing how many files in the X and Z direction {FileChunkSize:chunks,files_on_x_axis:numFiles}
it should also contain a json structure holding which file to use given a 'file coordinate' in an X_Z fashion {"FileStructure":{"0_0":file1,"0_1":file2,"1_0":file3}}

put together --> {FileChunkSize:X,files_on_x_axis:numFiles,"FileStructure:"{"0_0":file1,"0_1":file2,"1_0":file3}}


Loading files can only load E || W not both at the same time, Files must have at least 21 chunks in both X and Z direction

CustomWorldGen