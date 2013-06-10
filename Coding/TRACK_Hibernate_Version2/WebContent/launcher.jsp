<!-- 
Title: Distributed Database Query Engine Service (DDQES)
Description: The main interface where user enter query and view the result
Author: Ng Yi Ying
Date Created: 6 June, 2013
Date Modified: 6 June, 2013
 -->
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"
    import="java.util.Enumeration"
    import="track.hibernate.util.SQLProcesser"
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link href="css/style.css" rel="stylesheet" />
<script type="text/javascript" src="script/script.js"></script>
<title>Distributed Database Query Engine Service (DDQES)</title>
</head>
<body>
<%
	String output = "", errorMessage = "";
	Enumeration parameters = request.getParameterNames();
	if(parameters != null){
		while(parameters.hasMoreElements()){
			if(parameters.nextElement().toString().equalsIgnoreCase("sqlQuery")){
				SQLProcesser sqlProcesser = new SQLProcesser(request.getParameter("sqlQuery"));
				String[] returnedValue = sqlProcesser.validateQuery();
				if(returnedValue[0].equalsIgnoreCase("true")){
					
				}else{
					errorMessage = "<span style = \"color: red;\">Query: " + sqlProcesser.getUserQuery();
					errorMessage += "<br/>Error: " + returnedValue[1] + "</span><br/>";	
				}
			}
		}
	}
	out.println(output);
%>
<div id="header">
<a href="launcher.jsp"><img src="images/header.png" alt="Search Engine Collection" title ="Search Engine Collection" style="text-align:center;" /></a>
</div>
<hr color="black" />
<div id="query">
<br/>Submit your SQL query!<br/>
<form name="queryForm" id="queryForm" method="post" action="launcher.jsp">
<input name = "sqlQuery" type = "text" size="80"/>
<input type="submit"  name="btnSubmit" value="Submit"/><br/>
<% out.println(errorMessage); %>
<br/>
</form>
</div>
<br/>
<div id="body">
<div id="returnResult">
<div class="titlebar" onclick="showHide('resultSetTitle', 'resultSetContent', 'Result Set');">
<b id="resultSetTitle">[ - ] Result Set</b>
</div><!-- end of title bar -->
<div id="resultSetContent">
<table>
<tr>
<th>Customer ID</th>
<th>Customer Name</th>
</tr>
<tr>
<td>2</td>
<td>Jackie Chan</td>
</tr>
<tr>
<td>3</td>
<td>John Smith</td>
</tr>
</table>
</div><!-- end of resultSetContent -->
</div><!-- end of returnResult -->
</div><!-- end of body -->
<p style="clear: both;"></p>

<div id="body">
<div id="queryResult">
<div class="titlebar" onclick="showHide('processingDetailsTitle', 'processingDetailsContent', 'Processing Details');">
<b id="processingDetailsTitle">[ - ] Processing Details</b>
</div><!-- end of title bar -->
<div id="processingDetailsContent">
<div id="original">
<p class="title">Initial General Query Tree</p>
<p class="odd">SELECT * FROM Customer;</p>
<p class="even">SELECT * FROM Customer;</p>
<p class="odd">SELECT * FROM Customer;</p>
<p class="even">SELECT * FROM Customer;</p>
</div>
<div id="optimized">
<p class="title">Optimized Query Tree</p>
<p class="odd">SELECT * FROM Customer;</p>
<p class="even">SELECT * FROM Customer;</p>
<p class="odd">SELECT * FROM Customer;</p>
<p class="even">SELECT * FROM Customer;</p>
</div>
</div><!-- end of processingDetailsContent -->
<p style="clear: both;"></p>
</div><!-- end of result -->
</div><!-- end of body -->
<p style="clear: both;"></p>

<div id="body">
<div id="costResult">
<div class="titlebar" onclick="showHide('costDetailsTitle', 'costDetailsContent', 'Cost Calculation Details');">
<b id="costDetailsTitle">[ - ] Cost Calculation Details</b>
</div><!-- end of title bar -->
<div id="costDetailsContent">
<div id="original">
<p class="title">Initial General Query Tree</p>
<p class="odd">SELECT * FROM Customer;</p>
<p class="even">SELECT * FROM Customer;</p>
<p class="odd">SELECT * FROM Customer;</p>
<p class="even">SELECT * FROM Customer;</p>
</div>
<div id="optimized">
<p class="title">Optimized Query Tree</p>
<p class="odd">SELECT * FROM Customer;</p>
<p class="even">SELECT * FROM Customer;</p>
<p class="odd">SELECT * FROM Customer;</p>
<p class="even">SELECT * FROM Customer;</p>
</div>
</div><!-- end of costDetailsContent -->
</div><!-- end of result -->
</div><!-- end of body -->
</body>
</html>