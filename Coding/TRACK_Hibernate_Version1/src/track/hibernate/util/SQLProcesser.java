
/*
 * Title: Distributed Database Query Engine Service (DDQES)
 * Description: The class serves to handle query processing
 * Author: Ng Yi Ying
 * Data Created: 6 June, 2013
 * Data Modified: 7 June, 2013
 */
package track.hibernate.util;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.hibernate.Session;
import org.hibernate.exception.GenericJDBCException;
import org.hibernate.internal.SessionImpl;

import track.ddqes.application.TrackConstants;;

public class SQLProcesser implements TrackConstants{
	private String userQuery;
	private static Session session;
	private String[] queryIndex;
	private HashMap<String, Integer> keywordIndex;
	//private HashMap<String, HashMap<String, HashMap<String, String>>> decodedSQL;
	private HashMap<String, String> decodedTable;
	private HashMap<String, HashMap<String, String>> decodedColumn;
	private HashMap<String, ArrayList<String>> decodedCondition;
	private HashMap<String, String> decodedJoin;
	private HashMap<String, ArrayList<String>> decodedGroupBy;
	private HashMap<String, ArrayList<String>> decodedHaving;
	private HashMap<String, ArrayList<String>> decodedOrderBy;
	
	private HashMap<String, ArrayList<String>> tableStructure;
	private HashMap<String, ArrayList<String>> primaryKeys;
	
	public static void main(String[] args){
		System.out.print("Enter query: ");
		String q = new Scanner(System.in).nextLine();
		SQLProcesser sqlProcesser = new SQLProcesser(q);
		System.out.println(sqlProcesser.decomposeQuery());
	}
	
	public SQLProcesser(String q){
		// initialization
		//decodedSQL = new HashMap<String, HashMap<String, HashMap<String, String>>>();
		keywordIndex = new HashMap<String, Integer>();
		tableStructure = new HashMap<String, ArrayList<String>>();
		primaryKeys = new HashMap<String, ArrayList<String>>();
		decodedTable = new HashMap<String, String>();
		decodedColumn = new HashMap<String, HashMap<String,String>>();
		decodedCondition = new HashMap<String, ArrayList<String>>();
		decodedJoin = new HashMap<String, String>();
		decodedGroupBy = new HashMap<String, ArrayList<String>>();
		decodedHaving = new HashMap<String, ArrayList<String>>();
		decodedOrderBy = new HashMap<String, ArrayList<String>>();
		this.userQuery = q;
		queryIndex = userQuery.split(" ");
		for(int i = 0; i < queryIndex.length; i++){
			if(queryIndex[i].equalsIgnoreCase("select"))
				keywordIndex.put("select", i);
			else if(queryIndex[i].equalsIgnoreCase("from"))
				keywordIndex.put("from", i);
			else if(queryIndex[i].equalsIgnoreCase("where"))
				keywordIndex.put("where", i);
			else if(queryIndex[i].equalsIgnoreCase("group"))
				keywordIndex.put("group", i);
			else if(queryIndex[i].equalsIgnoreCase("having"))
				keywordIndex.put("having", i);
			else if(queryIndex[i].equalsIgnoreCase("order"))
				keywordIndex.put("order", i);
		}
		getTableStructure();
		getPrimaryKeys();
	}
	
	public void setUserQuery(String q){
		this.userQuery = q;
	}
	
	public String getUserQuery(){
		return this.userQuery;
	}
	
