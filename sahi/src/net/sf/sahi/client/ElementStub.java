package net.sf.sahi.client;

import java.util.ArrayList;
import java.util.List;

import net.sf.sahi.util.Utils;

/**
 * Sahi - Web Automation and Test Tool
 * 
 * Copyright  2006  V Narayan Raman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * ElementStub is a representation of a particular HTML DOM element on the browser.<br/>
 * It translates to its corresponding Sahi javascript API. 
 */
public class ElementStub {
	private final String elementType;
	private Object[] identifiers;
	private final Browser browser;
	private int index = -1;

	public ElementStub(String elementType, Browser browser, Object... args) {
		this.elementType = elementType;
		this.browser = browser;
		this.identifiers = args;
	}

	public String toString(){
		StringBuffer sb = new StringBuffer();
		boolean start = true; 
		for (Object o: identifiers){
			if (!start) {
				sb.append(", ");
				sb.append(getArgument(o, -1));
			} else {
				sb.append(getArgument(o, index));
			}
			start = false;
		}
		return "_sahi._" + elementType + "(" + sb.toString() + ")"; 
	}

	private Object getArgument(Object o, int ix) {
		String s = o.toString();
		if (o instanceof String){
			if (ix != -1){
				s = s + "[" + ix + "]";
			}
			if (!(s.length() > 1 && s.startsWith("/") && s.endsWith("/"))){
				return "\"" + Utils.escapeDoubleQuotesAndBackSlashes(s) + "\"";
			}
		}
		return s;		
	}
	
	/**
	 * Performs a click on this element.<br/>
	 * Internally calls browser.click(this);
	 * @throws ExecutionException
	 */
	public void click() throws ExecutionException {
		browser.click(this);
	}
	
	/**
	 * Performs a double click on this element.<br/>
	 * Internally calls browser.doubleClick(this);
	 * @throws ExecutionException
	 */
	public void doubleClick() throws ExecutionException {
		browser.doubleClick(this);
	}
	
	/**
	 * Performs a right click on this element.<br/>
	 * Internally calls browser.rightClick(this);
	 * @throws ExecutionException
	 */
	public void rightClick() throws ExecutionException {
		browser.rightClick(this);
	}
	
	/**
	 * Checks this element (applicable to checkboxes and radio buttons).<br/>
	 * Internally calls browser.check(this);
	 * @throws ExecutionException
	 */
	public void check() throws ExecutionException {
		browser.check(this);
	}
	
	/**
	 * Unchecks this element (applicable to checkboxes).<br/>
	 * Internally calls browser.uncheck(this);
	 * @throws ExecutionException
	 */
	public void uncheck() throws ExecutionException {
		browser.uncheck(this);
	}
	
	/**
	 * Brings focus on the element. 
	 * 
	 * @param element
	 * @throws ExecutionException
	 */
	public void focus() throws ExecutionException {
		browser.focus(this);
	}	
	
	/**
	 * Removes focus from the element. 
	 * 
	 * @param element
	 * @throws ExecutionException
	 */
	public void removeFocus() throws ExecutionException {
		browser.removeFocus(this);
	}
	
	/**
	 * Drags the element and drops it on another element.
	 * 
	 * @param dropElement Element to be dropped on
	 * @throws ExecutionException
	 */
	public void dragAndDropOn(ElementStub dropElement) throws ExecutionException {
		browser.dragDrop(this, dropElement);
	}
	
	/**
	 * Performs a mouseover on this element.<br/>
	 * Same as hover().<br/>
	 * Internally calls browser.mouseOver(this);
	 * @throws ExecutionException
	 */
	public void mouseOver() throws ExecutionException {
		browser.mouseOver(this);
	}	
	
	/**
	 * Performs a mousedown on this element.<br/>
	 * Internally calls browser.mouseDown(this);
	 * @throws ExecutionException
	 */
	public void mouseDown() throws ExecutionException {
		browser.mouseDown(this);
	}
	
	/**
	 * Performs a mouseup on this element.<br/>
	 * Internally calls browser.mouseUp(this);
	 * @throws ExecutionException
	 */
	public void mouseUp() throws ExecutionException {
		browser.mouseUp(this);
	}

	/**
	 * Performs a mouseover on this element.<br/>
	 * Same as mouseOver().<br/>
	 * Internally calls browser.mouseOver(this);
	 * @throws ExecutionException
	 */
	public void hover() throws ExecutionException {
		this.mouseOver();
	}

