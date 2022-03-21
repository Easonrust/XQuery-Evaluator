package antlr4.XQuery;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
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
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        String fname = "src/main/java/antlr4/XQuery/test.txt";
        if (args != null && args.length == 1) {
            fname = args[0];

        }
        try {
            // create parser
            FileInputStream fis = new FileInputStream(fname);
            ANTLRInputStream ais = new ANTLRInputStream(fis);
            XQueryLexer lexer = new XQueryLexer(ais);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            XQueryParser parser = new XQueryParser(tokens);

            // begin parsing at ap() rule
            ParseTree tree = parser.xq();
            MyXQueryVisitor myXPathVisitor = new MyXQueryVisitor();
            ArrayList<Node> res = myXPathVisitor.visit(tree);

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
}