	public String decomposeQuery(){
		String decomposeQuery = "";

		// process table
		if(keywordIndex.containsKey("where")){
			processTable(keywordIndex.get("from"), keywordIndex.get("where"));
		}else{
			processTable(keywordIndex.get("from"), queryIndex.length);
		}
		
		// process column
		processColumn(keywordIndex.get("select"), keywordIndex.get("from"));

		// process where, group by, having, order by
		if(keywordIndex.containsKey("where") && keywordIndex.containsKey("group")){
			processWhereCondition(keywordIndex.get("where"), keywordIndex.get("group"));
			
			if(keywordIndex.containsKey("group") && keywordIndex.containsKey("having")){
				processGroupCondition(keywordIndex.get("group"), keywordIndex.get("having"));
			
				if(keywordIndex.containsKey("having") && keywordIndex.containsKey("order")){
					// where, group by, having, order by
					processHavingCondition(keywordIndex.get("having"), keywordIndex.get("order"));
					processOrderCondition(keywordIndex.get("order"), queryIndex.length);
				}else if(keywordIndex.containsKey("having")){
					// where, group by, having, no order by
					processHavingCondition(keywordIndex.get("having"), queryIndex.length);
				}
			}else if(keywordIndex.containsKey("group") && keywordIndex.containsKey("order")){
				// where, group by, no having, order by
				processGroupCondition(keywordIndex.get("group"), keywordIndex.get("order"));
			}else{
				// where, group by, no having, no order by
				processGroupCondition(keywordIndex.get("group"), queryIndex.length);
			}
		}else if(keywordIndex.containsKey("where") && keywordIndex.containsKey("having")){
			processWhereCondition(keywordIndex.get("where"), keywordIndex.get("having"));
			
			if(keywordIndex.containsKey("having") && keywordIndex.containsKey("order")){
				// where, no group by, having, order by
				processHavingCondition(keywordIndex.get("having"), keywordIndex.get("order"));
				processOrderCondition(keywordIndex.get("order"), queryIndex.length);
			}else{
				// where, no group by, having, no order by
				processHavingCondition(keywordIndex.get("having"), queryIndex.length);
			}
		}else if(keywordIndex.containsKey("where") && keywordIndex.containsKey("order")){
			// where, no group by, no having, order by
			processWhereCondition(keywordIndex.get("where"), keywordIndex.get("order"));
			processOrderCondition(keywordIndex.get("order"), queryIndex.length);
		}else if(keywordIndex.containsKey("where")){
			// where, no group by, no having, no order by
			processWhereCondition(keywordIndex.get("where"), queryIndex.length);
		}
		
		// print join condition
		System.out.println("Join condition: ");
		for(String tables: decodedJoin.keySet()){
			System.out.println("Join tables: " + tables + ", " + decodedJoin.get(tables));
		}
		
		// analysis
		
		// generate decomposeQuery
		for(String table: decodedTable.keySet()){
			String singleQuery = "SELECT ";
			for(String column: decodedColumn.get(table).keySet()){
				//singleQuery += decodedColumn.get(table).get(column) + ", ";
				singleQuery += column + ", ";
			}
			singleQuery = singleQuery.substring(0, singleQuery.lastIndexOf(","));
			if(decodedTable.get(table).equalsIgnoreCase(table))
				singleQuery += " FROM " + decodedTable.get(table);
			else
				singleQuery += " FROM " + decodedTable.get(table) + " AS " +  table;
			
			if(decodedCondition.get(table) != null || decodedGroupBy.get(table) != null 
					|| decodedHaving.get(table) != null || decodedOrderBy.get(table) != null){
				if(decodedCondition.get(table) != null){
					singleQuery += " WHERE";
					for(int i = 0; i < decodedCondition.get(table).size(); i++){
						if(i == 0 && decodedCondition.get(table).get(i).substring(0, 3).equalsIgnoreCase("or ")){
							singleQuery += " " + decodedCondition.get(table).get(i).substring(3, 
									decodedCondition.get(table).get(i).length());
						}else if(i == 0 && decodedCondition.get(table).get(i).substring(0, 4).equalsIgnoreCase("and ")){
							singleQuery += " " + decodedCondition.get(table).get(i).substring(4, 
									decodedCondition.get(table).get(i).length());
						}else
							singleQuery += " " + decodedCondition.get(table).get(i);
					}
				}
				
				if(decodedGroupBy.get(table) != null){
					singleQuery += " GROUP BY";
					for(int i = 0; i < decodedGroupBy.get(table).size(); i++){
						singleQuery += " " + decodedGroupBy.get(table).get(i) + ",";
					}
					singleQuery = singleQuery.substring(0, singleQuery.lastIndexOf(","));
				}
				
				if(decodedHaving.get(table) != null){
					singleQuery += " HAVING";
					for(int i = 0; i < decodedHaving.get(table).size(); i++){
						singleQuery += " " + decodedHaving.get(table).get(i) + ",";
					}
					singleQuery = singleQuery.substring(0, singleQuery.lastIndexOf(","));
				}
				
				if(decodedOrderBy.get(table) != null){
					singleQuery += " ORDER BY";
					for(int i = 0; i < decodedOrderBy.get(table).size(); i++){
						singleQuery += " " + decodedOrderBy.get(table).get(i) + ",";
					}
					singleQuery = singleQuery.substring(0, singleQuery.lastIndexOf(","));
				}
			}
			decomposeQuery += singleQuery + "; ";
		}
		
		return decomposeQuery;
	}
	