	/**
	 * Sets the value of this form element.<br/>
	 * This method will do nothing for elements which do not have a value attribute.<br/>
	 * Internally calls browser.setValue(this, value);
	 * 
	 * @param value
	 * @throws ExecutionException
	 */
	public void setValue(String value) throws ExecutionException {
		browser.setValue(this, value);
	}
	
	/**
	 * Sets the value of this file upload element.<br/>
	 * This method will do nothing for elements which are not file upload fields.<br/>
	 * Internally calls browser.setFile(this, value); 
	 * 
	 * @param value
	 * @throws ExecutionException
	 */
	public void setFile(String value) throws ExecutionException  {
		browser.setFile(this, value);
	}
	
	/**
	 * Sets the value of a select element; Will unselect previously selected option(s).<br/>
	 * This method will do nothing for elements which are not select fields.<br/>
	 * Internally calls browser.choose(this, value, false);
	 * 
	 * @param value
	 * @throws ExecutionException
	 */
	public void choose(String value) throws ExecutionException {
		choose(value, false);
	}	
	
	/**
	 * Sets the value of a select element; Will not unselect previously selected option(s) if append is true.<br/>
	 * This method will do nothing for elements which are not select fields.<br/>
	 * Internally calls browser.choose(this, values, append);
	 * 
	 * @param value
	 * @param append: if true, options are selected without unselecting previous options in multi-select box
	 * @throws ExecutionException
	 */
	public void choose(String value, boolean append) throws ExecutionException {
		browser.choose(this, value, append);
	}
	
	/**
	 * Selects multiple options of a select element; Will unselect previously selected option(s).<br/>
	 * This method will do nothing for elements which are not select fields.<br/>
	 * Internally calls browser.choose(this, values, false);
	 * 
	 * @param values String array of option identifiers
	 * @throws ExecutionException
	 */
	public void choose(String[] values) throws ExecutionException {
		choose(values, false);
	}	
	
	/**
	 * Selects multiple options of a select element; Will not unselect previously selected option(s) if append is true.<br/>
	 * This method will do nothing for elements which are not select fields.<br/>
	 * Internally calls browser.choose(this, values, append);
	 * 
	 * @param values String array of option identifiers
	 * @param append: if true, options are selected without unselecting previous options in multi-select box
	 * @throws ExecutionException
	 */
	public void choose(String[] values, boolean append) throws ExecutionException {
		browser.choose(this, values, append);
	}
	
	/**
	 * Returns the inner text of an element.<br/>
	 * same as getText().<br/>
	 * Internally calls browser.getText(this);
	 * 
	 * @return
	 * @throws ExecutionException
	 */
	public String text() throws ExecutionException {
		return getText();
	}

	/**
	 * Returns the inner text of an element.<br/>
	 * same as text().<br/>
	 * Internally calls browser.getText(this);
	 * 
	 * @return
	 * @throws ExecutionException
	 */
	public String getText() throws ExecutionException {
		return browser.getText(this);
	}

	/**
	 * Writes the text into the Rich Text Editor (RTE).<br/>
	 * Internally calls browser.rteWrite(this, value);
	 * 
	 * @param value
	 * @throws ExecutionException
	 */
	public void rteWrite(String value) throws ExecutionException {
		browser.rteWrite(this, value);
	}
	
	/**
	 * Fetches the text of a Rich Text Editor (RTE).<br/>
	 * 
	 * @return text of the RTE
	 */
	public String rteText() {
		return browser.fetch("_sahi._rteText(" + this + ")");
	}	
	
	/**
	 * Fetches the HTML of a Rich Text Editor (RTE).<br/>
	 * This will be browser dependent
	 * 
	 * @return HTML of the RTE
	 */	
	public String rteHTML() {
		return browser.fetch("_sahi._rteHTML(" + this + ")");
	}	
	
	public String fetch() throws ExecutionException {
		return fetch(null);
	}
	
	/**
	 * Checks if an element is present on the browser.<br/>
	 * Internally calls browser.exists(this);
	 * Retries a few times if false. This can be controlled with script.max_reattempts_on_error in sahi.properties. 
	 * Use exists(true) to return in a single try.
	 * 
	 * @return true if the element exists on the browser
	 */
	public boolean exists(){
		return browser.exists(this);
	}

