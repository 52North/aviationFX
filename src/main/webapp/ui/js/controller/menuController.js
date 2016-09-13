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
      clickOutsideToClose:true,
      locals: {
        airspace: undefined
     }
    })
    .then(function(answer) {
      console.info('cancelled...'+answer);
    }, function() {
      $scope.status = 'You cancelled the dialog.';
    });
  }

  console.info("MenuController started");
}]);
