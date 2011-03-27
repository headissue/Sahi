package net.sf.sahi.client;

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
 * BrowserElements represents the different Accessor APIs that Sahi exposes.<br/>
 * Each of the APIs returns an ElementStub which is a representation of a particular 
 * HTML DOM element on the browser.<br/>
 * 
 * Have a look at <a href="http://sahi.co.in/w/browser-accessor-apis">Sahi Browser Accessor APIS</a>
 * for more information on each accessor.
 * 
 * Note that regular expressions based accessors are different from Sahi's native accessors in that they are quoted as Strings:<br/><br/>
 * Example: <br/>
 * 		<code>_link(/visible .*ext/)</code> in Sahi Script is equivalent to <br>
 * 		<code>browser.link("/visible .*ext/")</code>
 * <br/><br/>
 * Some examples using various identifiers:
 * 
 * <table border=1>
 * <tr><th>Type</th><th>Example</th></tr>
 * <tr><td>Pure index</td><td>browser.link(5)</td></tr>
 * <tr><td>id</td><td>browser.textbox("searchbox")</td></tr>
 * <tr><td>text</td><td>browser.cell("Ram")</td></tr>
 * <tr><td>value</td><td>browser.button("Click me")<br/>applies for button and submit only</td></tr>
 * <tr><td>className (css)</td><td>browser.cell("delete rounded-corner")</td></tr>
 * <tr><td>Regular Expression.<br/>(Can be used for any accessor type)</td><td>browser.button("/Click/")<br/>browser.cell("/delete .*corner/")</td></tr>
 * <tr><td>Index combination.<br/>(Can be used for any accessor type)</td><td>browser.link("delete[1]")<br/>browser.cell("/delete .*corner/[3]")</td></tr>
 * </table> 
 * <br/>
 * Some examples using DOM and Positional relations:
 * <table border=1>
 * <tr><td>Simple</td><td>browser.button("id")</td></tr>
 * <tr><td>Using near</td><td>browser.textbox("q").near(browser.cell("Ram"))</td></tr>
 * <tr><td>Using near and under</td><td>browser.checkbox(0).near(browser.cell("Ram")).under(browser.cell("Delete user"))</td></tr>
 * <tr><td>Using in</td><td>browser.link("delete").in(browser.table("summary"))</td></tr>
 * </table> 
 *  
 *  <br/>
 * Thanks to Srijayanth Sridhar for converting the Sahi Script documentation to Javadocs.
 * 
 */
public abstract class BrowserElements {
	protected Browser browser;
	/**
	 * Defines a generic accessor. <br/>
	 * A javascript eval will be performed on the parameter passed.<br/>
	 * Use this to execute custom javascript accessors when no other Sahi accessors can be used.<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.accessor("document.form1.textelement1").setValue("abcd");</code><br/>
	 * 
	 * @param args
	 * @return a stub representing the accessor
	 */
	public ElementStub accessor(Object... args) {return new ElementStub("accessor", browser, args);}
	/**
	 * Defines a button element. <br/>
	 * Usage: browser.button(identifier)<br/>
	 * HTML: {@code <input type="button" name="name" id="id" value="value">}<br/>
	 * HTML: {@code <button type="button" name="name" id="id">value</button>}<br/>
	 * Identifier: index, value, name, id<br/>
	 * Example:<br/>
	 * <code>browser.button("Sign in").click()</code><br/>
	 * 
	 * @param Identifier: can be index, value, name, id
	 * @return a stub representing the element
	 */
	public ElementStub button(Object... args) {return new ElementStub("button", browser, args);}

