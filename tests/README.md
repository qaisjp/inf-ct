# test suite

## installation

```bash
$ luarocks install busted --local
$ luarocks install luafilesystem --local
$ luarocks install moonscript --local
```

## usage

Run the following lines, start in the root of the repo.

The `busted` bonary will be in `~/.luarocks/bin/`.
Run from there or add to your PATH.

```bash
$ export PROJ=$(pwd)
$ cd tests
$ busted .
```
