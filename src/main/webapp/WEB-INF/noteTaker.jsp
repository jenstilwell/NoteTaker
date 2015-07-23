<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<html>
<head>
	<title>Note Taker</title>
</head>
<body>
	<h1>Note Taker</h1>
	
	<c:if test="${form.username == null}">
		<form:form method="POST" commandName="form" action="/NoteTaker/note/login">	
			
			<table>
				<tr>
					<td colspan="2">
						<form:errors path="username" cssStyle="color: #ff0000;"/>
						<c:if test="${form.message != null}">
							<span style="color:#ff0000;"><c:out value="${form.message}"/></span>
						</c:if>
					</td>
				</tr>
				<tr>
					<td>Username:</td>
					<td><form:input path="username" /></td>				
				</tr>
				<tr>
					<td>Password:</td>
					<td><form:input path="password" /></td>
				</tr>
				<tr>
					<td colspan="2">
						<input type="submit" name="submit" value="Submit">
					</td>
				</tr>
			</table>
		</form:form>
	</c:if>
	
	<c:if test="${form.username != null}">
	
		<h3>Welcome: ${form.username}</h3>
		
		<form:form method="POST" commandName="form" action="/NoteTaker/note/">	
			<form:hidden path="username"/>
			<form:hidden path="password"/>
				
			<table>
				<tr>
					<td colspan="2">
						<form:errors path="note" cssStyle="color: #ff0000;"/>
					</td>
				</tr>
				<tr>
					<td>Add Note:</td>
					<td><form:input path="note" /></td>				
				</tr>
				<tr>
					<td colspan="2">
						<input type="submit" name="submit" value="Submit">
					</td>
				</tr>
			</table>
		</form:form>
		
		<table>
			<tr>
				<th>Date</th>
				<th>Note</th>
			</tr>
			<c:forEach var="note" items="${form.notes}">
			<tr>
				<td> <fmt:formatDate value="${note.left}" pattern='MM/dd/yyyy HH:mm:ss'/> </td>
	            <td> <c:out value="${note.right}"/> </td>
	        </tr>
	    	</c:forEach>
    	</table>
		
	</c:if>
</body>
</html>