	public void processColumn(int startIndex, int endIndex){
		String processPart = "";
		for(int i = startIndex + 1; i < endIndex; i++){
			processPart += queryIndex[i] + " ";
		}
		
		String[] columns = processPart.trim().split(",");
		if(columns.length == 1 && columns[0].trim().equalsIgnoreCase("*")){
			for(String tableAlias : decodedTable.keySet()){
				if(decodedColumn.get(tableAlias) == null)
					decodedColumn.put(tableAlias, new HashMap<String,String>());
				for(int column = 0; column < tableStructure.get(decodedTable.get(tableAlias)).size(); column++){
					decodedColumn.get(tableAlias).put(tableAlias + "." + tableStructure.get(decodedTable.get(tableAlias)).get(column), 
							tableAlias + "." + tableStructure.get(decodedTable.get(tableAlias)).get(column));
				}
			}
		}else{
			for(int i = 0; i < columns.length; i++){
				HashMap<String, String> item = new HashMap<String, String>();
				String tableAlias = "";
				if(columns[i].toLowerCase().contains(" as ")){
					String[] column = columns[i].toLowerCase().split(" as ");
					tableAlias = getAlias(getTable(column[0].trim()));
					item.put(tableAlias + "." + column[0].trim(), column[1].trim().replace("\"", ""));
				}else{
					int aliasIndex = columns[i].indexOf("\"");
					if(aliasIndex > -1){
						tableAlias = getAlias(getTable(columns[i].substring(0, aliasIndex).trim()));
						item.put(tableAlias + "." + columns[i].substring(0, aliasIndex).trim(), 
								columns[i].substring(aliasIndex + 1, columns[i].length() - 1).replace("\"", "").trim());
					}else{
						// no column alias
						if(columns[i].contains(".")){
							String tableName = columns[i].substring(0, columns[i].indexOf(".")).trim();
							if(decodedTable.containsKey(tableName)){
								item.put(columns[i].trim(), columns[i].trim());
							}else{
								item.put(columns[i].replace(tableName, getAlias(tableName)), columns[i].trim());
							}
							tableAlias = getAlias(tableName);
						}else{
							tableAlias = getAlias(getTable(columns[i].trim()));
							item.put(tableAlias + "." + columns[i].trim(), columns[i].trim());
						}
					}
				}
				if(decodedColumn.get(tableAlias) == null)
					decodedColumn.put(tableAlias, new HashMap<String,String>());
				decodedColumn.get(tableAlias).putAll(item);
			}
		}
		
		// print item
		System.out.println("Select statement:");
		for(String table: decodedColumn.keySet()){
			for(String column: decodedColumn.get(table).keySet()){
				System.out.println("Table: " + table + ", Column: " + column
						+ ", Alias: " + decodedColumn.get(table).get(column));
			}
		}
	}
	
