package ss.main;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import ss.client.Client;
import ss.utils.logging.MessageLevels;

public class WebServer {

	public static void main(String[] args) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException
	{
		if (args.length == 0 || args[0].equals("-h") || args[0].equals("--help") || args.length != 3) {
			helpScreen();
			System.exit(0);
		}
		
		int storageServer = -1;
		try {
			storageServer = Integer.parseInt(args[0]);
		} catch(NumberFormatException ex) {
			System.out.println("The first argument should be a valid integer (1 or 2)");
			helpScreen();
			System.exit(0);
		}
		
		String operation = args[1];
		String argument = args[2];
		
		Client client = new Client(storageServer, true, MessageLevels.DATA);
		boolean valid = client.start(operation, argument);
		
		if (!valid)
			helpScreen();
	}
	
	public static void helpScreen()
	{
		System.out.println("Usage: securestore [server number] [command [arg]]\n"
				+ "securestore -h and securestore --help show this screen\n"
				+ "\n"
				+ "Options:\n"
				+ "[server number]         1                       => storage server 1\n"
				+ "                        2                       => storage server 2\n"
				+ "[command [arg]]         ls [dir]                => list files stored (\"ls /\" for base directory)\n"
				+ "                        upload [filename]       => upload file from disk to storage server\n"
				+ "                        download [filename]     => download file from storage server to disk\n"
				+ "                        rm [filename]           => remove file\n"
				+ "\n"
				+ "Note: files on disk are retreived from and stored to path specified in /opt/secure_storage/config.properties file");
	}

}