	/**
	 * Defines a checkbox. <br/>
	 * Usage: browser.checkbox(identifier)<br/>
	 * HTML: {@code <input type="checkbox" name="name" id="id" value="value">}<br/>
	 * Identifier: index, name, id, className<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.checkbox("Remember Me?").check()</code><br/>
	 * 
	 * @param Identifier: can be index, name, id, className 
	 * @return a stub representing the element
	 */
	public ElementStub checkbox(Object... args) {return new ElementStub("checkbox", browser, args);}
	/**
	 * Defines a image. <br/>
	 * Usage: browser.image(identifier)<br/>
	 * HTML: {@code <img src="/path/to/images/add.gif" id="id" alt="alt" title="title">}<br/>
	 * Identifier: index, title or alt, id, file name from src<br/>
	 * Notes: Use browser.image("add.gif") for an image with src "/path/to/images/add.gif" <br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.image("icon.png").click()</code><br/>
	 * 
	 * @param Identifier: can be index, title or alt, id, file name from src
	 * @return a stub representing the element
	 */
	public ElementStub image(Object... args) {return new ElementStub("image", browser, args);}
	/**
	 * Defines a image submit button. <br/>
	 * Usage: browser.image(identifier)<br/>
	 * HTML:{@code <input type="image" name="name" id="id" value="value" alt="alt" title="title" src="/images/file.gif">}<br/>
	 * Identifier:index, tilte/alt, name, id <br/>
	 * Notes: (Add support to treat this like browser.image)<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.imageSubmitButton("Sign In")</code><br/>
	 * 
	 * @param Identifier: can be index, tilte/alt, name, id
	 * @return a stub representing the element
	 */
	public ElementStub imageSubmitButton(Object... args) {return new ElementStub("imageSubmitButton", browser, args);}
	/**
	 * Defines a link. <br/>
	 * Usage: browser.link(identifier) <br/>
	 * HTML: {@code <a href="http://u/r/l" id="id">visible text</a>} <br/>
	 * Identifier: index, visible text, id<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.link("Continue").click()</code><br/>
	 * 
	 * @param Identifier: can be index, visible text, id
	 * @return a stub representing the element
	 */
	public ElementStub link(Object... args) {return new ElementStub("link", browser, args);}
	/**
	 * Defines a password field. <br/>
	 * Usage: browser.password(identifier) <br/>
	 * HTML: {@code <input type="password" name="name" id="id" value="value">}<br/>
	 * Identifier: index, name, id, className<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.password(0).setValue("!abcd1234")</code><br/>
	 * 
	 * @param Identifier: can be index, name, id, className
	 * @return a stub representing the element
	 */
	public ElementStub password(Object... args) {return new ElementStub("password", browser, args);}
	/**
	 * Defines a radio button. <br/>
	 * Usage: browser.radio(identifier) <br/>
	 * HTML: {@code <input type="radio" name="name" id="id" value="value">}<br/>
	 * Identifier: index, name, id, className<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.radio("Red").click()</code><br/>
	 *  
	 * @param Identifier: can be index, name, id, className
	 * @return a stub representing the element
	 */
	public ElementStub radio(Object... args) {return new ElementStub("radio", browser, args);}
	/**
	 * Defines a select on a drop down. <br/>
	 * Usage: browser.select(identifier) <br/>
	 * HTML: {@code <select name="name" id="id"></select>}<br/>
	 * Identifier: index, name, id, className<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.select("colors").choose("red")</code><br/>
	 * <code>browser.select("colors").choose(new String[]{"red", "blue"})</code><br/>
	 *  
	 * @param Identifier: can be index, name, id, className
	 * @return a stub representing the element
	 */
	public ElementStub select(Object... args) {return new ElementStub("select", browser, args);}
	/**
	 * Defines a submit button. <br/>
	 * Usage: browser.submit(identifier)<br/>
	 * HTML: {@code <input type="submit" name="name" id="id" value="value">}<br/>
	 * HTML: {@code <button type="submit" name="name" id="id">value</button>}<br/>
	 * Identifier: index, value, name, id<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.submit("Sign in").click()</code><br/>
	 * 
	 * @param Identifier: can be index, value, name, id
	 * @return a stub representing the element
	 */
	public ElementStub submit(Object... args) {return new ElementStub("submit", browser, args);}
	/**
	 * Defines a textarea. <br/>
	 * Usage: browser.textarea(identifier) <br/>
	 * HTML: {@code <textarea name="name" id="id">text</textarea>}<br/>
	 * Identifier: index, name, id, className<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.textarea("comments").setValue("A simple comment")</code><br/>
	 * 
	 * @param Identifier: can be index, name, id, className
	 * @return a stub representing the element
	 */
	public ElementStub textarea(Object... args) {return new ElementStub("textarea", browser, args);}
	/**
	 * Defines a textbox. <br/>
	 * Usage: browser.textbox(identifier) <br/>
	 * HTML: {@code <input type="textbox" name="name" id="id" value="value">}<br/>
	 * Identifier: index, name, id, className<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.textbox("username").setValue("Admin")</code><br/>
	 * 
	 * @param Identifier: can be index, name, id, className
	 * @return a stub representing the element
	 */
	public ElementStub textbox(Object... args) {return new ElementStub("textbox", browser, args);}
	/**
	 * Defines a cell. <br/>
	 * Usage: browser.cell(identifier) <br/>
	 * HTML: {@code <td id="id">text</td>}<br/>
	 * Identifier: index, id, text<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.cell("Click Me").click()</code><br/>
	 * 
	 * Note: This API also works like this:
	 * <code>browser.cell(browser.table("tableId"), "textInRow", "textInColumn").click()</code><br/>
	 * 
	 * @param Identifier: can be index, id, text
	 * @return a stub representing the element
	 */
	public ElementStub cell(Object... args) {return new ElementStub("cell", browser, args);}
	/**
	 * Defines a table. <br/>
	 * Usage: browser.table(identifier) <br/>
	 * HTML: {@code <table id="id">...</table>}<br/>
	 * Identifier: index, id<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.table("scores")</code><br/>
	 * 
	 * will return the table identified by "scores"
	 * 
	 *  
	 * @param Identifier: can be index, id
	 * @return a stub representing the element
	 */
	public ElementStub table(Object... args) {return new ElementStub("table", browser, args);}
	/**
	 * Defines an element accessed by id. <br/>
	 * Usage: browser.byId(identifier) <br/>
	 * HTML: {@code <anytag id="id" ></anytag>}<br/>
	 * Identifier: id<br/>
	 * Notes: This can be used for any tag with an id. This API does not accept regular expressions or indexes.
	 * <br/>
	 * Example:<br/>
	 * <code>browser.byId("main_form_button").click()</code><br/>
	 * 
	 *  
	 * @param Identifier: can be id
	 * @return a stub representing the element
	 */
	public ElementStub byId(Object... args) {return new ElementStub("byId", browser, args);}
	/**
	 * Defines an element accessed by class name. <br/>
	 * Usage: byClassName(identifier) <br/>
	 * HTML: {@code <anytag class="className">text</anytag>}<br/>
	 * Identifier: className<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.byClassName("rounded-corners-button").click()</code><br/>
	 * 
	 * will eval to the first button with css id "rounded-corners-button" on the browser then click on it.
	 *  
	 * @param Identifier: can be className
	 * @return a stub representing the element
	 */
	public ElementStub byClassName(Object... args) {return new ElementStub("byClassName", browser, args);}
	/**
	 * Defines an element accessed by xpath. <br/>
	 * Usage: browser.byXPath(identifier) <br/>
	 * Identifier: xpath expression as string<br/>
	 * Notes: This is a convenience method for people moving from Selenium or other tools to Sahi.<br/>
	 * <br/>
	 * Example 1:<br/>
	 * <code>browser.byXPath("//div/span").click()</code><br/>
	 * 
	 * will find the element identified by the xpath "//div/span" on the browser then invoke a click on it.<br/>
	 * <br/>
	 * Example 2: <br/>
	 * browser.byXPath("//table[3]//tr[1]/td[2]") <br/>
	 *  
	 * @param Identifier: can be xpath expression as string
	 * @return a stub representing the element
	 */
	public ElementStub byXPath(Object... args) {return new ElementStub("byXPath", browser, args);}
	/**
	 * Defines a bySeleniumLocator. <br/>
	 * Usage: browser.bySeleniumLocator(identifier) <br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.bySeleniumLocator("//div/span")</code><br/>
	 * 
	 * will find the element identified by the "//div/span" on the browser according to Selenium's locator strategies.
	 * 
	 *  
	 * @param args
	 * @return a stub representing the element
	 */
	public ElementStub bySeleniumLocator (Object... args) {return new ElementStub("bySeleniumLocator", browser, args);}
	/**
	 * Defines a row(tr) within a table element. <br/>
	 * Usage: browser.row(identifier) <br/>
	 * HTML: {@code <tr><td>te</td><td>xt</td></tr>}<br/>
	 * Identifier: id, className, text, index<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.row(0).in(browser.table("scores"))</code><br/>
	 * 
	 * will return the 1st row in table identified by "scores"
	 * 
	 *  
	 * @param Identifier: can be id, className, text, index
	 * @return a stub representing the accessor
	 */
	public ElementStub row(Object... args) {return new ElementStub("row", browser, args);}
	/**
	 * Defines a div element. <br/>
	 * Usage: browser.div(identifier) <br/>
	 * HTML: {@code <div id="id">text</div>}<br/>
	 * Identifier: index, id, text<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.div("Click me").in(browser.table("scores")).click()</code><br/>
	 * 
	 * will select the 1st row identified by text "Click me" in the table marked "scores" and invoke a click on it.
	 * 
	 *  
	 * @param Identifier: can be index, id, text
	 * @return a stub representing the accessor
	 */
	public ElementStub div(Object... args) {return new ElementStub("div", browser, args);}
	/**
	 * Defines a span element. <br/>
	 * Usage: browser.span(identifier) <br/>
	 * HTML: {@code <span id="id">text</span>}<br/>
	 * Identifier: index, id, text<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.span("login_btn").click()</code><br/>
	 * 
	 * will find a span element matching the parameter "login_btn" and then invoke a click on it.
	 * 
	 *  
	 * @param Identifier: can be index, id, text
	 * @return a stub representing the element
	 */
	public ElementStub span(Object... args) {return new ElementStub("span", browser, args);}