	/**
	 * Checks if an element is present on the browser.<br/>
	 * Internally calls browser.exists(this);
	 * 
	 * @param optimistic: if true will not retry till exists returns true.
	 * @return true if the element exists on the browser
	 */
	public boolean exists(boolean optimistic){
		return browser.exists(this, optimistic);
	}

	/**
	 * Fetches the value of a form field from the browser.<br/>
	 * Same as getValue().<br/>
	 * Internally calls browser.getValue(this)
	 * 
	 * @return value of the form field
	 * @throws ExecutionException
	 */
	public String value() throws ExecutionException {
		return getValue();
	}	

	/**
	 * Fetches the value of a form field from the browser.<br/>
	 * Same as value().<br/>
	 * Internally calls browser.getValue(this)
	 * 
	 * @return value of the form field
	 * @throws ExecutionException
	 */
	public String getValue() throws ExecutionException {
		return browser.getValue(this);
	}

	/**
	 * Returns the selected text visible in a select box (&lt;select&gt; tag).<br/>
	 * Same as getSelectedText().<br/>
	 * Internally calls browser.getSelectedText(this);
	 * 
	 * @return selected text
	 */	
	public String selectedText() throws ExecutionException {
		return getSelectedText();
	}	

	
	/**
	 * Checks for visibility of this element.<br/>
	 * If an element is hidden via style display attribute set to "none" or <br/> 
	 * if the element is hidden via style visibility attribute set to "hidden",<br/> 
	 * isVisible() returns false.
	 * 
	 * Retries a few times if false. This can be controlled with script.max_reattempts_on_error in sahi.properties. 
	 * Use exists(true) to return in a single try.
	 * 
	 * @return true if the element is visible on the screen
	 */

	public boolean isVisible() throws ExecutionException {
		return browser.isVisible(this, false);
	}	
	
	/**
	 * Checks for visibility of this element.<br/>
	 * If an element is hidden via style display attribute set to "none" or <br/> 
	 * if the element is hidden via style visibility attribute set to "hidden",<br/> 
	 * isVisible() returns false.
	 * 
	 * @param optimistic: if true, Sahi will return in a single try. If false, Sahi will retry a few times.
	 * @return true if the element is visible on the screen
	 */
	public boolean isVisible(boolean optimistic) throws ExecutionException {
		return browser.isVisible(this, optimistic);
	}	
	
	/**
	 * Returns the selected text visible in a select box (&lt;select&gt; tag).<br/>
	 * Same as selectedText().<br/>
	 * Internally calls browser.getSelectedText(this);
	 * 
	 * @return selected text
	 */	
	public String getSelectedText() throws ExecutionException {
		return browser.getSelectedText(this);
	}

	/**
	 * Returns true if the element contains the input text
	 * 
	 * @param text
	 * @return true if the element contains the input text
	 */
	public boolean containsText(String text) {
		return browser.containsText(this, text);
	}

	/**
	 * Returns true if the element's innerHTML contains the input html
	 * 
	 * @param html
	 * @return true if the element's innerHTML contains the input html
	 */
	public boolean containsHTML(String html) {
		return browser.containsHTML(this, html);
	}
	

	/**
	 * Returns the computed css style <br/>
	 * eg. browser.div("blackdiv").style("backgroundColor")
	 * 
	 * @param el
	 * @param attribute
	 * @return the computed css style 
	 */	
	public String style(String html) {
		return browser.style(this, html);
	}	
	
	/**
	 * Establishes an "in" relation with another element.<br/>
	 * Eg. {@code browser.link("delete").in(browser.cell("user1").parentNode()).click();}<br/>
	 * This clicks on the delete link in the same row as cell with content "user1"
	 * 
	 * 
	 * @param inEl
	 * @return Element with relation established
	 */
	public ElementStub in(ElementStub inEl) {
		return relation("in", inEl);
	}
	
	
	/**
	 * Establishes an "under" relation with another element.<br/>
	 * Checks for coordinate based alignment under a particular element within a specific threshold.<br/>
	 * Eg. {@code browser.checkbox(0).near(browser.cell("Ram")).under(browser.cell("Delete user"))}
	 * 
	 * @param underEl
	 * @return Element with relation established
	 */
	public ElementStub under(ElementStub underEl) {
		return relation("under", underEl);
	}
	
