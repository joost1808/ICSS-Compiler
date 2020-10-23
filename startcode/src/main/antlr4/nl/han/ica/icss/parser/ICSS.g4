grammar ICSS;

//--- LEXER: ---

// IF support:
IF: 'if';
ELSE: 'else';
BOX_BRACKET_OPEN: '[';
BOX_BRACKET_CLOSE: ']';


//Literals
TRUE: 'TRUE';
FALSE: 'FALSE';
PIXELSIZE: [0-9]+ 'px';
PERCENTAGE: [0-9]+ '%';
SCALAR: [0-9]+;


//Color value takes precedence over id idents
COLOR: '#' [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f];

//Specific identifiers for id's and css classes
ID_IDENT: '#' [a-z0-9\-]+;
CLASS_IDENT: '.' [a-z0-9\-]+;

//General identifiers
LOWER_IDENT: [a-z] [a-z0-9\-]*;
CAPITAL_IDENT: [A-Z] [A-Za-z0-9_]*;

//All whitespace is skipped
WS: [ \t\r\n]+ -> skip;

//
OPEN_BRACE: '{';
CLOSE_BRACE: '}';
SEMICOLON: ';';
COLON: ':';
PLUS: '+';
MIN: '-';
MUL: '*';
ASSIGNMENT_OPERATOR: ':=';




//--- PARSER: ---
stylesheet: (variableAssignment | stylerule)* | EOF;
stylerule: selector body;
selector: CLASS_IDENT #classSelector
        | ID_IDENT #idSelector
        | LOWER_IDENT #tagSelector;
body: OPEN_BRACE (decleration | ifClause | variableAssignment)* CLOSE_BRACE;
literal: COLOR #color
        | PERCENTAGE #percentage
        | PIXELSIZE #pixelsize
        | SCALAR #scalar
        | (TRUE|FALSE) #boolean;
propertyName: 'color' | 'background-color' | 'width' | 'height';
decleration: propertyName COLON operation SEMICOLON;
variableAssignment: variableReference ASSIGNMENT_OPERATOR operation SEMICOLON;
variableReference: CAPITAL_IDENT;
operation: (literal | variableReference) #value
            | operation MUL operation #multiplication
            | operation (PLUS|MIN) operation #additionSubtraction;
ifClause: IF BOX_BRACKET_OPEN operation BOX_BRACKET_CLOSE body elseClause?;
elseClause: ELSE body;