	/**
	 * Defines a spandiv element. <br/>
	 * Usage: browser.spandiv(identifier) <br/>
	 * HTML: {@code <span id="id">text</span> or <div id="id">text</div>}<br/>
	 * Identifier: index, id, text<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.spandiv("login_btn").click()</code><br/>
	 * 
	 * will find a span element matching the parameter "login_btn" and then invoke a click on it.
	 * 
	 *  
	 * @param Identifier: can be index, id, text
	 * @return a stub representing the element
	 */
	@Deprecated
	public ElementStub spandiv(Object... args) {return new ElementStub("spandiv", browser, args);}
	/**
	 * Defines an option element. <br/>
	 * Usage: browser.option(identifier) <br/>
	 * HTML: {@code <option id="id" value="value">text</option>}<br/>
	 * Identifier: text, value, id, index<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.option("red").click()</code><br/>
	 * 
	 * will choose "red" from a dropdown.
	 * 
	 *  
	 * @param Identifier: can be text, value, id, index
	 * @return a stub representing the element
	 */
	public ElementStub option(Object... args) {return new ElementStub("option", browser, args);}
	/**
	 * Defines a form's reset button. <br/>
	 * Usage: browser.reset(identifier) <br/>
	 * HTML: {@code <input type="reset" name="name" id="id" value="value">}<br/>
	 * Identifier: index, name, id, className<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.reset("Start Over").click()</code><br/>
	 * 
	 * will click the reset button identified by "Start Over"
	 *  
	 * @param Identifier: can be index, name, id, className
	 * @return a stub representing the element
	 */
	public ElementStub reset(Object... args) {return new ElementStub("reset", browser, args);}
	/**
	 * Defines a file element <br/>
	 * Usage: browser.file(identifier) <br/>
	 * HTML: {@code <input type="file" name="name" id="id" value="value">}<br/>
	 * Identifier: index, name, id, className<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.file("uploadme")</code><br/>
	 * 
	 * Use with setFile. Eg. <code>browser.file("uploadme").setFile("D:/my/file/path/file.txt")</code><br/>
	 *  
	 * @param Identifier: can be index, name, id, className
	 * @return a stub representing the element
	 */
	public ElementStub file(Object... args) {return new ElementStub("file", browser, args);}
	/**
	 * Identifies an element by text. <br/>
	 * Usage: browser.byText(identifier) <br/>
	 * HTML: {@code <anytag>text</anytag>}<br/>
	 * Identifier: text<br/>
	 * Notes: This can be used for any tag with text.
	 * <br/>
	 * Example:<br/>
	 * <code>browser.byText("Username", "TD")</code><br/>
	 * 
	 * will search for an element that matches "Username" within TD tags.
	 * 
	 *  
	 * @param Identifier: can be text
	 * @return a stub representing the element
	 */
	public ElementStub byText(Object... args) {return new ElementStub("byText", browser, args);}
	/**
	 * Defines a browser cookie. <br/>
	 * Usage: browser.cookie(identifier) <br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.cookie("session_id")</code><br/>
	 * 
	 * @param args
	 * @return a stub representing the element
	 */
	public ElementStub cookie(Object... args) {return new ElementStub("cookie", browser, args);}
	/**
	 * Returns position of an element. <br/>
	 * Usage: browser.position(identifier) <br/>
	 * Notes: Returns an array with the element’s x, y coordinate in pixels.<br/>
	 * <br/>
	 * Example 1:<br/>
	 * <code>browser.position(brower.div("id"))</code><br/>
	 * 
	 * will return the x, y coordinates of the div identified by "id", in pixels<br/>
	 * <br/>
	 * Example 2: <br/>
	 * <code>browser.position(browser.div("id")) may return [100, 180]</code> <br/>
	 * @param args
	 * @return a stub representing the element
	 */
	public ElementStub position(Object... args) {return new ElementStub("position", browser, args);}
	/**
	 * Defines a label. <br/>
	 * Usage: browser.label(identifier) <br/>
	 * HTML: {@code <label id="id">text</label>}<br/>
	 * Identifier: index, id, text<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.label("id")</code><br/>
	 * 
	 * will return the label identified by "id"
	 * 
	 *  
	 * @param Identifier: can be index, id, text
	 * @return a stub representing the element
	 */
	public ElementStub label(Object... args) {return new ElementStub("label", browser, args);}
	/**
	 * Defines a list. <br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.list("to_do_items")</code><br/>
	 * 
	 * will return a list identified by "to_do_items"
	 * 
	 *  
	 * @param args
	 * @return a stub representing the element
	 */
	public ElementStub list(Object... args) {return new ElementStub("list", browser, args);}
	/**
	 * Defines a listItem. <br/>
	 * Usage: browser.lisItem(identifier) <br/>
	 * HTML: {@code <li id="id">text</li>}<br/>
	 * Identifier: index, id, text (<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.lisItem("red")</code><br/>
	 * 
	 * will return the listItem identified by "red".
	 * 
	 *  
	 * @param Identifier: can be index, id, text 
	 * @return a stub representing the element
	 */
	public ElementStub listItem(Object... args) {return new ElementStub("listItem", browser, args);}
	/**
	 * Defines a parent node. <br/>
	 * Usage: browser.parentNode(identifier) <br/>
	 * HTML: {@code <div id="id"><a href="">aElement</a></div>}<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.parentNode(browser.listItem("red"))</code><br/>
	 *  
	 * will return the parent node in this case, the list element under which listItem "red" is contained 
	 * 
	 *  
	 * @param args
	 * @return a stub representing the element
	 */
	public ElementStub parentNode(Object... args) {return new ElementStub("parentNode", browser, args);}
	/**
	 * Defines a parent cell. <br/>
	 * Usage: browser.parentCell(identifier) <br/>
	 * HTML: {@code <td id="id"><a href="">aElement</a></td>}<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.parentCell(browser.link("continue"))</code><br/>
	 * 
	 * will return the parent cell["tr" HTML element] containing the link "continue" 
	 * 
	 *  
	 * @param args
	 * @return a stub representing the element
	 */
	public ElementStub parentCell(Object... args) {return new ElementStub("parentCell", browser, args);}
	/**
	 * Defines a parent row. <br/>
	 * Usage: browser.parentRow(identifier) <br/>
	 * HTML: {@code <tr><td>aCell</td></tr>}<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.parentRow(browser.cell("red"))</code><br/>
	 * 
	 * will return the parent row containing the cell containing the text "red"
	 * 
	 *  
	 * @param args
	 * @return a stub representing the element
	 */
	public ElementStub parentRow(Object... args) {return new ElementStub("parentRow", browser, args);}
	/**
	 * Defines a parent table. <br/>
	 * Usage: browser.parentTable(identifier) <br/>
	 * HTML: {@code <table class="api"><tr><td>sahi</td></tr></table>} <br/>
	 * <br/>
	 * Example 1:<br/>
	 * <code>browser.parentTable(browser.cell("sahi"))</code><br/>
	 * 
	 * will return parent table containing the cell identified by "sahi" <br/><br/>
	 * Example 2:<br/>
	 * <code>browser.parentTable(browser.row("student"))</code><br/>
	 * 
	 * will return parent table containing the row identified by "student" 
	 *  
	 *  
	 * @param args
	 * @return a stub representing the element
	 */
	public ElementStub parentTable(Object... args) {return new ElementStub("parentTable", browser, args);}
	/**
	 * Defines rte. <br/>
	 * Usage: browser.rte(identifier) <br/>
	 * HTML: {@code <iframe src="" name="name" id="id" ></iframe>}<br/>
	 * Identifier: index, id, name<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.rte(0)</code><br/>
	 * 
	 * will find the first rte(iframe) element on a page. 
	 * 
	 *  
	 * @param Identifier: can be index, id, name
	 * @return a stub representing the element
	 */
	public ElementStub rte(Object... args) {return new ElementStub("rte", browser, args);}
	/**
	 * Defines an iframe. <br/>
	 * Usage: browser.iframe(identifier) <br/>
	 * HTML: {@code <iframe src="" name="name" id="id" ></iframe>}<br/>
	 * Identifier: index, id, name <br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.iframe("documentation_text").click()</code><br/>
	 * 
	 * will find an iframe identified by "documentation_text"
	 * 
	 *  
	 * @param Identifier: can be index, id, name
	 * @return a stub representing the element
	 */
	public ElementStub iframe(Object... args) {return new ElementStub("iframe", browser, args);}
	/**
	 * Defines a table header. <br/>
	 * Usage: browser.tableHeader(identifier) <br/>
	 * HTML: {@code <th id="id">text</th>}<br/>
	 * Identifier: text, id<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.tableHeader("Price")</code><br/>
	 * 
	 * will find a table header with text "Price"
	 * 
	 *  
	 * @param Identifier: can be text, id
	 * @return a stub representing the element
	 */
	public ElementStub tableHeader(Object... args) {return new ElementStub("tableHeader", browser, args);}
	/**
	 * Defines an h1 element. <br/>
	 * Usage: browser.heading1(identifier)<br/>
	 * HTML: {@code <h1 id="id">text</h1>}<br/>
	 * Identifier: text, id<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.heading1("Scores")</code><br/>
	 * 
	 * @param Identifier: can be text, id
	 * @return a stub representing the element
	 */
	public ElementStub heading1(Object... args) {return new ElementStub("heading1", browser, args);}
	/**
	 * Defines an h2 element. <br/>
	 * Usage: browser.heading2(identifier) <br/>
	 * HTML: {@code <h2 id="id">text</h2>}<br/>
	 * Identifier: text, id<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.heading2("Scores")</code><br/>
	 * 
	 * will find an h2 element with text "Scores"
	 *  
	 * @param Identifier: can be text, id
	 * @return a stub representing the element
	 */
	public ElementStub heading2(Object... args) {return new ElementStub("heading2", browser, args);}
	/**
	 * Defines an h3 element. <br/>
	 * Usage: browser.heading3(identifier) <br/>
	 * HTML: {@code <h3 id="id">text</h3>}<br/>
	 * Identifier: text, id<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.heading3("Scores")</code><br/>
	 * 
	 * will find an h3 element with text "Scores"
	 *  
	 * @param Identifier: can be text, idargs
	 * @return a stub representing the element
	 */	
	public ElementStub heading3(Object... args) {return new ElementStub("heading3", browser, args);}
	/**
	 * Defines an h4 element. <br/>
	 * Usage: browser.heading4(identifier) <br/>
	 * HTML: {@code <h4 id="id">text</h4>}<br/>
	 * Identifier: text, id<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.heading4("Scores")</code><br/>
	 * 
	 * will find an h4 element with text "Scores"
	 *  
	 * @param Identifier: can be text, id
	 * @return a stub representing the element
	 */
	public ElementStub heading4(Object... args) {return new ElementStub("heading4", browser, args);}
	/**
	 * Defines an h5 element. <br/>
	 * Usage: browser.heading5(identifier) <br/>
	 * HTML: {@code <h5 id="id">text</h5>}<br/>
	 * Identifier: text, id<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.heading5("Scores")</code><br/>
	 * 
	 * will find an h5 element with text "Scores"
	 *  
	 * @param Identifier: can be text, id
	 * @return a stub representing the element
	 */
	public ElementStub heading5(Object... args) {return new ElementStub("heading5", browser, args);}
	/**
	 * Defines an h6 element. <br/>
	 * Usage: browser.heading6(identifier) <br/>
	 * HTML: {@code <h6 id="id">text</h6>}<br/>
	 * Identifier: text, id<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.heading6("Scores")</code><br/>
	 * 
	 * will find an h6 element with text "Scores"
	 *  
	 * @param Identifier: can be text, id
	 * @return a stub representing the element
	 */
	public ElementStub heading6(Object... args) {return new ElementStub("heading6", browser, args);}
	/**
	 * Defines an HTML input element of type hidden. <br/>
	 * Usage: browser.hidden(identifier) <br/>
	 * HTML: {@code <input type="hidden" name="name" id="id" value="value">}<br/>
	 * Identifier: index, name, id <br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.hidden("password")</code><br/>
	 * 
	 * will find an input element of type "hidden" identified by "password"
	 * 
	 *  
	 * @param Identifier: can be index, name, id, className 
	 * @return a stub representing the element
	 */
	public ElementStub hidden(Object... args) {return new ElementStub("hidden", browser, args);}
	/**
	 * Defines an area element. <br/>
	 * Usage: browser.area(identifier) <br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.area(0).in(browser.map("planetmap")).click()</code><br/>
	 * 
	 * will find the first area in the map named "planetmap" and invoke a click on it.
	 * 
	 *  
	 * @param args
	 * @return a stub representing the element
	 */
	public ElementStub area(Object... args) {return new ElementStub("area", browser, args);}
	/**
	 * Defines a map. <br/>
	 * Usage: browser.map(identifier) <br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.map("planetmap")</code><br/>
	 * 
	 * will return the map identified by "planetmap"
	 * 
	 *  
	 * @param args
	 * @return a stub representing the element
	 */
	public ElementStub map(Object... args) {return new ElementStub("map", browser, args);}
	/**
	 * Defines italic. <br/>
	 * Usage: browser.italic(identifier) <br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.italic("Text in Italics")</code><br/>
	 * 
	 * will return the element with text "text in Italics" which is italicized
	 * 
	 *  
	 * @param args
	 * @return a stub representing the element
	 */
	public ElementStub italic(Object... args) {return new ElementStub("italic", browser, args);}
	/**
	 * Defines bold. <br/>
	 * Usage: browser.bold(identifier) <br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.bold("Text in Bold")</code><br/>
	 * 
	 * will return the element with text "text in bold" which is in bold
	 * 
	 *  
	 * @param args
	 * @return a stub representing the element
	 */
	public ElementStub bold(Object... args) {return new ElementStub("bold", browser, args);}
	/**
	 * Defines emphasis. <br/>
	 * Usage: browser.emphasis(identifier) <br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.emphasis("This text is emphasised")</code><br/>
	 * 
	 * will return the element with text "This text is emphasised" which is emphasised
	 * 
	 *  
	 * @param args
	 * @return a stub representing the element
	 */
	public ElementStub emphasis(Object... args) {return new ElementStub("emphasis", browser, args);}
	/**
	 * Defines strong. <br/>
	 * Usage: browser.strong(identifier) <br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.strong("This is strong")</code><br/>
	 * 
	 * will return the element with text "This is strong" which is strong.
	 * 
	 *  
	 * @param args
	 * @return a stub representing the element
	 */
	public ElementStub strong(Object... args) {return new ElementStub("strong", browser, args);}
	/**
	 * Defines preformatted. <br/>
	 * Usage: browser.preformatted(identifier) <br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.preformatted("quote_block")</code><br/>
	 * 
	 * will find the first preformatted element identified by "quote_block" 
	 * 
	 *  
	 * @param args
	 * @return a stub representing the element
	 */
	public ElementStub preformatted(Object... args) {return new ElementStub("preformatted", browser, args);}
	/**
	 * Defines code. <br/>
	 * Usage: browser.code(identifier) <br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.code("code_block")</code><br/>
	 * 
	 * will find the first html code element identified by "code_block" 
	 * 
	 *  
	 * @param args
	 * @return a stub representing the element
	 */
	public ElementStub code(Object... args) {return new ElementStub("code", browser, args);}
	/**
	 * Defines block quote. <br/>
	 * Usage: browser.blockquote(identifier) <br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.blockquote("block_quote_element")</code><br/>
	 * 
	 * will return the html blockquote element identified by "block_quote_element".
	 * 
	 *  
	 * @param args
	 * @return a stub representing the element
	 */
	public ElementStub blockquote(Object... args) {return new ElementStub("blockquote", browser, args);}
	/**
	 * Defines xy. <br/>
	 * Usage: browser.xy(identifier) <br/>
	 * Notes: Specifies the coordinates on element where the event is fired. <br/>
	 * Negative values can be given to specify offset from right and bottom. <br/>
	 * <br/>
	 * Example 1:<br/>
	 * <code>browser.xy(browser.div("div_id"), 10, 20).click()</code><br/>
	 * 
	 * will click 10 pixels from the left and 20 pixels from the top of the div, identified by "div_id" <br/>
	 * <br/>
	 * Example 2:<br/>
	 * <code>browser.xy(browser.button("id"), -5, -10)).click()</code> <br/>
	 * clicks inside the button, 5px from the right and 10px from the bottom.
	 *  
	 * @param args
	 * @return a stub representing the element
	 */
	public ElementStub xy(Object... args) {return new ElementStub("xy", browser, args);}

