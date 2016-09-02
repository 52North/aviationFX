angular.module('aviationFX').controller("MapController", ['$scope', 'leafletData', '$location', '$websocket', function($scope, leafletData, $location, $websocket) {
  angular.extend($scope, {
    center: {
      lat: 51.505,
      lng: -0.09,
      zoom: 4
    },
    markers: {
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
      $scope.markers[payload.identification] = {
        lat: payload.currentPosition.latitude,
        lng: payload.currentPosition.longitude,
        draggable: false,
        data: payload,
        message: payload.gufi,
        rotationAngle: 0,
        icon: {
            iconUrl: 'img/aircraft.png',
            iconSize:     [32, 32],
            shadowSize:   [0, 0]
        }
      }
    }
  });

  messageUpdates.onError(function(event) {
    console.warn('connection Error', event);
  });

  console.info("MapController started");
}]);
