package antlr4.XQuery;

import java.util.*;

import static java.lang.System.exit;

public class XQueryRewriter {
    // root var graph
    private HashMap<String, ArrayList<String>> varGraph = new HashMap<>();
    private ArrayList<ArrayList<String>> tableGraph = new ArrayList<>();
    private HashMap<String, String> varToRoot = new HashMap<>();
    private HashMap<String, String> varToXq = new HashMap<>();
    private HashMap<String, ArrayList<String>> whereXq = new HashMap<>();
    private HashMap<String, ArrayList<String>> condPairs = new HashMap<>();
    private HashSet<String> constructed = new HashSet<>();
    private String originalReturn;

    public String parseFLWR(XQueryParser.XqContext ctx) {
        // check the type of ctx
        if (!(ctx instanceof XQueryParser.XQFLWRContext)) {
            return null;
        }
        XQueryParser.ForClauseContext forCtx = ((XQueryParser.XQFLWRContext) ctx).forClause();
        XQueryParser.WhereClauseContext whereCtx = ((XQueryParser.XQFLWRContext) ctx).whereClause();
        XQueryParser.ReturnClauseContext returnCtx = ((XQueryParser.XQFLWRContext) ctx).returnClause();
        if (whereCtx == null) return null;

        // Union find var
        int rootNum = 0;
        for (int i = 0; i < forCtx.var().size(); i++) {
            String var = forCtx.var(i).NAME().getText();
            String xq = forCtx.xq(i).getText();
            varToXq.put(var, xq);
            if (xq.startsWith("doc")) {
                if (!varGraph.containsKey(var)) {
                    ArrayList<String> newGraph = new ArrayList<String>() {{
                        add(var);
                    }};
                    varGraph.put(var, newGraph);
                    if (!varToRoot.containsKey(var)) {
                        varToRoot.put(var, var);
                    }
                    rootNum++;
                }
            } else {
                int end = xq.indexOf('/') == -1 ? xq.length() : xq.indexOf('/');
                String preVar = xq.substring(xq.indexOf('$') + 1, end);
                for (String key : varGraph.keySet()) {
                    if (varGraph.get(key).contains(preVar)) {
                        varGraph.get(key).add(var);
                        varToRoot.put(var, key);
                    }
                }
            }
        }
        if (rootNum <= 1) return null;

        // Union find where
        for (String key : varGraph.keySet()) {
            ArrayList<String> table = new ArrayList<String>() {{
                add(key);
            }};
            tableGraph.add(table);
        }
        String condText = whereCtx.cond().getText();
        String[] pairs = condText.split("and");
        for (String pair : pairs) {
            // split pair : "$tb eq $ta" -> "$tb" "$ta"
            String[] conds = pair.split("(eq)|(=)");
            // like "john"
            if (!conds[0].contains("$") || !conds[1].contains("$")) {
                String varCond = "";
                if (conds[0].contains("$")) {
                    varCond = conds[0].substring(1);
                }
                if (conds[1].contains("$")) {
                    varCond = conds[1].substring(1);
                }
                if (!varCond.isEmpty()) {
                    String root = varToRoot.get(varCond);
                    whereXq.computeIfAbsent(root, key -> new ArrayList<>());
                    whereXq.get(root).add(pair);
                }
            } else {
                String var1 = conds[0].substring(1);
                String var2 = conds[1].substring(1);
                String root1 = varToRoot.get(var1);
                String root2 = varToRoot.get(var2);

                if (!root1.equals(root2)) {
                    String root1Root2 = root1 + "+" + root2;
                    String root2Root1 = root2 + "+" + root1;
                    if (condPairs.containsKey(root1Root2) || condPairs.containsKey(root2Root1)) {
                        if (condPairs.containsKey(root1Root2)) {
                            condPairs.get(root1Root2).add(pair);
                        } else {
                            condPairs.get(root2Root1).add(pair);
                        }
                        continue;
                    }
                    condPairs.put(root1Root2, new ArrayList<String>() {{
                        add(pair);
                    }});
                    ArrayList<String> root1Position = new ArrayList<>(), root2Position = new ArrayList<>();
                    for (ArrayList<String> table : tableGraph) {
                        if (table.contains(root1)) root1Position = table;
                        if (table.contains(root2)) root2Position = table;
                    }
                    // connect root1Position and root2Position
                    if (!root1Position.equals(root2Position)) {
                        ArrayList<String> newTable = new ArrayList<>();
                        newTable.addAll(root1Position);
                        newTable.addAll(root2Position);
                        tableGraph.remove(root1Position);
                        tableGraph.remove(root2Position);
                        tableGraph.add(newTable);
                    }
                } else {
                    whereXq.computeIfAbsent(root1, key -> new ArrayList<>());
                    whereXq.get(root1).add(pair);
                }
            }
        }
        if (condPairs.size() == 0) return null;
        // System.out.println("varGraph="+varGraph);
        // System.out.println("varToXq="+varToXq);
        // System.out.println("varToRoot="+varToRoot);
        // System.out.println("condPairs="+condPairs);
        // System.out.println("tableGraph="+tableGraph);
        // System.out.println("whereXq="+whereXq);
        // System.out.println("start construct");

        // parse return
        originalReturn = returnCtx.xq().getText();
        return constructJoin();
    }


