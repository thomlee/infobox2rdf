package hk.hku.cs.data.preprocessor;

import org.xml.sax.*;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.*;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.helpers.DefaultHandler;

public class infobox2Tri extends DefaultHandler {
	private String pageName = "";
	private String infoboxName = "";
	private String propertyName = "";
	private String entryValue = "";
	private String str1 = "";
	private int count = 0;
	private boolean isPage = false;
	private boolean isTitle = false;
	private boolean isInfobox = false;
	private boolean isInfoboxName = false;
	private boolean isEntry = false;
	private boolean isProperty = false;
	private boolean isValue = false;
	private static Connection conn = null;
	private static Statement stat = null;
	private static PreparedStatement statement = null;
    
	private String sub = "";
	private String pre = "";
	private String obj = "";
	
	private static int maxBatchSize = 500000;
	private int batchSize = 0;
	private int insertCount = 0;
	
	private Set<PreAndObj> preObjs;

	class PreAndObj {
		private String pre;
		private String obj;
		
		PreAndObj(String predicate, String object) {
			this.pre = predicate;
			this.obj = object;
		}
		
		public int hashCode() {
			return 31 * this.pre.hashCode() + this.obj.hashCode();
		}
		
		public boolean equals(Object poObject) {
			if (poObject instanceof PreAndObj) {
				PreAndObj po = (PreAndObj) poObject;
				if (this.obj.equals(po.obj) && this.pre.equals(po.pre)) {
					return true;
				}
			}
			
			return false;
		}
	}
	public infobox2Tri() {
		super();
	}

