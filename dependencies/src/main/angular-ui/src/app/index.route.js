(function() {
  'use strict';

  angular
    .module('microServicesGui')
    .config(routeConfig);

  /** @ngInject */
  function routeConfig($stateProvider, $urlRouterProvider) {

    $stateProvider
      .state('home', {
        url: '/',
        templateUrl: 'app/graph/graph.html',
        controller: 'GraphController',
        controllerAs: 'graph'
      });

    $urlRouterProvider.otherwise('/');
  }

})();
