Forked to add functionalities needed for my master's thesis.

# inmemantlr

inmemantlr provides the functionality of [ANTLR v4](http://www.antlr.org/) 
through a simple API. Usually ANTLR requires the user to perform the grammar generation and compilation 
steps. Instead, inmemantlr does all of these steps automatically
and in-memory while keeping all of the original ANTLR objects accessible through
its `GenericParser` class which is serializable, and hence, can be reused at a
later point in time or across different applications. inmemantlr can be used
via an easy-to-use Java API or as command-line tool.

Moreover, you can easily generate a parse tree from a parsed file and convert
it into various formats such as `.dot`, `.xml` or `.json`. A parse tree
can be processed/translated by means of inmemantlr's `ParseTreeProcessor` class.

All of the above-mentioned inmemantlr features are illustrated by
[examples](#toc). inmemantlr is ready to use for all of the
[grammars-v4](https://github.com/antlr/grammars-v4) grammars (for detailed
examples please have a look at [grammars-v4](#grammars-v4)).

# Status

[![Linux Build Status](https://api.travis-ci.org/julianthome/inmemantlr.svg?branch=master)][travis]
[![Test Coverage](https://codecov.io/gh/julianthome/inmemantlr/branch/master/graph/badge.svg)][coverage]
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.julianthome/inmemantlr/badge.svg)][mcentral]

[travis]: https://travis-ci.org/julianthome/inmemantlr
[coverage]: https://codecov.io/gh/julianthome/inmemantlr
[mcentral]: https://maven-badges.herokuapp.com/maven-central/com.github.julianthome/inmemantlr

# Authors and major contributors

- Julian Thome, julian.thome@gmail.com, main author
- [Alan Stewart](https://github.com/alankstewart) (code style improvements, performance improvements, Tool customization)
- Radoslaw Cymer (bugfixes, code style improvements, typos in Javadoc)
- [Nikolas Havrikov](https://github.com/havrikov) (code style, bugfixes, performance improvements)
- [Calogero G. Zarba](https://github.com/calogero81) (feature improvements)
- [dafei1288](https://github.com/dafei1288) (Maintainability)

# TOC

[Integration](#integration)

[API Usage Scenarios](#api-usage-scenarios)
  * [Get started](#get-started)
  * [Simple parsing](#simple-parsing)
  * [Parse tree generation](#parse-tree-generation)
  * [Parse tree serialization](#parse-tree-serialization)
  * [Parse tree pruning](#parse-tree-pruning)
  * [Parse tree processing](#parse-tree-processing)
  * [Sequential parsing](#sequential-parsing)
  * [Non-combined grammars](#non-combined-grammars)
  * [Lexer-only grammars](#lexer-only-grammars)
  * [Accessing ANTLR objects](#accessing-antlr-objects)
  * [Parser serialization](#parser-serialization)
  * [grammars-v4](#grammars-v4)

[Licence](#licence)

# Integration

## Maven 
inmemantlr is available on maven central and can be integrated by
using the following dependency in the `pom.xml` file. Note, that the maven
releases do not necessarily contain the newest changes that are available in
the repository. The maven releases are kept in sync with the tagged
[releases](https://github.com/julianthome/inmemantlr/releases). The API
documentation for every release is avalable
[here](http://www.javadoc.io/doc/com.github.julianthome/inmemantlr). However,
the content of this documentation, in particular the code examples and usage
scenarios, is always aligned with the master branch of this repository. Hence,
it might be that the latest inmemantlr features are not yet available through
the maven package.

```xml
<dependency>
    <groupId>com.github.julianthome</groupId>
    <artifactId>inmemantlr-api</artifactId>
    <version>1.9.2</version>
</dependency>
```

# API Usage Scenarios

The following code snippets shows an example how to use the API of inmemantlr;
descriptions are provided as source code comments. For the sake of simplicity,
exception handling is omitted for all of the following examples.

## Get started

The code sample below shows how you could get started with inmemantlr. The
class `GenericParserToGo` (available as of release 1.6) provides a very simple API that should be sufficient
for most of the use cases: you only have to provide the ANTLR grammar file in
conjunction with the file/string to parse, and a call to `parse()` (with
the string and starting-rule as parameters) will return the corresponding parse
tree.


```java
File gfile = new File("Java.g4");
File cfile = new File("HelloWorld.java");
ParseTree pt = new GenericParserToGo(gfile).parse(cfile, "compilationUnit");
```


## Simple parsing

``` java
// 1. load grammar
File f = new File("Java.g4");
GenericParser gp = new GenericParser(f);
// 2. load file content into string
String s = FileUtils.loadFileContent("HelloWorld.java");
// 3. set listener for checking parse tree elements. Here you could use any ParseTreeListener implementation. The default listener is used per default
gp.setListener(new DefaultListener());
// 4. compile Lexer and parser in-memory
gp.compile();
// 5. parse the string that represents the content of HelloWorld.java
ParserRuleContext ctx = gp.parse(s, "compilationUnit", GenericParser.CaseSensitiveType.NONE);
```

## Parse tree generation

While providing access to the original `ParseTree` data-structure of ANTLR
which can be obtained through the `ParserRuleContext` object, inmemantlr also
provides it's own `ParseTree` data-structure and some ready-to-use utility
classes which help with everyday tasks such as pruning, data-conversion,
tree-traversal and translation.

If you would like to obtain the `ParseTree` from a parsed file,
the following snippet could be of use:

``` java
File f = new File("Java.g4");
GenericParser gp = new GenericParser(f);
String s = FileUtils.loadFileContent("HelloWorld.java");

// this listener will create a parse tree from the java file
DefaultTreeListener dlist = new DefaultTreeListener();

gp.setListener(dlist);
gp.compile();

ParserRuleContext ctx = gp.parse(s, "compilationUnit", GenericParser.CaseSensitiveType.NONE);
// get access to the parse tree of inmemantlr
ParseTree pt = dlist.getParseTree();
```

## Parse tree serialization

As depicted below, our `ParseTree` implementation can be serialized to various
output formats such as `.xml`, `.json` or `.dot`.

```java
String xml = parseTree.toXml();
String json = parseTree.toJson();
String dot = parseTree.toDot();
```

Serialization may be useful in case you would like to use parsing results
across different applications or in case you would like get a quick and simple
visualization of your parse tree by means of graphviz as in the example
below. 

<img src="https://github.com/julianthome/inmemantlr/blob/master/images/pt.png" alt="Example Parse tree" width="400px" align="second">

## Parse tree pruning

In case you are just interested in particular nodes of the parse tree, it is
possible to pass a lambda expression as parameter to the `DefaultTreeListener`
as illustrated in the example below. Only those nodes remain in the parse
tree for which the lambda expression returns true.

``` java
private Set<String> filter  = new HashSet<>(Arrays.asList(new String []{
        "alternation", "expr", "literal",
}));
// ...
dlist = new DefaultTreeListener(s -> filter.contains(s));
// ...
```


## Parse tree processing

With inmemantlr, you can easily process or translate a given Parse tree by means of an
`ParseTreeProcessor`. Note, that ANTLR can automatically generate
visitors/listeners from a given grammar which you can obtain through the
`GenericParser` member function `getAllCompiledObjects`. However,
in case you would like to develop a simple application inmemantlr
`ParseTreeProcessor` might be sufficient for your use case.

The following example illustrates how to process a simple
parse tree that represents a mathematical expression. Given the grammar definition
below, parsing the string `'3+100'` would yield this parse tree:

<img src="https://github.com/julianthome/inmemantlr/blob/master/images/simpleop.png" alt="ParseTree derived from simple expression '3+100'" width="200px" align="second">

```
grammar Ops;

Plus: '+';
Minus: '-';
Number: '-'?([0-9]|[1-9][0-9]+);

s: (expression)* EOF;
plus: Plus;
minus: Minus;
operation: plus | minus;
expression: operand operation operand;
operand: Number;

WS  :  [ \t\r\n]+ -> skip;
```

The following code example illustrates how to compute the result of a
mathematical expression based on the above-mentioned grammar.


```java
// ...
gp.compile();
// this example shows you how one could use inmemantlr for sequential parsing
ParseTree pt;
gp.parse("3+100");
pt = t.getParseTree();
// Process the Parse tree bottom-up starting from the leafs up to the root node
ParseTreeProcessor<String, String> processor = new ParseTreeProcessor<String, String>(pt) {
  @Override
  public String getResult() {
    // when all nodes have been processed, the result is available in the smap
    // value of the root node which is returned here
    return smap.get(pt.getRoot());
  }
  @Override
  protected void initialize() {
    // initialize smap - a data structure that keeps track of the intermediate
    // values for every node
    pt.getNodes().forEach(n -> smap.put(n, n.getLabel()));
  }
  // This operation is executed for each and every node in left to right and
  // bottom up order. Non-leaf nodes are processed only if all of their siblings
  // have been already processed
  @Override
  protected void process(ParseTreeNode n) {
    if(n.getRule().equals("expression")){
      int n0 = Integer.parseInt(smap.get(n.getChild(0)));
      int n1 = Integer.parseInt(smap.get(n.getChild(2)));
      int result = 0;
      switch(smap.get(n.getChild(1))) {
        case "+":
          result = n0 + n1;
        break;
        case "-":
          result = n0 - n1;
        break;
      }
      // store computation result of addition subtraction for current node
      smap.put(n, String.valueOf(result));
    } else {
      // when node is no expression NT, propate child node value 1:1
      // to parent
      simpleProp(n);
    }
  }
};
// compute the result
processor.process();
// print the computation results which is 103
System.out.println(processor.getResult());
```

A more practical example on how to use the Parse tree processor can be found within
my [CTrans project](https://github.com/julianthome/ctrans) which takes
a given boolean formula and translates it into CNF or DNF, respectively.

## Sequential parsing 

If you have multiple strings to parse one after another,
the following code snippet might be useful:

```java
File f = new File("Simple.g4");
GenericParser gp = new GenericParser(f);

// note that the listener should always be set before
// the compilation. Otherwise, the listener cannot
// capture the parsing information.
gp.setListener(new DefaultTreeListener());
gp.compile();

ParseTree pt;
gp.parse("PRINT a+b");
pt = t.getParseTree();
// do something with parsing result

gp.parse("PRINT \"test\"");
pt = t.getParseTree();
// do something with parsing result
```

## Non-combined grammars

```java
// define array of ANTLR files to consider -- inmemantlr will automatically
// analyses their interdependencies
File files [] = {
  new File("MySQLLexer.g4"),
  new File("MySQLParser.g4")
};
// simply pass files to constructor
GenericParser gp = new GenericParser(files);
// parser is ready to use
```

## Lexer-only grammars

In case you are interested in only using a lexer grammar, you can use the 
`lex` method which will provide you with a list of tokens.

``` java
GenericParser gp = new GenericParser(new File("LexerGrammar"));
gp.compile();
try {
    List<Token> tokens = gp.lex("a09");
    Assertions.assertEquals(tokens.size(), 2);
} catch (IllegalWorkflowException e) {
    Assertions.assertFalse(true);
}
```


## Accessing ANTLR objects

For accessing the ANTLR parser/lexer objects, you can use the
`getAllCompiledObjects` method which will return the source and byte code of
the source files that were generated by ANTLR and the corresponding byte code
generated by inmenantlr.

```java
// get access to ANTLR objects
MemoryTupleSet set = gp.getAllCompiledObjects();
// memory tuple contains the generated source code of ANTLR
// and the associated byte code
for(MemoryTuple tup : set) {
  // get source code object
  MemorySource = tup.getSource();
  // get byte code objects
  Set<MemoryByteCode> bcode = tup.getByteCodeObjects();
}
```

Through the method `writeAntlrArtifactsTo`, which belongs to the `GenericParser`
class, inmemantlr also offers the possibility to write the ANTLR artifacts,
i.e., the Java source files for a grammar/lexer, to a file.

``` java
// ...
GenericParser gp = new GenericParser(tc,sgrammarcontent);
// ...
gp.writeAntlrAritfactsTo("/tmp/grammar");
```

After the call above, you will find the `.java` files in the specified
destination `/tmp/grammar`.

## Parser serialization

For avoiding unnecessary compilation and for enabling the re-use of a generic
parser across different Java applications or runs, it is possible to serialize
a generic parser.

A generic parser could be serialized to a file with the following code:
```java
// store a generic parser in the file "/tmp/gp.out" and
// overwrite the file if it already exists
gp.store("/tmp/gp.out", true);
```

A generic parser can be loaded from a file with the following
code:

```java
// load generic parser from file /tmp/gp.out
GenericParser gp = GenericParser.load("/tmp/gp.out");
```

## grammars-v4

The [grammars-v4](https://github.com/antlr/grammars-v4) repository is added as
a submodule. For executing all the grammars-v4 test cases, one could run the
following commands from within the `inmemantlr-api` maven module.

```bash
git submodule init
git submodule update
mvn -Dtest=TestExternalGrammars test
```

# Licence

The MIT License (MIT)

Copyright (c) 2016 Julian Thome <julian.thome.de@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
of the Software, and to permit persons to whom the Software is furnished to do
so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
