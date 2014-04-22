$Index = new $Page();

$Index.goTo = function() {
  /**
   * Check your deployment settings for the right port
   */
  var $port = 7733;
  _navigateTo("http://localhost:"+ $port +"/index.htm")
}
