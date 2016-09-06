angular.module('aviationFX').controller("MapController", ['$scope', 'leafletData', '$location', '$websocket', function($scope, leafletData, $location, $websocket) {
  angular.extend($scope, {
    center: {
      lat: 51.505,
      lng: -0.09,
      zoom: 4
    },
    markers: {
    },
    paths: {
    }
  });

  leafletData.getMap('aviationMap').then(function(map) {
    $scope.map = map;
  });

  $scope.panMap = function() {
    leafletData.getMap('aviationMap').then(function(map) {
      map.panTo([-33.8650, 151.2094], 10);
    });
  };

  var currentLoc = $location.absUrl();
  currentLoc = currentLoc.substring(currentLoc.indexOf('://'), currentLoc.indexOf('/ui'));
  var messageUpdates = $websocket('ws'+currentLoc+'/api/messages/websocket');

  $scope.parseFlight = function(payload) {
    payload.id = payload.gufi.split("-").join("_");

    if (!$scope.markers[payload.id] && payload.currentPosition) {
      $scope.markers[payload.id] = {
        lat: payload.currentPosition.latitude,
        lng: payload.currentPosition.longitude,
        draggable: false,
        data: payload,
        message: payload.gufi,
        rotationAngle: payload.bearing,
        icon: {
            iconUrl: 'img/aircraft.png',
            iconSize:     [32, 32],
            iconAnchor:   [16, 16],
            popupAnchor:  [0, -32],
            shadowSize:   [0, 0]
        },
        history: [{
          lat: payload.currentPosition.latitude,
          lng: payload.currentPosition.longitude
        }]
      };

      setTimeout(function() {
        leafletData.getMarkers('aviationMap').then(function(markers) {
          if (markers && markers[payload.id]) {
            markers[payload.id].on('click', function(ev) {
              $scope.activeFlightRoutes(payload.id);
            });

            markers[payload.id].getPopup().on('close', $scope.hideFlightRoutes);
          }
          else {
            console.info('Marker not found: '+ payload.id, +'; '+JSON.stringify(markers));
          }
        });
      },
      500);

    }
    else if (payload.currentPosition) {
      $scope.markers[payload.id].lat = payload.currentPosition.latitude;
      $scope.markers[payload.id].lng = payload.currentPosition.longitude;
      $scope.markers[payload.id].history.push({
        lat: payload.currentPosition.latitude,
        lng: payload.currentPosition.longitude
      });
    }

    if (payload.route && payload.route.positionList && payload.route.positionList.length > 0) {
      $scope.markers[payload.id].icon = {
          iconUrl: 'img/aircraft_r.png',
          iconSize:     [32, 32],
          iconAnchor:   [16, 16],
          popupAnchor:  [0, -32],
          shadowSize:   [0, 0]
      }
    }
  }

  $scope.parseAirspace = function(payload) {
    if (payload.outerRing) {
      var coords = [];
      payload.outerRing.forEach(function(c) {
        coords.push({
          lat: c.latitude,
          lng: c.longitude
        });
      });
      var poly = L.polygon(coords, {
        color: '#000080',
        weight: 8,
        opacity: 0.5
      });
      poly.addTo($scope.map).bindPopup("I am a polygon.");
    }
  }

  messageUpdates.onMessage(function(message) {
    if (message && message.data) {
      var payload = JSON.parse(message.data);
      // console.info(JSON.stringify(payload, null, 4));

      if (payload.gufi) {
        $scope.parseFlight(payload);
      }
      else if (payload.type) {
        $scope.parseAirspace(payload);
      }
      else {
        console.warn("Not supported: "+JSON.stringify(payload));
      }


    }
  });

  $scope.activeFlightRoutes = function(id) {
    console.info('Showing flight route for '+id);
    $scope.paths = {};
    if ($scope.markers[id].data.route) {
      var latlngs = [];
      $scope.markers[id].data.route.positionList.forEach(function(pos) {
        latlngs.push({lat: pos.latitude, lng: pos.longitude});
      });

      $scope.paths["planned"] = {
        color: '#800000',
        weight: 8,
        latlngs: latlngs,
      };
    }

    $scope.paths["actual"] = {
      color: '#008000',
      weight: 8,
      latlngs: $scope.markers[id].history,
    };
  }

  $scope.hideFlightRoutes = function(id) {
    console.info('Hiding flight route for '+id);
    $scope.paths = {};
  }

  messageUpdates.onError(function(event) {
    console.warn('connection Error', event);
  });

  console.info("MapController started");
}]);
