#!/bin/sh
curl -s "https://get.sdkman.io?ci=true" -o install_sdkman.sh
/bin/bash install_sdkman.sh
export SDKMAN_DIR="${SDKMAN_DIR:-$HOME/.sdkman}"
set +u
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 25-tem
