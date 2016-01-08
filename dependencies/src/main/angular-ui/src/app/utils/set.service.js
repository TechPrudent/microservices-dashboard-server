(function () {
  'use strict';

  angular
    .module('microServicesGui')
    .factory('SetService', SetService);

  /** @ngInject */
  function SetService($http, $rootScope) {

    var factory = {
      add: add,
      has: has
    };
    return factory;

    function add(element, array) {
      if (array.indexOf(element) === - 1) {
        array.push(element);
      }
      return array;
    }

    function has(element, array) {
      return array.indexOf(element) > -1;
    }

  }
})();
