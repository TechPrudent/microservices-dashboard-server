(function () {
  'use strict';

  angular
    .module('microServicesGui')
    .factory('GraphService', GraphService);

  /** @ngInject */
  function GraphService($http) {
    var factory = {
      getGraph: getGraph,
      getStates: getStates,
      getTypes: getTypes,
      getGroups: getGroups
    };
    return factory;

    function getGraph() {
      return $http.get('/dependencies/graph');
    }

    function getStates() {
      return [{key: "UP", value: "UP"}, {key: "DOWN", value: "DOWN"}, {key: "UNKNOWN", value: "UNKNOWN"}];
    }

    function getTypes() {
      return [{key: "DB", value: "DB"}, {key: "MICROSERVICE", value: "MICROSERVICE"}, {
        key: "REST",
        value: "REST"
      }, {key: "SOAP", value: "SOAP"}];
    }

    function getGroups() {
      return [{key: "BCI", value: "BCI"}, {key: "BPS", value: "BPS"}, {key: "BUSC", value: "BUSC"}, {
        key: "CRMODS",
        value: "CRMODS"
      }, {key: "CSL", value: "CSL"}, {key: "IMA", value: "IMA"}, {key: "MBP", value: "MBP"}, {
        key: "NGRP",
        value: "NGRP"
      }, {key: "OCT", value: "OCT"}, {key: "PDB", value: "PDB"}, {key: "PPT", value: "PPT"}, {
        key: "RHE",
        value: "RHE"
      }, {key: "ROSY", value: "ROSY"}, {key: "SAPACHE", value: "SAPACHE"}];
    }

  }
})();
