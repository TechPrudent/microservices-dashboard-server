(function () {
  'use strict';

  describe('GraphService', function () {

    var $httpBackend, graph, result, GraphService;
    beforeEach(module('microServicesGui'));

    beforeEach(inject(function (_$httpBackend_, _GraphService_) {
      $httpBackend = _$httpBackend_;
      GraphService = _GraphService_;
    }));

    it('Should call graph rest service', function () {
      givenGraph();
      givenBackendReturnsGraph();
      whenGetGraphCalled();
      thenExpectResultToBeGraph();
    });

    it('Should not call graph rest service', function () {
      givenGraph();
      givenBackendReturnsError();
      whenGetGraphCalled();
      thenExpectResultToBeEmpty();
    });

    function givenGraph() {
      graph = [{
        "name": "Top Level",
        "parent": "null"
      }];
    }

    function givenBackendReturnsGraph() {
      $httpBackend
        .expectGET('/rest/graph')
        .respond(function (method, url, data, headers) {
          return [200, graph];
        });
    }

    function givenBackendReturnsError() {
      $httpBackend
        .expectGET('/rest/graph')
        .respond(function (method, url, data, headers) {
          return [404];
        });
    }

    function whenGetGraphCalled() {
      result = GraphService.getGraph().then(function (value) {
        result = value;
      }, function (error) {
        result = error;
      });
      $httpBackend.flush();
    }

    function thenExpectResultToBeGraph() {
      expect(result.status).toEqual(200);
      expect(result.data).toEqual(graph);
    }

    function thenExpectResultToBeEmpty() {
      expect(result.status).toEqual(404);
      expect(result.data).toBeUndefined();
    }
  });
})();