    public String constructJoin() {
        String joinRet = "";
        if (tableGraph.isEmpty()) return joinRet;

        ArrayList<String> firstTable = tableGraph.get(0);
        if (tableGraph.size() == 1) {
            joinRet = setJoin(firstTable);
        } else {
            String firstSet = setJoin(firstTable);
            ArrayList<String> nextTable = tableGraph.get(1);
            String nextSet = setJoin(nextTable);
            String ret = joinTwoSets(firstSet, nextSet);
            joinRet = ret;
            for (int i = 2; i < tableGraph.size(); i++) {
                firstSet = ret;
                nextTable = tableGraph.get(i);
                nextSet = setJoin(nextTable);
                ret = joinTwoSets(firstSet, nextSet);
                joinRet = ret;
            }
        }
        // remove the "\n\n,"
        joinRet = "for $tuple in " + joinRet.substring(0, joinRet.length() - 3) + "\n";
        return joinRet + constructReturn();
    }

    public String joinTwoSets(String firstSet, String nextSet) {
        String joinSetsRet = "join ( \n";
        joinSetsRet = joinSetsRet + firstSet + nextSet + "\n [], []),\n\n";
        return joinSetsRet;
    }

    public String setJoin(ArrayList<String> currTable) {
        if (currTable.size() == 1) {
            return constructJoinEle(currTable.get(0));
        }
        StringBuilder joinRet = new StringBuilder();
        Set<String> usedElements = new HashSet<>();
        for (String root1 : currTable) {
            for (String root2 : currTable) {
                if (!condPairs.containsKey(root1 + "+" + root2)) {
                    continue;
                }
                if (constructed.contains(root1) && constructed.contains(root2)) {
                    continue;
                }
                joinRet.insert(0, "join ( \n");
                if (!constructed.contains(root1) && !constructed.contains(root2)) {
                    joinRet.append(constructJoinEle(root1));
                    joinRet.append("\n\n");
                    joinRet.append(constructJoinEle(root2));
                    joinRet.append("\n\n");
                    usedElements.add(root1);
                    usedElements.add(root2);
                    joinRet.append(constructCondEle(root1, root2));
                } else if (constructed.contains(root1) && !constructed.contains(root2)) {
                    joinRet.append(constructJoinEle(root2));
                    joinRet.append("\n\n");
                    joinRet.append(constructCondEle(usedElements, root2));
                    usedElements.add(root2);

                } else if (constructed.contains(root2) && !constructed.contains(root1)) {
                    joinRet.append(constructJoinEle(root1));
                    joinRet.append("\n\n");
                    joinRet.append(constructCondEle(usedElements, root1));
                    usedElements.add(root1);

                }
                joinRet.append("),\n\n");
            }
        }
        return joinRet.toString();
    }

