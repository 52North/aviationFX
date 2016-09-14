angular.module('aviationFX').controller("MapController", function($scope, leafletData, $location, $websocket, $mdDialog, apiService, subscriptionService) {
  angular.extend($scope, {
    center: {
      lat: 51.505,
      lng: -0.09,
      zoom: 4
    },
    markers: {
    },
    paths: {
    },
    polygons: {
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

  var dummyCount = 0;

  $scope.parseFlight = function(message) {
    var payload = message.message;
    payload.id = payload.gufi.split("-").join("_");

    var subscriptions = subscriptionService.getSubscriptions();
    var iconUrl;

    if (subscriptions[message.subscriptionId] && subscriptions[message.subscriptionId].filter) {
      iconUrl = 'img/aircraft_red.png';
    }
    else {
      //this might be also on another subscription but matching both
      if ($scope.markers[payload.id] && $scope.markers[payload.id].icon.iconUrl === 'img/aircraft_red.png') {
        iconUrl = 'img/aircraft_red.png';
      }
      else {
        iconUrl = 'img/aircraft.png';
      }
    }

    // iconUrl = 'img/aircraft.png';

    // if (payload.route && payload.route.positionList && payload.route.positionList.length > 0) {
    //   iconUrl = 'img/aircraft_r.png';
    // }

    if (!$scope.markers[payload.id] && payload.currentPosition) {
      $scope.markers[payload.id] = {
        lat: payload.currentPosition.latitude,
        lng: payload.currentPosition.longitude,
        draggable: false,
        data: payload,
        message: payload.gufi,
        rotationAngle: payload.bearing,
        icon: {
            iconUrl: iconUrl,
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

      // if ($scope.markers[payload.id].icon.iconUrl === 'img/aircraft_red.png') {
      //   iconUrl = 'img/aircraft.png';
      // }
      // else {
      //   iconUrl = 'img/aircraft_red.png';
      // }

      if ($scope.markers[payload.id].icon.iconUrl !== iconUrl) {
        $scope.markers[payload.id].icon = {
            iconUrl: iconUrl,
            iconSize:     [32, 32],
            iconAnchor:   [16, 16],
            popupAnchor:  [0, -32],
            shadowSize:   [0, 0]
        }
      }

    }

  }

  $scope.parseAirspace = function(message) {
    var payload = message.message;
    if (payload.outerRing) {

      var addedPoly;
      if (!$scope.polygons[payload.identifier]) {
          //new airspace
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

          addedPoly = poly.addTo($scope.map);
          $scope.polygons[payload.identifier] = addedPoly;
      }
      else {
         addedPoly = $scope.polygons[payload.identifier];
      }

      addedPoly.data = payload;

      var parentScope = $scope;
      var PopupController = function($scope) {
        $scope.openSubscriptionDialog = function(ev, id) {
          parentScope.openSubscriptionDialog(ev, id);
        }
      }

      var template = '<h3>'+addedPoly.data.identifier+'</h3>'+
        '<ul><li>Type: '+addedPoly.data.type+'</li><li>Note: '+addedPoly.data.annotationNote+'</li></ul>'+
        '<md-button ng-click="openSubscriptionDialog($event, \''+addedPoly.data.identifier+'\')" class="md-primary">'+
          '<md-icon md-font-set="material-icons">add</md-icon> new subscription'+
        '</md-button>';
      var popup = L.popup.angular({
          template: template,
          controller: PopupController
      });

      addedPoly.bindPopup(popup);
      addedPoly.popup = popup;
    }

  }

  messageUpdates.onMessage(function(message) {
    if (message && message.data) {
      var payload = JSON.parse(message.data);
      // console.info(JSON.stringify(payload, null, 4));

      if (payload.message.gufi) {
        $scope.parseFlight(payload);
      }
      else if (payload.message.type) {
        $scope.parseAirspace(payload);
      }
      else {
        console.warn("Not supported: "+JSON.stringify(payload));
      }


    }
  });

  $scope.openSubscriptionDialog = function(ev, id) {
    console.info($scope.polygons[id]);
    $scope.map.closePopup();

    $mdDialog.show({
      controller: 'NewSubscriptionController',
      templateUrl: 'templates/newsubscription.tmpl.html',
      parent: angular.element(document.body),
      targetEvent: ev,
      clickOutsideToClose:true,
      locals: {
        airspace: $scope.polygons[id]
     }

    })
    .then(function(answer) {
      console.info('cancelled...'+answer);
    }, function() {
      $scope.status = 'You cancelled the dialog.';
    });
  }

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
});
