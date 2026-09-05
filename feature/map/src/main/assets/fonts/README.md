# Roboto Bold map glyphs

`roboto-bold-glyphs.zip` contains all 256 unmodified PBF ranges from
[korywka/fonts.pbf at 2d1fd482a5afa0a856277148ee0af3cc6a9db0ff](https://github.com/korywka/fonts.pbf/tree/2d1fd482a5afa0a856277148ee0af3cc6a9db0ff/Roboto%20Bold).
That project generated these glyphs with fontnik from OpenMapTiles' Roboto fonts.
Each range covers 256 Unicode BMP code points; empty ranges are included so missing
characters complete locally as well. Latin and Cyrillic are included.

Roboto is licensed under Apache License 2.0; the original license from
[OpenMapTiles](https://github.com/openmaptiles/fonts/blob/master/roboto/LICENSE.txt)
is included alongside the archive as `Roboto-LICENSE.txt`.

The archive is stored in the APK. No download or generation is required at build
or runtime. MapLibre's public OkHttp extension serves only the pin font from this
archive before any network I/O. Tile, style, sprite and basemap font requests are
passed through. The map itself still requires its usual network resources.

To reproduce the archive, download every PBF in the pinned directory, verify each
file against its Git blob SHA, then zip the files in filename order with DEFLATE
and a fixed timestamp of 2026-09-05 00:00:00. No font data is modified.