    // for where return
    public String constructJoinEle(String root) {
        StringBuilder joinEle = new StringBuilder("for ");
        StringBuilder returnEle = new StringBuilder("return <tuple> {\n");
        ArrayList<String> currVars = varGraph.get(root);
        for (int i = 0; i < currVars.size(); i++) {
            String var = currVars.get(i);
            if (i < currVars.size() - 1) {
                joinEle.append("$").append(var).append(" in ").append(varToXq.get(var)).append(",").append("\n");
                returnEle.append("<").append(var).append(">{$").append(var).append("}").append("</").append(var).append(">,").append("\n");
            } else {
                joinEle.append("$").append(var).append(" in ").append(varToXq.get(var)).append("\n");
                returnEle.append("<").append(var).append(">{$").append(var).append("}").append("</").append(var).append(">").append("\n");
            }
        }
        if (whereXq.containsKey(root)) {
            joinEle.append("where ");
            for (String whereStr : whereXq.get(root)) {
                String[] whereList = whereStr.split("(eq)|(=)");
                joinEle.append(whereList[0]).append(" eq ").append(whereList[1]).append("\n");
            }
        }
        returnEle.append("}</tuple>,");
        return joinEle.append(returnEle).toString();
    }

    public String constructCondEle(String root1, String root2) {
        StringBuilder condEle1 = new StringBuilder("[");
        StringBuilder condEle2 = new StringBuilder("[");
        ArrayList<String> varPairs = condPairs.get(root1 + "+" + root2);
        for (String varPair : varPairs) {
            String[] vars = varPair.split("(eq)|(=)");
            String var1 = vars[0].substring(1), var2 = vars[1].substring(1);
            if (varToRoot.get(var1).equals(root1) && varToRoot.get(var2).equals(root2)) {
                condEle1.append(var1).append(",");
                condEle2.append(var2).append(",");
            } else if (varToRoot.get(var1).equals(root2) && varToRoot.get(var2).equals(root1)) {
                condEle1.append(var2).append(",");
                condEle2.append(var1).append(",");
            }
        }
        if (!constructed.contains(root2) && !constructed.contains(root1)) {
            constructed.add(root1);
            constructed.add(root2);
        } else if (constructed.contains(root2)) {
            StringBuilder tmp = condEle1;
            condEle1 = condEle2;
            condEle2 = tmp;
            constructed.add(root1);
        } else {
            constructed.add(root2);
        }
        return condEle1.substring(0, condEle1.length() - 1) + "] , " + condEle2.substring(0, condEle2.length() - 1) + "]";
    }

    public String constructCondEle(Set<String> usedRoots, String unused) {
        StringBuilder condEle1 = new StringBuilder("[");
        StringBuilder condEle2 = new StringBuilder("[");
        for (String root : usedRoots) {
            String rootUnused = root + "+" + unused, unusedRoot = unused + "+" + root;
            if (!condPairs.containsKey(rootUnused) && !condPairs.containsKey(unusedRoot)) {
                continue;
            }

            ArrayList<String> varPairs = !condPairs.containsKey(rootUnused) ? condPairs.get(unusedRoot) : condPairs.get(rootUnused);
            for (String varPair : varPairs) {
                String[] vars = varPair.split("(eq)|(=)");
                String var1 = vars[0].substring(1), var2 = vars[1].substring(1);
                if (varToRoot.get(var1).equals(root) && varToRoot.get(var2).equals(unused)) {
                    condEle1.append(var1).append(",");
                    condEle2.append(var2).append(",");
                } else if (varToRoot.get(var1).equals(unused) && varToRoot.get(var2).equals(root)) {
                    condEle1.append(var2).append(",");
                    condEle2.append(var1).append(",");
                }
            }
        }
        constructed.add(unused);

        return condEle1.substring(0, condEle1.length() - 1) + "] , " + condEle2.substring(0, condEle2.length() - 1) + "]";
    }

    public String constructReturn() {
        String joinReturn = "return\n";
        return joinReturn + originalReturn.replaceAll("\\$([A-Za-z0-9_]+)", "\\$tuple/$1/*");
    }
}
