function getRawLinks(){
  var $links;
  _set($links, document.links);
  return document.links;
}

function getLinks(){
  var retVal = [];
  var $links = getRawLinks();
  for (var i=0; i<2; i++){
    retVal[i] = _getText($links[i]);
  }
  return retVal;
}

function f1(){
	var $links = [];
	_assertExists(_link("Link Test"));
	$links = getLinks();
	for (var i=0; i<$links.length; i++){
		var $linkText = $links[i];
		_click(_link($linkText));
		if (_condition(_link('Back') != null)){
			_click(_link('Back'));
		}else{
			_navigateTo("index.htm");
		}
	}
}

