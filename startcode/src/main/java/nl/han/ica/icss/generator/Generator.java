package nl.han.ica.icss.generator;


import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;

import java.util.ArrayList;

public class Generator {

	public String generate(AST ast) {
        return buildASTString(ast.root.body, new StringBuilder());
	}

	public String buildASTString(ArrayList<ASTNode> body, StringBuilder stringBuilder) {
	    for (ASTNode node : body) {
	        if (node instanceof Stylerule) {
	            stringBuilder.append(printStylerule((Stylerule) node));
	            buildASTString(((Stylerule) node).body, stringBuilder);
	            stringBuilder.append("}\n");
            }
	        if (node instanceof Declaration) {
                stringBuilder.append("  ").append(printDeclaration((Declaration) node)).append("\n");
            }
        }
	    return stringBuilder.toString();
    }

    public String printStylerule(Stylerule stylerule) {
        return stylerule.selectors.get(0).toString() + " {\n";
    }

    public String printDeclaration(Declaration declaration) {
	    StringBuilder stringBuilder = new StringBuilder();
	    stringBuilder.append(declaration.property.name).append(": ");
	    if (declaration.expression instanceof Literal) {
            stringBuilder.append(getLiteralValue((Literal) declaration.expression)).append(";");
        }
	    return stringBuilder.toString();
    }

    public String getLiteralValue(Literal expression) {
	    if (expression instanceof ColorLiteral) {
	        return ((ColorLiteral) expression).value;
        } else if (expression instanceof PercentageLiteral) {
	        return ((PercentageLiteral) expression).value + "%";
        } else if (expression instanceof PixelLiteral) {
	        return ((PixelLiteral) expression).value + "px";
        } else if (expression instanceof ScalarLiteral) {
	        return String.valueOf(((ScalarLiteral) expression).value);
        }
	    return "";
    }
}
