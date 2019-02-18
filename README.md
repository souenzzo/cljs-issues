# Steps to reproduce

*  `lein run`

* Open firefox (easier to see it in firefox) browser at `http://localhost:8081`

* Open your console (F12), go to network tab and enable  `size` and `transferred`

* Click on buttons and checkout the differente between with and without token (POST just work with token)

![](error.png?raw=true)

( transfer size on get without token is way smaller, due gzip )
