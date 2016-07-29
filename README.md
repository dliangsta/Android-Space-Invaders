# spaceinvaders
Space invaders game for Android OS. This is an extension from a tutorial found at gamecodeschool.com. To try the code, try copy and pasting the .zip file into your AndroidStudio workspace and running that. 

Otherwise, create a project in Android Studio and name the activity SpaceInvadersActivity. Then replace the AndroidManifest.xml file (from the manifests) with mine or make your own and under "android.name=".SpaceInvaders" add: 
"android:theme="@android:style/Theme.NoTitleBar.Fullscreen"
"android:screenOrientation="landscape"

without quotations. Then add the 3 .png images to the drawable folder, found at SpaceInvaders\app\src\main\res\drawable and create a folder in app\src\main called assets and add the sounds (.ogg) there. Finally, find where the Java code is stored. Mine is stored at SpaceInvaders\app\src\main\java\com\example\david\spaceinvaders. Copy and paste the PlayerShip.java, Bullet.java, DefenceBrick.java, and Invader.java classes in.
