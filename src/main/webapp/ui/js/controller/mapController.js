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

  $scope.panMap = function() {
    leafletData.getMap('aviationMap').then(function(map) {
      map.panTo([-33.8650, 151.2094], 10);
    });
  };

  var currentLoc = $location.absUrl();
  currentLoc = currentLoc.substring(currentLoc.indexOf('://'), currentLoc.indexOf('/ui'));
  var messageUpdates = $websocket('ws'+currentLoc+'/api/messages/websocket');

  messageUpdates.onMessage(function(message) {
    if (message && message.data) {
      var payload = JSON.parse(message.data);
      // console.info(JSON.stringify(payload, null, 4));

      if (!payload.gufi) {
        console.warn("Not supported: "+JSON.stringify(payload));
        return;
      }
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
        console.info("Has Route: "+JSON.stringify(payload.route))
        $scope.markers[payload.id].icon = {
            iconUrl: 'img/aircraft_r.png',
            iconSize:     [32, 32],
            iconAnchor:   [16, 16],
            popupAnchor:  [0, -32],
            shadowSize:   [0, 0]
        }
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
