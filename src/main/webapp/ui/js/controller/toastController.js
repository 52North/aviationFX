angular.module('aviationFX')
.controller('ToastController', ['$scope', '$mdToast',
function ($scope, $mdToast) {
  $scope.$on('DO_TOAST', function (evt, toastObject) {
    console.info('Should do toast: '+JSON.stringify(toastObject));
    $mdToast.show(
      $mdToast.simple()
        .textContent(toastObject.message)
        .hideDelay(5000)
    );
  });

  console.info("ToastController started");
}]);
