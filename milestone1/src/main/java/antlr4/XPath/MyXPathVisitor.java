package antlr4.XPath;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MyXPathVisitor extends XPathBaseVisitor<ArrayList<Node>> {
    private ArrayList<Node> cur = new ArrayList<>();
    private Document inputDoc;

    public static void stripEmptyElements(Node node) {
        NodeList children = node.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if(child.getNodeType() == Node.TEXT_NODE) {
                if (child.getTextContent().trim().length() == 0) {
                    child.getParentNode().removeChild(child);
                    i--;
                }
            }
            stripEmptyElements(child);
        }
    }

    private static ArrayList<Node> unique(ArrayList<Node> nodeList){
        ArrayList<Node> res = new ArrayList<>();
        for (Node node1 : nodeList) {
            if (node1 == null)
                continue;
            boolean eq = false;
            for (Node node2 : res) {
                if (node1.isSameNode(node2)) {
                    eq = true;
                    break;
                }
            }
            if (!eq) {
                res.add(node1);
            }
        }
        return res;
    }

    private ArrayList<Node> getChildren(Node ctx) {
        ArrayList<Node> res = new ArrayList<>();
        NodeList nList = ctx.getChildNodes();
        for (int i = 0, len = nList.getLength(); i < len; i++) {
            // if current node is root, we only read the root element
            if (ctx.getNodeType() == Node.DOCUMENT_NODE) {
                if (nList.item(i).getNodeType() == Node.ELEMENT_NODE)
                    res.add(nList.item(i));
            }
            else
                res.add(nList.item(i));
        }
        return res;
    }

    @Override
    public ArrayList<Node> visitApChildren(XPathParser.ApChildrenContext ctx) {
        // doc(fileName)/rp
        visit(ctx.doc());
        cur = visit(ctx.rp());
        return cur;
    }

    @Override
    public ArrayList<Node> visitApAll(XPathParser.ApAllContext ctx) {
        // doc(fileName)//rp
        visit(ctx.doc());
        // //
        Queue<Node> queue = new LinkedList<>(cur);
        ArrayList<Node> res = new ArrayList<>(cur);
        while (!queue.isEmpty()) {
            Node tmp = queue.poll();
            List<Node> tmpList = getChildren(tmp);
            if (tmpList.size() == 0)
                continue;
            res.addAll(tmpList);
            queue.addAll(tmpList);
        }
        cur = res;
        // rp
        return unique(visit(ctx.rp()));
    }

    @Override
    public ArrayList<Node> visitDoc(XPathParser.DocContext ctx) {
        String fname = ctx.fname().getText();
        fname = fname.substring(1, fname.length() - 1);
        File file = new File(fname);
        ArrayList<Node> ret = new ArrayList<>();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            inputDoc = db.parse(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (inputDoc != null) {
            stripEmptyElements(inputDoc);
            inputDoc.getDocumentElement().normalize();
            ret.add(inputDoc);
        }
        cur = ret;
        return ret;
    }

    @Override
    public ArrayList<Node> visitTagName(XPathParser.TagNameContext ctx) {
        ArrayList<Node> res = new ArrayList<>();
        for (Node node : cur) {
            for (Node child : getChildren(node)) {
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element) child;
                    if (elem.getTagName().equals(ctx.NAME().getText())) {
                        res.add(child);
                    }
                }
            }
        }
        cur = res;
        return res;
    }

    @Override
    public ArrayList<Node> visitAllChildren(XPathParser.AllChildrenContext ctx) {
        ArrayList<Node> res = new ArrayList<>();
        for (Node node : cur) {
            res.addAll(getChildren(node));
        }
        cur = res;
        return res;
    }

    @Override
    public ArrayList<Node> visitCurrent(XPathParser.CurrentContext ctx) {
        return cur;
    }

    @Override
    public ArrayList<Node> visitParent(XPathParser.ParentContext ctx) {
        ArrayList<Node> parent = new ArrayList<>();
        for (Node node : cur) {
            if (node.getNodeType() == Node.DOCUMENT_NODE)
                continue;
            parent.add(node.getParentNode());
        }
        cur = unique(parent);
        return cur;
    }

    @Override
    public ArrayList<Node> visitText(XPathParser.TextContext ctx) {
        ArrayList<Node> res = new ArrayList<>();
        for (Node node : cur) {
            for (Node child : getChildren(node)) {
                if (child.getNodeType() == Node.TEXT_NODE && !child.getNodeValue().isEmpty() && !child.getNodeValue().equals("\n")) {
                    res.add(child);
                }
            }
        }
        cur = res;
        return res;
    }

    @Override
    public ArrayList<Node> visitAttribute(XPathParser.AttributeContext ctx) {
        ArrayList<Node> res = new ArrayList<>();
        String attrName = ctx.NAME().getText();
        for (Node node : cur) {
            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;
            Node att = ((Element) node).getAttributeNode(attrName);
            if (att != null) {
                res.add(att);
            }
        }
        cur = res;
        return cur;
    }

    @Override
    public ArrayList<Node> visitRpwithP(XPathParser.RpwithPContext ctx) {
        return visit(ctx.rp());
    }

    @Override
    public ArrayList<Node> visitRpChildren(XPathParser.RpChildrenContext ctx) {
        visit(ctx.rp(0));
        cur = unique(visit(ctx.rp(1)));
        return cur;
    }

    @Override
    public ArrayList<Node> visitRpAll(XPathParser.RpAllContext ctx) {
        this.visit(ctx.rp(0));
        Queue<Node> queue = new LinkedList<>(cur);
        ArrayList<Node> res = new ArrayList<>(cur);
        while (!queue.isEmpty()) {
            Node tmp = queue.poll();
            List<Node> tmpList = getChildren(tmp);
            res.addAll(tmpList);
            queue.addAll(tmpList);
        }
        cur = res;
        cur = unique(visit(ctx.rp(1)));
        return cur;
    }

    @Override
    public ArrayList<Node> visitRpFilter(XPathParser.RpFilterContext ctx) {
        visit(ctx.rp());
        ArrayList<Node> tmp = new ArrayList<>(cur);
        ArrayList<Node> res = new ArrayList<>();
        for (Node node : tmp) {
            cur = new ArrayList<Node>(){{
                add(node);
            }};
            ArrayList<Node> filter = visit(ctx.filter());
            if (!filter.isEmpty()) {
                res.add(node);
            }
        }
        cur = res;
        return cur;
    }

    @Override
    public ArrayList<Node> visitTwoRp(XPathParser.TwoRpContext ctx) {
        // save former cur
        ArrayList<Node> tmp = new ArrayList<>(cur);
        ArrayList<Node> res = visit(ctx.rp(0));
        cur = tmp;
        res.addAll(visit(ctx.rp(1)));
        cur = unique(res);
        return res;
    }

    @Override
    public ArrayList<Node> visitFilterRp(XPathParser.FilterRpContext ctx) {
        ArrayList<Node> tmp = new ArrayList<>(cur);
        ArrayList<Node> res = visit(ctx.rp());
        cur = tmp;
        return res;
    }

    @Override
    public ArrayList<Node> visitFilterEqual(XPathParser.FilterEqualContext ctx) {
        ArrayList<Node> tmp = new ArrayList<>(cur);
        ArrayList<Node> res1 = this.visit(ctx.rp(0));
        // clone tmp
        cur = new ArrayList<>(tmp);
        ArrayList<Node> res2 = this.visit(ctx.rp(1));
        cur = tmp;
        boolean eq = false;
        for (Node node1 : res1) {
            for (Node node2 : res2) {
                if (node1.isEqualNode(node2)) {
                    eq = true;
                    break;
                }
            }
        }
        return eq ? cur : new ArrayList<>();
    }

    @Override
    public ArrayList<Node> visitFilterIs(XPathParser.FilterIsContext ctx) {
        ArrayList<Node> tmp = new ArrayList<>(cur);
        ArrayList<Node> res1 = this.visit(ctx.rp(0));
        // clone tmp
        cur = new ArrayList<>(tmp);
        ArrayList<Node> res2 = this.visit(ctx.rp(1));
        cur = new ArrayList<>(tmp);
        boolean eq = false;
        for (Node node1 : res1) {
            for (Node node2 : res2) {
                if (node1.isSameNode(node2)) {
                    eq = true;
                    break;
                }
            }
        }
        return eq ? cur : new ArrayList<>();
    }

    @Override
    public ArrayList<Node> visitFilterConstant(XPathParser.FilterConstantContext ctx) {
        ArrayList<Node> tmp = new ArrayList<>(cur);
        ArrayList<Node> res = this.visit(ctx.rp());
        String constant = ctx.constant().getText();
        constant = constant.substring(1, constant.length() - 1);
        cur = new ArrayList<>(tmp);
        boolean eq = false;
        for (Node node : res) {
            if ((node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.ATTRIBUTE_NODE) && node.getNodeValue().equals(constant)) {
                eq = true;
                break;
            }
        }
        return eq ? cur : new ArrayList<>();
    }

    @Override
    public ArrayList<Node> visitFilterwithP(XPathParser.FilterwithPContext ctx) {
        return visit(ctx.filter());
    }

    @Override
    public ArrayList<Node> visitFilterAnd(XPathParser.FilterAndContext ctx) {
        ArrayList<Node> tmp = new ArrayList<>(cur);
        ArrayList<Node> res1 = new ArrayList<>(visit(ctx.filter(0)));
        // clone tmp
        cur = new ArrayList<>(tmp);
        ArrayList<Node> res2 = new ArrayList<>(visit(ctx.filter(1)));
        cur = tmp;
        if (!res1.isEmpty() && !res2.isEmpty())
            return res1;
        return new ArrayList<>();
    }

    @Override
    public ArrayList<Node> visitFilterOr(XPathParser.FilterOrContext ctx) {
        ArrayList<Node> tmp = new ArrayList<>(cur);
        ArrayList<Node> res1 = new ArrayList<>(visit(ctx.filter(0)));
        // clone tmp
        cur = new ArrayList<>(tmp);
        ArrayList<Node> res2 = new ArrayList<>(visit(ctx.filter(1)));
        cur = tmp;
        if (res1.isEmpty() && res2.isEmpty())
            return new ArrayList<>();
        return res1.isEmpty() ? res2 : res1;
    }

    @Override
    public ArrayList<Node> visitFilterNot(XPathParser.FilterNotContext ctx) {
        ArrayList<Node> tmp = new ArrayList<>(cur);
        ArrayList<Node> filter = visit(ctx.filter());
        cur = tmp;
        if (filter.isEmpty())
            return cur;
        return new ArrayList<>();
    }
}
