'use strict';

var Controls = function() {
  this.controlBar = element(by.css('#control-bar'));
  this.clearButton = this.controlBar.element(by.css('button'));
  this.searchNode = element(by.css('#searchnode'));
};

module.exports = new Controls();
