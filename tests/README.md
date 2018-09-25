# test suite

## installation

```bash
$ luarocks install busted --local
$ luarocks install luafilesystem --local
$ luarocks install moonscript --local
```

## usage

Run the following lines, start in the root of the repo.

```bash
$ export PROJ=$(pwd)
$ cd tests
$ busted .
```
