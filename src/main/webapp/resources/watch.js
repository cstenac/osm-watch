//vim: ts=4 sw=4 et        

var map = undefined;

var currentlyDrawnPolygon = null;

function openNewAlertBox() {
	$("#existing_id_input").val("");
	$("#tags_input").val("");
	$("#polygon_input").val("");
	$("#name_input").val("My alert " + new Date().getTime());

	$("#add_alert_box").lightbox_me({
		centered:true,
		onLoad: function() {

			
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
			wkt += lng + " " + lat;
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


function fillFilterFieldsFromClassAndParams(filterClass, filterParams) {
	if (filterClass == "fr.openstreetmap.watch.matching.misc.FrenchCadastreImportFilter") {
		$("#filter_type option[value='cadastre']").attr('selected', 'selected');
	} else if (filterClass == "fr.openstreetmap.watch.matching.misc.FirstTimeContributorFilter") {
		$("#filter_type option[value='first_time']").attr('selected', 'selected');
	} else if (filterClass == "fr.openstreetmap.watch.matching.josmexpr.JOSMExprFilter") {
		$("#filter_type option[value='josm']").attr('selected', 'selected');
		$("#josm_params_input").val(filterParams);
	} else {
		$("#filter_type option[value='custom']").attr('selected', 'selected');
		$("#filter_class_input").val(filterClass);
		$("#filter_params_input").val(filterParams);

	}
}

function editAlert(key) {
	for (var idx in currentAlertList) {
		var a =currentAlertList[idx];
		console.info("Checking key " + key + " vs " + a.key);
		if (a.key == key) {
			
			
			openNewAlertBox();
			$("#existing_id_input").val(a.id);
			fillFilterFieldsFromClassAndParams(a.filterClass, a.filterParams);
			$("#filter_type").change();
			$("#name_input").val(a.name);
			$("#polygon_input").val(a.polygon);

		}
	}
}

function setAlertPublic(key, publicAlert) {
	var url = "api/set_alert_public?key=" + key + "&public=" + publicAlert;
	$.getJSON(url, function(json) {
		reloadAlertsList();
	});
}

function prettyFilter(filterClass, filterParams) {
	if (filterClass == null) return "No filter";
	if (filterClass == "fr.openstreetmap.watch.matching.josmexpr.JOSMExprFilter") {
		return "JOSM (" + filterParams + ")";
	}
	if (filterClass == "fr.openstreetmap.watch.matching.misc.FirstTimeContributorFilter") {
		return "New contributor";
	}
	if (filterClass == "fr.openstreetmap.watch.matching.misc.FrenchCadastreImportFilter") {
		return "French Cadastre Import";
	}
	return filterClass + " (" + filterParams + ")";
}

var currentAlertList = [];

function reloadAlertsList() {
	$("#alerts_list").html("<h3>Loading, please wait ...</h3>");
	$("#public_alerts_list").html("<h3>Loading, please wait ...</h3>");

	$.getJSON("api/list_alerts", function(data) {
		$("#alerts_list").html("<table class=\"alerts_list\" id=\"alerts_list_table\"></table>");

		var aaData = [];
		
		currentAlertList = data.alerts;

		for (var i in data.alerts) {
			var rowData = [$.format.date(new Date(data.alerts[i].creation_timestamp), "yyyy/MM/dd"),
			               data.alerts[i].name,
			               prettyFilter(data.alerts[i].filterClass, data.alerts[i].filterParams)
			               ];

			rowData.push("<a href=\"api/rss_feed?key="  + data.alerts[i].key + "\">RSS feed</a>");
			rowData.push(data.alerts[i].nb_matches);

			var actions = "[<a href=\"#\" onclick=\"deleteAlert('"+ data.alerts[i].key + "')\">Delete</a>] ";
			actions += "[<a href=\"#\" onclick=\"editAlert('"  + data.alerts[i].key + "')\">Edit</a>] ";
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
//{ "sTitle": "Id" },
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
			               data.alerts[i].name,
			               prettyFilter(data.alerts[i].filterClass, data.alerts[i].filterParams)
			               ];
			rowData.push("<a href=\"api/rss_feed?key="  + data.alerts[i].key + "\">RSS feed</a>");
			rowData.push(data.alerts[i].nb_matches);
			aaData.push(rowData);
		}

		$("#public_alerts_list_table").dataTable({
			"bJQueryUI": true,
			"aaData" : aaData,
			"aoColumns" : [
//{ "sTitle": "Id" },
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
	
	$("#filter_type").change(function(e) {
		filterType = $("#filter_type").val();
		if (filterType == "josm") {
			$("#josm_syntax_help").show();
			$("#josm_fields").show();
			$("#custom_fields").hide();
		} else if (filterType == "custom"){
			$("#josm_syntax_help").hide();
			$("#josm_fields").hide();
			$("#custom_fields").show();
		} else {
			$("#josm_syntax_help").hide();
			$("#josm_fields").hide();
			$("#custom_fields").hide();
		}
	});

	$("#add_alert_button").click(function(e) {
		e.preventDefault();
		wkt = $("#polygon_input").val();
		name = $("#name_input").val();

		filterType = $("#filter_type").val();
		filterClass = null;
		filterParams = null;
		if (filterType == "josm") {
			if ($("#josm_params_input").val().length > 0) {
				filterClass = "fr.openstreetmap.watch.matching.josmexpr.JOSMExprFilter";
				filterParams = $("#josm_params_input").val();
			}
		} else if (filterType == "cadastre") {
			filterClass = "fr.openstreetmap.watch.matching.misc.FrenchCadastreImportFilter";
		} else if (filterType == "first_time") {
			filterClass = "fr.openstreetmap.watch.matching.misc.FirstTimeContributorFilter";
		} else if (filterType == "custom") {
			filterClass = $("#filter_class_input").val();
			filterParams = $("#filter_params_input").val();
		}


		var url = "api/edit_alert";
		var data = {"name" : name};
		if (wkt.length > 0) {
			data["wkt"] = wkt;
		}
		if (filterClass != null) data["filterClass"] = filterClass;
		if (filterParams != null) data["filterParams"] = filterParams;
		
		if ($("#existing_id_input").val().length > 0) {
			data["id"] = $("#existing_id_input").val();
		}

		console.log("Sending alert to " + url + " -> " + data);
		$.ajax({type: 'POST', url: url, dataType: "json",
			data : data,
			success: function(json) {
				console.log("Alert added");
				$("#add_alert_box").trigger("close");
				reloadAlertsList();
			}, error: function(x,y,z) {
				window.alert("Failed to create alert: " + x.responseText);
				console.log(x.responseText);
				console.log(y);
				console.log(z);
			}
		});
		
	});

	reloadAlertsList();
});