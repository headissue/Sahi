/*var fs = require('fs');
var address = phantom.args[1];
var workspace = phantom.args[0] ? phantom.args[0] +"/target" : "/tmp";
var stream = getLogFile();


function logTraffic(page) {
  page.onResourceRequested = function (request) {
    try {
      stream.write('Request ' + JSON.stringify(request, undefined, 2));
    } catch(e) {
      stream.write('Exception' + JSON.stringify(e, undefined, 2));
    }
  };
  page.onResourceReceived = function (response) {
    try {
    stream.write('Receive ' + JSON.stringify(response, undefined, 2));
    } catch(e) {
      stream.write('Exception' + JSON.stringify(e, undefined, 2));
    };
  }
}
function getLogFile() {
  var path =workspace+"/phantomTraffic";
  fs.touch(path);
  return fs.open(path, 'w');
}

if (phantom.args.length === 0) {
  console.log('Usage: sahi.js &lt;Sahi Playback Start URL&gt;');
  phantom.exit();
} else {
  //var address = unescape(phantom.args[0]); // use if < v1.7
  console.log('Loading ' + address);
  var page = new WebPage();
  logTraffic(page);
  page.open(address, function(status) {
    if (status === 'success') {
      var title = page.evaluate(function() {
        return document.title;
      });
      console.log('Page title is ' + title);
    } else {
      console.log('FAIL to load the address');
    }
  });
}
stream.flush();
 */


if (phantom.args.length === 0) {
  console.log('Usage: phantom.js &lt;Sahi Playback Start URL&gt;');
  phantom.exit();
} else {
  //var address = unescape(phantom.args[0]); // use if < v1.7
  var address = phantom.args[0];
  console.log('Loading ' + address);
  var page = new WebPage();
  page.open(address, function(status) {
    if (status === 'success') {
      page.viewportSize = { width: 1000, height: 1000 };
      var title = page.evaluate(function() {
        return document.title;
      });
      console.log('Page title is ' + title);
    } else {
      console.log('FAIL to load the address');
    }
  });
}