	public void processTable(int startIndex, int endIndex){
		String processPart = "";
		for(int i = startIndex + 1; i < endIndex; i++){
			processPart += queryIndex[i] + " ";
		}
		String[] tables = processPart.split(",");
		for(int i = 0; i < tables.length; i++){
			if(tables[i].toLowerCase().contains(" as ")){
				String[] table = tables[i].toLowerCase().split(" as ");
				decodedTable.put(table[1].replace("\"", "").trim(), table[0].trim());
			}else{
				int aliasIndex = tables[i].indexOf("\"");
				if(aliasIndex > -1){
					decodedTable.put(tables[i].substring(aliasIndex + 1, tables[i].length() - 1).replace("\"", "").trim(), 
							tables[i].substring(0, aliasIndex - 1).trim());
				}else{
					decodedTable.put(tables[i].trim(), tables[i].trim());
				}
			}
		}
		
		// print item
		System.out.println("Table:");
		for(String key: decodedTable.keySet()){
			System.out.println("Table: " + decodedTable.get(key) + ", Alias: " + key);
		}
	}
	
	public void processHavingCondition(int startIndex, int endIndex){
		String processPart = "";
		for(int i = startIndex + 1; i < endIndex; i++){
			processPart += queryIndex[i] + " ";
		}
		processSection(processPart, PROCESS_HAVING);
		printItem(decodedHaving, PROCESS_HAVING);
	}

	public void processGroupCondition(int startIndex, int endIndex){
		String processPart = "";
		for(int i = startIndex + 2; i < endIndex; i++){
			processPart += queryIndex[i] + " ";
		}
		processSection(processPart, PROCESS_GROUP);
		printItem(decodedGroupBy, PROCESS_GROUP);
	}
	
	public void processOrderCondition(int startIndex, int endIndex){
		String processPart = "";
		for(int i = startIndex + 2; i < endIndex; i++){
			processPart += queryIndex[i] + " ";
		}
		processSection(processPart, PROCESS_ORDER);
		printItem(decodedOrderBy, PROCESS_ORDER);
	}
	
	public void processWhereCondition(int startIndex, int endIndex){
		String processPart = "";
		for(int i = startIndex + 1; i < endIndex; i++){
			processPart += queryIndex[i] + " ";
		}
		processSection(processPart, PROCESS_CONDITION);
		printItem(decodedCondition, PROCESS_CONDITION);
	}
	
