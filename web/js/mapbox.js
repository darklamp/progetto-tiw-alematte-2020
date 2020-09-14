mapboxgl.accessToken = '';

const map = new mapboxgl.Map({
    container: 'map',
    style: mapStyle,
    center: [0.0, 0.0],
    zoom: 2
});


const geoGsonPath = /*[[@{/manager/campaign/maps}]]*/ "";
(function getGeoJson() {
    $.ajax({
        type: "POST",
        url: geoGsonPath,
        data: "campaignId=" + campaignId,
        dataType: "json",
        success: function (jsonContent) {
            jsonContent.features.forEach(function(marker) {

                // create a HTML element for each feature
                var el = document.createElement('div');
                el.setAttribute("onclick","openDetail("+marker.properties.title+")");
                if (marker.properties.description){
                    el.className = "marker-green";
                }
                else el.className = 'marker';

                // make a marker for each feature and add to the map
                var asd = new mapboxgl.Marker(el)
                    .setLngLat(marker.geometry.coordinates)
                    .addTo(map);

            });
        },
        error: function () {
            alert("Error");
        },
        async: true,
    });
}());
