
for (( c=0; c<=90; c=c+10 ))
do
	nextC=$(($c+10))
	sed -i -e "s/protocol.mp.swapFreq\t\t$c/protocol.mp.swapFreq\t\t$nextC/g" config.cfg
	java -cp /home/luca/peersim-1.0.5/*: peersim.Simulator config.cfg
done