	public void processSection(String processPart, int type){
		ArrayList<String> item = new ArrayList<String>();
		switch(type){
			case PROCESS_COLUMN:
				break;
			case PROCESS_TABLE:
				break;
			case PROCESS_CONDITION:
				String[] andConditions = processPart.toLowerCase().split(" and ");
				for(int i = 0; i < andConditions.length; i++){
					if(andConditions[i].toLowerCase().contains(" or ")){
						String[] orConditions = andConditions[i].toLowerCase().split(" or ");
						for(int j = 0; j < orConditions.length; j++){
							orConditions[j] = orConditions[j].replace("(", "").replace(")", "");
							if(i == 0)
								processConditions(orConditions[j],"", item);
							else
								processConditions(orConditions[j], "or", item);
						}
					}else{
						andConditions[i] = andConditions[i].replace("(", "").replace(")", "");
						if(i == 0)
							processConditions(andConditions[i], "", item);
						else
							processConditions(andConditions[i], "and", item);
					}
				}
				break;
			case PROCESS_GROUP:
				String gTable = "";
				String[] gColumn = processPart.trim().split("\\,");
				if(gColumn.length > 1){
					for(int i = 0; i < gColumn.length; i++){
						if(gColumn[i].contains(".")){
							if(gColumn[i].indexOf(".") - 1 == 0)
								gTable = String.valueOf(gColumn[i].charAt(0));
							else
								gTable = gColumn[i].substring(0, gColumn[i].indexOf(".")).trim();
						}else{
							gTable = getTable(gColumn[i].trim());
						}
						if(decodedTable.containsKey(gTable)){
							gColumn[i] = gTable + "." + gColumn[i];
							if(decodedGroupBy.get(gTable) == null){
								decodedGroupBy.put(gTable, new ArrayList<String>());
							}
							decodedGroupBy.get(gTable).add(gColumn[i].trim());
						}else{
							String gAlias = getAlias(gTable);
							if(decodedGroupBy.get(gAlias) == null){
								decodedGroupBy.put(gTable, new ArrayList<String>());
							}
							if(decodedGroupBy.get(gAlias) == null)
								decodedGroupBy.put(gAlias, new ArrayList<String>());
							decodedGroupBy.get(gAlias).add(gAlias + "." + gColumn[i].trim());
						}
					}
				}else{
					if(processPart.contains(".")){
						if(processPart.indexOf(".") - 1 == 0)
							gTable = String.valueOf(processPart.charAt(0));
						else
							gTable = processPart.substring(0, processPart.indexOf(".")).trim();
					}else{
						gTable = getTable(processPart.trim());
					}
					if(decodedTable.containsKey(gTable)){
						processPart = gTable + "." + processPart;
						if(decodedGroupBy.get(gTable) == null){
							decodedGroupBy.put(gTable, new ArrayList<String>());
						}
						decodedGroupBy.get(gTable).add(processPart.trim());
					}else{
						String gAlias = getAlias(gTable);
						if(decodedGroupBy.get(gAlias) == null){
							decodedGroupBy.put(gAlias, new ArrayList<String>());
						}
						decodedGroupBy.get(gAlias).add(gAlias + "." + processPart.trim());
					}
				}
				break;
			case PROCESS_HAVING:
				String hTable = "";
				String[] hColumns = processPart.split("\\,");
				if(hColumns.length > 1){
					for(int i = 0; i < hColumns.length; i++){
						String hColumn = hColumns[i].substring(hColumns[i].indexOf("(") + 1, hColumns[i].indexOf(")")).trim();
						if(hColumn.contains(".")){
							if(hColumn.indexOf(".") - 1 == 0){
								hTable = String.valueOf(hColumn.charAt(0));
							}else
								hTable = hColumn.substring(0, hColumn.indexOf(".")).trim();
						}else{
							hTable = getTable(hColumn.trim());
						}
						if(decodedTable.containsKey(hTable)){
							processPart = hColumns[i].replace(hColumn, hTable + "." + hColumn).trim();
							if(decodedHaving.get(hTable) == null){
								decodedHaving.put(hTable, new ArrayList<String>());
							}
							decodedHaving.get(hTable).add(hColumns[i].trim());
						}else{
							String hAlias = getAlias(hTable);
							if(decodedHaving.get(hAlias) == null){
								decodedHaving.put(hAlias, new ArrayList<String>());
							}
							decodedHaving.get(hAlias).add(hColumns[i].replace(hColumn, hAlias + "." + hColumn).trim());
						}
					}
				}else{
					String hColumn = processPart.substring(processPart.indexOf("(") + 1, processPart.indexOf(")")).trim();
					if(hColumn.contains(".")){
						if(hColumn.indexOf(".") - 1 == 0){
							hTable = String.valueOf(hColumn.charAt(0));
						}else
							hTable = hColumn.substring(0, hColumn.indexOf(".")).trim();
					}else{
						hTable = getTable(hColumn.trim());
					}
					if(decodedTable.containsKey(hTable)){
						processPart = processPart.replace(hColumn, hTable + "." + hColumn).trim();
						if(decodedHaving.get(hTable) == null){
							decodedHaving.put(hTable, new ArrayList<String>());
						}
						decodedHaving.get(hTable).add(processPart.trim());
					}else{
						String hAlias = getAlias(hTable);
						if(decodedHaving.get(hAlias) == null){
							decodedHaving.put(hAlias, new ArrayList<String>());
						}
						decodedHaving.get(hAlias).add(processPart.replace(hColumn, hAlias + "." + hColumn).trim());
					}
				}
				break;
			case PROCESS_ORDER:
				String oTable = "";
				String[] orderParts = processPart.split(",");
				if(orderParts.length > 1){
					for(int i = 0; i < orderParts.length; i++){
						if(orderParts[i].contains(".")){
							if(orderParts[i].indexOf(".") - 1 == 0)
								oTable = String.valueOf(orderParts[i].charAt(0));
							else
								oTable = orderParts[i].substring(0, orderParts[i].indexOf(".") - 1).trim();
						}else{
							// check if there is ASC, DESC
							String[] oColumn = orderParts[i].trim().split(" ");
							oTable = getTable(oColumn[0]);
						}
						if(decodedTable.containsKey(oTable)){
							processPart = oTable + "." + processPart;
							if(decodedOrderBy.get(oTable) == null){
								decodedOrderBy.put(oTable, new ArrayList<String>());
							}
							decodedOrderBy.get(oTable).add(orderParts[i].trim());
						}else{
							String oAlias = getAlias(oTable);
							if(decodedOrderBy.get(oAlias) == null){
								decodedOrderBy.put(oAlias, new ArrayList<String>());
							}
							decodedOrderBy.get(oAlias).add(oAlias + "." + orderParts[i].trim());
						}
					}
				}else{
					if(processPart.contains(".")){
						if(processPart.indexOf(".") - 1 == 0)
							oTable = String.valueOf(processPart.charAt(0));
						else
							oTable = processPart.substring(0, processPart.indexOf(".") - 1).trim();
					}else{
						// check if there is ASC, DESC
						String[] oColumn = processPart.trim().split(" ");
						oTable = getTable(oColumn[0]);
					}
					if(decodedTable.containsKey(oTable)){
						processPart = oTable + "." + processPart;
						if(decodedOrderBy.get(oTable) == null){
							decodedOrderBy.put(oTable, new ArrayList<String>());
						}
						decodedOrderBy.get(oTable).add(processPart.trim());
					}else{
						String oAlias = getAlias(oTable);
						if(decodedOrderBy.get(oAlias) == null){
							decodedOrderBy.put(oAlias, new ArrayList<String>());
						}
						decodedOrderBy.get(oAlias).add(oAlias + "." + processPart.trim());
					}
				}
				break;
				
			default:
				break;
		}
	}
	
