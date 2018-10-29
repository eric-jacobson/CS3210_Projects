/*
    This class provides a recursive descent parser 
    for Corgi (a simple calculator language),
    creating a parse tree which can be interpreted
    to simulate execution of a Corgi program
*/

import java.util.*;
import java.io.*;

public class Parser {

   private Lexer lex;

   public Parser( Lexer lexer ) {
      lex = lexer;
   }

   public Node parseProgram() {
      System.out.println("-----> parsing <program>:");

      Node first = parseFuncCall();

      // Look ahead to see if a funcDef follows the funcCall
      Token token = lex.getNextToken();
      
      if(token.isKind("eof")){
        return new Node("prgrm", first, null, null);  // TODO: Add prgrm to Node kinds
      } else {
        lex.putBackToken(token);
        Node second = parseFuncDefs();
        return new Node("prgrm", first, second, null);
      }
   }

   private Node parseFuncCall(){
      System.out.println("-----> parsing <funcCall>:");

      //Node first = parseVar();

      Token token = lex.getNextToken();
      String funcName = token.getDetails();
      token = lex.getNextToken();
      errorCheck( token, "single", "(" );
      token = lex.getNextToken();

      if(token.getDetails() == ")"){

        return new Node("fcall", funcName, null, null, null);

      } else {
        lex.putBackToken(token);
        Node first = parseArgs();
        //token = lex.getNextToken();
        //errorCheck(token, "single", ")");
        return new Node("fcall", funcName, first, null, null);
      }
          
   }

   private Node parseFuncDefs(){
      System.out.println("-----> parsing <funcDefs>:");

      Node first = parseFuncDef();

      // Look ahead to see if there are more funcDefs
      Token token = lex.getNextToken();

      if(token.isKind("eof")){
        return new Node("fdefs", first, null, null);  // TODO: Add fdefs to Node kinds
      } else {
          lex.putBackToken(token);
          Node second = parseFuncDef();
          return new Node("fdefs", first, second, null);
      }

   }

   private Node parseFuncDef(){
      System.out.println("-----> parsing <params>:");
      
      Token token = lex.getNextToken();

      errorCheck( token, "var", "def" );
      token = lex.getNextToken();
      String funcName = token.getDetails();
      token = lex.getNextToken();
      errorCheck( token, "single", "(" );
      token = lex.getNextToken();

      if(token.getDetails() == ")"){
         token = lex.getNextToken();

         if(token.getDetails() == "end"){
            return new Node("funcDef", funcName, null, null, null);
         } else {
            lex.putBackToken(token);
            Node second = parseStatements();
            return new Node("fdef", funcName, null, second, null);
         }
      } else {
         lex.putBackToken(token);
         Node first = parseParams();
         token = lex.getNextToken();

         if(token.getDetails() == "end"){
            return new Node("fdef", funcName, first, null, null);
         } else {
            lex.putBackToken(token);
            Node second = parseStatements();
            return new Node("fdef", funcName, first, second, null);
         }
      }
   }

   private Node parseParams(){
      System.out.println("-----> parsing <params>:");

      Node first = parseFactor();

      // Look ahead to see if there are more params
      Token token = lex.getNextToken();

      if(token.getDetails() == ")"){
        return new Node("params", first, null, null);
      } else {
        //errorCheck(token, "single", ",");
        lex.putBackToken(token);
        Node second = parseParams();
        return new Node("params", first, second, null);
      }
   }

   private Node parseArgs(){
      System.out.println("-----> parsing <args>:");

      Node first = parseExpr();

      // Look ahead to see if there are more args
      Token token = lex.getNextToken();

      if(token.getDetails() == ")"){
        return new Node("args", first, null, null);
      } else {
        //errorCheck(token, "single", ",");
        lex.putBackToken(token);
        Node second = parseArgs();
        return new Node("args", first, second, null);
      }
   }

   private Node parseStatements() {
      System.out.println("-----> parsing <statements>:");
 
      Node first = parseStatement();
 
      // look ahead to see if there are more statement's
      Token token = lex.getNextToken();
 
      if ( token.isKind("eof") || token.getDetails() == "else" || token.getDetails() == "end") {
         return new Node( "stmts", first, null, null );
      }
      else {
         lex.putBackToken( token );
         Node second = parseStatements();
         return new Node( "stmts", first, second, null );
      }
   }// <statements>

