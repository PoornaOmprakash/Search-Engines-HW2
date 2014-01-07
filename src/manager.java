import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class manager {
	 static Qryop root;// = new QryopOr();
	 public TreeNode top;
	 private Stack<TreeNode> parent;
	public static List<String> get_tokens(String line){
		List<String> tokens = new ArrayList<String>();
		while (line.length()>0){
			//System.out.println(line);
			if(line.startsWith(" "))
				line=line.substring(1);
			if(line.startsWith("#AND")){
				tokens.add("#AND");
				line = line.substring(4);
			}
			else if(line.startsWith("#WEIGHT"))
			{
				tokens.add("#WEIGHT");
				line = line.substring(7);
			}
			else if(line.startsWith("#UW"))
			{
				int index=line.indexOf('(');
				String sub=line.substring(0,index);
				tokens.add(sub);
				line=line.substring(index);
			}
			else if(line.startsWith("#OR")){
				tokens.add("#OR");
				line = line.substring(3);
			}
			else if(line.startsWith("#NEAR")){
				int index=line.indexOf('(');
				String sub=line.substring(0,index);
				tokens.add(sub);
				line = line.substring(index);
			}
			else if(line.startsWith("(")){
				tokens.add("(");
				line = line.substring(1);
			}
			else if(line.startsWith(")")){
				tokens.add(")");
				line = line.substring(1);
			}
			else{
				//System.out.println(line);
				String [] tok = null;
				int ind;
				if(line.contains(" "))
					ind=line.indexOf(" ");
				else if(line.contains(")"))
					ind=line.indexOf(")");   //To deal with the last query term
				//int ind1=line.indexOf(" ");  //Index of " " in the line
				//System.out.println(ind1);
				//int ind2=line.indexOf(")");
				//System.out.println(ind2);
				//int ind=Math.min(ind1, ind2);
				else 
					ind=line.length();
				//System.out.println(ind);
				String token=line.substring(0,ind);
				String term;
				String t;
				if(token.contains(".")&& !isNumeric(token))
				{
				    String [] temp=token.split("\\.");
				    term=temp[0];
				    String body=temp[1];
				    try{
				    	tok=QryEval.tokenizeQuery(term);
				    }catch(IOException e) { e.printStackTrace(); }
				    if(tok.length!=0)
				    {
				    	t=tok[0]+"."+body;
				    	tokens.add(t);
				    }
				}
				else
					{
					try{
			    	tok=QryEval.tokenizeQuery(token);
			    }catch(IOException e) { e.printStackTrace(); }
			    if(tok.length!=0)
					   tokens.add(tok[0]);
			   }
				
			line = line.substring(token.length());
			    
				//int ind=tokens.length;
				
				//System.out.println(tokens);
				/*Pattern nextToken = Pattern.compile("(\\w+)|(\\d+(\\.\\d+)?)");//"^\\w+");
				Matcher nextTokenMatcher = nextToken.matcher(line);
				
				if(nextTokenMatcher.find()){
					String md = nextTokenMatcher.group();
					String[] tok=null;
					try {
						tok = QryEval.tokenizeQuery(md);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
					if(tok.length!=0)
					   tokens.add(tok[0]);
					line = line.substring(md.length());
				}*/
			}
			
		}
		/*for(String s:tokens)
			System.out.println(s);*/
		return tokens;
	}
    
  public static void parse_tree(List<String> tokens){
	  Stack<Qryop> st = new Stack <Qryop> ();  
	  int n;
	    st.push(root);
		if(st.size()==0){
            return;
		}
		//parent=new Stack<TreeNode>();
		//int i=0;
		while (tokens.size()>0){
			String token = tokens.remove(0);
			//System.out.println(token);
			if(token.equals("#AND")){
				//if (st.lastElement().type.equals("#WEIGHT") && ((QryopWeight)st.lastElement()).weight.size() == st.lastElement().args.size())
				//{
					//System.out.println(token);
				//	((QryopWeight)st.lastElement()).weight.add(Double.parseDouble(token));
				//}
				Qryop op = new QryopAnd();
				op.type="#AND";
				st.lastElement().args.add(op);
				st.push(op);
			}
			else if(token.equals("#OR")){
				//if (st.lastElement().type.equals("#WEIGHT") && ((QryopWeight)st.lastElement()).weight.size() == st.lastElement().args.size())
				//{
				//	((QryopWeight)st.lastElement()).weight.add(Double.parseDouble(token));
				//}
				Qryop op = new QryopOr();
				op.type="#OR";
				st.lastElement().args.add(op);
				st.push(op);
			}
			else if(token.equals("#WEIGHT"))
			{
				//if (st.lastElement().type.equals("#WEIGHT") && ((QryopWeight)st.lastElement()).weight.size() == st.lastElement().args.size())
				//{
				//	((QryopWeight)st.lastElement()).weight.add(Double.parseDouble(token));
				//}
				Qryop op=new QryopWeight();
				op.type="#WEIGHT";
				st.lastElement().args.add(op);
				st.push(op);
			}
			else if(token.startsWith("#UW"))
			{
			
				//if (st.lastElement().type.equals("#WEIGHT") && ((QryopWeight)st.lastElement()).weight.size() == st.lastElement().args.size())
				//{
				//	((QryopWeight)st.lastElement()).weight.add(Double.parseDouble(token));
				//}
				String [] sub = token.split("/");
				n=Integer.parseInt(sub[1]);
				Qryop op=new QryopUW(n);
				op.type="#UW";
			    //System.out.println(sub[1]);
				st.lastElement().args.add(op);
			    st.push(op);
			}
			else if(token.startsWith("#NEAR"))
			{
				//if (st.lastElement().type.equals("#WEIGHT") && ((QryopWeight)st.lastElement()).weight.size() == st.lastElement().args.size())
				//{
				//	((QryopWeight)st.lastElement()).weight.add(Double.parseDouble(token));
				//}
				String [] sub=token.split("/");
				n=Integer.parseInt(sub[1]);
				Qryop op = new QryopNear(n);
				op.type="#NEAR";
				st.lastElement().args.add(op);
				st.push(op);
			}
				
			else if(token.equals("(")){
				//nothing
			}
			else if(token.equals(")")){
				Qryop top=st.pop();   //Pop the topmost element from the stack containing the tokens
				if(top.type.equals("#WEIGHT"))
				{
					double weight_sum=0;
					for(double wt:((QryopWeight)top).weight)   //Calculate total weight, to normalize
						weight_sum+=wt;
					for(int x=0;x<((QryopWeight)top).weight.size();x++)   //Assign normalized weights
						((QryopWeight)top).weight.set(x,((QryopWeight)top).weight.get(x)/weight_sum);
				}
			}
			else {   //It is a term and not an operator
				if (st.lastElement().type.equals("#WEIGHT") && ((QryopWeight)st.lastElement()).weight.size() == st.lastElement().args.size())
				{
					((QryopWeight)st.lastElement()).weight.add(Double.parseDouble(token));
				}
				else if((token.contains(".")) && !isNumeric(token))           //Field based search  //It's getting confused between 0.4 and term.field
				{
					//System.out.println(token);
					String [] temp = token.split("\\.");
					//System.out.println(temp);
					QryopTerm t=new QryopTerm(temp[0]);
					t.field=temp[1];
					//st.lastElement().
					st.lastElement().args.add(t);
				}
				else
				   st.lastElement().args.add(new QryopTerm(token));
			}
		}
	}
  
  public static boolean isNumeric(String str)  
  {  
    try  
    {  
      Double.parseDouble(str);  
    }  
    catch(NumberFormatException nfe)  
    {  
      return false;  
    }  
    return true;  
  }
}