	public void processConditions(String section, String type, ArrayList<String> item){
		if(section.indexOf(".") > -1){
			String firstTable = section.substring(0, section.indexOf(".")).trim();
			// contain indication of which table does the column belong to, i.e book.bid > 200001
			// check if it's condition or join statement
			if((section.indexOf("=") > -1 && !(section.indexOf(">") > -1) && !(section.indexOf("<") > -1)) 
					&& !(section.toLowerCase().indexOf("not ") > -1)){
				String secondPart = section.substring(section.indexOf("=") + 1, 
						section.length()).trim();
				if(secondPart.contains(".")){
					// join condition
					String secondTable = secondPart.substring(0, secondPart.indexOf(".")).trim();
					if(!firstTable.equalsIgnoreCase(secondTable)){
						decodedJoin.put(firstTable + "," + secondTable, section.trim());
						// add id to columns for later join (if not being selected in select statement)
						addPrimaryKeyToTable(decodedTable.get(firstTable), decodedTable.get(secondTable));
					}
				}else{
					// check if the secondPart is a table column or 
					String secondTable = getAlias(getTable(secondPart));
					if(secondTable == null){
						// where condition
						if(!decodedTable.containsKey(firstTable)){
							firstTable = getAlias(firstTable);
						}
						if(decodedCondition.get(firstTable) == null){
							decodedCondition.put(firstTable, new ArrayList<String>());
						}
						decodedCondition.get(firstTable).add(type + " " + section.trim());

					}else{
						// join condition
						if(!firstTable.equalsIgnoreCase(secondTable)){
							if(!decodedTable.containsKey(firstTable)){
								firstTable = getAlias(firstTable);
							}
							if(!decodedTable.containsKey(secondTable)){
								firstTable = getAlias(secondTable);
							}
							decodedJoin.put(firstTable + "," + secondTable, 
									section.replace(secondPart, secondTable + "." + secondPart).trim());
							
							// add id to columns for later join (if not being selected in select statement)
							addPrimaryKeyToTable(decodedTable.get(firstTable), decodedTable.get(secondTable));
						}
					}
				}
			}else{
				firstTable = firstTable.replace("not ", "").trim();
				if(!decodedTable.containsKey(firstTable)){
					firstTable = getAlias(firstTable);
				}
				
				if(decodedCondition.get(firstTable) == null)
					decodedCondition.put(firstTable, new ArrayList<String>());
				decodedCondition.get(firstTable).add(type + " " + section.trim());
			}
		}else{
			// no indication of which table does the column belonged to, manual mapping
			String[] conditionParts = section.trim().split(" ");
			String table = "";
			if(conditionParts[0].trim().equalsIgnoreCase("not")){
				table = getAlias(getTable(conditionParts[1]));
				section = section.replace(conditionParts[1], table + "." + conditionParts[1]);
			}
			else{
				table = getAlias(getTable(conditionParts[0]));
				section = table + "." + section;
			}
			if(decodedCondition.get(table) == null)
				decodedCondition.put(table, new ArrayList<String>());
			decodedCondition.get(table).add(type + " " + section.trim());
		}
	}
	
