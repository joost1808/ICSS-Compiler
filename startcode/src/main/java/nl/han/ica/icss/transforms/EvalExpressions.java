package nl.han.ica.icss.transforms;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class EvalExpressions implements Transform {

    private IHANLinkedList<HashMap<String, Literal>> variableValues;

    public EvalExpressions() {
        variableValues = new HANLinkedList<>();
    }

    @Override
    public void apply(AST ast) {
        variableValues = new HANLinkedList<>();
        variableValues.addFirst(new HashMap<>());
        evaluateBody(ast.root.body);
    }

    private void evaluateBody(ArrayList<ASTNode> body) {
        for (ASTNode node : body) {
            if (node instanceof VariableAssignment) {
                evaluateVariableAssignment((VariableAssignment) node);
            }
            if (node instanceof Declaration) {
                evaluateDeclaration((Declaration) node);
            }
            if (node instanceof Stylerule) {
                evaluateBody(((Stylerule) node).body);
            }
            if (node instanceof IfClause) {
                IfClause ifClause = (IfClause) node;
                if (ifClause.conditionalExpression instanceof VariableReference) {
                    ifClause.conditionalExpression = evaluateVariableReference((VariableReference) ifClause.conditionalExpression);
                }
                evaluateBody(ifClause.body);
                if (ifClause.elseClause != null) {
                    evaluateBody(ifClause.elseClause.body);
                }
            }
        }
    }

    private void evaluateVariableAssignment(VariableAssignment variableAssignment) {
        if (variableAssignment.expression instanceof Operation) {
            Literal evaluation = evaluateOperation((Operation) variableAssignment.expression);
            variableValues.getFirst().put(
                    variableAssignment.name.name,
                    evaluation
            );
            variableAssignment.expression = evaluation;
        } else if (variableAssignment.expression instanceof Literal) {
            variableValues.getFirst().put(
                    variableAssignment.name.name,
                    (Literal) variableAssignment.expression
            );
        }
    }

    private void evaluateDeclaration(Declaration declaration) {
        if (declaration.expression instanceof Operation) {
            declaration.expression = evaluateOperation((Operation) declaration.expression);
        } else if (declaration.expression instanceof VariableReference) {
            declaration.expression = evaluateVariableReference((VariableReference) declaration.expression);
        }
    }

    private Literal evaluateOperation(Operation operation) {
        if (operation.lhs instanceof VariableReference) {
            operation.lhs = evaluateVariableReference((VariableReference) operation.lhs);
        } else if (operation.lhs instanceof Operation) {
            operation.lhs = evaluateOperation((Operation) operation.lhs);
        }
        if (operation.rhs instanceof VariableReference) {
            operation.rhs = evaluateVariableReference((VariableReference) operation.rhs);
        } else if (operation.rhs instanceof Operation) {
            operation.rhs = evaluateOperation((Operation) operation.rhs);
        }

        if (operation instanceof AddOperation) {
            return evaluateAddOperation((Literal) operation.lhs, (Literal) operation.rhs);
        } else if (operation instanceof SubtractOperation) {
            return evaluateSubtractOperation((Literal) operation.lhs, (Literal) operation.rhs);
        } else if (operation instanceof MultiplyOperation) {
            return evaluateMultiplyOperation((Literal) operation.lhs, (Literal) operation.rhs);
        }
        return new ScalarLiteral(0);
    }

    private Literal evaluateAddOperation(Literal lhs, Literal rhs) {
        if (lhs instanceof PercentageLiteral) {
            return new PercentageLiteral(
                    ((PercentageLiteral) lhs).value + ((PercentageLiteral) rhs).value
            );
        } else if (lhs instanceof PixelLiteral) {
            return new PixelLiteral(
                    ((PixelLiteral) lhs).value + ((PixelLiteral) rhs).value
            );
        } else if (lhs instanceof ScalarLiteral) {
            return new ScalarLiteral(
                    ((ScalarLiteral) lhs).value + ((ScalarLiteral) rhs).value
            );
        }
        return new ScalarLiteral(0);
    }

    private Literal evaluateSubtractOperation(Literal lhs, Literal rhs) {
        if (lhs instanceof PercentageLiteral) {
            return new PercentageLiteral(
                    ((PercentageLiteral) lhs).value - ((PercentageLiteral) rhs).value
            );
        } else if (lhs instanceof PixelLiteral) {
            return new PixelLiteral(
                    ((PixelLiteral) lhs).value - ((PixelLiteral) rhs).value
            );
        } else if (lhs instanceof ScalarLiteral) {
            return new ScalarLiteral(
                    ((ScalarLiteral) lhs).value - ((ScalarLiteral) rhs).value
            );
        }
        return new ScalarLiteral(0);
    }

    private Literal evaluateMultiplyOperation(Literal lhs, Literal rhs) {
        if (lhs instanceof ScalarLiteral) {
            if (rhs instanceof PercentageLiteral) {
                return new ScalarLiteral(
                        ((PercentageLiteral) rhs).value/10
                                * ((ScalarLiteral) lhs).value
                );
            } else if (rhs instanceof PixelLiteral) {
                return new PixelLiteral(
                        ((ScalarLiteral) lhs).value
                                * ((PixelLiteral) rhs).value
                );
            } else if (rhs instanceof ScalarLiteral) {
                return new ScalarLiteral(
                        ((ScalarLiteral) lhs).value
                                * ((ScalarLiteral) rhs).value
                );
            }
        } else {
            if (lhs instanceof PercentageLiteral) {
                return new ScalarLiteral(
                        ((PercentageLiteral) lhs).value/10
                                * ((ScalarLiteral) rhs).value
                );
            } else if (lhs instanceof PixelLiteral) {
                return new PixelLiteral(
                        ((ScalarLiteral) rhs).value
                                * ((PixelLiteral) lhs).value
                );
            }
        }
        return new ScalarLiteral(0);
    }

    private Literal evaluateVariableReference(VariableReference variableReference) {
        Literal found = null;
        for (int i = 0; i < variableValues.getSize(); i++) {
            if (variableValues.get(i).containsKey(variableReference.name)) {
                found = variableValues.get(i).get(variableReference.name);
                break;
            }
        }
        if (found == null) {
            return new ScalarLiteral(0);
        }
        return found;
    }
}
