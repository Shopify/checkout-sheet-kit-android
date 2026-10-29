#!/usr/bin/env bash
# MIT License
#
# Copyright 2023-present, Shopify Inc.
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

set -euo pipefail
script="$(cd "$(dirname "$0")" && pwd)/release_publication.sh"

assert_route() {
  local tag="$1" version="$2" publication="$3" actual expected
  actual=$(bash "$script" "$tag")
  expected=$(printf 'version=%s\npublication=%s\ntask=:lib:publish%sPublicationToOssrh-staging-apiRepository' \
    "$version" "$publication" "$publication")
  [[ "$actual" == "$expected" ]] || { echo "Incorrect routing for $tag" >&2; exit 1; }
}

assert_route 4.0.0 4.0.0 Relocation
assert_route v4.0.0 4.0.0 Relocation
assert_route 3.6.4 3.6.4 Release
assert_route v3.6.4 3.6.4 Release
assert_route 3.6.10 3.6.10 Release
assert_route 3.10.0 3.10.0 Release

for tag in '' main 4.0.1 5.0.0 2.0.0 3.6 3.06.4 3.6.04 3.6.4-rc.1 3.6.4-SNAPSHOT vv3.6.4 '3.6.4; echo unsafe' $'3.6.4\ntask=unsafe'; do
  if output=$(bash "$script" "$tag" 2>/dev/null); then
    echo "Unexpectedly accepted tag: $tag" >&2
    exit 1
  fi
  [[ -z "$output" ]] || { echo "Invalid tag emitted publication outputs" >&2; exit 1; }
done
if bash "$script" >/dev/null 2>&1 || bash "$script" 3.6.4 extra >/dev/null 2>&1; then
  echo "Unexpectedly accepted invalid argument count" >&2
  exit 1
fi

echo "Release publication routing tests passed."
