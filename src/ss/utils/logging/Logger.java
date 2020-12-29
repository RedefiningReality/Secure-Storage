/** 
 * Logger class for logging messages. 
 *
 * @version 3.0
 * @since 3.0
 */

package ss.utils.logging;

import static ss.utils.logging.MessageLevels.DATA;
import static ss.utils.logging.MessageLevels.ERROR;
import static ss.utils.logging.MessageLevels.HIGHEST_PRIORITY;
import static ss.utils.logging.MessageLevels.INFO;
import static ss.utils.logging.MessageLevels.WARNING;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
	/* Create variables that control message logging */

	// Creates a new boolean for controlling the printing of console messages
	private static boolean verbose = true;
	// Creates a new integer for specifying the level of messages printed to the
	// console
	private static int messageLevel = ERROR;
	// Creates a new string which contains the name of the class logging the
	// message
	private String className;

	private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

	private static boolean print = true;
	private static BufferedWriter logFile;

	/**
	 * Creates a new logger for logging messages to the console. This constructor
	 * sets the verbose and message level (verbose being true and message level
	 * being ERROR).
	 * 
	 * @since 3.0
	 */
	public Logger() {
		// Sets global variable className to a new String
		this.className = new String();
	}

	/**
	 * Creates a new logger for logging messages to the console. This constructor
	 * sets the verbose and message level (verbose being true and message level
	 * being ERROR). Additionally, it accepts the parameter <code>className</code>,
	 * which sets the name of the class logging the message.
	 * 
	 * @param className
	 * @since 3.0
	 */
	public Logger(String className) {
		// Sets global variable className to the specified value
		this.className = className;
	}

	/**
	 * Sets the value of verbose. This method is used to set if messages should be
	 * logged to the console or not. This does not apply to <code>
	 * HIGHEST_PRIORITY</code> messages.
	 * 
	 * @param verbose
	 *            Whether or not messages should be logged
	 * @see #verbose
	 * @see #printMessage
	 * @since 3.0
	 */
	public static void setVerbose(boolean verbose) {
		// Assigns the global boolean verbose to the verbose parameter
		Logger.verbose = verbose;
	}

	/**
	 * Returns the value of verbose. This method is used to see if messages should
	 * be logged to the console or not. This does not apply to <code>
	 * HIGHEST_PRIORITY</code> messages.
	 * 
	 * @return boolean Whether or not messages should be logged
	 * @see #verbose
	 * @see #printMessage
	 * @since 3.0
	 */
	public static boolean getVerbose() {
		// Returns the value of verbose
		return verbose;
	}

	/**
	 * Sets the value of messageLevel. This method is used to set the message level.
	 * 0 => HIGHEST_PRIORITY 1 => ERROR 2 => WARNING 3 => INFO 4 => DATA
	 * 
	 * @param messageLevel
	 *            The log level of messages to be printed
	 * @see #messageLevel
	 * @see #printMessage(java.lang.String, int)
	 * @since 3.0
	 */
	public static void setMessageLevel(int messageLevel) {
		// Assigns the global integer messageLevel to the messageLevel parameter
		Logger.messageLevel = messageLevel;
	}

	/**
	 * Gets the value of messageLeve. This method is used to get the message level.
	 * 
	 * @return integer The log level of messages to be printed
	 * @see #messageLevel
	 * @see #printMessage(java.lang.String, int)
	 * @since 3.0
	 */
	public static int getMessageLevel() {
		// Returns the value of messageLevel
		return messageLevel;
	}

	public static void setDateTimeFormat(String dateTimeFormat) {
		Logger.formatter = DateTimeFormatter.ofPattern(dateTimeFormat);
	}
	
	public static void printToConsole() {
		Logger.print = true;
	}
	
	public static void suppressPrintToConsole() {
		Logger.print = false;
	}

	public static void setLogFile(String fileName) throws IOException {
		File file = new File(fileName);
		FileWriter fileWriter = new FileWriter(file.getAbsoluteFile(), true);
		Logger.logFile = new BufferedWriter(fileWriter);
	}

	public static void setLogFile(String filePath, String fileName) throws IOException {
		File file = new File(filePath + fileName);
		FileWriter fileWriter = new FileWriter(file.getAbsoluteFile(), true);
		Logger.logFile = new BufferedWriter(fileWriter);
	}

	/**
	 * Prints messages to the console. This method checks to see if the level of the
	 * message is greater than or equal than the desired message level, and if it
	 * is, it logs the message to the console. If the message level is
	 * <code>ERROR</code>, then the system is exited with error code 1 (Error).
	 * 
	 * @param message
	 *            The message to print
	 * @param level
	 *            The log level of the message
	 * @see #messageLevel
	 * @see #verbose
	 * @see #className
	 * @see Messages
	 * @since 3.0
	 */
	public void printMessage(String message, int level) {
		// Creates a variable which holds the string value of level
		// For example: 1 = "ERROR"
		String stringLevel;

		// If the level is equal to 'ERROR' (1)
		if (level == ERROR) {
			// Sets the value of stringLevel to "ERROR"
			stringLevel = "ERROR";

			// If the level is equal to 'WARNING' (2)
		} else if (level == WARNING) {
			// Sets the value of stringLevel to "WARNING"
			stringLevel = "WARNING";

			// If the level is equal to 'INFO' (3)
		} else if (level == INFO) {
			// Sets the value of stringLevel to "INFO"
			stringLevel = "INFO";

			// If the level is equal to 'DATA' (4)
		} else if (level == DATA) {
			// Sets the value of stringLevel to "DATA"
			stringLevel = "DATA";

			// Otherwise
		} else {
			// Sets the value of stringLevel to "HIGHEST_PRIORITY"
			// This is for visual purposes only, since if the level is
			// 'HIGHEST_PRIORITY', the program will not print the level to the
			// console
			stringLevel = "HIGHEST_PRIORITY";
		}

		// If verbose is equal to true, if the desired message level is greater
		// or equal to the desired message level, and if the message level is
		// not HIGHEST_PRIORITY
		if ((verbose && messageLevel >= level) && level != HIGHEST_PRIORITY) {
			// Prints out the class name, the string value of the level,
			// and the message to the console
			if (print)
				System.out.println(
						formatter.format(LocalDateTime.now()) + " " + className + ": [" + stringLevel + "] " + message);

			if (logFile != null)
				try {
					logFile.write(formatter.format(LocalDateTime.now()) + " " + className + ": [" + stringLevel + "] "
							+ message);
					logFile.newLine();
					logFile.flush();
				} catch (IOException ex) {
				}

			// If the message level is HIGHEST_PRIORITY
		} else if (level == HIGHEST_PRIORITY) {
			// Prints out the class name and the message to the console
			if (print)
				System.out.println(formatter.format(LocalDateTime.now()) + " " + className + ": " + message);

			if (logFile != null)
				try {
					logFile.write(formatter.format(LocalDateTime.now()) + " " + className + ": " + message);
					logFile.newLine();
					logFile.flush();
				} catch (IOException ex) {
				}
		}
	}
}