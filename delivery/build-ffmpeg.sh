#!/bin/bash
# Build ffmpeg for transcoding FLV files to WEBM
# For safety we disable everything except the codecs we need
set -e -u

mkdir -p build-ffmpeg
cd build-ffmpeg

BUILDROOT="$(pwd)"
export PATH="$BUILDROOT/bin:$PATH"
export PKG_CONFIG_PATH="$BUILDROOT/lib/pkgconfig"

# Build nasm
NASM_VERSION=2.15.05
if [ ! -e bin/nasm ]; then
  wget -nc https://www.nasm.us/pub/nasm/releasebuilds/2.15.05/nasm-$NASM_VERSION.tar.bz2
  tar -jxf nasm-$NASM_VERSION.tar.bz2
  cd nasm-$NASM_VERSION
  ./configure --prefix="$BUILDROOT"
  make -j$(nproc)
  make install
  cd ..
fi

# Build yasm
YASM_VERSION=1.3.0
if [ ! -e bin/yasm ]; then
  wget -nc https://www.tortall.net/projects/yasm/releases/yasm-$YASM_VERSION.tar.gz
  tar -zxvf yasm-$YASM_VERSION.tar.gz
  cd yasm-$YASM_VERSION
  ./configure --prefix="$BUILDROOT"
  make -j$(nproc)
  make install
  cd ..
fi

# Build libopus
OPUS_VERSION=1.5.1
if [ ! -e lib/libopus.a ]; then
  wget -nc https://downloads.xiph.org/releases/opus/opus-$OPUS_VERSION.tar.gz
  tar xzvf opus-$OPUS_VERSION.tar.gz
  cd opus-$OPUS_VERSION
  ./configure --prefix="$BUILDROOT"
  make -j$(nproc)
  make install
  cd ..
fi

# Build libvpx
if [ ! -e lib/libvpx.a ]; then
  if [ ! -e libvpx ]; then git clone --depth 1 https://chromium.googlesource.com/webm/libvpx.git; fi
  cd libvpx
  ./configure --prefix="$BUILDROOT" --disable-examples --disable-unit-tests --disable-vp9 --as=yasm
  make -j$(nproc)
  make install
  cd ..
fi

# Build ffmpeg
FFMPEG_VERSION=n5.1.4
#wget -nc https://ffmpeg.org/releases/ffmpeg-snapshot.tar.bz2
#tar -jxf ffmpeg-snapshot.tar.bz2
wget -nc https://github.com/FFmpeg/FFmpeg/archive/refs/tags/${FFMPEG_VERSION}.tar.gz
tar -zxf ${FFMPEG_VERSION}.tar.gz
mv FFmpeg-${FFMPEG_VERSION} ffmpeg
cd ffmpeg
./configure --prefix="$BUILDROOT" \
            --pkg-config-flags=--static \
            --disable-everything \
            --disable-network \
            --disable-ffprobe \
            --disable-ffplay \
            --disable-doc \
            --enable-decoder='aac*,ac3*,flv,h264,mp3,opus,vorbis,qcelp,atrac*,rv*,svq*' \
            --enable-demuxer=mov,m4v,matroska,flv,rm \
            --enable-muxer=webm \
            --enable-encoder=libvpx_vp8,libopus \
            --enable-filter=aresample,scale \
            --enable-protocol=pipe \
            --enable-libvpx \
            --enable-libopus
make -j$(nproc)
make install
cd ..