	/**
	 * Defines a datebox. <br/>
	 * Usage: browser.datebox(identifier) <br/>
	 * HTML: {@code <input type="date" name="name" id="id" value="value">}<br/>
	 * Identifier: index, name, id, className<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.datebox("today").setValue("2011-09-11")</code><br/>
	 * 
	 * @param Identifier: can be index, name, id, className
	 * @return a stub representing the element
	 */
	public ElementStub datebox(Object... args) {return new ElementStub("datebox", browser, args);}

	/**
	 * Defines a datetimebox. <br/>
	 * Usage: browser.datetimebox(identifier) <br/>
	 * HTML: {@code <input type="datetime" name="name" id="id" value="value">}<br/>
	 * Identifier: index, name, id, className<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.datetimebox("today").setValue("2011-09-12T23:56Z")</code><br/>
	 * 
	 * @param Identifier: can be index, name, id, className
	 * @return a stub representing the element
	 */
	public ElementStub datetimebox(Object... args) {return new ElementStub("datetimebox", browser, args);}

	/**
	 * Defines a datetimelocalbox. <br/>
	 * Usage: browser.datetimelocalbox(identifier) <br/>
	 * HTML: {@code <input type="datetimelocal" name="name" id="id" value="value">}<br/>
	 * Identifier: index, name, id, className<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.datetimelocalbox("today").setValue("2011-09-12T01:00")</code><br/>
	 * 
	 * @param Identifier: can be index, name, id, className
	 * @return a stub representing the element
	 */
	public ElementStub datetimelocalbox(Object... args) {return new ElementStub("datetimelocalbox", browser, args);}
	
