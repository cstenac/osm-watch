//vim: ts=4 sw=4 et        

var map = undefined;

var currentlyDrawnPolygon = null;

function openNewAlertBox() {
	$("#add_alert_box").lightbox_me({
		centered:true,
		onLoad: function() {

			$("#tags_input").val("");
			$("#polygon_input").val("");
			$("#name_input").val("My alert " + new Date().getTime());

			var mapnikLayer = new L.TileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {                                                                                                                    
				maxZoom: 18,                                                                                                                                                                                       
				attribution: 'OpenStreetMap'                                                                                                                                                                       
			});    
			map = new L.Map('map', {                                                                                                                                                                               
				center: new L.LatLng(25, 0),                                                                                                                                                                       
				zoom:3                                                                                                                                                                                
			});                                                                                                                                                                                                    
			map.addLayer(mapnikLayer);
			
			var drawControl = new L.Control.Draw({
			    position: 'topright',
			    polyline: false,
			    marker: false,
			    circle: false,
			    rectangle: false
			});
			map.addControl(drawControl);
			
			drawControl.handlers.polygon.on('activated', function() {
				if (currentlyDrawnPolygon != null) {
					map.removeLayer(currentlyDrawnPolygon);
				}
			});
			map.on('draw:poly-created', function(e) {
				onDrawingEnded(e);
			});
			
		}
	});
}

function onDrawingEnded(e) {
	if (currentlyDrawnPolygon != null) {
		map.removeLayer(currentlyDrawnPolygon);
	}
	if (L.polygon && (e.poly instanceof L.Polygon)) {
		var wkt = "POLYGON((";

		$.each(e.poly.getLatLngs(), function(i) {
			if (i > 0) wkt += ", ";
			lng = e.poly.getLatLngs()[i].lng.toFixed(4);
			lat = e.poly.getLatLngs()[i].lat.toFixed(4);
			wkt += lng + " " + lat
		});

		wkt += ", ";
		lng = e.poly.getLatLngs()[0].lng.toFixed(4);
		lat = e.poly.getLatLngs()[0].lat.toFixed(4);
		wkt += lng + " " + lat;

		wkt += "))";
		$("#polygon_input").val(wkt);
		console.info("Setting WKT " + wkt);

		currentlyDrawnPolygon = e.poly;
		map.addLayer(currentlyDrawnPolygon);
	}
};

function deleteAlert(key) {
	var url = "api/delete_alert?key=" + key;
	$.getJSON(url, function(json) {
		reloadAlertsList();
	});
}

function reloadAlertsList() {
	$("alerts_list").html("<h3>Loading, please wait ...</h3>");
	$.getJSON("api/list_alerts", function(data) {
		var html = "<table class=\"alerts_list\" >";

		html += "<tr><th>Created</th><th>Alert name</th><th>Watched tags</th><th>Link</th><th>Nb. matches</th><th>Remove</th></tr>";
		for (var i in data.alerts) {
			html += "<tr>";
			html += "<td> " + data.alerts[i].id + " " + new Date(data.alerts[i].creation_timestamp) + "</td>";
			html += "<td>" + data.alerts[i].name + "</td><td>";
			if (data.alerts[i].tags == null) {
				html += "all tags";
			} else {
				html += data.alerts[i].tags;
			}
			html += "</td><td><a href=\"api/rss_feed?key="  + data.alerts[i].key + "\">RSS feed</a></td>";
			html += "<td>" + data.alerts[i].nb_matches + "</td>";
			html += "<td><a href=\"#\" onclick=\"deleteAlert('"+ data.alerts[i].key + "')\">Remove</a></td>";
			html += "</tr>";
		}
		html +="</table>";

		$("#alerts_list").html(html);
	});
}

$(document).ready(function() {
	$("#new_alert_button").click(function(e) {
		openNewAlertBox();
	});

	$("#add_alert_button").click(function(e) {
		wkt = $("#polygon_input").val();
		tags = $("#tags_input").val();
		name = $("#name_input").val();

		var url = "api/new_alert?wkt=" + wkt + "&tags=" + tags + "&name=" + name;
		console.log("Sending alert to " + url);
		$.ajax({url: url, dataType: "json", success: function(json) {
			console.log("Alert added");
			$("#add_alert_box").trigger("close");

			reloadAlertsList();

		}, error: function(x,y,z) {
			console.log(x.responseText);
			console.log(y);
			console.log(z);
		}
		});
		e.preventDefault();
	});

	reloadAlertsList();
});