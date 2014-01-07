import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class TreeNode {   
	TreeNode parent;
	  Vector<TreeNode> children;
	  Qryop qop;
	  
	  public TreeNode(TreeNode parent, Qryop qop)
	  {
	    this.parent = parent;
	    if (parent != null)
	    {
	      parent.children.add(this);
	      parent.qop.args.add(qop);// actually only this is enough.
	    }
	    this.children = new Vector<TreeNode>();
	    this.qop = qop;
	  }
}