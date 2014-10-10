function $Page($url){
  var $this = this;
  $this.url=$url;
}

$Page.prototype.goTo = function () {
    var $this = this;
    _navigateTo($this.url);
}