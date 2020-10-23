package nl.han.ica.icss.ast;

import java.util.ArrayList;

public class Body extends ASTNode {
    public ArrayList<ASTNode> body;

    public Body() {}

    public Body(ArrayList<ASTNode> body) {
        this.body = body;
    }
}