   private Node parseStatement() {   
      System.out.println("-----> parsing <statement>:");
 
      Token token = lex.getNextToken();

      if ( token.isKind("string") ){
         return new Node( "print", token.getDetails(), null, null, null );
      }

      if( token.isKind("bif1")|| token.isKind("bif2")) {
         Node first = parseParams();
         token = lex.getNextToken();
         errorCheck( token, "single", "(" );
         return new Node ( token.getDetails(), first, null, null);
      }

      else if ( token.matches("bif0","nl") ) {
         token = lex.getNextToken();
         errorCheck( token, "single", "(" );
         token = lex.getNextToken();
         errorCheck( token, "single", ")" );
         return new Node( "nl", null, null, null );
      }

      else if ( token.matches("var","return") ){
         Node first = parseExpr();
         return new Node("return", first, null, null);
      }

      else if ( token.isKind("var") && token.getDetails().equals("if") ) {
         Node first = parseExpr();
         token = lex.getNextToken();
         if(token.getDetails().equals("else")){
            token = lex.getNextToken();
            if(token.getDetails().equals("end")){
               return  new Node("if_else", first, null, null);
            }
            else{
               lex.putBackToken(token);
               Node third = parseStatements();
               token = lex.getNextToken();
               errorCheck( token, "var", "end");
               return  new Node("if_else", first, null, third);
            }
         }
         else{
            lex.putBackToken(token);
            Node second = parseStatements();
            if(token.getDetails().equals("end")){
               return  new Node("if_else", first, second, null);
            }
            else{
               Node third = parseStatements();
               return  new Node("if_else", first, second, third);
            }
         }
      }

      else if( token.isKind("var") && token.getDetails() != "if" && token.getDetails() != "return" ) {
         Token temp = lex.getNextToken();
         if(temp.getDetails().equals("=")) {
            String varName = token.getDetails();
            Node first = parseExpr();
            errorCheck(temp, "single", "=");
            return new Node("sto", varName, first, null, null);
         }
         else if(temp.getDetails() == "(") {
            lex.putBackToken(temp);
            lex.putBackToken(token);
            return parseFuncCall();
         }
         else {
            System.out.println("Statement cannot start with " + token );
            System.exit(1);
            return null;
         }
      }
      else {
         System.out.println("Statement cannot start with " + token );
         System.exit(1);
         return null;
      }
   }// <statement>

   private Node parseExpr() {
      System.out.println("-----> parsing <expr>");

      Node first = parseTerm();

      // look ahead to see if there's an addop
      Token token = lex.getNextToken();
 
      if ( token.matches("single", "+") ||
           token.matches("single", "-") 
         ) {
         Node second = parseExpr();
         return new Node( token.getDetails(), first, second, null );
      }
      else {// is just one term
         lex.putBackToken( token );
         return first;
      }

   }// <expr>

   private Node parseTerm() {
      System.out.println("-----> parsing <term>");

      Node first = parseFactor();

      // look ahead to see if there's a multop
      Token token = lex.getNextToken();
 
      if ( token.matches("single", "*") || token.matches("single", "/")) {
         Node second = parseTerm();
         return new Node( token.getDetails(), first, second, null );
      } else {// is just one factor
         lex.putBackToken( token );
         return first;
      }
      
   }// <term>

   private Node parseFactor() {
      System.out.println("-----> parsing <factor>");

      Token token = lex.getNextToken();

      if ( token.isKind("num") ) {
         return new Node("num", token.getDetails(), null, null, null );
      }
      else if ( token.isKind("var") ) {
         return new Node("var", token.getDetails(), null, null, null );
      }
      else if ( token.matches("single","(") ) {
         Node first = parseExpr();
         token = lex.getNextToken();
         errorCheck( token, "single", ")" );
         return first;
      }
      else if ( token.isKind("bif0") ) {
         String bifName = token.getDetails();
         token = lex.getNextToken();
         errorCheck( token, "single", "(" );
         token = lex.getNextToken();
         errorCheck( token, "single", ")" );
         
         return new Node( bifName, null, null, null );
      }
      else if ( token.isKind("bif1") ) {
         String bifName = token.getDetails();
         token = lex.getNextToken();
         errorCheck( token, "single", "(" );
         Node first = parseExpr();
         token = lex.getNextToken();
         errorCheck( token, "single", ")" );
         
         return new Node( bifName, first, null, null );
      }
      else if ( token.isKind("bif2") ) {
         String bifName = token.getDetails();
         token = lex.getNextToken();
         errorCheck( token, "single", "(" );
         Node first = parseExpr();
         token = lex.getNextToken();
         errorCheck( token, "single", "," );
         Node second = parseExpr();
         token = lex.getNextToken();
         errorCheck( token, "single", ")" );
         
         return new Node( bifName, first, second, null );
      }
      else if ( token.matches("single","-") ) {
         Node first = parseFactor();
         return new Node("opp", first, null, null );
      }
      else {
         System.out.println("Can't have factor starting with " + token );
         System.exit(1);
         return null;
      }
      
   }// <factor>

  // check whether token is correct kind
  private void errorCheck( Token token, String kind ) {
    if( ! token.isKind( kind ) ) {
      System.out.println("Error:  expected " + token + 
                         " to be of kind " + kind );
      System.exit(1);
    }
  }

  // check whether token is correct kind and details
  private void errorCheck( Token token, String kind, String details ) {
    if( ! token.isKind( kind ) || 
        ! token.getDetails().equals( details ) ) {
      System.out.println("Error:  expected " + token + 
                          " to be kind=" + kind + 
                          " and details=" + details );
      System.exit(1);
    }
  }

}
