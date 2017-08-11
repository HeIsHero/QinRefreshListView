How To Use:
===
  一、在项目的build.gradle文件下添加以下代码:
    <pre>
      	allprojects {
	&nbsp;&nbsp;&nbsp;repositories {
	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;...
	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;maven { url 'https://www.jitpack.io' }
	&nbsp;&nbsp;&nbsp;}
	}
    </pre>
  二、在module的build.gradle文件下添加以下代码:
     <pre>
     		dependencies {
	        &nbsp;&nbsp;&nbsp;compile 'com.github.HeIsHero:SimpleRefreshListView:1.3.0'
	}
  <br/>
  三、API<br/>
  &nbsp;1.setOnRefreshListener(设置下拉刷新和上拉加载的监听方法)<br/>
  &nbsp;2.refreshComplete(关闭下拉刷新)<br/>
  &nbsp;3.loadMoreComplete(关闭上拉加载)<br/>


