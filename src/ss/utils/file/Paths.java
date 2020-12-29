package ss.utils.file;

import java.io.File;

public class Paths {
	//For more efficient file transfer, this method gives us the size of the expected file 
	public static int getSize(String filename)
	{
		return (int) new File(filename).length(); 
	}
	
	public static String removeFileExtension(String filename)
	{
		return filename.contains(".") ? filename.substring(0, filename.lastIndexOf('.')) : filename;
	}
	
	public static String combinePaths(String path1, String path2)
	{
		while (path2.startsWith("/"))
			path2 = path2.substring(1);
		while (path1.endsWith("/"))
			path1 = path1.substring(0, path1.length()-1);
		
		return path1 + "/" + path2;
	}
}