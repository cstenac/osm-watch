<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
    <head>
        <title>OSM Watch</title>
        <style type="text/css">
            table,th,td {
                border:1px solid black;
                padding: 5px;
            }
        </style>
    </head>
    
    <body>
        <h1>OSM Watch</h1>
        
        <c:if test="${empty ud}">
            Please <a href="authenticate">authenticate</a> to continue.
        </c:if>
        <c:if test="${not empty ud}">
            Welcome <c:out value="${ud.screenName}" />
            
            Alerts
            <c:forEach  items="${ud.alerts}" var="alert">
                "ALERT " ${alert.polygonWKT}
            </c:forEach>
            
        </c:if>

    </body>
</html>