'use strict';

var BottomBar = function() {
  this.bottomBar = element(by.css('#bottombar'));
  this.buttons = element.all(by.css('.button-div'));
};

module.exports = new BottomBar();
