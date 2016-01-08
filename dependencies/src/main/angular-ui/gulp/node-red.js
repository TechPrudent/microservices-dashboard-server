'use strict';

var gulp = require('gulp');
var shell = require('gulp-shell');

gulp.task('node-red',
  shell.task([
      'node ../node_modules/node-red/red.js flows.json'
    ],
    {
      cwd: 'node_red_stubs'
    }
  ));
