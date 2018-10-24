package main

import (
	"bytes"
	"fmt"
	"go/ast"
	"go/parser"
	"go/token"
	// "strings"
)

func parseFunc(filename, functionname string) (fun *ast.FuncDecl, fset *token.FileSet) {
	fset = token.NewFileSet()
	if file, err := parser.ParseFile(fset, filename, nil, 0); err == nil {
		for _, d := range file.Decls {
			if f, ok := d.(*ast.FuncDecl); ok && f.Name.Name == functionname {
				fun = f
				return
			}
		}
	} else {
		panic(err)
	}
	panic("function not found")
}

func main() {
	// Parse source file and extract the AST without comments for
	// this function, with position information referring to the
	// file set fset.
	funcAST, fset := parseFunc("example_test.go", "ExampleFprint")

	// Print the function body into buffer buf.
	// The file set is provided to the printer so that it knows
	// about the original source formatting and can add additional
	// line breaks where they were present in the source.
	var buf bytes.Buffer
	ast.Fprint(&buf, fset, funcAST.Body, nil)

	// Remove braces {} enclosing the function body, unindent,
	// and trim leading and trailing white space.
	s := buf.String()
	// s = s[1 : len(s)-1]
	// s = strings.TrimSpace(strings.Replace(s, "\n\t", "\n", -1))

	// Print the cleaned-up body text to stdout.
	fmt.Println(s)
	// fmt.Printf("%+v", funcAST.Body.List[0].Results)
}
