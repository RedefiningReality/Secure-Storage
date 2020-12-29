package ss.utils.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import ss.utils.logging.Logger;

import static ss.utils.logging.MessageLevels.INFO;
import static ss.utils.logging.MessageLevels.DATA;
import static ss.utils.logging.MessageLevels.WARNING;

public class PropertiesFile {

	private static boolean isInitialized = false;

	private static String propFileName;
	private static String propFileLocation;

	private static Properties properties;
	private static FileInputStream inputStream;
	private static FileOutputStream outputStream;

	private static Logger logger;

	public static void initialize() throws FileNotFoundException, IOException {
		propFileName = "config.properties";
		propFileLocation = "";

		properties = new Properties();

		if (!new File(propFileLocation + propFileName).exists())
			outputStream = new FileOutputStream(propFileLocation + propFileName);
		else
			outputStream = new FileOutputStream(propFileLocation + propFileName, true);

		inputStream = new FileInputStream(propFileLocation + propFileName);
		properties.load(inputStream);

		logger = new Logger("PropertiesFile");

		isInitialized = true;
	}

	public static void initialize(String propFileName) throws IOException {
		PropertiesFile.propFileName = propFileName;
		propFileLocation = "";

		properties = new Properties();

		if (!new File(propFileLocation + propFileName).exists())
			outputStream = new FileOutputStream(propFileLocation + propFileName);
		else
			outputStream = new FileOutputStream(propFileLocation + propFileName, true);

		inputStream = new FileInputStream(propFileLocation + propFileName);
		properties.load(inputStream);

		logger = new Logger("PropertiesFile");

		isInitialized = true;
	}

	public static void initialize(String propFileName, String propFileLocation) throws IOException {
		PropertiesFile.propFileName = propFileName;
		PropertiesFile.propFileLocation = propFileLocation;

		properties = new Properties();

		if (!new File(propFileLocation + propFileName).exists())
			outputStream = new FileOutputStream(propFileLocation + propFileName);
		else
			outputStream = new FileOutputStream(propFileLocation + propFileName, true);

		inputStream = new FileInputStream(propFileLocation + propFileName);
		properties.load(inputStream);

		logger = new Logger("PropertiesFile");

		isInitialized = true;
	}

	public static boolean isInitialized() {
		return isInitialized;
	}

	public static boolean addProperty(String property, String value) throws IOException {
		if (isInitialized)
			if (!properties.containsKey(property)) {
				properties.setProperty(property, value);
				return true;
			}

		return false;
	}

	public static String setProperty(String property, String value) throws IOException {
		if (isInitialized)
			return (String) properties.setProperty(property, value);

		return null;
	}

	public static String getProperty(String property) throws IOException {
		if (isInitialized)
			return properties.getProperty(property);

		return null;
	}

	public static void saveToFile() throws IOException {
		if (isInitialized) {
			inputStream.close();
			outputStream.close();

			logger.printMessage("Removing existing properties file", INFO);
			logger.printMessage("Path: " + propFileLocation + propFileName, DATA);
			try {
				Files.deleteIfExists(Paths.get(propFileLocation + propFileName));
			} catch (IOException ex) {
				logger.printMessage("Unable to save to properties file. " + "It may be in use by another process.",
						WARNING);
				return;
			}
			logger.printMessage("Removed file successfully", INFO);

			outputStream = new FileOutputStream(propFileLocation + propFileName);

			logger.printMessage("Saving properties to file", INFO);
			properties.store(outputStream,
					" This file contains the properties used by the storage server application\n"
					+ " Please do not modify the contents of this file while it is running\n"
					+ " Make sure that all properties point to existing ips/file/directories\n"
					+ " or the program will not run correctly");
			logger.printMessage("Saved properties successfully", INFO);
		}
	}

	public static void removeFile() throws IOException {
		if (isInitialized) {
			inputStream.close();
			outputStream.close();

			logger.printMessage("Removing existing properties file", INFO);
			logger.printMessage("Path: " + propFileLocation + propFileName, DATA);
			try {
				Files.deleteIfExists(Paths.get(propFileLocation + propFileName));
			} catch (IOException ex) {
				logger.printMessage("Unable to remove properties file. " + "It may be in use by another process",
						WARNING);
				return;
			}
			logger.printMessage("Removed file successfully", INFO);

			isInitialized = false;
		}
	}

	public static void setPropertyFileName(String propFileName) throws IOException {
		PropertiesFile.propFileName = propFileName;

		inputStream = new FileInputStream(propFileLocation + propFileName);
		properties.load(inputStream);
	}

	public static String getPropertyFileName() {
		return propFileName;
	}

	public static void setPropertyFileLocation(String propFileLocation) throws IOException {
		PropertiesFile.propFileLocation = propFileLocation;

		inputStream = new FileInputStream(propFileLocation + propFileName);
		properties.load(inputStream);
	}

	public static String getPropertyFileLocation() {
		return propFileLocation;
	}
}