// vim: ts=4 sw=4 et        

var map = undefined;

function newAlert() {
	$("#add_alert_box").lightbox_me({
		centered:true,
		onLoad: function() {

		    var mapnikLayer = new L.TileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {                                                                                                                    
		        maxZoom: 18,                                                                                                                                                                                       
		        attribution: 'OpenStreetMap'                                                                                                                                                                       
		    });    

		    drawControl = new L.Control.Draw({
		    options : { position: 'bottomright' , drawMarker : false, drawPolyline: false} }
		    );
		    map = new L.Map('map', {                                                                                                                                                                               
		        center: new L.LatLng(25, 0),                                                                                                                                                                       
		        zoom:3                                                                                                                                                                                
		    });                                                                                                                                                                                                    
		    map.addLayer(mapnikLayer);
		    map.addControl(drawControl);
		    
		    /*
			var geometry = new L.Polygon([]);
				geometry.addLatLng(new L.LatLng(42, 0));
				geometry.addLatLng(new L.LatLng(52, 0));
				geometry.addLatLng(new L.LatLng(52, 15));
				geometry.addLatLng(new L.LatLng(42, 15));
			map.addLayer(geometry);
			*/
			
		    map.on('drawend', function(e) {
				onDrawingEnded(e);
			});
		}
	});
}

function onDrawingEnded(e) {
	console.info("Drawing " + e.poly);
	if (L.polygon && (e.poly instanceof L.Polygon)) {
		console.info("is a polygon");
		var wkt = "POLYGON((";
		
		$.each(e.poly.getLatLngs(), function(i) {
			if (i > 0) wkt += ", ";
			lng = e.poly.getLatLngs()[i].lng.toFixed(4);
			lat = e.poly.getLatLngs()[i].lat.toFixed(4);
			wkt += lng + " " + lat
		});
		
		wkt += "))";
		$("#polygon_input").val(wkt);
		console.info("Setting WKT " + wkt);
		
		map.draw.disable();
		
/*		
			var geometry = new L.Polygon([]);
		$.each(e.poly.getLatLngs(), function(i, latlng) {
			geometry.addLatLng(new L.LatLng(latlng.lat, latlng.lng));
		});
		map.addLayer(geometry);
				*/
	}
};

$(document).ready(function() {
	$("#new_alert_button").click(function(e) {
		newAlert();
	});
	
	$("#add_alert_button").click(function(e) {
		wkt = $("#polygon_input").val();
		tags = $("#tags_input").val();
		
		var url = "api/new_alert?wkt=" + wkt + "&tags=" + tags;
		console.log("Sending alert to " + url);
		$.ajax({url: url, dataType: "json", success: function(json) {
			console.log("Alert added");
			$("#add_alert_box").trigger("close");
		}, error: function(x,y,z) {
			console.log(x.responseText);
			console.log(y);
			console.log(z);
			}
		});
		e.preventDefault();
	});
		
});