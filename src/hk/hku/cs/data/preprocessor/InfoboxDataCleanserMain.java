package hk.hku.cs.data.preprocessor;

import java.io.*;
import java.util.*;
import java.text.*;

public class InfoboxDataCleanserMain {
	public static void main(String args[]) {
		if (args.length < 3) {
			System.out.println("Arguments order :<data XML path> <template XML path> <output XML path>");
			return;
		}
		
		String dataXmlPathname = args[0].trim();
		String templateXmlPathname = args[1].trim();
		String outputXmlPathname = args[2].trim();
		
		try {
			InfoboxDataCleanser cleanser = new InfoboxDataCleanser();
			cleanser.initRedirect(templateXmlPathname);
			if (cleanser.isErrorFound() || cleanser.isWarningFound()) {
				exportErrorAndWarningLog(templateXmlPathname, cleanser, outputXmlPathname);
				System.out.println("Errors/warnings found in template. Please refer to log for details");
				return;
			}

			cleanser.cleanse(dataXmlPathname, outputXmlPathname);
			if (cleanser.isErrorFound() || cleanser.isWarningFound()) {
				exportErrorAndWarningLog(dataXmlPathname, cleanser, outputXmlPathname);
				System.out.println("Errors/warnings found in data. Please refer to log for details");
				return;
			}
		}
		catch (Exception e) {
		}
	}
	
	private static void exportErrorAndWarningLog(String xmlPathname, InfoboxDataCleanser cleanser,
			String outputXmlPathname) {
		System.out.println("Exporting parsing error and warning log...");

		String logDir = createLogDirectory(outputXmlPathname);
		String logPathname = getLogPathname(logDir);
		try {
			FileOutputStream fos = new FileOutputStream(new File(logPathname));
			PrintStream ps = new PrintStream(fos);

			printErrorMessage(ps, xmlPathname, cleanser.getErrors());
			printErrorMessage(ps, xmlPathname, cleanser.getWarnings());

			ps.close();
			fos.close();
		}
		catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	private static String createLogDirectory(String outputXmlPathname) {
		String outputDir = new File(outputXmlPathname).getParent();
		String logDir = outputDir + File.separator + "log";
		new File(logDir).mkdirs();
		
		return logDir;
	}
	
	private static String getLogPathname(String logDir) {
		SimpleDateFormat logTimeFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String logTimeStr = logTimeFormat.format(new Date());
		
		return logDir + File.separator + logTimeStr + ".log";
	}
	
	private static void printErrorMessage(PrintStream ps, String xmlPathname, List<String> errorMessages) {
		for (String errorMessage : errorMessages) {
			ps.println(xmlPathname + " " + errorMessage);
		}
	}
}
