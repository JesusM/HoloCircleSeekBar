<h1>Android HoloCircleSeekBar</h1>

A Circle SeekBar inspired by Android Holo ColorPicker designed by Marie Schweiz and developed by Lars Werkman.

![image](/images/device-2015-04-08-225534.png)
![image](/images/device-2015-09-21-225940.png)

<h2>How to integrate in your proyect</h2>

Import it on your **build.gradle**

```
repositories {
    maven {
	    url "https://jitpack.io"
    }
}

dependencies {
    compile 'com.github.JesusM:HoloCircleSeekBar:v2.2.2'
}
```


<h2>Documentation</h2>
To add the widget, you only need to add this to your xml:

    <com.jesusm.holocircleseekbar.lib.HoloCircleSeekBar
        android:layout_centerInParent="true"
        android:id="@+id/picker"
        android:layout_width="285dp"
        android:layout_height="290dp"
        app:max="100"
        app:pointer_color="@color/point_color"
        app:pointer_halo_color="@color/point_halo_color"
        app:pointer_size="20dp"
        app:text_color="@color/text_color"
        app:text_size="65sp"
        app:wheel_active_color="@color/wheel_active_color"
        app:wheel_unactive_color="@color/wheel_unactive_color"/>

Don't forget to add this at the root of your View in the xml layout to use the custom attributes:

	xmlns:app="http://schemas.android.com/apk/res-auto"
        
You can change some widget components (text size, wheel color, point color, etc) sith this attrs.
 
        app:max="100"
        app:pointer_color="#0174DF"
        app:pointer_halo_color="#88252525"
        app:pointer_size="34"
        app:text_color="#FF0000"
        app:text_size="65"
        app:wheel_active_color="#00BFFF"
        app:wheel_unactive_color="#FFCCCCCC" 

To get the value of the circle seekbar

	HoloSeekBar picker = (HoloSeekBar) findViewById(R.id.picker);
	picker.getValue();
	
<H2>License</H2>
	
 	 Copyright 2012 Jesús Manzano
 	
 	 Licensed under the Apache License, Version 2.0 (the 	"License");
 	 you may not use this file except in compliance with 	the License.
 	 You may obtain a copy of the License at
 	
 	     http://www.apache.org/licenses/LICENSE-2.0
 	
 	 Unless required by applicable law or agreed to in 	writing, software
	 distributed under the License is distributed on an 	"AS IS" BASIS,
 	 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 	either express or implied.
 	 See the License for the specific language governing 	permissions and
 	 limitations under the License.
 	
 	
<h2>Original by</h2>
**Lars Werkman**

<h2>Modified By</h2>
**Jesús Manzano**
