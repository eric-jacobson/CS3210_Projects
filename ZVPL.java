import java.io.*;
import java.util.*;

public class ZVPL
{
  static String fileName;
  static Scanner keys;

  static int max;
  static int[] mem;
  static int ip, bp, sp, rv, hp, numPassed, gp;
  static int step;

  public static void main(String[] args) throws Exception {

    keys = new Scanner( System.in );

    if( args.length != 2 ) {
      System.out.println("Usage: java VPL <vpl program> <memory size>" );
      System.exit(1);
    }

    fileName = args[0];

    max = Integer.parseInt( args[1] );
    mem = new int[max];

    // load the program into the front part of
    // memory
    Scanner input = new Scanner( new File( fileName ) );
    String line;
    StringTokenizer st;
    int opcode;

    ArrayList<IntPair> labels, holes;
    labels = new ArrayList<IntPair>();
    holes = new ArrayList<IntPair>();
    int label;

    // load the code

    int k=0;
    while ( input.hasNextLine() ) {
      line = input.nextLine();
      System.out.println("parsing line [" + line + "]");
      if( line != null )
      {// extract any tokens
        st = new StringTokenizer( line );
        if( st.countTokens() > 0 )
        {// have a token, so must be an instruction (as opposed to empty line)

          opcode = Integer.parseInt(st.nextToken());

          // load the instruction into memory:

          if( opcode == labelCode )
          {// note index that comes where label would go
            label = Integer.parseInt(st.nextToken());
            labels.add( new IntPair( label, k ) );
          }
          else if( opcode == noopCode ){
          }
          else
          {// opcode actually gets stored
            mem[k] = opcode;  k++;
 
            if( opcode == callCode || opcode == jumpCode ||
                opcode == condJumpCode )
            {// note the hole immediately after the opcode to be filled in later
              label = Integer.parseInt( st.nextToken() );
              mem[k] = label;  holes.add( new IntPair( k, label ) );
              ++k;
            }

            // load correct number of arguments (following label, if any):
            for( int j=0; j<numArgs(opcode); ++j )
            {
              mem[k] = Integer.parseInt(st.nextToken());
              ++k;
            }

          }// not a label

        }// have a token, so must be an instruction
      }// have a line
    }// loop to load code
    
    //System.out.println("after first scan:");
    //showMem( 0, k-1 );

    // fill in all the holes:
    int index;
    for( int m=0; m<holes.size(); ++m )
    {
      label = holes.get(m).second;
      index = -1;
      for( int n=0; n<labels.size(); ++n )
        if( labels.get(n).first == label )
          index = labels.get(n).second;
      mem[ holes.get(m).first ] = index;
    }

    System.out.println("after replacing labels:");
    showMem( 0, k-1 );

    // initialize registers:
    bp = k;  sp = k+2;  ip = 0;  rv = -1;  hp = max;
    numPassed = 0;
    
    int codeEnd = bp-1;

    System.out.println("Code is " );
    showMem( 0, codeEnd );

    gp = codeEnd + 1;

    // start execution:
    boolean done = false;
    int op, a=0, b=0, c=0;
    int actualNumArgs;

    int step = 0;

    int oldIp = 0;

    // repeatedly execute a single operation
    // *****************************************************************

    do {

/*    // show details of current step
      System.out.println("--------------------------");
      System.out.println("Step of execution with IP = " + ip + " opcode: " +
          mem[ip] + 
         " bp = " + bp + " sp = " + sp + " hp = " + hp + " rv = " + rv );
      System.out.println(" chunk of code: " +  mem[ip] + " " +
                            mem[ip+1] + " " + mem[ip+2] + " " + mem[ip+3] );
      System.out.println("--------------------------");
      System.out.println( " memory from " + (codeEnd+1) + " up: " );
      showMem( codeEnd+1, sp+3 );
      System.out.println("hit <enter> to go on" );
      keys.nextLine();
*/

      oldIp = ip;

      op = mem[ ip ];  ip++;
      // extract the args into a, b, c for convenience:
      a = -1;  b = -2;  c = -3;

      // numArgs is wrong for these guys, need one more!
      if( op == callCode || op == jumpCode ||
                op == condJumpCode )
      {
        actualNumArgs = numArgs( op ) + 1;
      }
      else
        actualNumArgs = numArgs( op );

      if( actualNumArgs == 1 )
      {  a = mem[ ip ];  ip++;  }
      else if( actualNumArgs == 2 )
      {  a = mem[ ip ];  ip++;  b = mem[ ip ]; ip++; }
      else if( actualNumArgs == 3 )
      {  a = mem[ ip ];  ip++;  b = mem[ ip ]; ip++; c = mem[ ip ]; ip++; }
 
      // implement all operations here:
      // ********************************************

      // put your work right here!

      if ( op == oppCode ) {
         a = mem[ip + 1];
    		b = mem[ip + 2]; 
         mem[ bp + 2 + a ] = - mem[ bp + 2 + b ];
         ip += 3;

      }
      if ( op == litCode){
        a = mem[ip +1 ];  n = mem[ip + 2]; 
        mem[ bp + 2 + a] = n; 
        ip += 3;
      }
     
      if ( op == copyCode ) {
         a = mem[ip + 1];
    		b = mem[ip + 2]; 
         mem[ bp+ 2 + a ] =  mem[ bp+ 2 + b ];
         ip += 3;

      }

      if ( op == getCode ) {
         a = mem[ip + 1];
    		b = mem[ip + 2];
         c = mem[ip + 3]; 
         
         int pos =  mem[ bp + 2 + b ] + mem[ bp + 2 + c ];
         mem[ bp + 2 + a ] = mem [pos];
         ip += 4;
         
      }
      if ( op ==  putCode ) {
         a = mem[ip + 1];
    		b = mem[ip + 2];
         c = mem[ip + 3];
         int pos = mem[bp + 2 + a] + mem[bp + 2 + b];
    		mem[pos] = mem[bp + 2 + c];
    		ip += 4;

      }
      
      if ( op ==  haltCode ) {
         break; 
      }
      
      if ( op ==  inputCode ) {
         a = mem[ip + 1];
         
         Scanner sc = new Scanner(System.in);
    		System.out.print("? ");
    	   int value = sc.nextInt();
    		mem[bp + 2 + a] = value;     		
    	
    		ip += 2;

      }

      else
      {
        System.out.println("Fatal error: unknown opcode [" + op + "]" );
        System.exit(1);
      }
       
      step++;

    }while( !done );
    

  }// main

