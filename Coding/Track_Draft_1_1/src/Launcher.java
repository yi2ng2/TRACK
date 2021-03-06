/*
 * Team Name	: TRACK
 * Team Member	: Frederic Colin, Henry Loharja, Ng Yi Ying
 * Created on	: 17 March, 2013
 * Modified on	: 24 March, 2013 
 * Description	: Launching program to generate database structure based on dictionary
 */
import java.io.File;
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class Launcher {
	Document document;
    DocumentBuilder documentBuilder;
    DocumentBuilderFactory documentBuilderFactory;
    NodeList nodeList;
    File xmlInputFile;
    MySQLAccess dbHandler;
    
	public Launcher(){
		dbHandler = new MySQLAccess();
	}
	
	public void readXML(String filename){
        try
        {
            xmlInputFile = new File(filename);
            documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(xmlInputFile);
            nodeList = document.getElementsByTagName("*");
            document.getDocumentElement().normalize();
            
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
	}
	
	public void processXML(){
		//int siteID = 0;
		for (int index = 0; index < nodeList.getLength(); index++)
        {
            Node node = nodeList.item(index);
            if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                Element element = (Element) node;
                if(element.getTagName().trim() == "site"){
                	System.out.println("-----------");
                	performDatabaseAction("CREATE DATABASE IF NOT EXISTS site" + element.getAttribute("id") + ";");
                	performDatabaseAction("USE site" + element.getAttribute("id") + ";");
                	System.out.println("CREATE DATABASE IF NOT EXISTS site" + element.getAttribute("id") + ";");
                	System.out.println("USE site" + element.getAttribute("id") + ";");
            		//siteID = Integer.parseInt(element.getAttribute("id"));
            		/*
                	NamedNodeMap nodeMap = element.getAttributes();
                	for(int i = 0; i < nodeMap.getLength(); i++){
                		System.out.println(element.getTagName() + " has attribute " + nodeMap.item(i).getNodeName() + " with the value of " + element.getAttribute(nodeMap.item(i).getNodeName()));
                	}
                	*/
                }else if(element.getTagName().trim() == "table"){
                	performDatabaseAction(getCreateSQLStatement(element.getAttribute("name")));
                	/*
                	NamedNodeMap nodeMap = element.getAttributes();
                	for(int i = 0; i < nodeMap.getLength(); i++){
                		System.out.println(element.getTagName() + " has attribute " + nodeMap.item(i).getNodeName() + " with the value of " + element.getAttribute(nodeMap.item(i).getNodeName()));
                		System.out.println("In siteID: " + siteID);
                		System.out.println("value: " + element.getTextContent());
                	}
                	*/
                }
            }
        }
	}
	
	private String getCreateSQLStatement(String table){
		String sql = "CREATE TABLE IF NOT EXISTS " + table + " ";
		switch(table){
			case "publisher":
				sql += "(id integer PRIMARY KEY, name char(100), nation char(3)); ";
				break;
			
			case "book":
				sql += "(id integer PRIMARY KEY, title char(100), authors char(200), publisher_id integer, copies integer); ";
				break;
			
			case "customer":
				sql += "(id integer PRIMARY KEY, name char(25), rank integer); ";
				break;
			
			case "ordert":
				sql += "(customer_id integer, book_id integer, quantity integer); ";
				break;
				
			default:
				break;
		}
		System.out.println(sql);
		return sql;
	}
	
	public void performDatabaseAction(String query){
		try {
			//db.readDatabase("SELECT * FROM yyildcarddb.cdhd00");
			dbHandler.updateDatabase(query);
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void endProcess(){
		dbHandler.closeDatabase();
	}
	
	public static void main(String[] args){
		Launcher launcher = new Launcher();
		launcher.readXML("environment_variables.xml");
		launcher.processXML();
		launcher.endProcess();
	}

}

