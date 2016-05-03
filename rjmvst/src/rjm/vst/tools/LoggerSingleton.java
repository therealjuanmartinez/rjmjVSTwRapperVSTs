package rjm.vst.tools;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;

public class LoggerSingleton {


	final static boolean enabled = false;

	private static LoggerSingleton instance;

	private LoggerSingleton() {
		// Exists only to defeat instantiation.
		logBuff = new StringBuffer();
		count = 0;
	}

	public static LoggerSingleton getInstance() {
		if(instance == null) {
			instance = new LoggerSingleton();

		}
		return instance;
	}


	private static StringBuffer logBuff;
	private static Date lastWrite;

	private static int count;

	public static void log(String message)
	{
		if (enabled)
		{
			try
			{
				logBuff.append(message + "\n");
                count++;
			}
			catch (Exception e)
			{
				count = 0;
				logBuff = new StringBuffer();
				logBuff.append(message + "\n");
			}

			lastWrite = new Date();

			if (count > 70)
			{
				count = 0;
				writelog();
				logBuff.setLength(0);
			}
		}
	}

	private static void writelog()
	{
		try
		{
			//HACK JMM
			//TODO remove this thing
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(

					new FileOutputStream("c:\\temp\\jwrapper_log.txt", true), "UTF-8"));

			try
			{
				writer.write(logBuff.toString());
				writer.close();
			} catch (IOException e)
			{
			}
		} catch (UnsupportedEncodingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
