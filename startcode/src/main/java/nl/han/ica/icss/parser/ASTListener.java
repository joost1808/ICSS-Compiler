package nl.han.ica.icss.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;


import nl.han.ica.datastructures.HANStack;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends ICSSBaseListener {

    //Accumulator attributes:
    private AST ast;

    //Use this to keep track of the parent nodes when recursively traversing the ast
    private IHANStack<ASTNode> currentContainer;

    public ASTListener() {
        ast = new AST();
        currentContainer = new HANStack<>();
    }

    public AST getAST() {
        ast.setRoot((Stylesheet) currentContainer.pop());
        return ast;
    }

    @Override
    public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
        ArrayList<ASTNode> stylerules = new ArrayList<>();
        while (currentContainer.peek() != null) {
            stylerules.add(currentContainer.pop());
        }
        Collections.reverse(stylerules);
        currentContainer.push(new Stylesheet(stylerules));
    }

    @Override
    public void exitStylerule(ICSSParser.StyleruleContext ctx) {
        Body styleRuleBody = new Body();
        while (currentContainer.peek() instanceof Body) {
            styleRuleBody = (Body) currentContainer.pop();
        }
        Selector selector = null;
        if (currentContainer.peek() instanceof Selector) {
            selector = (Selector) currentContainer.pop();
        }
        ASTNode stylerule = new Stylerule(selector, styleRuleBody.body);
        currentContainer.push(stylerule);
    }

    @Override
    public void exitClassSelector(ICSSParser.ClassSelectorContext ctx) {
        currentContainer.push(new ClassSelector(ctx.getChild(0).getText()));
    }

    @Override
    public void exitIdSelector(ICSSParser.IdSelectorContext ctx) {
        currentContainer.push(new IdSelector(ctx.getChild(0).getText()));
    }

    @Override
    public void exitTagSelector(ICSSParser.TagSelectorContext ctx) {
        currentContainer.push(new TagSelector(ctx.getChild(0).getText()));
    }

    @Override
    public void exitBody(ICSSParser.BodyContext ctx) {
        ArrayList<ASTNode> astNodes = new ArrayList<>();
        while (currentContainer.peek() instanceof Declaration
                | currentContainer.peek() instanceof IfClause
                | currentContainer.peek() instanceof VariableAssignment) {
            astNodes.add(currentContainer.pop());
        }
        Collections.reverse(astNodes);
        currentContainer.push(new Body(astNodes));
    }

    @Override
    public void exitColor(ICSSParser.ColorContext ctx) {
        currentContainer.push(new ColorLiteral(ctx.getChild(0).getText()));
    }

    @Override
    public void exitPercentage(ICSSParser.PercentageContext ctx) {
        currentContainer.push(new PercentageLiteral(ctx.getChild(0).getText()));
    }

    @Override
    public void exitPixelsize(ICSSParser.PixelsizeContext ctx) {
        currentContainer.push(new PixelLiteral(ctx.getChild(0).getText()));
    }

    @Override
    public void exitScalar(ICSSParser.ScalarContext ctx) {
        currentContainer.push(new ScalarLiteral(ctx.getChild(0).getText()));
    }

    @Override
    public void exitBoolean(ICSSParser.BooleanContext ctx) {
        currentContainer.push(new BoolLiteral(ctx.getChild(0).getText()));
    }

    @Override
    public void exitPropertyName(ICSSParser.PropertyNameContext ctx) {
        currentContainer.push(new PropertyName(ctx.getChild(0).getText()));
    }

    @Override
    public void exitDecleration(ICSSParser.DeclerationContext ctx) {
        Declaration declaration = new Declaration();
        declaration.addChild(currentContainer.pop());
        declaration.addChild(currentContainer.pop());
        currentContainer.push(declaration);
    }

    @Override
    public void exitVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
        VariableAssignment variableAssignment = new VariableAssignment();
        ASTNode expression = currentContainer.pop();
        ASTNode variableReference = currentContainer.pop();
        variableAssignment.addChild(variableReference);
        variableAssignment.addChild(expression);
        currentContainer.push(variableAssignment);
    }

    @Override
    public void exitVariableReference(ICSSParser.VariableReferenceContext ctx) {
        currentContainer.push(new VariableReference(ctx.getChild(0).getText()));
    }

    @Override
    public void exitAdditionSubtraction(ICSSParser.AdditionSubtractionContext ctx) {
        Operation operation;
        String operator = ctx.getChild(1).getText();
        if (operator.equals("+")) {
            operation = new AddOperation();
        } else {
            operation = new SubtractOperation();
        }
        ASTNode rhs = currentContainer.pop();
        ASTNode lhs = currentContainer.pop();
        operation.addChild(lhs);
        operation.addChild(rhs);
        currentContainer.push(operation);
    }

    @Override
    public void exitMultiplication(ICSSParser.MultiplicationContext ctx) {
        Operation operation = new MultiplyOperation();
        ASTNode rhs = currentContainer.pop();
        ASTNode lhs = currentContainer.pop();
        operation.addChild(lhs);
        operation.addChild(rhs);
        currentContainer.push(operation);
    }

    //Niet gebruikt
    @Override
    public void exitValue(ICSSParser.ValueContext ctx) {

    }

    @Override
    public void exitIfClause(ICSSParser.IfClauseContext ctx) {
        ElseClause elseClause = null;
        Body ifBody = new Body();
        Expression expression = null;
        if (currentContainer.peek() instanceof ElseClause) {
            elseClause = (ElseClause) currentContainer.pop();
        }
        if (currentContainer.peek() instanceof Body) {
            ifBody = (Body) currentContainer.pop();
        }
        if (currentContainer.peek() instanceof Expression) {
            expression = (Expression) currentContainer.pop();
        }
        if (elseClause == null) {
            currentContainer.push(new IfClause(expression, ifBody.body));
        } else {
            currentContainer.push(new IfClause(expression, ifBody.body, elseClause));
        }
    }

    @Override
    public void exitElseClause(ICSSParser.ElseClauseContext ctx) {
        Body elseBody = new Body();
        if (currentContainer.peek() instanceof Body) {
            elseBody = (Body) currentContainer.pop();
        }
        currentContainer.push(new ElseClause(elseBody.body));
    }
}
