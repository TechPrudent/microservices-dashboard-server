(function () {
  'use strict';

  angular
    .module('microServicesGui')
    .directive('msgNavigation', NavigationDirective);

  /** @ngInject */
  function NavigationDirective() {
    return {
      templateUrl: 'app/navigation/navigation.html'
    };
  }

})();
