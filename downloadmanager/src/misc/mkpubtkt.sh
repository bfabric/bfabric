#!/bin/bash
#
# script to sign the cookie needed by pubtkt
#
# $Id$

# first argument is the username
_UID="$1"
# second argument are the comma separated tokens
_TOKENS="$2"

# check for openssl
_OPENSSL=$(which openssl)
[ -z "${_OPENSSL}" ] && exit 1

# check for the privat key
_PRIVKEY="/srv/ug/conf/tkt_privkey_dsa.pem"
[ -r "${_PRIVKEY}" ] || exit 1

_TKTNAME="auth_FGCZ"
# generate the time in seconds the cookie is valid (1 day)
_VALIDUNTIL=$(date --date='tomorrow' +%s)
_GRACEPERIOD=$_VALIDUNTIL
_UDATA=""

# generate the cookie data
_COOKIE="uid=${_UID};validuntil=${_VALIDUNTIL};graceperiod=${_GRACEPERIOD};tokens=${_TOKENS};udata=${_UDATA}"
# sign it
_SIG=$(echo -n "${_COOKIE}" | "${_OPENSSL}" dgst -dss1 -sign "${_PRIVKEY}" | "${_OPENSSL}" enc -base64 -A)

# echo to stdout
echo "${_TKTNAME}=${_COOKIE};sig=${_SIG}"
