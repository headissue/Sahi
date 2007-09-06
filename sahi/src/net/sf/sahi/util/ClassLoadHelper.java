package net.sf.sahi.util;


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

public class ClassLoadHelper {
//    private static URLClassLoader customLoader = new URLClassLoader(getURLs());

    public static Class getClass(String name) throws ClassNotFoundException {
        return Class.forName(name);
    }
//
//    private static URL[] getURLs() {
//        ArrayList urls = new ArrayList();
//        addFileURLs("../extlib", urls);
//        addFileURLs("../lib", urls);
//        addFileURLs("../classes", urls);
//        return (URL[]) urls.toArray(new URL[urls.size()]);
//    }
//
//    private static List addFileURLs(String dir, List urls) {
//        File[] files = new File(dir).listFiles();
//        for (int i = 0; i < files.length; i++) {
//            File file = files[i];
//            try {
//                urls.add(file.toURL());
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            }
//        }
//        return urls;
//    }
}
