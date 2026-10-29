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

# Emit GitHub Actions outputs; never fall back to a default library version.
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 <release-tag>" >&2
  exit 1
fi

version="${1#v}"
if [[ "$version" == "4.0.0" ]]; then
  publication=Relocation
elif [[ "$version" =~ ^3\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)$ ]]; then
  publication=Release
else
  echo "Unsupported release tag '$1': expected 4.0.0 (relocation) or 3.x.y (library patch), optionally prefixed with v." >&2
  exit 1
fi

printf 'version=%s\n' "$version"
printf 'publication=%s\n' "$publication"
printf 'task=:lib:publish%sPublicationToOssrh-staging-apiRepository\n' "$publication"
