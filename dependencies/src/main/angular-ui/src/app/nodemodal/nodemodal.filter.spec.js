(function() {
  'use strict';

  describe("NodeModalFilter", function() {

    beforeEach(module('microServicesGui'));

    it('should have a node filter', inject(function($filter) {
      expect($filter('nodeModalFilter')).toBeDefined();
    }));

    it('should have a node filter that produces an array of nodes',
      inject(function($filter) {

        var node1 = {id:'node1'};
        var node2 = {id:'node2'};
        var node3 = {id:'node3'};
        var nodesList = [node1, node2, node3];
        var linkedNode = {id: 'node2'};

        var nodes = $filter('nodeModalFilter')(nodesList, linkedNode);

        expect(nodes.length).toEqual(1);
        expect(nodes[0]).toEqual(node2);
      }));
  });
})();
