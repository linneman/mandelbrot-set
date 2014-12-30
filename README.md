# Generation of the Mandelbrot Set as Clojurescript Application

This is an implementation of the famous [Mandelbrot set](http://en.wikipedia.org/wiki/Mandelbrot_set) renderer exclusively running within the web browser. It illustrates the capabilities of modern Javascript engines combined with the HTML5 canvas API. The application is completely written in Clojurescript and shows what can be achieved with this programming platform.

A significant advantage of Clojure is that it targets both, the server and the client side of modern web applications. While this demo focuses mostly on the Clojurescript client, there is a minimalistic [jetty](http://eclipse.org/jetty) based server included in the package as well to illustrate the server code. For a full-fledged [AJAX](http://en.wikipedia.org/wiki/Ajax_(programming) baesd web application completely written in the Clojure programming environment refer to [project-alpha](http://project-alpha.org).

The following sections give a brief explanation how to compile and run client and server side code.

## Quick Start

### Downloading the Source Code
Clojure uses the package management tool [Leiningen](https://github.com/technomancy/leiningen) as a defacto standard now. Leiningen is initially provided as a single script file which will download the rest of its implementation and all libraries the application depends on from the web.

* Download the script leiningen from [Github](https://github.com/technomancy/leiningen) and make it accessible to your shell
* Type the following command from your shell

        lein self-install

* Clone the project form this repository

        git clone git://github.com/linneman/mandelbrot-set.git
        cd mandelbrot-set
        lein deps

### Compile Clojurescript Code

The Javascript code running in the browser is generated from Clojurescript and needs to be compiled initially. Type

    lein cljsbuild once

to compile both, a debug and release version. Be aware that the build mechanism does not always detect all dependencies in case you do changes on the client side so its always a good idea to prepend a 'clean' command (replace once by clean) to ensure that everything is build correctly.

### Start the Server application

The server side of the application can be started directly from the shell by entering

    lein run

##  REPL Based Web Development
The so called Read-Eval-Print-Loop, in short REPL, is a very powerful way of changing Clojure code while its is executed. This is in fact one of most powerful concepts in software development and allows to try and apply changes from the program editor directly while a program is still running. You can connect your favorite editor to both, the client side running compiled Clojurescript code within the Webbrowser and the server side running Clojure code on top of the JVM. We give a very brief summary of the required steps to do this in the following. You a required to have Emacs installed on your machine and to setup it appropriately. You will need the Emacs extensions [cider](https://github.com/clojure-emacs/cider), [clojure-mode](https://github.com/technomancy/clojure-mode). Alternatively you can clone the Emacs setup which was used for the most of the development in this project from [Github](https://github.com/linneman/emacs-setup).

### Server Side
* start Emacs, with the exception of the web browser the following commands are all executed from the Emacs environment
* start an ANSI-Terminal with meta-x followed by "ansi-term", form there and enter the commands after the colon:

        sh$: cd mandelbrot-set (project's root directory)
        sh$: lein run
* open file src/project-alpha-server/app/core.clj
* connect slime with meta-x followed by "cider-connect"
* enter 'localhost' for the server and port number 7888 in the command line dialogue afterwords
* emacs should show two buffers now - 'core.clj' and the 'cider-repl'. If not setup the frames manually. Refer to the emacs documentation how to do this.
* change the namespace to core.clj by hitting ctrl-x meta-n. Set 'core.clj' as active buffer first
* you can now evaluate the Clojure expressions in core.clj the cursor after the expression and pressing crtl-x-e
* More information is available on the [cider project](https://github.com/clojure-emacs/cider)

### Client Side
* start a web browser (a recent version of firefox, chrome, safari or opera is required), usage of a webkit base browser (chrome,safari) provides better usage experience
* within Emacs (same instance as in server) follow these steps:
* open a shell with meta-x followed by "shell", from there enter the commands after the colon:

        sh$: cd mandelbrot-set (project's root directory)
        sh$: lein trampoline cljsbuild repl-listen
* load the webpage http://localhost:3000/repl.html within your web browser, your client repl should now be operational which you should check e.g. like this:

        ClojureScript:cljs.user> (+ 1 1)
        2
* If the repl does not react try to reload the page
* Setup Emacs to display a clojurescript buffer e.g. 'src/client/app/mandelbrot.cljs' and the buffer 'shell' (cljs-repl)
* put the cursor behind the first 'ns'-expression and switch namespace to this expression by hitting 'crtl-c e'
* you can now evaluate other clojurescript expressions from this buffer by 'crtl-c e'

## Licence
This implementation code stands under the terms of the
[GNU General Public Licence](http://www.gnu.org/licenses/gpl.html).

December 2014, Otto Linnemann

## Resources and links
Thanks to all the giants whose shoulders we stand on. And the giants theses giants stand on...
And special thanks to Rich Hickey (and the team) for Clojure. Really, thanks!

* Clojure: http://clojure.org
* Cojurescript: https://github.com/clojure/clojurescript
* Leiningen: https://github.com/technomancy/leiningen
