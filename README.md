Sahi
====

I wanted to contribute to Sahi Open Source, but it's currently maintained in svn while most open source projects are shifting towards git. Sahi Pro is very well maintained, but Sahi OS isn't actively maintained or updated. This may be a better place to add features to Sahi OS, especially if those features are currently only found in Sahi Pro.

How to compile
--------------

For people starting fresh,

1) ~~Checkout Sahi code from SVN from https://sahi.svn.sourceforge.net/svnroot/sahi/branches/sahi_rhino_controller2~~ Clone Sahi from this git repo

2) In Eclipse, do File -> Import -> Existing Projects into Workspace -> Next -> Select the above checked out directory as root directory. Project should now be imported.

3) Right click build.xml -> Run As -> Ant Build. This will either work or fail.

If it fails:

4) Right click build.xml -> Run As -> External Tools Configurations ... -> Environment tab. Add new environment variable PATH with value D:\your_java_path\bin . Make sure "Append environment to native environment" is checked. Apply and Run.

It should now pass.

An ant configuration is created only if you have done Run As Ant at least once. So do step 3 before step 4.

Pulled from [the Sahi forums](http://sahi.co.in/forums/discussion/comment/4093#Comment_4093) (Thanks Narayan)

Licenses
--------
See sahi/docs/licenses

Notes
-----
This was pulled from svn using [svn2git](https://github.com/nirvdrum/svn2git), so all prior subversion commits are available
