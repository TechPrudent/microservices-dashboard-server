(function () {
  'use strict';


  angular.module('microServicesGui')
    .filter('nodeFilter', function () {
      return function (nodes, nodeSearch) {
        var filteredNodes = [];
        for (var i = 0; i < nodes.length; i++) {
          if (validateId(nodes[i]) && validateStatus(nodes[i]) && validateType(nodes[i]) && validateGroup(nodes[i])) {
            //nodes[i].index = filteredNodes.length;
            filteredNodes.push(nodes[i]);
          }
        }

        function validateId(node) {
          return nodeSearch.id !== undefined ? (node.id !== undefined && node.id.toLowerCase().indexOf(nodeSearch.id) > -1) : true;
        }

        function validateStatus(node) {
          return (nodeSearch.status !== undefined && nodeSearch.status !== null && nodeSearch.status.key !== "ALL") ? (node.details !== undefined && node.details.status !== undefined && node.details.status.toUpperCase() === nodeSearch.status.key) : true;
        }

        function validateType(node) {
          return (nodeSearch.type !== undefined && nodeSearch.type !== null && nodeSearch.type.key !== "ALL") ? (node.details !== undefined && node.details.type !== undefined && node.details.type.toUpperCase() === nodeSearch.type.key) : true;
        }

        function validateGroup(node) {
          return (nodeSearch.group !== undefined && nodeSearch.group !== null && nodeSearch.group.key !== "ALL") ? (node.details !== undefined && node.details.group !== undefined && node.details.group.toUpperCase() === nodeSearch.group.key) : true;
        }

        return filteredNodes;
      };
    });
})();