  // use symbolic names for all opcodes:

  // op to produce comment
  private static final int noopCode = 0;

  // ops involved with registers
  private static final int labelCode = 1;
  private static final int callCode = 2;
  private static final int passCode = 3;
  private static final int allocCode = 4;
  private static final int returnCode = 5;  // return a means "return and put
           // copy of value stored in cell a in register rv
  private static final int getRetvalCode = 6;//op a means "copy rv into cell a"
  private static final int jumpCode = 7;
  private static final int condJumpCode = 8;

  // arithmetic ops
  private static final int addCode = 9;
  private static final int subCode = 10;
  private static final int multCode = 11;
  private static final int divCode = 12;
  private static final int remCode = 13;
  private static final int equalCode = 14;
  private static final int notEqualCode = 15;
  private static final int lessCode = 16;
  private static final int lessEqualCode = 17;
  private static final int andCode = 18;
  private static final int orCode = 19;
  private static final int notCode = 20;
  private static final int oppCode = 21;
  
  // ops involving transfer of data
  private static final int litCode = 22;  // litCode a b means "cell a gets b"
  private static final int copyCode = 23;// copy a b means "cell a gets cell b"
  private static final int getCode = 24; // op a b means "cell a gets
                                                // contents of cell whose 
                                                // index is stored in b"
  private static final int putCode = 25;  // op a b means "put contents
     // of cell b in cell whose offset is stored in cell a"

  // system-level ops:
  private static final int haltCode = 26;
  private static final int inputCode = 27;
  private static final int outputCode = 28;
  private static final int newlineCode = 29;
  private static final int symbolCode = 30;
  private static final int newCode = 31;
  
  // global variable ops:
  private static final int allocGlobalCode = 32;
  private static final int toGlobalCode = 33;
  private static final int fromGlobalCode = 34;

  // debug ops:
  private static final int debugCode = 35;

  // return the number of arguments after the opcode,
  // except ops that have a label return number of arguments
  // after the label, which always comes immediately after 
  // the opcode
  private static int numArgs( int opcode )
  {
    // highlight specially behaving operations
    if( opcode == labelCode ) return 1;  // not used
    else if( opcode == jumpCode ) return 0;  // jump label
    else if( opcode == condJumpCode ) return 1;  // condJump label expr
    else if( opcode == callCode ) return 0;  // call label

    // for all other ops, lump by count:

    else if( opcode==noopCode ||
             opcode==haltCode ||
             opcode==newlineCode ||
             opcode==debugCode
           ) 
      return 0;  // op

    else if( opcode==passCode || opcode==allocCode || 
             opcode==returnCode || opcode==getRetvalCode || 
             opcode==inputCode || 
             opcode==outputCode || opcode==symbolCode ||
             opcode==allocGlobalCode
           )  
      return 1;  // op arg1

    else if( opcode==notCode || opcode==oppCode || 
             opcode==litCode || opcode==copyCode || opcode==newCode ||
             opcode==toGlobalCode || opcode==fromGlobalCode

           ) 
      return 2;  // op arg1 arg2

    else if( opcode==addCode ||  opcode==subCode || opcode==multCode ||
             opcode==divCode ||  opcode==remCode || opcode==equalCode ||
             opcode==notEqualCode ||  opcode==lessCode || 
             opcode==lessEqualCode || opcode==andCode ||
             opcode==orCode || opcode==getCode || opcode==putCode
           )
      return 3;
   
    else
    {
      System.out.println("Fatal error: unknown opcode [" + opcode + "]" );
      System.exit(1);
      return -1;
    }

  }// numArgs

  private static void showMem( int a, int b )
  {
    for( int k=a; k<=b; ++k )
    {
      System.out.println( k + ": " + mem[k] );
    }
  }// showMem

}// VPL