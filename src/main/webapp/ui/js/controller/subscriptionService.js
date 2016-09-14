angular.module('aviationFX')
.factory('subscriptionService', function() {

  var subscriptions = {};
  var factory = {};

  factory.getSubscriptions = function() {
    return subscriptions;
  }

  factory.addSubscription = function(sub) {
    subscriptions[sub.id] = sub;
  }

  return factory;
});
