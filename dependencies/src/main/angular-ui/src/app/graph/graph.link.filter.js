(function () {
  'use strict';


angular.module('microServicesGui')
  .filter('linkFilter', ["SetService",  function (SetService) {
    return function (links, nodes) {
      var filteredLinks = [];
      for (var i = 0; i < links.length; i++) {
        for (var j = 0; j < nodes.length; j++) {
          if (links[i].source.id === nodes[j].id) {
            for (var k = 0; k < nodes.length; k++) {
              if (links[i].target.id === nodes[k].id) {
                SetService.add(links[i], filteredLinks );
                break;
              }
            }
          }
        }
      }
      return filteredLinks;
    };
  }]);
})();
