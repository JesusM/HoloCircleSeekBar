<h1>Android HoloCircleSeekBar</h1>

A Circle SeekBar inspired by Android Holo ColorPicker designed by Marie Schweiz and developed by Lars Werkman.

![image](https://lh3.googleusercontent.com/-EuIwYqaMhB4/URouyGV0_pI/AAAAAAAAA3s/Fboe1ob5xYg/s512/photo.jpg)
![image](https://lh4.googleusercontent.com/-nNIvt3_fjgE/UP8tiKd-7qI/AAAAAAAAAuY/esuQXaicKsg/s514/scree.png)



<h2>Documentation</h2>
Add this to your xml

	<com.jesusm.holocircleseekbar.HoloCircleSeekBar
        android:id="@+id/picker"
                android:layout_width="285dp"
        android:layout_height="290dp"/>

Don't forget to add this at the root of your View in the xml layout when you use HoloCircleSeekBar:

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
