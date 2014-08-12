$Index = new $Page();
$Root = new $Page();


/**
 * Check your deployment settings for the right url
 */

$Root.url = "";
$Index.url = "/index.htm";

$Index.goTo = function() {
  var $this = this;
  _navigateTo($this.url);
}

