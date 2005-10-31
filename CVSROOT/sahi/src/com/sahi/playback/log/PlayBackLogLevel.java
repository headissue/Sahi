package com.sahi.playback.log;

import java.util.logging.Level;

/**
 * User: nraman
 * Date: Jun 23, 2005
 * Time: 1:29:48 AM
 */
public class PlayBackLogLevel extends Level {
	private static final long serialVersionUID = 7518716444395222643L;
	public static final Level ERROR = new PlayBackLogLevel("ERROR", Level.WARNING.intValue()+2);    
    public static final Level FAILURE = new PlayBackLogLevel("FAILURE", Level.WARNING.intValue()+1);
    public static final Level SUCCESS = new PlayBackLogLevel("SUCCESS", Level.INFO.intValue()-1);
    public PlayBackLogLevel(String name, int value) {
        super(name, value);
    }
}
