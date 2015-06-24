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
      page.viewportSize = { width: 400, height: 1000 };
    } else {
      console.log('FAIL to load the address');
    }
  });
}
