(function() {
  'use strict';

  angular
    .module('microServicesGui')
    .config(config);

  /** @ngInject */
  function config($logProvider) {

    // enable log debug
    $logProvider.debugEnabled(true);

  }

})();
