package net.sf.sahi.util;

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
public class ClassLoadHelper {
//    private static URLClassLoader customLoader = new URLClassLoader(getURLs());
    @SuppressWarnings("unchecked")
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
