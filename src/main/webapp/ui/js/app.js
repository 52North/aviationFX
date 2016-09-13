angular.module('aviationFX', [
  'ngMaterial',
  'ngRoute',
  'ngWebSocket',
  'ui-leaflet',
])
.config(['$routeProvider','$locationProvider', function ($routeProvider, $locationProvider) {
  $routeProvider
  .when('/status/:jobId', {
    templateUrl: 'templates/job-status.html'
  })
  .when('/apidoc', {
    templateUrl: 'templates/api-doc.html'
  })
  .otherwise({
    templateUrl: 'templates/home.html'
  });

}])
.factory('apiService', function($http, $location) {

  var services = [];
  var factory = {};

  var currentLoc = $location.absUrl();
  currentLoc = currentLoc.substring(0, currentLoc.indexOf('/ui'));


  factory.getServices = function() {
    return services;
  }

  factory.updateServices = function() {
    return $http.get(currentLoc+'/api/capabilities').then(function(response) {
      services = response.data.pubSubServices;
    });
  }

  factory.updateServices();

  factory.subscribe = function(options) {
    return $http.post(currentLoc+'/api/subscribe', options);
  }

  factory.updateServiceCredentials = function(resource, user, pw) {
    return $http.post(resource, {
      user: user,
      password: pw
    });
  }

  return factory;
});
