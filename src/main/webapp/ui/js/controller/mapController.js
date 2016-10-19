angular.module('aviationFX').controller("MapController", function($scope, leafletData, $location, $websocket, $mdDialog, apiService, subscriptionService) {
  angular.extend($scope, {
    center: {
      lat: 38.381667,
      lng: -121.748056,
      zoom: 8
    },
    markers: {
    },
    paths: {
    },
    polygons: {
    }
  });

  $scope.markerObjects = {};

  leafletData.getMap('aviationMap').then(function(map) {
    $scope.map = map;

    var openaip_cached_basemap = L.tileLayer("http://{s}.tile.maps.openaip.net/geowebcache/service/tms/1.0.0/openaip_basemap@EPSG%3A900913@png/{z}/{x}/{y}.png", {
        maxZoom: 14,
        minZoom: 4,
        tms: true,
        detectRetina: true,
        subdomains: '12',
        format: 'image/png',
        transparent: true,
        opacity: 0.5,
        attribution: 'OpenAIP Map Tiles'
    }).addTo(map);
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
    var alertRequired = false;
    payload.id = payload.gufi.split("-").join("_");

    var subscriptions = subscriptionService.getSubscriptions();
    var iconUrl;

    if (subscriptions[message.subscriptionId] && subscriptions[message.subscriptionId].filter) {
      iconUrl = 'img/aircraft_red.png';

      if (!$scope.markers[payload.id] || $scope.markers[payload.id].icon.iconUrl !== 'img/aircraft_red.png') {
        //new matching gufi -> alert popup
        alertRequired = true;
      }
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

    if (!$scope.markers[payload.id] && payload.currentPosition) {

      $scope.markers[payload.id] = {
        lat: payload.currentPosition.latitude,
        lng: payload.currentPosition.longitude,
        draggable: false,
        data: payload,
        message: payload.gufi,
        iconAngle: payload.bearing,
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
        leafletData.getMarkers('aviationMap').then(function(retrieveMarkers) {
          if (retrieveMarkers && retrieveMarkers[payload.id] && !retrieveMarkers[payload.id].actionsSetup) {
            retrieveMarkers[payload.id].on('click', function(ev) {
              $scope.activeFlightRoutes(payload.id);
            });
            retrieveMarkers[payload.id].actionsSetup = true;
            retrieveMarkers[payload.id].getPopup().on('close', $scope.hideFlightRoutes);
            $scope.markerObjects[payload.id] = retrieveMarkers[payload.id];
          }
          else {
            console.info('Marker not found: '+ payload.id, +'; '+JSON.stringify(retrieveMarkers));
          }
        });
      },
      500);

    }
    else if (payload.currentPosition) {
      $scope.markers[payload.id].data = payload;
      $scope.markers[payload.id].lat = payload.currentPosition.latitude;
      $scope.markers[payload.id].lng = payload.currentPosition.longitude;
      $scope.markers[payload.id].history.push({
        lat: payload.currentPosition.latitude,
        lng: payload.currentPosition.longitude
      });

      if ($scope.markers[payload.id].icon.iconUrl !== iconUrl || $scope.markers[payload.id].rotationAngle !== payload.bearing) {
        $scope.markers[payload.id].icon = {
            iconUrl: iconUrl,
            iconSize:     [32, 32],
            iconAnchor:   [16, 16],
            popupAnchor:  [0, -32],
            shadowSize:   [0, 0]
        }
      }

      if ($scope.markerObjects[payload.id] && $scope.markerObjects[payload.id].setIconAngle) {
        $scope.markerObjects[payload.id].setIconAngle(payload.bearing);
      }


    }

    if (alertRequired) {
      $scope.alertOnMatchingGufi($scope.markers[payload.id]);
    }

  }

  $scope.alertOnMatchingGufi = function(matchingMarker) {
    var confirm = $mdDialog.confirm()
          .title('A Flight has entered a subscribed Airspace')
          .textContent('Flight with GUFI \''+matchingMarker.data.id+'\' has entered a subscribed Airspace!')
          .ariaLabel('Alert')
          .ok('Go to Flight!')
          .cancel('Ignore');

    $mdDialog.show(confirm).then(function() {
      $scope.map.panTo(new L.LatLng(matchingMarker.lat, matchingMarker.lng));
    }, function() {
      console.info("ignore for now");
    });
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
            color: '#FFBF00',
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
      var payloadArray = JSON.parse(message.data);
      // console.info(JSON.stringify(payload, null, 4));

      if (payloadArray) {
        payloadArray.forEach(function(payload) {
          if (payload.message.gufi) {
            $scope.parseFlight(payload);
          }
          else if (payload.message.type) {
            $scope.parseAirspace(payload);
          }
          else {
            console.warn("Not supported: "+JSON.stringify(payload));
          }
        });
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
