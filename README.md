# XQuery-Evaluator

## Introducation
This is a XQuery evaluator implemented by Java and ANTR 4. @[Zehui Jiao](https://github.com/r-light) and I did this project for CSE 232B (Database System Implementation).

## Goal
We use as vehicle the W3C Standard XML query language XQuery.

The course project is structured in 3 milestones:

I - implementing an XPath evaluator

II - implementing a XQuery evaluator

III - improving the evaluator from Milestone II by adding join optimizations


## Milestone
Milestones 1 and 2 (Naïve Evaluation): A straightforward query execution engine receives the simplified XQuery and an input XML file and evaluates the query using a recursive evaluation routine which, given an XQuery expression (path, concatenation, element creation, etc) and a list of input nodes, produces a list of output nodes. For the XQuery parser, we recommend generating it using the ANTLR 4 parser generator. Provided with a grammar, ANTLR generates a compiler which automatically constructs abstract syntax trees of  its input expressions.

Milestone 1 delivers a naive evaluator for XPath

Milestone 2 extends it to XQuery

Milestone 3 (Efficient Evaluation): Implement an algorithm which detects the fact that the FOR and WHERE clause computation can be implemented using the join operator. You may assume that the input XQueries to be optimized are in the simplified syntax given in the note. No need to first normalize your queries to this form.


