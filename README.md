
How To Use:<br/>
  1.在项目的build.gradle文件下添加以下代码:
    <pre>
      	allprojects {
	&nbsp;&nbsp;&nbsp;repositories {
	&nbsp;&nbsp;&nbsp;&nbsp;...
	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;maven { url 'https://www.jitpack.io' }
	&nbsp;&nbsp;&nbsp;}
	}
    </pre><br/><br/><br/>
  2.在module的build.gradle文件下添加以下代码:
     <pre>
     		dependencies {
	        &nbsp;&nbsp;&nbsp;compile 'com.github.HeIsHero:SimpleRefreshListView:1.3.0'
	}


