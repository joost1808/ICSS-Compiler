package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.ArrayList;
import java.util.HashMap;

public class Checker {

    private IHANLinkedList<HashMap<String, ExpressionType>> variableTypes;

    public void check(AST ast) {
        variableTypes = new HANLinkedList<>();
        HashMap<String, ExpressionType> globalScope = new HashMap<>();
        variableTypes.addFirst(globalScope);
        checkBody(ast.root.body);
    }

    private void checkBody(ArrayList<ASTNode> body) {
        for (ASTNode node : body) {
            if (node instanceof VariableAssignment) {
                checkVariableAssignment((VariableAssignment) node);
            }
            if (node instanceof Declaration) {
                checkDeclaration((Declaration) node);
            }
            if (node instanceof Stylerule) {
                variableTypes.addFirst(new HashMap<>());
                checkBody(((Stylerule) node).body);
                variableTypes.removeFirst();
            } else if (node instanceof IfClause) {
                checkIfClause((IfClause) node);
            }
        }
    }

    private void checkVariableAssignment(VariableAssignment variableAssignment) {
        if (variableAssignment.expression instanceof Operation) {
            checkOperation((Operation) variableAssignment.expression);
        } else if (variableAssignment.expression instanceof VariableReference) {
            variableTypes.getFirst().put(
                    variableAssignment.name.name,
                    checkUndefinedVariable((VariableReference) variableAssignment.expression)
            );
        } else {
            variableTypes.getFirst().put(
                    variableAssignment.name.name,
                    getExpressionType((Literal) variableAssignment.expression)
            );
        }
    }

    private void checkDeclaration(Declaration declaration) {
        if (declaration.expression instanceof Operation) {
            checkOperation((Operation) declaration.expression);
            if (declaration.expression instanceof AddOperation
            | declaration.expression instanceof SubtractOperation) {
                checkAddSubtractOperation((Operation) declaration.expression);
            }
            if (declaration.expression instanceof MultiplyOperation) {
                checkMultiplyOperation((Operation) declaration.expression);
            }
        } else if (declaration.expression instanceof VariableReference) {
            checkUndefinedVariable((VariableReference) declaration.expression);
        }
    }

    private void checkIfClause(IfClause ifClause) {
        variableTypes.addFirst(new HashMap<>());
        checkConditionalExpression(ifClause);
        checkBody(ifClause.body);
        if (ifClause.elseClause != null) {
            variableTypes.removeFirst();
            variableTypes.addFirst(new HashMap<>());
            checkBody(ifClause.elseClause.body);
            variableTypes.removeFirst();
        } else {
            variableTypes.removeFirst();
        }
    }

    private void checkConditionalExpression(IfClause ifClause) {
        ExpressionType expression = ExpressionType.UNDEFINED;
        if (ifClause.conditionalExpression instanceof VariableReference) {
            expression = checkUndefinedVariable((VariableReference) ifClause.conditionalExpression);
        } else if (ifClause.conditionalExpression instanceof Literal) {
            expression = getExpressionType((Literal) ifClause.conditionalExpression);
        }
        if (!expression.equals(ExpressionType.BOOL)) {
            ifClause.conditionalExpression.setError("Condition is not of type Boolean");
        }
    }

    private void checkOperation(Operation operation) {
        ExpressionType lhs = checkOperationSide(operation.lhs);
        ExpressionType rhs = checkOperationSide(operation.rhs);
        if (lhs.equals(ExpressionType.COLOR)) {
            operation.lhs.setError("Operand cannot be of type Color");
        } else if (rhs.equals(ExpressionType.COLOR)) {
            operation.rhs.setError("Operand cannot be of type Color");
        }
    }

    private ExpressionType checkOperationSide(Expression side) {
        if (side instanceof VariableReference) {
            return checkUndefinedVariable((VariableReference) side);
        } else if (side instanceof Operation) {
            checkOperation((Operation) side);
            if (side instanceof MultiplyOperation) {
                MultiplyOperation mul = (MultiplyOperation) side;
                checkMultiplyOperation(mul);
                if (checkOperationSide(mul.lhs).equals(ExpressionType.SCALAR)
                        && checkOperationSide(mul.rhs).equals(ExpressionType.SCALAR)) {
                    return ExpressionType.SCALAR;
                } else if (!checkOperationSide(mul.lhs).equals(ExpressionType.SCALAR)) {
                    return checkOperationSide(mul.lhs);
                } else {
                    return checkOperationSide(mul.rhs);
                }
            }
        } else {
            return getExpressionType((Literal) side);
        }
        return ExpressionType.UNDEFINED;
    }

    private void checkAddSubtractOperation(Operation operation) {
        ExpressionType lhs = checkOperationSide(operation.lhs);
        ExpressionType rhs = checkOperationSide(operation.rhs);
        if (!lhs.equals(rhs)) {
            operation.setError("Operands are not of the same type");
        }
    }

    private void checkMultiplyOperation(Operation operation) {
        ExpressionType lhs = checkOperationSide(operation.lhs);
        ExpressionType rhs = checkOperationSide(operation.rhs);
        if (!lhs.equals(ExpressionType.SCALAR)
        && !rhs.equals(ExpressionType.SCALAR)) {
            operation.setError("At least one operand has to be of type Scalar");
        }
    }

    private ExpressionType checkUndefinedVariable(VariableReference variableReference) {
        ExpressionType found = null;
        for (int i = 0; i < variableTypes.getSize(); i++) {
            if (variableTypes.get(i).containsKey(variableReference.name)) {
                found = variableTypes.get(i).get(variableReference.name);
                break;
            }
        }
        if (found == null) {
            variableReference.setError("Variable is undefined");
            return ExpressionType.UNDEFINED;
        }
        return found;
    }

    private ExpressionType getExpressionType(Literal value) {
        if (value instanceof BoolLiteral) {
            return ExpressionType.BOOL;
        } else if (value instanceof ColorLiteral) {
            return ExpressionType.COLOR;
        } else if (value instanceof PercentageLiteral) {
            return ExpressionType.PERCENTAGE;
        } else if (value instanceof PixelLiteral) {
            return ExpressionType.PIXEL;
        } else if (value instanceof ScalarLiteral) {
            return ExpressionType.SCALAR;
        } else {
            return ExpressionType.UNDEFINED;
        }
    }
}
