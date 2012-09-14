<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
<title>OSM Watch</title>

<link rel="stylesheet" href="resources/style.css" />

<link rel="stylesheet" href="http://ia601209.us.archive.org/23/items/LeafletCDN/dist/leaflet.css" />
<script src="//ajax.googleapis.com/ajax/libs/jquery/1.8.1/jquery.min.js"></script>

<script src="resources/jquery.lightbox_me.js"></script>

<link rel="stylesheet" href="http://cdn.leafletjs.com/leaflet-0.4/leaflet.css" />
<script src="http://cdn.leafletjs.com/leaflet-0.4/leaflet.js"></script>

<script src="resources/draw/Map.Draw.js"></script>
<script src="resources/draw/Control.Draw.js"></script>



<script src="resources/watch.js"></script>


</head>

<body>
    <h1>OSM Watch</h1>

    <c:if test="${empty ud}">
            Please <a href="authenticate">authenticate</a> to continue.
        </c:if>
    <c:if test="${not empty ud}">
            Welcome <c:out value="${ud.screenName}" />
            
            Alerts
            <c:forEach items="${ud.alerts}" var="alert">
                "ALERT " ${alert.polygonWKT}
            </c:forEach>

        <a href="#" id="new_alert_button">New alert</a>

        <div id="add_alert_box">
            <a id="close_x" class="close sprited" href="#">close</a>
            <center>
                <h2>Add an alert</h2>
            </center>
            <div id="map"></div>
            <form>
                <table>
                    <tr>
                        <td>Watched tags (comma-separated, empty for all tags)</td>
                        <td><input name="tags" id="tags_input" /></td>
                    </tr>
                    <tr>
                        <td>Watched polygon (empty for everywhere)</td>
                        <td><input name="polygon" id="polygon_input"/></td>
                    </tr>
                      <tr>
                        <td><input type="submit" value="Add" id="add_alert_button" /></td>
                    </tr>
                </table>

            </form>


        </div>






    </c:if>











</body>
</html>