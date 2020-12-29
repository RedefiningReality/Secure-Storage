package ss.main;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import ss.utils.encryption.AES;
import ss.utils.encryption.RSA;

public class KeyGenerator {

	public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException
	{
		if (args.length == 0 || args[0].equals("-h") || args[0].equals("--help")
				|| args.length == 1 || args.length > 3) {
			helpScreen();
			System.exit(0);
		}
		
		String publicKey = fullPath(args[0], args[1] + ".pub");
		String privateKey = fullPath(args[0], args[1] + ".key");
		
		new RSA(publicKey, privateKey, 4096);
		
		if (args.length == 3 && args[2].equals("1")) {
			String aesKey = fullPath(args[0], "aes.key");
			String aesIV = fullPath(args[0], "aes.txt");
			
			new AES(aesKey, aesIV, false);
		}
	}
	
	public static String fullPath(String directory, String name)
	{
		if (directory.endsWith("/"))
			return directory + name;
		else
			return directory + "/" + name;
	}
	
	public static void helpScreen()
	{
		System.out.println("Usage: gen-keys [directory] [name]\n"
				+ "   or: gen-keys [directory] [name] [gen aes]\n"
				+ "gen-keys -h and gen-keys --help show this screen\n"
				+ "\n"
				+ "Options:\n"
				+ "[directory]             => key storage directory\n"
				+ "[name]                  => key name\n"
				+ "[gen aes]       0       => only generate RSA keys (same as gen-keys [directory] [name])\n"
				+ "                1       => generate AES keys for file storage along with RSA keys");
	}
}
