angular.module('aviationFX').controller("NewSubscriptionController", function($scope, $mdDialog, airspace, apiService) {
  $scope.airspace = airspace;
  $scope.pubSub = apiService.getServices();
  $scope.selectedService = {
  };

  $scope.$watch("selectedService.host", function(newValue, oldValue) {
    if (newValue && newValue !== oldValue) {
      $scope.pubSub.forEach(function(p) {
        if (p.host === newValue) {
          $scope.selectedService = p;
        }
      });
    }
  });

  $scope.$watch("pubSub", function(newValue, oldValue) {
    if (newValue) {
      $scope.pubSub = newValue;
      $scope.pubSub.forEach(function(p) {
        if (p.host === $scope.selectedService.host) {
          $scope.selectedService = p;
        }
      });
    }
  });

  console.info("NewSubscriptionController started: "+$scope.airspace);

  $scope.cancel = function(event, reason) {
    $mdDialog.hide(reason);
  }

  $scope.updateServiceCredentials = function() {
    apiService.updateServiceCredentials($scope.selectedService.details,
      $scope.selectedService.username, $scope.selectedService.password).then(function(response) {
        apiService.updateServices().then(function () {
          $scope.pubSub = apiService.getServices();
        });
      });
  }

  $scope.subscribe = function() {
    var opts = {
      host: $scope.selectedService.host,
      pubId: $scope.selectedService.pubId,
      deliveryMethod: $scope.selectedService.deliveryMethod
    };
    if ($scope.airspace) {
      opts.areaOfInterest = {
        outerRing: $scope.airspace.data.outerRing
      }
    }
    apiService.subscribe(opts).then(function(response) {
      $scope.subscribeSuccess = true;
    });
  }
});
