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
<b>Result Set</b><br/><br/>
<table>
<tr>
<td>Customer ID</td>
<td>Customer Name</td>
</tr>
<tr>
<td>2</td>
<td>Jackie Chan</td>
</tr>
</table>
</div><!-- end of returnResult -->
</div><!-- end of body -->

<p style="clear: both;"></p>
<div id="body">
<div id="queryResult">
<p><b>Processing Details</b><br/>
</p>
<div id="original">
<b>Initial General Query Tree</b><br/><br/>
<p class="odd">SELECT * FROM Customer;</p>
<p class="even">SELECT * FROM Customer;</p>
<p class="odd">SELECT * FROM Customer;</p>
<p class="even">SELECT * FROM Customer;</p>
</div>
<div id="optimized">
<b>Optimized Query Tree</b><br/><br/>
<p class="odd">SELECT * FROM Customer;</p>
<p class="even">SELECT * FROM Customer;</p>
<p class="odd">SELECT * FROM Customer;</p>
<p class="even">SELECT * FROM Customer;</p>
</div>
</div><!-- end of result -->
</div><!-- end of body -->
<p style="clear: both;"></p><br/>
<div id="body">
<div id="costResult">
<p><b>Cost Calculation Details</b><br/>
</p>
<div id="original">
<b>Initial General Query Tree</b><br/><br/>
<p class="odd">SELECT * FROM Customer;</p>
<p class="even">SELECT * FROM Customer;</p>
<p class="odd">SELECT * FROM Customer;</p>
<p class="even">SELECT * FROM Customer;</p>
</div>
<div id="optimized">
<b>Optimized Query Tree</b><br/><br/>
<p class="odd">SELECT * FROM Customer;</p>
<p class="even">SELECT * FROM Customer;</p>
<p class="odd">SELECT * FROM Customer;</p>
<p class="even">SELECT * FROM Customer;</p>
</div>
</div><!-- end of result -->
</div><!-- end of body -->
</body>
</html>