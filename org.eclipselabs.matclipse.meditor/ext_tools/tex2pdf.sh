#!/bin/bash
cd "$1"
cp $2 __tmp.tex
pdflatex -interaction=nonstopmode __tmp.tex >__tmp.log
#pdflatex -interaction=nonstopmode __tmp.tex 
#latex __tmp.tex
mv __tmp.pdf ${2:0:${#2}-4}.pdf
rm __tmp.*
exit 0