	public void addPrimaryKeyToTable(String firstTable, String secondTable){
		for(int k = 0; k < primaryKeys.get(firstTable).size(); k++){
			if(decodedColumn.get(firstTable) == null)
				decodedColumn.put(firstTable, new HashMap<String,String>());
			
			if(decodedColumn.get(firstTable).get(firstTable + "." + primaryKeys.get(firstTable).get(k)) == null){
				decodedColumn.get(firstTable).put(firstTable + "." + primaryKeys.get(firstTable).get(k), 
						firstTable + "." + primaryKeys.get(firstTable).get(k));
			}
		}
		
		for(int k = 0; k < primaryKeys.get(secondTable).size(); k++){
			if(decodedColumn.get(secondTable) == null)
				decodedColumn.put(secondTable, new HashMap<String,String>());
			
			if(decodedColumn.get(secondTable).get(secondTable + "." + primaryKeys.get(secondTable).get(k)) == null){
				decodedColumn.get(secondTable).put(secondTable + "." + primaryKeys.get(secondTable).get(k), 
						secondTable + "." + primaryKeys.get(secondTable).get(k));
			}
		}
	}
	
	public String getTable(String column){
		String belongedTable = null;
		for(String table: tableStructure.keySet()){
			if(tableStructure.get(table).contains(column) && decodedTable.containsKey(table)){
				belongedTable = table;
				break;
			}
		}
		return belongedTable;
	}
	
	public String getAlias(String tableName){
		String alias = null;
		for(String table: decodedTable.keySet()){
			if(decodedTable.get(table).equalsIgnoreCase(tableName)){
				alias = table;
				break;
			}
		}
		return alias;
	}
	
