angular.module('aviationFX')
.controller('MenuController', ['$scope', '$mdSidenav', '$mdDialog', function($scope, $mdSidenav, $mdDialog) {
  $scope.toggleLeft = function() {
    $mdSidenav('left')
    .toggle()
    .then(function () {
      console.debug("toggle is done");
    });
  }

  $scope.openSubscriptionDialog = function(ev) {
    $scope.toggleLeft();
    $mdDialog.show({
      controller: 'NewSubscriptionController',
      templateUrl: 'templates/newsubscription.tmpl.html',
      parent: angular.element(document.body),
      targetEvent: ev,
      clickOutsideToClose:true
    })
    .then(function(answer) {
      $scope.status = 'You said the information was "' + answer + '".';
    }, function() {
      $scope.status = 'You cancelled the dialog.';
    });
  }

  console.info("MenuController started");
}]);