	/**
	 * Defines a emailbox. <br/>
	 * Usage: browser.emailbox(identifier) <br/>
	 * HTML: {@code <input type="email" name="name" id="id" value="value">}<br/>
	 * Identifier: index, name, id, className<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.emailbox("username").setValue("a@example.com")</code><br/>
	 * 
	 * @param Identifier: can be index, name, id, className
	 * @return a stub representing the element
	 */
	public ElementStub emailbox(Object... args) {return new ElementStub("emailbox", browser, args);}

	/**
	 * Defines a monthbox. <br/>
	 * Usage: browser.monthbox(identifier) <br/>
	 * HTML: {@code <input type="month" name="name" id="id" value="value">}<br/>
	 * Identifier: index, name, id, className<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.monthbox("month").setValue("2011-01")</code><br/>
	 * 
	 * @param Identifier: can be index, name, id, className
	 * @return a stub representing the element
	 */
	public ElementStub monthbox(Object... args) {return new ElementStub("monthbox", browser, args);}
	
	/**
	 * Defines a numberbox. <br/>
	 * Usage: browser.numberbox(identifier) <br/>
	 * HTML: {@code <input type="number" name="name" id="id" value="value">}<br/>
	 * Identifier: index, name, id, className<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.numberbox("points").setValue("1000")</code><br/>
	 * 
	 * @param Identifier: can be index, name, id, className
	 * @return a stub representing the element
	 */
	public ElementStub numberbox(Object... args) {return new ElementStub("numberbox", browser, args);}

