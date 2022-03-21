package antlr4.XQuery;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.xpath.XPathLexer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import static java.lang.System.exit;

public class Main {
    public static void main(String[] args) {
        String fname = "src/main/java/antlr4/XQuery/test.txt";
        boolean rewrite = false;
        if (args != null && args.length == 1) {
            fname = args[0];
        }
        if (args != null && args.length > 1) {
            if (args[1].equals("-r")) {
                rewrite = true;
            }
        }
        long startTime = System.currentTimeMillis();
        ArrayList<Node> res = new ArrayList<>();
        if (rewrite) {
            try {
                // create parser
                FileInputStream fis = new FileInputStream(fname);
                ANTLRInputStream ais = new ANTLRInputStream(fis);
                XQueryLexer lexer = new XQueryLexer(ais);
                CommonTokenStream tokens = new CommonTokenStream(lexer);
                XQueryParser parser = new XQueryParser(tokens);

                XQueryParser.XqContext FLWRctx = parser.xq();
                XQueryRewriter rewriter = new XQueryRewriter();

                String rewirteXq = rewriter.parseFLWR(FLWRctx);
                MyXQueryVisitor eval = new MyXQueryVisitor();

                if (rewirteXq != null && !rewirteXq.isEmpty()) {
                    try (PrintWriter out = new PrintWriter("rewriteFile.txt")) {
                        out.println(rewirteXq);
                    }
                    startTime = System.currentTimeMillis();
                    XQueryLexer lexerNew = new XQueryLexer(new ANTLRFileStream("rewriteFile.txt"));
                    CommonTokenStream tokensNew = new CommonTokenStream(lexerNew);

                    XQueryParser parserNew = new XQueryParser(tokensNew);

                    ParseTree tree = parserNew.xq();
                    res = eval.visit(tree);
                } else {
                    try (PrintWriter out = new PrintWriter("rewriteFile.txt")) {
                        out.println("Can not rewrite this query!");
                    }
                    res = eval.visit(FLWRctx);
                }

                // write xml
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                Document doc = documentBuilder.newDocument();
                Element ele = doc.createElement("RESULT");
                if (res.size() == 1) {
                    Node element = res.get(0);
                    if (element != null) {
                        Node importedNode = doc.importNode(element, true);
                        doc.appendChild(importedNode);
                    }
                } else {
                    for (Node element : res) {
                        if (element != null) {
                            Node importedNode = doc.importNode(element, true);
                            ele.appendChild(importedNode);
                        }
                    }
                    doc.appendChild(ele);
                }

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(new File("result.xml"));

                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                transformer.transform(source, result);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                // create parser
                FileInputStream fis = new FileInputStream(fname);
                ANTLRInputStream ais = new ANTLRInputStream(fis);
                XQueryLexer lexer = new XQueryLexer(ais);
                CommonTokenStream tokens = new CommonTokenStream(lexer);
                XQueryParser parser = new XQueryParser(tokens);

                XQueryParser.XqContext FLWRctx = parser.xq();
                MyXQueryVisitor eval = new MyXQueryVisitor();
                res = eval.visit(FLWRctx);

                // write xml
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                Document doc = documentBuilder.newDocument();
                Element ele = doc.createElement("RESULT");
                if (res.size() == 1) {
                    Node element = res.get(0);
                    if (element != null) {
                        Node importedNode = doc.importNode(element, true);
                        doc.appendChild(importedNode);
                    }
                } else {
                    for (Node element : res) {
                        if (element != null) {
                            Node importedNode = doc.importNode(element, true);
                            ele.appendChild(importedNode);
                        }
                    }
                    doc.appendChild(ele);
                }

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(new File("result.xml"));

                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                transformer.transform(source, result);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        long endTime = System.currentTimeMillis(); //获取结束时间
        System.out.println("running time： " + (endTime - startTime) + "ms");
    }
}

