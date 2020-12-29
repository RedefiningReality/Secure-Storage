package ss.utils.encryption; 

import java.nio.file.Files; 
import java.nio.file.Paths;
import java.io.*; 
/*import java.io.FileWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.FileInputStream; 
import java.io.FileOutputStream;*/
import java.nio.charset.StandardCharsets;
import java.security.*; 
import java.security.spec.*; 
import java.util.Base64;
import javax.crypto.*; 

/* 
	The rsa_utils class contains a set of methods htat will enable us to complete the following: 
	1. Generate new public/private key pairs 
	2. Retrieve public/private key pairs 
	3. Encrypt and decrypt a string or file 
*/ 

public class RSA
{
	private PrivateKey privateKey; 
	private PublicKey publicKey;

	/* There are three different operating styles 
		1. User only provides key size and will use teh default file names 
		2. User provides desired file names for public/private keys 
		3. For decryption purposes where the user only has the public keys 
	*/
	
	public RSA(String publicKeyPath) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException
	{
		initStored(publicKeyPath);
	}
	
	public RSA(String publicKeyPath, String privateKeyPath) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException
	{
		initStored(publicKeyPath, privateKeyPath);
	}
	
	public RSA(String publicKeyPath, String privateKeyPath, int size) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException
	{
		initNotStored(publicKeyPath, privateKeyPath, size);
	}

	//If the user has a saved key pair, the stored key will be set to true; otherwise false 
	public RSA(String publicKeyPath, String privateKeyPath, int size, boolean isStored) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException
	{
		if(isStored)
			initStored(publicKeyPath, privateKeyPath);
		else
			initNotStored(publicKeyPath, privateKeyPath, size);
	}
	
	private void initStored(String publicKeyPath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException
	{
		byte[] bytes = Files.readAllBytes(Paths.get(publicKeyPath)); 
		X509EncodedKeySpec X509encoded = new X509EncodedKeySpec(bytes);
		
		KeyFactory factory = KeyFactory.getInstance("RSA"); 
		this.publicKey = factory.generatePublic(X509encoded);
	}
	
	private void initStored(String publicKeyPath, String privateKeyPath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException
	{
		if (publicKeyPath != null)
			initStored(publicKeyPath);

		byte[] bytes = Files.readAllBytes(Paths.get(privateKeyPath)); 
		PKCS8EncodedKeySpec PKCS8encoded = new PKCS8EncodedKeySpec(bytes);
		
		KeyFactory factory = KeyFactory.getInstance("RSA");
		privateKey = factory.generatePrivate(PKCS8encoded); 
	}
	
	private void initNotStored(String publicKeyPath, String privateKeyPath, int size) throws NoSuchAlgorithmException, FileNotFoundException, IOException {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA"); 
		generator.initialize(size);
		
		KeyPair pair = generator.generateKeyPair(); 
		this.privateKey = pair.getPrivate(); 
		this.publicKey = pair.getPublic(); 

		try (FileOutputStream out = new FileOutputStream(privateKeyPath)) {
			out.write(privateKey.getEncoded()); 
		}
		try(FileOutputStream out = new FileOutputStream(publicKeyPath)) {
			out.write(publicKey.getEncoded()); 
		}
	}

	public String encrypt(String plaintext) throws Exception 
	{
		Cipher cipher = Cipher.getInstance("RSA"); 
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		
		byte[] bytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8)); 
		return Base64.getEncoder().encodeToString(bytes); 
	}

	//test method (check to see if used elsewhere, if not delete) that used the private key for decyrption 
	public String decrypt(String ciphertext) throws Exception
	{
		byte[] bytes = Base64.getDecoder().decode(ciphertext); 
		Cipher cipher = Cipher.getInstance("RSA");
		
		cipher.init(Cipher.DECRYPT_MODE, privateKey); 
		return new String(cipher.doFinal(bytes), StandardCharsets.UTF_8);
	}

	//This method enables the encryption of a given file, the user specifies whether they are using the private key or public key during encryption 
	public void encryptFile(String plaintext, String encrypted, boolean usePrivate) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, FileNotFoundException, IOException, IllegalBlockSizeException, BadPaddingException 
	{
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		
		if(usePrivate)
			cipher.init(Cipher.ENCRYPT_MODE, privateKey);
		else
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		
		try (FileInputStream in = new FileInputStream(plaintext);
				FileOutputStream out = new FileOutputStream(encrypted)) {
			processFile(cipher, in, out); 
		}
	}

	//This method enables the decryption of a givne file, the user specifies whether they are using the private key or public key during decryption 
	public void decryptFile(String encrypted, String plaintext, boolean usePrivate) throws FileNotFoundException, IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
	{
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		
		if(usePrivate)
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
		else
			cipher.init(Cipher.DECRYPT_MODE, publicKey);
		
		try (FileInputStream in = new FileInputStream(encrypted);
				FileOutputStream out = new FileOutputStream(plaintext)) {
			processFile(cipher, in, out); 
		}
	}

	//This helper method helps to cut down on redundant code 
	//check code for potential security concerns as it deals with reading and writing to a file 
	private static void processFile(Cipher cipher, InputStream in, OutputStream out) throws IOException, IllegalBlockSizeException, BadPaddingException
	{
		byte[] inputBuffer = new byte[1024];
		byte[] outputBuffer;
		
		int length; 
		while ((length = in.read(inputBuffer)) != -1) {
			outputBuffer = cipher.update(inputBuffer, 0, length);
			if(outputBuffer != null)
				out.write(outputBuffer);
		}
		
		outputBuffer = cipher.doFinal(); 
		if (outputBuffer != null)
			out.write(outputBuffer);
	}
}	


