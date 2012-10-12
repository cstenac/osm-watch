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
function setAlertPublic(key, publicAlert) {
	var url = "api/set_alert_public?key=" + key + "&public=" + publicAlert;
	$.getJSON(url, function(json) {
		reloadAlertsList();
	});
}

function reloadAlertsList() {
	$("#alerts_list").html("<h3>Loading, please wait ...</h3>");
	$("#public_alerts_list").html("<h3>Loading, please wait ...</h3>");

	$.getJSON("api/list_alerts", function(data) {
		$("#alerts_list").html("<table class=\"alerts_list\" id=\"alerts_list_table\"></table>");

		var aaData = [];

		for (var i in data.alerts) {
			var rowData = [$.format.date(new Date(data.alerts[i].creation_timestamp), "yyyy/MM/dd"),
			               data.alerts[i].name];
			if (data.alerts[i].tags == null) {
				rowData.push("All");
			} else {
				rowData.push(data.alerts[i].tags);
			}
			rowData.push("<a href=\"api/rss_feed?key="  + data.alerts[i].key + "\">RSS feed</a>");
			rowData.push(data.alerts[i].nb_matches);
			
			var actions = "[<a href=\"#\" onclick=\"deleteAlert('"+ data.alerts[i].key + "')\">Delete</a>] ";
			if (data.alerts[i].publicAlert) {
				actions += "[<a href=\"#\" onclick=\"setAlertPublic('"+ data.alerts[i].key + "', false)\">Make private</a>]";
			} else {
				actions += "[<a href=\"#\" onclick=\"setAlertPublic('"+ data.alerts[i].key + "', true)\">Make public</a>]";
			}
			rowData.push(actions);

			aaData.push(rowData);
		}

		$("#alerts_list_table").dataTable({
			"bJQueryUI": true,
			"aaData" : aaData,
			"aoColumns" : [
//			               { "sTitle": "Id" },
			               { "sTitle": "Created" },
			               { "sTitle": "Name" },
			               { "sTitle": "Filter", "sClass": "center" },
			               { "sTitle": "RSS", "sClass": "center" },
			               { "sTitle": "Matches", "sClass": "center" },
			               { "sTitle": "Actions", "sClass": "center" }
			               ]

		});
	});
	$.getJSON("api/list_alerts?public=true", function(data) {
		$("#public_alerts_list").html("<table class=\"alerts_list\" id=\"public_alerts_list_table\"></table>");

		var aaData = [];

		for (var i in data.alerts) {
			var rowData = [$.format.date(new Date(data.alerts[i].creation_timestamp), "yyyy/MM/dd"),
			               data.alerts[i].name];
			if (data.alerts[i].tags == null) {
				rowData.push("All");
			} else {
				rowData.push(data.alerts[i].tags);
			}
			rowData.push("<a href=\"api/rss_feed?key="  + data.alerts[i].key + "\">RSS feed</a>");
			rowData.push(data.alerts[i].nb_matches);
			aaData.push(rowData);
		}

		$("#public_alerts_list_table").dataTable({
			"bJQueryUI": true,
			"aaData" : aaData,
			"aoColumns" : [
//			               { "sTitle": "Id" },
			               { "sTitle": "Created" },
			               { "sTitle": "Name" },
			               { "sTitle": "Filter", "sClass": "center" },
			               { "sTitle": "RSS", "sClass": "center" },
			               { "sTitle": "Matches", "sClass": "center" },
			               ]

		});
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