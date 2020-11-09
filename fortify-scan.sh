#!/bin/bash

# Set scan options
# Modular scan doesn't work properly yet, so for now we just add the fortify-client-api build model
# Note that either approach requires fortify-client-api to be translated/scanned on the same machine
# before running this script.
#scanOpts="-include-modules fortify-client-api -scan"
scanOpts="-b fortify-client-api -scan" 

# Load and execute actual scan script from GitHub
curl -s https://raw.githubusercontent.com/fortify-ps/gradle-helpers/1.0/fortify-scan.sh | bash -s - ${scanOpts}