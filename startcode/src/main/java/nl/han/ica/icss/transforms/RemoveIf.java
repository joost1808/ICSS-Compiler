package nl.han.ica.icss.transforms;

//BEGIN UITWERKING

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;

import java.util.ArrayList;
import java.util.Iterator;
//EIND UITWERKING

public class RemoveIf implements Transform {

    @Override
    public void apply(AST ast) {
        findIfClause(ast.root.body);
    }

    private void findIfClause(ArrayList<ASTNode> body) {
        for (int i = 0; i < body.size(); i++) {
            if (body.get(i) instanceof Stylerule) {
                findIfClause(((Stylerule) body.get(i)).body);
            }
            if (body.get(i) instanceof IfClause) {
                IfClause ifClause = (IfClause) body.get(i);
                findIfClause(ifClause.body);
                body.addAll(getIfBody(ifClause));
                body.remove(body.get(i));
            }
        }
    }

    private ArrayList<ASTNode> getIfBody(IfClause ifClause) {
        if (ifClause.conditionalExpression instanceof BoolLiteral) {
            if (((BoolLiteral) ifClause.conditionalExpression).value) {
                return ifClause.body;
            } else if (!((BoolLiteral) ifClause.conditionalExpression).value) {
                return ifClause.elseClause.body;
            } else {
                return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }
}
