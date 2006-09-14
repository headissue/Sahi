/**
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

package net.sf.sahi.playback.log;

import java.util.logging.Level;

/**
 * User: nraman
 * Date: Jun 23, 2005
 * Time: 1:29:48 AM
 */
public class PlayBackLogLevel extends Level {
	private static final long serialVersionUID = 7518716444395222643L;
	public static final Level ERROR = new PlayBackLogLevel("ERROR", Level.SEVERE.intValue()+15);
    public static final Level FAILURE = new PlayBackLogLevel("FAILURE", Level.SEVERE.intValue()+14);
    public static final Level SUCCESS = new PlayBackLogLevel("SUCCESS", Level.SEVERE.intValue()+10);
	public static final Level INFO2 = new PlayBackLogLevel("INFO", Level.SEVERE.intValue()+5);
    public static final Level START = new PlayBackLogLevel("START", Level.SEVERE.intValue()+6);
    public static final Level STOP = new PlayBackLogLevel("STOP", Level.SEVERE.intValue()+7);
    public PlayBackLogLevel(String name, int value) {
        super(name, value);
    }
}
