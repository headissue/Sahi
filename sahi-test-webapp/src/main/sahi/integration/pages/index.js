$Index = new $Page();
$Root = new $Page();


/**
 * Check your deployment settings for the right url
 */

$Root.port=7733;
$Root.url="http://127.0.1.1:"+ $Root.port;

$Index.url = $Root.url +"/index.htm";


$Index.goTo = function() {
  var $this = this;
  _navigateTo($this.url);
}