	/**
	 * Establishes a "near" relation with another element.<br/>
	 * Eg. {@code browser.link("delete").near(browser.cell("user1")).click();}<br/>
	 * This clicks on the delete link near a cell with content "user1"
	 * 
	 * 
	 * @param nearEl
	 * @return Element with relation established
	 */
	public ElementStub near(ElementStub nearEl) {
		return relation("near", nearEl);
	}

	private ElementStub relation(String relationType, ElementStub inEl) {
		Object[] newArray = new Object[this.identifiers.length + 1];
		System.arraycopy(this.identifiers, 0, newArray, 0, this.identifiers.length);
		newArray[this.identifiers.length] = new ElementStub(relationType, this.browser, inEl);
		this.identifiers = newArray;
		return this;
	}
	
	/**
	 * Fetches the string value of a property of this element.<br/>
	 * Eg.<br/>
	 * browser.div("content").fetch("innerHTML")
	 * 
	 * @return
	 * @throws ExecutionException
	 */	
	public String fetch(String string) throws ExecutionException {
		string = (string != null) ? (this + "." + string) : this.toString();
		String value = browser.fetch(string);
		if (value.equals("undefined") || value.equals("null")) return null;
		return value;
	}
	
	/**
	 * Returns true if the element is checked. Is meaningful only for radio buttons and checkboxes<br/>
	 * Eg.<br/>
	 * browser.radio("female").checked()
	 * 
	 * @return
	 * @throws ExecutionException
	 */	
	public boolean checked() throws ExecutionException {
		return browser.checked(this);
	}
	
	/**
	 * @return the first parentNode
	 */
	public ElementStub parentNode() {
		return new ElementStub("parentNode", this.browser, this);
	}	

	/**
	 * Returns the first parentNode of given tagName.<br/>
	 * Eg.<br/>
	 * browser.link("click me").parentNode("TABLE") will ignore TDs, TRs etc. 
	 * and will directly return the parent table.
	 * 
	 * @param tagName
	 * @return the first parentNode of given tagName
	 * 
	 */
	public ElementStub parentNode(String tagName) {
		return new ElementStub("parentNode", this.browser, this, tagName);
	}	

	private ElementStub parentNode(String tagName, String occurrence) {
		return new ElementStub("parentNode", this.browser, this, tagName, occurrence);
	}
	
	/**
	 * Returns the nth parentNode of given tagName.
	 * 
	 * @param tagName
	 * @param occurrence
	 * @return
	 */
	public ElementStub parentNode(String tagName, int occurrence) {
		return parentNode(tagName, "" + occurrence);
	}
	
	
	/**
	 * Returns a list of element stubs similar to this one<br/><br/>
	 * 
	 * Eg.<br/>
	 * <pre>
	 * browser.span("/Delete/").collectSimilar()
	 * browser.div("css-class-name").in(browser.div("container")).collectSimilar()
	 * </pre>
	 * 
	 * @return
	 */

	public List<ElementStub> collectSimilar() {
		int count = this.countSimilar();
		List<ElementStub> els = new ArrayList<ElementStub>();
		for (int i=0; i<count; i++) {
			final ElementStub el = new ElementStub(elementType, browser, identifiers);
			el.setIndex(i);
			els.add(el);
		}
		return els;
	}
	
	// called only from test
	void setIndex(int index) {
		this.index = index;
	}

	/**
	 * Returns a count of elements similar to this
	 * 
	 * @return
	 */
	public int countSimilar(){
		StringBuffer sb = new StringBuffer();
		boolean start = true; 
		for (Object o: identifiers){
			if (!start) {
				sb.append(", ");
			}
			start = false;
			sb.append(getArgument(o, -1));
		}
		String toFetch = "_sahi._count(\"" + "_" + elementType + "\", "  + sb.toString() + ")"; 
		final String countStr = browser.fetch(toFetch);
		try {
			return Integer.parseInt(countStr);
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * Performs a keyUp on this element.<br/>
	 * Internally calls browser.keyUp;
	 * @param keyCode
	 * @param charCode
	 */
	public void keyUp(int keyCode, int charCode) {
		browser.keyUp(this, keyCode, charCode);
	}

	
	/**
	 * Performs a keyDown on this element.<br/>
	 * Internally calls browser.keyDown;
	 * @param keyCode
	 * @param charCode
	 */
	public void keyDown(int keyCode, int charCode) {
		browser.keyDown(this, keyCode, charCode);
	}
}
