package antlr4.XPath;

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
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String fname = "src/main/java/antlr4/XPath/test.txt";
        if(args!=null&&args.length==1){
            fname =  args[0];

        }
        try {
            // create parser
            FileInputStream fis = new FileInputStream(fname);
            ANTLRInputStream ais = new ANTLRInputStream(fis);
            XPathLexer lexer = new XPathLexer(ais);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            XPathParser parser = new XPathParser(tokens);

            // begin parsing at ap() rule
            ParseTree tree = parser.ap();
            MyXPathVisitor myXPathVisitor = new MyXPathVisitor();
            ArrayList<Node> res = myXPathVisitor.visit(tree);

            // write xml
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document doc = documentBuilder.newDocument();
            Element ele = doc.createElement("RESULT");

            for (Node element : res) {
                if (element != null) {
                    Node importedNode = doc.importNode(element, true);
                    ele.appendChild(importedNode);
                }
            }
            doc.appendChild(ele);

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
    // }

    }
}
