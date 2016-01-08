(function () {
  'use strict';

  angular
    .module('microServicesGui')
    .controller('NodeModalController', NodeModalController);

  function NodeModalController($scope, $filter, GraphService, NodeService, $modalInstance, SetService) {

    $scope.states = GraphService.getStates();
    $scope.types = GraphService.getTypes();
    $scope.groups = GraphService.getGroups();

    $scope.newNode = NodeService.getNode();
    if ($scope.newNode === undefined) {
      $scope.newNode = {};
      $scope.newNode.details = {};
    } else {
      if ($scope.newNode.details !== undefined) {
        $scope.states.forEach(function (d) {
          if (d.key === $scope.newNode.details.status) {
            $scope.newNode.details.status = d;
          }
        });
        $scope.types.forEach(function (d) {
          if (d.key === $scope.newNode.details.type) {
            $scope.newNode.details.type = d;
          }
        });
        $scope.groups.forEach(function (d) {
          if (d.key === $scope.newNode.details.group) {
            $scope.newNode.details.group = d;
          }
        });
      }
    }

    $scope.newNode.linkedNodes = [];

    $scope.nodesList = {
      placeholder: "node-placeholder",
      connectWith: ".links-container"
    };

    $scope.availableNodes = [];
    GraphService.getGraph().then(function (result) {
      $scope.nodes = result.data;
      searchLinkedNodes();
    });

    $scope.ok = function () {
      saveNode();
      $modalInstance.close($scope.newNode);
    };

    $scope.cancel = function () {
      saveNode();
      $modalInstance.dismiss('cancel');
    };

    function saveNode(){
      if ($scope.newNode !== undefined && $scope.newNode.details !== undefined){
        $scope.newNode.details.status = $scope.newNode.details.status.key;
        $scope.newNode.details.type = $scope.newNode.details.type.key;
        $scope.newNode.details.group = $scope.newNode.details.group.key;
      }
    }

    function searchLinkedNodes() {
      //Add the nodes to the links
      $scope.nodes.nodes.forEach(function (node, i) {
        node.index = i;
        $scope.availableNodes.push(node);
        $scope.nodes.links.forEach(function (d) {
          if (d.source === node.index) {
            d.source = node;
          }
          if (d.target === node.index) {
            d.target = node;
          }
        });
      });

      //Search for the nodes connected by the link
      $scope.nodes.links.forEach(function (link) {
        var filteredNodes;
        if (link.source.id === $scope.newNode.id) {
          filteredNodes = $filter("nodeModalFilter")($scope.nodes.nodes, link.target);
          if (filteredNodes.length > 0) {
            nodeFound(filteredNodes[0]);
          }
        } else if (link.target.id === $scope.newNode.id) {
          filteredNodes = $filter("nodeModalFilter")($scope.nodes.nodes, link.source);
          if (filteredNodes.length > 0) {
            nodeFound(filteredNodes[0]);
          }
        }
      });
    }

    /**
     * When a node is found, add it to the linkedNodes array and remove it from the availableNodes array
     * @param node
     */
    function nodeFound(node) {
      $scope.newNode.linkedNodes = SetService.add(node, $scope.newNode.linkedNodes);
      if ($scope.availableNodes.indexOf(node) > -1) {
        $scope.availableNodes.splice($scope.availableNodes.indexOf(node), 1);
      }
    }

  }

})();
