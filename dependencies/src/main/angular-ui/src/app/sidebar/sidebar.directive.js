(function () {
  'use strict';

  angular
    .module('microServicesGui')
    .directive('msgSideBar', SideBarDirective);

  /** @ngInject */
  function SideBarDirective($window) {
    return {
      templateUrl: 'app/sidebar/sidebar.html',
      link: sideBarDirectiveLink
    };

    function sideBarDirectiveLink(scope, element) {
      angular.element($window).bind('resize', function () {
        $('');
      });
    }

  }
})();
