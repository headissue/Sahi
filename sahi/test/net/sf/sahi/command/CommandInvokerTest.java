/**
 * Sahi - Web Automation and Test Tool
 *
 * Copyright  2006  V Narayan Raman
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * Authors: Dodda Rampradeep and Deepak Lewis
 */
package net.sf.sahi.command;

import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpResponse;
import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

import java.net.URL;
import java.io.File;

public class CommandInvokerTest extends MockObjectTestCase {
    private CommandInvoker commandInvoker;
    private final File HELPFILE = new File("help.txt");

    protected void setUp() throws Exception {
        commandInvoker = new CommandInvoker();
        HELPFILE.createNewFile();
    }

    protected void tearDown() throws Exception {
        HELPFILE.delete();
    }

    public void xtestExecuteRunsACommand() throws InterruptedException {
        assertTrue(HELPFILE.exists());
        HttpResponse response = commandInvoker.execute(prepareMockHttpRequest(getCommandPath("test.cmd"), true));
        String actualResponse = new String(response.data());
        assertEquals(CommandInvoker.SUCCESS, actualResponse);
        assertFalse(HELPFILE.exists());
    }

    public void xtestExecuteReturnsFailureForInvalidCommand() throws InterruptedException {
        HttpResponse response = commandInvoker.execute(prepareMockHttpRequest("invalid", true));
        String actualResponse = new String(response.data());
        assertEquals(CommandInvoker.FAILURE, actualResponse);
    }

    public void xtestExecuteRunsACommandInAsyncMode() throws InterruptedException {
        HttpResponse response = commandInvoker.execute(prepareMockHttpRequest(getCommandPath("test.cmd"), false));
        String actualResponse = new String(response.data());
        assertEquals(CommandInvoker.SUCCESS, actualResponse);
    }

    private HttpRequest prepareMockHttpRequest(String commandToExecute, boolean sync) {
        Mock mock = mock(HttpRequest.class);
        mock.expects(once()).method("getParameter").with(eq(RequestConstants.COMMAND)).will(returnValue(commandToExecute));
        mock.expects(once()).method("getParameter").with(eq(RequestConstants.SYNC)).will(returnValue(Boolean.toString(sync)));
        return (HttpRequest) mock.proxy();
    }

    public void testDummy(){
    }

    private String getCommandPath(String command) {
        URL resource = this.getClass().getResource(command);
        String commandToExecute = (null == resource) ? "invalid" : resource.getPath();
        if (commandToExecute.startsWith("/"))
            commandToExecute = commandToExecute.replaceFirst("/", "");
        return commandToExecute;
    }

}
