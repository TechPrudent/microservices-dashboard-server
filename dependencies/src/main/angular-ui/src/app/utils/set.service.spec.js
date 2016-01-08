(function () {
  'use strict';

  describe('SetService', function () {

    var SetService;
    beforeEach(module('microServicesGui'));

    beforeEach(inject(function (_SetService_) {
      SetService = _SetService_;
    }));

    it('should add the node to the set', function () {
      var node = {};
      var nodeSet = [];
      SetService.add(node, nodeSet);
      expect(SetService.has(node, nodeSet)).toBe(true);
    });
  });
})();