	public void printItem(HashMap<String, ArrayList<String>> item, int type){
		switch(type){
			case PROCESS_COLUMN:
				//
				return;
			case PROCESS_TABLE:
				//
				return;
			case PROCESS_CONDITION:
				System.out.println("Where clause:");
				break;
			case PROCESS_GROUP:
				System.out.println("Group by:");
				break;
			case PROCESS_HAVING:
				System.out.println("Having:");
				break;
			case PROCESS_ORDER:
				System.out.println("Order by:");
				break;
			default:
		}
		
		for(String key: item.keySet()){
			for(int i = 0; i < item.get(key).size(); i++){
				System.out.println("Table: " + key + ", Condition: " + item.get(key).get(i));
			}
		}
	}
	
	public String[] validateQuery(){
		String[] returnedValue = new String[2];
		returnedValue[0] = "true";
		returnedValue[1] = "";
		if(session == null || !session.isOpen())
			session = HibernateUtil.getSessionFactory().openSession();
		Connection connection = ((SessionImpl)session).connection();
		try {
			connection.prepareStatement(userQuery);
		} catch (SQLException e) {
			//e.printStackTrace();
			returnedValue[0] = "false";
			returnedValue[1] = e.getMessage();
		} catch(GenericJDBCException e){
			//e.printStackTrace();
			returnedValue[0] = "false";
			returnedValue[1] = e.getMessage();
		}
		return returnedValue;
	}
	
	public void closeDatabaseConnection(){
		session.close();
		HibernateUtil.shutdown();
	}
	
	public void getTableStructure(){
		String[] pColumns = PUBLISHER_TABLE[1].split(",");
		ArrayList<String> pTableColumns = new ArrayList<String>();
		for(int i = 0; i < pColumns.length; i++){
			pTableColumns.add(pColumns[i]);
		}
		tableStructure.put("publisher", pTableColumns);
		
		String[] cColumns = CUSTOMER_TABLE[1].split(",");
		ArrayList<String> cTableColumns = new ArrayList<String>();
		for(int i = 0; i < cColumns.length; i++){
			cTableColumns.add(cColumns[i]);
		}
		tableStructure.put("customer", cTableColumns);
		
		String[] bColumns = BOOK_TABLE[1].split(",");
		ArrayList<String> bTableColumns = new ArrayList<String>();
		for(int i = 0; i < bColumns.length; i++){
			bTableColumns.add(bColumns[i]);
		}
		tableStructure.put("book", bTableColumns);
		
		String[] oColumns = ORDERS_TABLE[1].split(",");
		ArrayList<String> oTableColumns = new ArrayList<String>();
		for(int i = 0; i < oColumns.length; i++){
			oTableColumns.add(oColumns[i]);
		}
		tableStructure.put("orders", oTableColumns);
	}
	
	public void getPrimaryKeys(){
		String[] pColumns = PUBLISHER_TABLE[0].split(",");
		ArrayList<String> pPrimaryKeys = new ArrayList<String>();
		for(int i = 0; i < pColumns.length; i++){
			pPrimaryKeys.add(pColumns[i]);
		}
		primaryKeys.put("publisher", pPrimaryKeys);
		
		String[] cColumns = CUSTOMER_TABLE[0].split(",");
		ArrayList<String> cPrimaryKeys = new ArrayList<String>();
		for(int i = 0; i < cColumns.length; i++){
			cPrimaryKeys.add(cColumns[i]);
		}
		primaryKeys.put("customer", cPrimaryKeys);
		
		String[] bColumns = BOOK_TABLE[0].split(",");
		ArrayList<String> bPrimaryKeys = new ArrayList<String>();
		for(int i = 0; i < bColumns.length; i++){
			bPrimaryKeys.add(bColumns[i]);
		}
		primaryKeys.put("book", bPrimaryKeys);
		
		String[] oColumns = ORDERS_TABLE[0].split(",");
		ArrayList<String> oPrimaryKeys = new ArrayList<String>();
		for(int i = 0; i < oColumns.length; i++){
			oPrimaryKeys.add(oColumns[i]);
		}
		primaryKeys.put("orders", oPrimaryKeys);
	}
}
