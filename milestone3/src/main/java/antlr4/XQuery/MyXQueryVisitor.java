package antlr4.XQuery;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;

public class MyXQueryVisitor extends XQueryBaseVisitor<ArrayList<Node>> {
    private ArrayList<Node> cur = new ArrayList<>();
    private HashMap<String, ArrayList<Node>> ctxMap = new HashMap<>();
    private Deque<HashMap<String, ArrayList<Node>>> mapStack = new LinkedList<>();
    private Document inputDoc, outputDoc;

    public static void stripEmptyElements(Node node) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE) {
                if (child.getTextContent().trim().length() == 0) {
                    child.getParentNode().removeChild(child);
                    i--;
                }
            }
            stripEmptyElements(child);
        }
    }

    private static ArrayList<Node> unique(ArrayList<Node> nodeList) {
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
            } else
                res.add(nList.item(i));
        }
        return res;
    }

    // XPath part
    @Override
    public ArrayList<Node> visitApChildren(XQueryParser.ApChildrenContext ctx) {
        // doc(fileName)/rp
        visit(ctx.doc());
        cur = visit(ctx.rp());
        return cur;
    }

    @Override
    public ArrayList<Node> visitApAll(XQueryParser.ApAllContext ctx) {
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
    public ArrayList<Node> visitDoc(XQueryParser.DocContext ctx) {
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
    public ArrayList<Node> visitTagName(XQueryParser.TagNameContext ctx) {
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
    public ArrayList<Node> visitAllChildren(XQueryParser.AllChildrenContext ctx) {
        ArrayList<Node> res = new ArrayList<>();
        for (Node node : cur) {
            res.addAll(getChildren(node));
        }
        cur = res;
        return res;
    }

    @Override
    public ArrayList<Node> visitCurrent(XQueryParser.CurrentContext ctx) {
        return cur;
    }

    @Override
    public ArrayList<Node> visitParent(XQueryParser.ParentContext ctx) {
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
    public ArrayList<Node> visitText(XQueryParser.TextContext ctx) {
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
    public ArrayList<Node> visitAttribute(XQueryParser.AttributeContext ctx) {
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
    public ArrayList<Node> visitRpwithP(XQueryParser.RpwithPContext ctx) {
        return visit(ctx.rp());
    }

    @Override
    public ArrayList<Node> visitRpChildren(XQueryParser.RpChildrenContext ctx) {
        visit(ctx.rp(0));
        cur = unique(visit(ctx.rp(1)));
        return cur;
    }

    @Override
    public ArrayList<Node> visitRpAll(XQueryParser.RpAllContext ctx) {
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
    public ArrayList<Node> visitRpFilter(XQueryParser.RpFilterContext ctx) {
        visit(ctx.rp());
        ArrayList<Node> tmp = new ArrayList<>(cur);
        ArrayList<Node> res = new ArrayList<>();
        for (Node node : tmp) {
            cur = new ArrayList<Node>() {{
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
    public ArrayList<Node> visitTwoRp(XQueryParser.TwoRpContext ctx) {
        // save former cur
        ArrayList<Node> tmp = new ArrayList<>(cur);
        ArrayList<Node> res = visit(ctx.rp(0));
        cur = tmp;
        res.addAll(visit(ctx.rp(1)));
        cur = unique(res);
        return res;
    }

    @Override
    public ArrayList<Node> visitFilterRp(XQueryParser.FilterRpContext ctx) {
        ArrayList<Node> tmp = new ArrayList<>(cur);
        ArrayList<Node> res = visit(ctx.rp());
        cur = tmp;
        return res;
    }

    @Override
    public ArrayList<Node> visitFilterEqual(XQueryParser.FilterEqualContext ctx) {
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
    public ArrayList<Node> visitFilterIs(XQueryParser.FilterIsContext ctx) {
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
    public ArrayList<Node> visitFilterConstant(XQueryParser.FilterConstantContext ctx) {
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
    public ArrayList<Node> visitFilterwithP(XQueryParser.FilterwithPContext ctx) {
        return visit(ctx.filter());
    }

    @Override
    public ArrayList<Node> visitFilterAnd(XQueryParser.FilterAndContext ctx) {
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
    public ArrayList<Node> visitFilterOr(XQueryParser.FilterOrContext ctx) {
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
    public ArrayList<Node> visitFilterNot(XQueryParser.FilterNotContext ctx) {
        ArrayList<Node> tmp = new ArrayList<>(cur);
        ArrayList<Node> filter = visit(ctx.filter());
        cur = tmp;
        if (filter.isEmpty())
            return cur;
        return new ArrayList<>();
    }

    // XQuery Part
    private Node makeText(String str) {
        Document doc = inputDoc;
        return doc.createTextNode(str);
    }

    private Node makeElem(String tagName, ArrayList<Node> nodelist) {
        if (outputDoc == null) {
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                outputDoc = db.newDocument();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Node tag = outputDoc.createElement(tagName);
        for (Node node : nodelist) {
            if (node != null) {
                Node importedNode = outputDoc.importNode(node, true);
                tag.appendChild(importedNode);
            }
        }
        return tag;
    }

    @Override
    public ArrayList<Node> visitXQVar(XQueryParser.XQVarContext ctx) {
        String var = ctx.var().NAME().getText();
        if (!ctxMap.containsKey(var)) {
            throw new RuntimeException("no such variable: " + var);
        }
        return new ArrayList<>(ctxMap.get(var));
    }

    @Override
    public ArrayList<Node> visitXQStrConst(XQueryParser.XQStrConstContext ctx) {
        String strConst = ctx.constant().getText();
        strConst = strConst.substring(1, strConst.length() - 1);
        ArrayList<Node> res = new ArrayList<>();
        res.add(makeText(strConst));
        return res;
    }

    @Override
    public ArrayList<Node> visitXQAp(XQueryParser.XQApContext ctx) {
        return visit(ctx.ap());
    }

    @Override
    public ArrayList<Node> visitXQwithP(XQueryParser.XQwithPContext ctx) {
        return visit(ctx.xq());
    }

    // TODO: whether update the ctxMap
    @Override
    public ArrayList<Node> visitTwoXQ(XQueryParser.TwoXQContext ctx) {
        HashMap<String, ArrayList<Node>> curMap = new HashMap<>(ctxMap);
        ArrayList<Node> res = visit(ctx.xq(0));
        ctxMap = new HashMap<>(curMap);
        res.addAll(visit(ctx.xq(1)));
        ctxMap = new HashMap<>(curMap);
        return res;
    }

    // todo: check
    @Override
    public ArrayList<Node> visitXQRp(XQueryParser.XQRpContext ctx) {
        cur = visit(ctx.xq());
        cur = unique(visit(ctx.rp()));
        return cur;
    }

    @Override
    public ArrayList<Node> visitXQRpAll(XQueryParser.XQRpAllContext ctx) {
        cur = visit(ctx.xq());
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

        return unique(visit(ctx.rp()));
    }

    @Override
    public ArrayList<Node> visitXQTag(XQueryParser.XQTagContext ctx) {
        String tagName = ctx.NAME(0).getText();
        if (!tagName.equals(ctx.NAME(1).getText())) {
            throw new RuntimeException("Invalid tag name");
        }
        ArrayList<Node> tmp = visit(ctx.xq());
        ArrayList<Node> res = new ArrayList<>();
        res.add(makeElem(tagName, tmp));

        return res;
    }

    @Override
    public ArrayList<Node> visitXQFLWR(XQueryParser.XQFLWRContext ctx) {
        HashMap<String, ArrayList<Node>> curMap = new HashMap<>(ctxMap);
        mapStack.push(curMap);
        ArrayList<Node> res = new ArrayList<>();
        FLWR(ctx, 0, res);

        ctxMap = mapStack.pop();
        cur = res;

        return res;
    }

    private void FLWR(XQueryParser.XQFLWRContext ctx, int i, ArrayList<Node> res) {
        // check if the for clause part is end
        if (i >= ctx.forClause().var().size()) {
            if (ctx.letClause() != null) {
                visit(ctx.letClause());
            }
            if (ctx.whereClause() != null) {
                ArrayList<Node> cond = visit(ctx.whereClause());
                if (!cond.isEmpty()) {
                    res.addAll(visit(ctx.returnClause()));
                }
            } else {
                res.addAll(visit(ctx.returnClause()));
            }
            return;
        }

        // utilize backtracking to perform for clause
        ArrayList<Node> temp = visit(ctx.forClause().xq(i));
        String varName = ctx.forClause().var(i).NAME().getText();
        for (Node n : temp) {
            HashMap<String, ArrayList<Node>> curMap = new HashMap<>(ctxMap);
            mapStack.push(curMap);
            ArrayList<Node> tmp = new ArrayList<>();
            tmp.add(n);

            // backtracking
            ctxMap.put(varName, tmp);
            FLWR(ctx, i + 1, res);
            ctxMap = mapStack.pop();
        }
    }

    @Override
    public ArrayList<Node> visitLetClause(XQueryParser.LetClauseContext ctx) {
        ArrayList<Node> tmp = new ArrayList<>(cur);
        for (int i = 0; i < ctx.var().size(); i++) {
            cur = new ArrayList<>(tmp);
            ctxMap.put(ctx.var(i).NAME().getText(), visit(ctx.xq(i)));
        }
        cur = new ArrayList<>(tmp);
        return null;
    }

    @Override
    public ArrayList<Node> visitWhereClause(XQueryParser.WhereClauseContext ctx) {
        return visit(ctx.cond());
    }

    @Override
    public ArrayList<Node> visitReturnClause(XQueryParser.ReturnClauseContext ctx) {
        return this.visit(ctx.xq());
    }

    @Override
    public ArrayList<Node> visitJoinClause(XQueryParser.JoinClauseContext ctx) {
        HashMap<String, ArrayList<Node>> preMap = new HashMap<>(ctxMap);
        ArrayList<Node> res1 = visit(ctx.xq(0));
        ctxMap = new HashMap<>(preMap);
        ArrayList<Node> res2 = visit(ctx.xq(1));

        ArrayList<Node> res = new ArrayList<>();

        // get the size of the attr list
        int attrSize = ctx.attr(0).NAME().size();

        // the case when attrSize == 0
        if (attrSize == 0) {
            for (Node n1 : res1) {
                for (Node n2 : res2) {
                    ArrayList<Node> newList = new ArrayList<>();
                    newList.addAll(getChildren(n1));
                    newList.addAll(getChildren(n2));
                    res.add(makeElem("tuple", newList));
                }
            }
            return res;
        }

        ArrayList<HashMap<String, ArrayList<Node>>> MapList = new ArrayList<>();
        for (int i = 0; i < attrSize; i++) {
            String attrName = ctx.attr(0).NAME(i).getText();
            HashMap<String, ArrayList<Node>> attrMap = new HashMap<>();
            for (Node n : res1) {
                Node key = ((Element) n).getElementsByTagName(attrName).item(0);
                // necessary to check here?
                if (((Element) key).getTagName().equals(attrName)) {
                    String content = key.getTextContent();
                    attrMap.computeIfAbsent(content, k -> new ArrayList<>());
                    attrMap.get(content).add(n);
                }
            }
            MapList.add(attrMap);
        }

        ArrayList<Node> currNode = new ArrayList<>(res2);

        // start compare
        for (int i = 0; i < attrSize; i++) {
            String attrName1 = ctx.attr(0).NAME(i).getText();
            String attrName2 = ctx.attr(1).NAME(i).getText();
            ArrayList<Node> curList = new ArrayList<>();
            if (i == 0) {
                for (Node n2 : currNode) {
                    Node compare = ((Element) n2).getElementsByTagName(attrName2).item(0);
                    String content = compare.getTextContent();
                    if (!MapList.get(i).containsKey(content))
                        continue;
                    Node compareChild = compare.getFirstChild();
                    for (Node n1 : MapList.get(i).get(content)) {
                        Node base = ((Element) n1).getElementsByTagName(attrName1).item(0);
                        Node baseChild = base.getFirstChild();
                        if (compareChild.getNodeType() == Node.TEXT_NODE || baseChild.getNodeType() == Node.TEXT_NODE) {
                            if (compareChild.getTextContent().equals(baseChild.getTextContent())) {
                                ArrayList<Node> tmpList = new ArrayList<>();
                                for (Node child1: getChildren(n1))
                                    tmpList.add(child1.cloneNode(true));
                                for (Node child2: getChildren(n2))
                                    tmpList.add(child2.cloneNode(true));
                                curList.add(makeElem("tuple", tmpList));
                            }
                        } else if (compareChild.isEqualNode(baseChild)) {
                            ArrayList<Node> tmpList = new ArrayList<>();
                            for (Node child1: getChildren(n1))
                                tmpList.add(child1.cloneNode(true));
                            for (Node child2: getChildren(n2))
                                tmpList.add(child2.cloneNode(true));
                            curList.add(makeElem("tuple", tmpList));
                        }
                    }
                }
            } else {
                for (Node n2: currNode) {
                    Node compare = ((Element) n2).getElementsByTagName(attrName2).item(0);
                    Node base = ((Element) n2).getElementsByTagName(attrName1).item(0);
                    if(compare != null && base != null) {
                        Node compareChild = compare.getFirstChild();
                        Node baseChild = base.getFirstChild();
                        if (compareChild.getNodeType() == Node.TEXT_NODE || baseChild.getNodeType() == Node.TEXT_NODE) {
                            if (compareChild.getTextContent().equals(baseChild.getTextContent())) {
                                curList.add(n2);
                            }
                        }
                        else {
                            if (compareChild.isEqualNode(baseChild)) {
                                curList.add(n2);
                            }
                        }
                    }
                }
            }
            currNode = unique(curList);
        }
        res = currNode;
        return res;
    }

    @Override
    public ArrayList<Node> visitXQLet(XQueryParser.XQLetContext ctx) {
        HashMap<String, ArrayList<Node>> curMap = new HashMap<>(ctxMap);
        mapStack.push(curMap);
        visit(ctx.letClause());
        ArrayList<Node> result = visit(ctx.xq());
        ctxMap = mapStack.pop();
        return result;
    }

    @Override
    public ArrayList<Node> visitXQJoin(XQueryParser.XQJoinContext ctx) {
        return visit(ctx.joinClause());
    }

    @Override
    public ArrayList<Node> visitCondEqual(XQueryParser.CondEqualContext ctx) {
        ArrayList<Node> tmp = new ArrayList<>(cur);
        ArrayList<Node> res1 = new ArrayList<>(visit(ctx.xq(0)));
        cur = new ArrayList<>(tmp);
        ArrayList<Node> res2 = new ArrayList<>(visit(ctx.xq(1)));
        cur = new ArrayList<>(tmp);
        Node node = null;
        ArrayList<Node> res = new ArrayList<>();
        res.add(node);
        boolean eq = false;

        for (Node n1 : res1) {
            for (Node n2 : res2) {
                if (n1.isEqualNode(n2)) {
                    eq = true;
                    break;
                }
            }
        }
        return eq ? res : new ArrayList<>();
    }

    @Override
    public ArrayList<Node> visitCondIs(XQueryParser.CondIsContext ctx) {
        ArrayList<Node> tmp = new ArrayList<>(cur);
        ArrayList<Node> res1 = new ArrayList<>(visit(ctx.xq(0)));
        cur = new ArrayList<>(tmp);
        ArrayList<Node> res2 = new ArrayList<>(visit(ctx.xq(1)));
        cur = new ArrayList<>(tmp);
        Node node = null;
        ArrayList<Node> res = new ArrayList<>();
        res.add(node);
        boolean eq = false;
        for (Node node1 : res1) {
            for (Node node2 : res2) {
                if (node1.isSameNode(node2)) {
                    eq = true;
                    break;
                }
            }
        }
        return eq ? res : new ArrayList<>();
    }

    @Override
    public ArrayList<Node> visitCondEmpty(XQueryParser.CondEmptyContext ctx) {
        ArrayList<Node> tmp = new ArrayList<>(cur);
        ArrayList<Node> empty = visit(ctx.xq());
        cur = new ArrayList<>(tmp);
        Node node = null;
        ArrayList<Node> res = new ArrayList<>();
        res.add(node);
        boolean eq = false;
        if (empty.isEmpty()) {
            eq = true;
        }
        return eq ? res : new ArrayList<>();
    }

    @Override
    public ArrayList<Node> visitCondSome(XQueryParser.CondSomeContext ctx) {
        HashMap<String, ArrayList<Node>> curMap = new HashMap<>(ctxMap);
        ArrayList<Node> tmp = new ArrayList<>(cur);
        for (int i = 0; i < ctx.var().size(); i++) {
            String v = ctx.var(i).NAME().getText();
            cur = new ArrayList<>(tmp);
            ArrayList<Node> res = unique(visit(ctx.xq(i)));
            ctxMap.put(v, res);
        }
        cur = new ArrayList<>(tmp);
        ArrayList<Node> result = new ArrayList<>(visit(ctx.cond()));
        ctxMap = curMap;
        cur = new ArrayList<>(tmp);
        return result;
    }

    @Override
    public ArrayList<Node> visitCondWithPar(XQueryParser.CondWithParContext ctx) {
        return visit(ctx.cond());
    }

    @Override
    public ArrayList<Node> visitCondAnd(XQueryParser.CondAndContext ctx) {
        ArrayList<Node> tmp = new ArrayList<>(cur);
        ArrayList<Node> res1 = visit(ctx.cond(0));
        cur = new ArrayList<>(tmp);
        ArrayList<Node> res2 = visit(ctx.cond(1));
        cur = new ArrayList<>(tmp);
        if (!res1.isEmpty() && !res2.isEmpty()) {
            return res1;
        }
        return new ArrayList<>();
    }

    @Override
    public ArrayList<Node> visitCondOr(XQueryParser.CondOrContext ctx) {
        ArrayList<Node> tmp = new ArrayList<>(cur);
        ArrayList<Node> res1 = visit(ctx.cond(0));
        cur = new ArrayList<>(tmp);
        ArrayList<Node> res2 = visit(ctx.cond(1));
        cur = new ArrayList<>(tmp);
        if (!res1.isEmpty()) {
            return res1;
        }
        if (!res2.isEmpty()) {
            return res2;
        }
        return new ArrayList<>();
    }

    @Override
    public ArrayList<Node> visitCondNot(XQueryParser.CondNotContext ctx) {
        ArrayList<Node> tmp = new ArrayList<>(cur);
        ArrayList<Node> cond = visit(ctx.cond());
        cur = new ArrayList<>(tmp);
        if (cond.isEmpty()) {
            Node node = null;
            ArrayList<Node> res = new ArrayList<>();
            res.add(node);
            return res;
        } else {
            return new ArrayList<>();
        }
    }


}

