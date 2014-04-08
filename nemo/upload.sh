# !/bin/bash

nxjc LineCar.java
nxjc nxt_line.java

nxjlink -o nxt_line.nxj nxt_line && sudo nxjupload nxt_line.nxj