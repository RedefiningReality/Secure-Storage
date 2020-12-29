package ss.utils.encryption;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey; 
import javax.crypto.spec.SecretKeySpec; 
import javax.crypto.spec.IvParameterSpec; 
import java.util.Base64; 
import java.nio.charset.StandardCharsets; 
import java.nio.file.Files;
import java.nio.file.Paths; 
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException; 

/* 
	The aes_utils class contains a set of methods that will enable us to complete the following: 
		1. Generate new symmetric keys 
		2. Retrieve symmetric keys for use in encryption/decryption 
		3. Encrypt and decrypt a string or file 
*/ 
public class AES 
{
	private SecretKey key;
	private IvParameterSpec iv;

	//Will be updating code to deprecate this function 
	public AES(String keyPath, String ivPath) throws IOException
	{
		initStored(keyPath, ivPath);
	}

	//During operation, this code enables the user to provide a desired key and iv location for either retrieving stored keys (stored_keys = true) or storing new keys
	public AES(String keyPath, String ivPath, boolean isStored) throws IOException, NoSuchAlgorithmException
	{
		if(isStored)
			initStored(keyPath, ivPath);
		else
			initNotStored(keyPath, ivPath);
	}
	
	private void initStored(String keyPath, String ivPath) throws IOException
	{
		byte[] keyBytes = Files.readAllBytes(Paths.get(keyPath)); 
		this.key  = new SecretKeySpec(keyBytes, "AES");
		
		byte[] ivBytes = Files.readAllBytes(Paths.get(ivPath)); 
		this.iv = new IvParameterSpec(ivBytes); 
	}
	
	private void initNotStored(String keyPath, String ivPath) throws NoSuchAlgorithmException, FileNotFoundException, IOException
	{
		KeyGenerator generator = KeyGenerator.getInstance("AES"); 
		generator.init(256);
		this.key = generator.generateKey();
		
		byte[] ivBytes = new byte[16];
		SecureRandom srandom = new SecureRandom(); 
		srandom.nextBytes(ivBytes);
		
		this.iv = new IvParameterSpec(ivBytes); 
	
		try (FileOutputStream out = new FileOutputStream(ivPath)) {
			out.write(ivBytes); 
		}
		
		try (FileOutputStream out = new FileOutputStream(keyPath)) {
			byte[] keyBytes = key.getEncoded(); 
			out.write(keyBytes); 
		}
	}

	//This test method encrypts a given string and returns a string 
	public String encrypt(String plaintext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException
	{
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key, iv); 
		
		byte[] bytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
		return Base64.getEncoder().encodeToString(bytes); 
	}

	//This test method decrypts a given string and returns a string 
	public String decrypt(String ciphertext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException
	{
		byte[] bytes = Base64.getDecoder().decode(ciphertext); 
		
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); 
		cipher.init(Cipher.DECRYPT_MODE, key, iv);
		
		return new String(cipher.doFinal(bytes), StandardCharsets.UTF_8);
	}

	//This method enables the encryption of a given file
	public void encryptFile(String plaintext, String encrypted) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, FileNotFoundException, IllegalBlockSizeException, BadPaddingException, IOException
	{
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); 
		cipher.init(Cipher.ENCRYPT_MODE, key, iv);
		
		processFile(cipher, plaintext, encrypted);
	}

	//This method enables the decyprtion of a given file 
	public void decryptFile(String encrypted, String plaintext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, FileNotFoundException, IllegalBlockSizeException, BadPaddingException, IOException
	{
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, key, iv);
		
		processFile(cipher, encrypted, plaintext);
	}

	//This helper method cuts down on redundant code 
	//Check code for potential security concerns as it deals with reading and writing to a file 
	private void processFile(Cipher cipher, String inputFile, String outputFile) throws FileNotFoundException, IOException, IllegalBlockSizeException, BadPaddingException
	{
		try (FileInputStream in = new FileInputStream(inputFile);
				FileOutputStream out = new FileOutputStream(outputFile)) {
			
			byte[] inputBuffer = new byte [1024];
			byte[] outputBuffer;
			
			int length; 
			while((length = in.read(inputBuffer)) != -1)
			{
				outputBuffer = cipher.update(inputBuffer, 0, length);
				if(outputBuffer != null)
					out.write(outputBuffer);
			}
			
			outputBuffer = cipher.doFinal(); 
			if (outputBuffer != null)
				out.write(outputBuffer);
		}
	}
}