	/**
	 * Defines a rangebox. <br/>
	 * Usage: browser.rangebox(identifier) <br/>
	 * HTML: {@code <input type="range" name="name" id="id" value="value" min="1" max="10">}<br/>
	 * Identifier: index, name, id, className<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.rangebox("range").setValue("3")</code><br/>
	 * 
	 * @param Identifier: can be index, name, id, className
	 * @return a stub representing the element
	 */
	public ElementStub rangebox(Object... args) {return new ElementStub("rangebox", browser, args);}
	
	/**
	 * Defines a searchbox. <br/>
	 * Usage: browser.searchbox(identifier) <br/>
	 * HTML: {@code <input type="search" name="name" id="id" value="value">}<br/>
	 * Identifier: index, name, id, className<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.searchbox("home").setValue("find me")</code><br/>
	 * 
	 * @param Identifier: can be index, name, id, className
	 * @return a stub representing the element
	 */
	public ElementStub searchbox(Object... args) {return new ElementStub("searchbox", browser, args);}

	
	/**
	 * Defines a telbox. <br/>
	 * Usage: browser.telbox(identifier) <br/>
	 * HTML: {@code <input type="tel" name="name" id="id" value="value">}<br/>
	 * Identifier: index, name, id, className<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.telbox("home").setValue("1212192121")</code><br/>
	 * 
	 * @param Identifier: can be index, name, id, className
	 * @return a stub representing the element
	 */
	public ElementStub telbox(Object... args) {return new ElementStub("telbox", browser, args);}

