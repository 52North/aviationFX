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

}]);;
