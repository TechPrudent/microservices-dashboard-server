'use strict';

var NavBar = function() {
  this.titleArea = element(by.css('.title-area'));
  this.h1El = this.titleArea.element(by.css('h1'));
};

module.exports = new NavBar();
