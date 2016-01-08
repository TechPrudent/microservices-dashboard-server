(function () {
  'use strict';

  angular.module('microServicesGui')
    .filter('nodeModalFilter', function () {
      return function (nodes, i) {
        var foundNodes = [];
        nodes.forEach(function(d) {
          if (d.id === i.id) {
            foundNodes.push(d);
          }
        });
        return foundNodes;
      };
    });
})();