	/**
	 * Defines a timebox. <br/>
	 * Usage: browser.timebox(identifier) <br/>
	 * HTML: {@code <input type="time" name="name" id="id" value="value">}<br/>
	 * Identifier: index, name, id, className<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.datebox("username").setValue("10:20:20.000")</code><br/>
	 * 
	 * @param Identifier: can be index, name, id, className
	 * @return a stub representing the element
	 */
	public ElementStub timebox(Object... args) {return new ElementStub("timebox", browser, args);}

	/**
	 * Defines a urlbox. <br/>
	 * Usage: browser.urlbox(identifier) <br/>
	 * HTML: {@code <input type="url" name="homepage" id="id" value="value">}<br/>
	 * Identifier: index, name, id, className<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.urlbox("homepage").setValue("http://sahi.co.in/")</code><br/>
	 * 
	 * @param Identifier: can be index, name, id, className
	 * @return a stub representing the element
	 */
	public ElementStub urlbox(Object... args) {return new ElementStub("urlbox", browser, args);}
	
	/**
	 * Defines a weekbox. <br/>
	 * Usage: browser.weekbox(identifier) <br/>
	 * HTML: {@code <input type="week" name="name" id="id" value="value">}<br/>
	 * Identifier: index, name, id, className<br/>
	 * <br/>
	 * Example:<br/>
	 * <code>browser.weekbox("week").setValue("2011-W04")</code><br/>
	 * 
	 * @param Identifier: can be index, name, id, className
	 * @return a stub representing the element
	 */
	public ElementStub weekbox(Object... args) {return new ElementStub("weekbox", browser, args);}
}
