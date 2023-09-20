#!/bin/bash

if [ -z "$DESTDIR" ]; then
   DESTDIR=/opt/webrecorder
fi
set -e -u

mkdir -p "$DESTDIR/etc" "$DESTDIR/bin" "$DESTDIR/pywb/collections/wr"

# Install pywb and uwsgi
python3 -m venv venv "$DESTDIR"
"$DESTDIR"/bin/pip install pywb uwsgi
cp -Rv pywb/ "$DESTDIR/pywb/"

# Install oauth2-proxy
curl -sL "https://github.com/oauth2-proxy/oauth2-proxy/releases/download/v7.5.0/oauth2-proxy-v7.5.0.linux-amd64.tar.gz" | tar -zxv -C "$DESTDIR/bin" --strip-components 1
COOKIE_SECRET="$(dd if=/dev/urandom bs=32 count=1 2>/dev/null | base64 | tr -d -- '\n' | tr -- '+/' '-_')"
cp -v oauth2-proxy.cfg "$DESTDIR/etc/oauth2-proxy.cfg"
sed -i "s/@@COOKIE_SECRET@@/$COOKIE_SECRET/" "$DESTDIR/etc/oauth2-proxy.cfg"

# Install systemd unit files
cp -v systemd/* /etc/systemd/system/
systemctl daemon-reload
systemctl enable webrecorder-pywb.socket
systemctl enable webrecorder-oauth2-proxy.service