	public static void main(String args[]) {
		long startTime = System.currentTimeMillis();
		if(args.length < 4) {
			System.out.println("Arguments order: <cleansed infobox XML file path> <DB name> <DB user>" +
					" <DB password> [insert batch size]");
		} else {
			try {
				SAXParserFactory sf = SAXParserFactory.newInstance();
				SAXParser sp = sf.newSAXParser();
				infobox2Tri reader = new infobox2Tri();
//				Class.forName("org.sqlite.JDBC");
//				conn = DriverManager.getConnection("jdbc:sqlite:" + args[1]);
				Class.forName("org.postgresql.Driver");
				conn= DriverManager.getConnection("jdbc:postgresql://localhost/" + args[1], args[2], args[3]); 				
				stat = conn.createStatement();
				System.out.println("Deleting existing data...");
				stat.executeUpdate("DROP TABLE IF EXISTS triplets");
				stat.executeUpdate("CREATE TABLE triplets (subject VARCHAR NOT NULL, predicate VARCHAR NOT NULL," +
						" object VARCHAR NOT NULL);");

				statement = conn.prepareStatement("INSERT INTO triplets (subject, predicate, object)" +
						" VALUES (?, ?, ?);");
				conn.setAutoCommit(false);
				
				if (args.length > 4) { 
					maxBatchSize = Integer.parseInt(args[4]);
				}
			
				sp.parse(new InputSource(args[0]), reader);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		
		float timeEclapsed = (float) (System.currentTimeMillis() - startTime) / 1000;
		
		System.out.println("Time eclapsed: " + timeEclapsed + "s");
	} 	
	
    public void startElement (String namespace, String localName, String qName, Attributes atts) {
        if (qName == "page") {
            isPage = true;
        }
        if (qName == "title") {
            if (isPage) {
                isTitle = true;
            }
        }
        if (qName == "infobox") {
            if (isPage) {
                isInfobox = true;
            }
        }

        if (qName == "name") {
            if (isInfobox) {
                isInfoboxName = true;
            }
        }
        if (qName == "entry") {
            if (isInfobox) {
                isEntry = true;
            }
        }
        if (qName == "property") {
            if (isEntry) {
                isProperty = true;
            }
        }
        if (qName == "value") {
            if (isEntry) {
                isValue = true;
            }
        }  
    }
  
    public void endElement (String namespace, String localName, String qName) {
        if (qName == "page") {
            pageName = "";
            isPage = false;
        }
        if (qName == "title") {
            if (isPage) {
                isTitle = false;
            }
        }
        if (qName == "infobox") {
            if (isPage) {
                infoboxName = "";
                isInfobox = false;
            }
        }
        if (qName == "name") {
            if (isInfobox) {
                isInfoboxName = false;
            }
            infoboxName = convertHtmlReservedChars(infoboxName);
        }
        if (qName == "entry") {
            if (isInfobox) {
                isEntry = false;
            }
        }
        if (qName == "property") {
            if (isEntry) {
                isProperty = false;
            }
        }
        if (qName == "value") {
            if (isEntry){
            	str1 = infoboxName +  "#" + convertHtmlReservedChars(propertyName);
            	str1 = str1.trim();
                sub = "<"+pageName+">";
        		pre = "<"+str1+">";

                String regex = "\\[\\[.*?\\]\\]";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(entryValue);
                List<String> u = new LinkedList<String>();
                if(!matcher.find()){
                	if(entryValue != ""){ 
                		obj = entryValue;
                		packLiteralObj(obj);
                	}
                }else{
                	u.add(matcher.group());
                	while(matcher.find()){
                		u.add(matcher.group());
                	}
                }
                
                if(u.size() == 1){
                    String left_str = entryValue.replaceAll("\\[\\[.+?\\]\\]", "");
                    if(left_str == "" || !Pattern.compile("[a-zA-Z0-9]").matcher(left_str).find()){
                        objExtractor1(u);
                    } else {
						objExtractor2(u, left_str);
					}
                }
                if(u.size() > 1){                   
                    String left_str = entryValue.replaceAll("\\[\\[.*?\\]\\]", "");
                    if (left_str == ""|| !Pattern.compile("[a-zA-Z0-9]").matcher(left_str).find()) {
                    	objExtractor3(u);
                    }else{
                    	objExtractor4(u);
                    }
                }

                propertyName = "";
                str1 = "";
                entryValue = "";
                isValue = false;
            }
        }
    }

	private void objExtractor4(List<String> u) {
		String s1 = "";
		if (Pattern.compile("\\[\\[.+?\\|.+?\\]\\]").matcher(entryValue).find()) {
			for (String s : u) {
				if (Pattern.compile("\\[\\[.+?\\|.+?\\]\\]").matcher(s).find()) {
					obj = s.substring(2, s.indexOf("|"));
					packUrlObj(obj);
				} else {
					obj = s.substring(2, s.length() - 2);
					packUrlObj(obj);
				}
			}
			s1 = entryValue.replaceAll("\\[\\[[^\\[\\[]*?\\|", " ");
			s1 = replaceNoise(s1);
			packLiteralObj(s1);
		} else {
			for (String s : u) {
				obj = s.substring(2, s.length() - 2);
				packUrlObj(obj);
			}
			s1 = entryValue;
			s1 = replaceNoise(s1);
			packLiteralObj(s1);
		}
	}

	private void objExtractor3(List<String> u) {
		String s2 = "";
		if (Pattern.compile("\\[\\[.+?\\|.+?\\]\\]").matcher(entryValue).find()) {
			for (String s : u) {
				if (Pattern.compile("\\[\\[.+?\\|.+?\\]\\]").matcher(s).find()) {
					s2 = s2	+ s.substring(s.indexOf("|") + 1, s.length() - 2) + " ";

					obj = s.substring(2, s.indexOf("|"));
					packUrlObj(obj);
				} else {
					obj = s.substring(2, s.length() - 2);
					packUrlObj(obj);
					s2 = s2 + s.substring(2, s.length() - 2) + " ";
				}
			}
			s2 = s2.replaceAll("\\s{2,}", " ");
			packLiteralObj(s2);
			s2 = "";
		}else{
			for (String s : u) {
				obj = s.substring(2, s.length() - 2);
				packUrlObj(obj);
			}
		}
	}

	private void objExtractor2(List<String> u, String left_str) {
		String s1 = "";
		for (String s : u) {
			if (Pattern.compile("\\[\\[.+?\\|.+?\\]\\]").matcher(s).find()) {
				obj = s.substring(s.indexOf("|") + 1, s.length() - 2) + left_str;
				packLiteralObj(obj);
				
				obj = s.substring(2, s.indexOf("|"));
				packUrlObj(obj);
			} else {
				obj = s.substring(2, s.length() - 2);
				packUrlObj(obj);
				
				s1 = entryValue;
				s1 = replaceNoise(s1);
				packLiteralObj(s1);
			}
		}
	}

	private String replaceNoise(String s1) {
		s1 = s1.replaceAll("\\[\\[", " ");
		s1 = s1.replaceAll("\\]\\]", " ");
		s1 = s1.replaceAll("\\s{2,}", " ");
		return s1;
	}

	private void objExtractor1(List<String> u) {
		for (String s : u) {
			if (Pattern.compile("\\[\\[.+?\\|.+?\\]\\]").matcher(s).find()) {
				obj = s.substring(s.indexOf("|") + 1, s.length() - 2);
				packLiteralObj(obj);

				obj = s.substring(2, s.indexOf("|"));
				packUrlObj(obj);
			} else {
				obj = s.substring(2, s.length() - 2);
				packUrlObj(obj);
			}
		}
	}

	private void packLiteralObj(String obj) {
		obj = obj.trim();
		if (!obj.isEmpty()) {
			insert(sub, pre, "\"" + obj + "\""); 
		}
	}

	private void packUrlObj(String obj) {
		obj = "<" + obj.trim() + ">";
		insert(sub, pre, obj);
	}

	public void insert(String sub, String pre, String obj) {
		PreAndObj po = new PreAndObj(pre, obj);
		if (preObjs.contains(po)) {
			return;
		}
		
		try {
			statement.setString(1, sub);
			statement.setString(2, pre);
			statement.setString(3, obj);
			statement.addBatch();
			preObjs.add(po);
			
			System.out.print("Inserted " + ++insertCount + " RDF triplets\r");
			if(++batchSize == maxBatchSize){
				System.out.print("Committing...                        \r");
				statement.executeBatch();
				statement.clearBatch();
				conn.commit();
				batchSize = 0;
			}
		}
		catch (SQLException e) {
			throw new RuntimeException("Exception occurred during data insertion: " + e.getMessage());
		}
	}
  
    public void characters(char[] ch, int start, int length) {
        if (isTitle) {
            for (int i = start; i < start + length; i++) {
                pageName += ch[i];
            }
            pageName = convertHtmlReservedChars(pageName);
            count++;
            pageName = pageName.trim();
            preObjs = new HashSet<PreAndObj>();
        }
        if (isInfoboxName) {
            for (int i = start; i < start + length; i++) {
                infoboxName += ch[i];
            }
        }
        if (isProperty) {
            for (int i = start; i < start + length; i++) {
                propertyName += ch[i];

            }
        }
        if (isValue) {
            for (int i = start; i < start + length; i++) {
                entryValue += ch[i];
            }
            entryValue = convertHtmlReservedChars(entryValue);
        }
    }

    private static String convertHtmlReservedChars(String inputStr) {
		String outputStr = new String(inputStr);
		outputStr = outputStr.replaceAll("&quot;", "\"");
		outputStr = outputStr.replaceAll("&apos;", "'");
		outputStr = outputStr.replaceAll("&amp;", "&");
		outputStr = outputStr.replaceAll("&lt;", "<");
		outputStr = outputStr.replaceAll("&gt;", ">");
		outputStr = outputStr.replaceAll("&nbsp;", " ");
		
		outputStr = outputStr.trim();
		return outputStr;
	}
    
    public void endDocument(){
    	try {
    		System.out.print("Committing...                        \r");
    		statement.executeBatch();
    		statement.clearBatch();
			conn.commit();
			System.out.print("Creating indices...\r");
			stat.executeUpdate("CREATE INDEX index_sub ON triplets (subject);");
		    stat.executeUpdate("CREATE INDEX index_pre ON triplets (predicate);");
		    System.out.print("Committing...                        \r");
		    conn.commit();
			conn.close();
			
			if (insertCount > 0) {
				System.out.println();
			}
			System.out.println("Process completed. " + insertCount + " RDF triplets imported.");
		} catch (SQLException e) {
			throw new RuntimeException("Exception occurred during data insertion: " + e.getMessage());
		}
	}
}