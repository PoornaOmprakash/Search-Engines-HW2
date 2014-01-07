/**
 * 
 */
import java.util.ArrayList;
import java.util.List;


public class QryOpTree {
	public String name;
	public List<QryOpTree> children;
	public QryOpTree parent = null;
	public QryOpTree(String name, List<QryOpTree> children, QryOpTree parent) {
		super();
		this.name = name;
		this.children = children;
		this.parent = parent;
	}
	public QryOpTree(String name) {
		super();
		this.children = new ArrayList<QryOpTree>();
		this.name = name;
		this.parent = null;
	}
	public String toString(){
		String rep = this.name + " ( ";
		for ( QryOpTree a : this.children ){
			rep += a;
			rep += " ";
		}
		rep += ") ";
		
		return rep;
	}
	/*public static void main( String[] arguments){
		QryOp a = new QryOp("root");
		a.children.add(new QryOp("leftchild"));
		a.children.add(new QryOp("rightchild"));
		System.out.println(a);
		
		
	}*/
}


