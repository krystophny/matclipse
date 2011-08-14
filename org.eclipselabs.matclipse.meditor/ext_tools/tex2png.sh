#!/bin/bash

cd "$1"
latex temp.tex
dvips temp.dvi
gs -r300x300 -dTextAlphaBits=4 -dGraphicsAlphaBits=4 -sDEVICE=bmp16m -sOutputFile=temp.bmp -dBATCH -dNOPAUSE temp.ps
mogrify -crop 0x0 temp.bmp
convert -bordercolor white -border 5x2 -antialias -quality 100 -geometry 35%x35% temp.bmp "$2"
rm temp.*
exit 0
