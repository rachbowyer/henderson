# Henderson

<img src="https://github.com/rachbowyer/henderson/blob/main/barton-square-cut.png" alt="Picture of William Barton Rogers transformed by the Square Limit transformation" width="250"/>

Henderson is a partial implementation in Clojure of the Henderson Picture Language
(HPL) described in Structure and Interpretation of Computer Programs by Harold Abelson 
and Gerald Sussman (aka [SICP](https://mitpress.mit.edu/sites/default/files/sicp/index.html)).

HPL was invented by Professor Peter Henderson and first 
described in his 1982 paper (Henderson, P. (1982) Functional Geometry. Proc. 
ACM Symp. on Lisp and Functional Programming. pp. 179-187). An updated version of
the paper is available [here](https://eprints.soton.ac.uk/257577/1/funcgeo2.pdf).
SICP uses HPL to illustrate abstraction, 
high order functions, combinators and composition.

Around 6 years ago, I was working through SICP in Clojure and arrive at Chapter
2.2.4. I was unable to render my _painters_ so set about writing an
implementation in Clojure. I always planned to pull the code out into a standalone
library, but never found the time until now!

The code works well in rendering the _painters_ in 2.2.4, but I feel uncomfortable
with the implementation. IO is interwoven into the combinators and has to be mocked out
in the tests. It would be interested in trying a Free Monad based approach... one day.

## Installation

Henderson is available from Clojars.

[![Clojars Project](https://img.shields.io/clojars/v/rachbowyer/henderson.svg)](https://clojars.org/rachbowyer/henderson)

## Usage
To make the Henderson functions available type:

    (require '[henderson.core :as hc])
    (require '[henderson.examples :as he])

There are predefined painters _wave_, _wave2_, _wave4_ and _rogers_ as described
in SICP. The function _show-picture_ will render a painter. 
Examples (from page 133 SICP 2nd Edition)

     (hc/show-picture (hc/right-split he/wave 4) 400 400)
     (hc/show-picture (hc/corner-split he/rogers 4) 400 400)

Note that the origin is at the top left, positive x goes right
and positive y goes down in accordance with Java2d and most UI systems.

For more information see the docs in /doc and of course SICP!

## License for Henderson

Distributed under the Creative Commons Attribution-ShareAlike 4.0
International licence (["CC BY-SA 4.0"](https://creativecommons.org/licenses/by-sa/4.0/))


