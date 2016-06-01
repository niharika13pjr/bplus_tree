package bptree;

/**
 * The {@code BPlusTree} class implements B+-trees. Each {@code BPlusTree} stores its elements in the main memory (not
 * on disks) for simplicity.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 * 
 * @param <K>
 *            the type of keys
 * @param <V>
 *            the type of values
 */
public class BPlusTree<K extends Comparable<K>, V> {

	/**
	 * The maximum number of pointers that each {@code Node} of this {@code BPlusTree} can have.
	 */
	protected int degree;

	/**
	 * The root node of this {@code BPlusTree}.
	 */
	protected Node<K> root;

	/**
	 * Constructs a {@code BPlusTree}.
	 * 
	 * @param degree
	 *            the maximum number of pointers that each {@code Node} of this {@code BPlusTree} can have.
	 */
	public BPlusTree(int degree) {
		this.degree = degree;
	}

	/**
	 * Copy-constructs a {@code BPlusTree}.
	 * 
	 * @param tree
	 *            another {@code BPlusTree} to copy from.
	 */
	@SuppressWarnings("unchecked")
	public BPlusTree(BPlusTree<K, V> tree) {
		this.degree = tree.degree;
		if (tree.root instanceof LeafNode)
			this.root = new LeafNode<K, V>((LeafNode<K, V>) tree.root);
		else
			this.root = new NonLeafNode<K>((NonLeafNode<K>) tree.root);
	}

	/**
	 * Returns the degree of this {@code BPlusTree}.
	 * 
	 * @return the degree of this {@code BPlusTree}.
	 */
	public int degree() {
		return degree;
	}

	/**
	 * Returns the root {@code Node} of this {@code BPlusTree}.
	 * 
	 * @return the root {@code Node} of this {@code BPlusTree}.
	 */
	public Node<K> root() {
		return root;
	}

	/**
	 * Finds the {@code LeafNode} in this {@code BPlusTree} that must be responsible for the specified key.
	 * 
	 * @param key
	 *            the search key.
	 * @return the {@code LeafNode} in this {@code BPlusTree} that must be responsible for the specified key.
	 */
	@SuppressWarnings("unchecked")
	public LeafNode<K, V> find(K key) {
		Node<K> c = root;
		while (c instanceof NonLeafNode) {
			c = ((NonLeafNode<K>) c).child(key);
		}
		return (LeafNode<K, V>) c;
	}

	/**
	 * Finds the parent {@code Node} of the specified {@code Node}.
	 * 
	 * @param node
	 *            a {@code Node}.
	 * @return the parent {@code Node} of the specified {@code Node}; {@code null} if the parent cannot be found.
	 */
	public NonLeafNode<K> findParent(Node<K> node) {
		Node<K> p = root;
		while (p != null) {
			K key = node.firstKey();
			Node<K> c = ((NonLeafNode<K>) p).child(key);
			if (c == node) { // if found the parent of the node.
				return (NonLeafNode<K>) p;
			}
			p = c;
		}
		return null;
	}

	/**
	 * Inserts the specified key and the value into this {@code BPlusTree}.
	 * 
	 * @param key
	 *            the key to insert.
	 * @param value
	 *            the value to insert.
	 */
	public void insert(K key, V value) {
		LeafNode<K, V> leaf; // the leaf node where insertion will occur
		if (root == null) { // if the root is null
			leaf = new LeafNode<K, V>(degree);
			root = leaf;
		} else { // if root is not null
			leaf = find(key);
		}
		if (leaf.hasRoom()) { // if the leaf node has room for the new entry
			leaf.insert(key, value);
		} else { // if split is required
			LeafNode<K, V> t = new LeafNode<K, V>(degree + 1); // create a temporary leaf node
			t.copy(leaf, 0, leaf.numberOfKeys());// copy everything to the temporary node
			t.insert(key, value); // insert the key and value to the temporary node
			LeafNode<K, V> nLeaf = new LeafNode<K, V>(degree); // create a new leaf node
			nLeaf.setSuccessor(leaf.successor()); // chaining
			leaf.clear(); // clear the leaf node
			leaf.setSuccessor(nLeaf); // chaining from leaf to nLeaf
			int m = (int) Math.ceil(degree / 2.0); // compute the split point
			leaf.copy(t, 0, m); // put the first half into leaf
			nLeaf.copy(t, m, t.numberOfKeys()); // put the second half to nLeaf
			insertInParent(leaf, nLeaf.firstKey(), nLeaf); // use the first key of nLeaf as the separator.
		}
	}

	/**
	 * Inserts pointers to the specified {@code Node}s into an appropriate parent {@code Node}.
	 * 
	 * @param n
	 *            a {@code Node}.
	 * @param key
	 *            the key that splits the {@code Node}s
	 * @param nn
	 *            a new {@code Node}.
	 */
	void insertInParent(Node<K> n, K key, Node<K> nn) {
		if (n == root) { // if the root was split
			root = new NonLeafNode<K>(degree); // create a new node
			root.insert(key, n, 0); // make the new root point to the nodes.
			root.pointers[1] = nn;
			return;
		}
		NonLeafNode<K> p = findParent(n);
		if (p.hasRoom()) {
			p.insertAfter(key, nn, n); // insert key and nn right after n
		} else { // if split is required
			NonLeafNode<K> t = new NonLeafNode<K>(degree + 1); // crate a temporary node
			t.copy(p, 0, p.numberOfKeys()); // copy everything of p to the temporary node
			t.insertAfter(key, nn, n); // insert key and nn after n
			p.clear(); // clear p
			int m = (int) Math.ceil(degree / 2.0); // compute the split point
			p.copy(t, 0, m - 1);
			NonLeafNode<K> np = new NonLeafNode<K>(degree); // create a new node
			np.copy(t, m, t.numberOfKeys()); // put the second half to np
			insertInParent(p, t.keys[m - 1], np); // use the middle key as the separator
		}
	}

	/**
	 * Deletes the specified key and the value from this {@code BPlusTree}.
	 * 
	 * @param key
	 *            the key to delete.
	 * @param value
	 *            the value to delete.
	 */
	public void delete(K key, V value) {
		// please implement the body of this method so that we can remove key-value pairs from the tree (refer to page
		// 498 in the text book).
		Node<K> node = find(key);//find node which contains the key
		delete_entry(node, key, value);//delete entry will be called when key/value is present
	}
	int nodePos,siblingNodePos;
	public void delete_entry(Node<K> node, K key, V value){
		Node<K> parent;
		if(node!=root)//check if node is root or not
		{
		parent=findParent(node);//finding parent
		}
		else
		{
			parent=null;//set parent to null
		}
		//check if key exist in the node
		int pos=-1;
		for(int i = 0; i<node.numberOfKeys; i++){
			if(node.keys[i] == key){
				node.keys[i]=null;
				if(node instanceof LeafNode)//check if node is LeafNode
				node.pointers[i]=null;
				else
					node.pointers[nodePos]=null;
				pos=i;
			}
		}
		//shifting remaining pointers to left
		if(pos!=-1)
		{
			if(node instanceof LeafNode)//check if node is LeafNode
			{
		for(int i=pos;i<node.numberOfKeys-1;i++)
		{
			node.keys[i]=node.keys[i+1];
			node.pointers[i]=node.pointers[i+1];
		}
		node.keys[node.numberOfKeys-1]=null;
		node.numberOfKeys--;
			}
			else //if node is NonLeafNode
			{
				for(int i=pos;i<node.numberOfKeys-1;i++)//shifting key for NonLeaf
				{
					node.keys[i]=node.keys[i+1];
				}
				for(int i=nodePos;i<node.numberOfKeys;i++)//shifting pointer for Nonleaf
				{
					node.pointers[i]=node.pointers[i+1];
				}
				node.pointers[node.numberOfKeys]=null;
				node.keys[node.numberOfKeys-1]=null;
				node.numberOfKeys--;
			}
		}
		
		int remainingChild=0;
		int childpos=0;
		for(int i=0;i<degree;i++)//calculate the remaining child and child position
		{
			if(node.pointers[i]!=null)//if node pointer is not null
			{
				remainingChild++;
				childpos=i;
			}
		}
		//too few values or pointers in the node
		int  minNonLeaf = (int) Math.ceil(degree / 2.0);
		int  minLeaf = (int) Math.ceil((degree-1)/ 2.0);
		boolean toofewValues = false;
		if(node instanceof LeafNode)
		{
			if(node.numberOfKeys<minLeaf)
				toofewValues = true;
		}
		else
		{
			if(remainingChild<minNonLeaf)
				toofewValues = true;
		}
		if(node==root && remainingChild==1)//make node the root if remaining child is 1
		{
		root=(Node<K>)node.pointers[childpos];//make child the root
		node.clear();
		}
		else if(toofewValues)
		{
		nodePos=0;//finding index of node
		siblingNodePos=0;
		K parentKey;
		for(int i = 0;i<=parent.numberOfKeys; i++)
		{
			if((Node<K>)parent.pointers[i]==node)
				nodePos=i;
		
		}
		if(nodePos==0)
		{
		siblingNodePos=1;//setting position for siblingNode
		parentKey=parent.keys[0];
		}
		else 
		{
			siblingNodePos=nodePos-1;
			parentKey=parent.keys[siblingNodePos];
		}	
		Node<K> siblingNode = (Node<K>)parent.pointers[siblingNodePos];//finding sibling node
		if(node.numberOfKeys+siblingNode.numberOfKeys<=(degree-1))//checking if both can fit in same node
		{
			if(nodePos<siblingNodePos)//check if node has a previous or next child of parent node
			{//swap using 3 variables
				Node<K> t=node; //temp=n
				node=siblingNode;//n=siblingNode
				siblingNode=t;//siblingNode=temp
			}
			if(node instanceof LeafNode)//check if node is a LeafNode
			{
				siblingNode=node;//shifting values and keys from node to sibling Node
			}
			else
			{
				siblingNode.keys[0]= parentKey;//shift node from parent to sibling node
				siblingNode.numberOfKeys++;//increment number of keys of siblingNode
				int j=0;
				for(int i=0;i<siblingNode.pointers.length-1;i++)
				{
					siblingNode.pointers[i]=siblingNode.pointers[i+1];
				}
				for(int i=siblingNode.numberOfKeys;i<degree-1;i++)
				{
					siblingNode.keys[i]=node.keys[j];
					siblingNode.pointers[i]=node.keys[j];
					j++;
				
				}
				while(j>0)
				{
					siblingNode.numberOfKeys++;
					j--;
				}
			}
			delete_entry(parent,parentKey,null);//delete entry
			
		}
		//Redistribution borrow an entry from N'
		else if(nodePos>siblingNodePos)
		{
			if(node instanceof NonLeafNode)//check if node is a NonLeafNode
			{
				for(int i=0;i<degree-1;i++)//shifting the value and pointer of node
				{
					node.keys[i]=node.keys[i+1];
					node.pointers[i]=node.pointers[i+1];		
			}
				node.keys[0]=parentKey;
				node.numberOfKeys++;
				parentKey=siblingNode.keys[1];
				siblingNode.pointers[2]=null;//set siblingNode pointer to null
				siblingNode.keys[1]=null;//set siblingNode key to null
				siblingNode.numberOfKeys--;//decrement number of keys of siblingNode
			}			
			else
			{
				for(int i=0;i<degree-1;i++)
				{
					node.keys[i]=node.keys[i+1];
					node.pointers[i]=node.pointers[i+1];		
			}
			node.pointers[0]=siblingNode.pointers[1];
			node.keys[0]=siblingNode.keys[1];
			node.numberOfKeys++;//increment number of keys of node
			siblingNode.numberOfKeys--;//decrement number of keys siblingNode
			siblingNode.keys[1]=null;//removing the key
			siblingNode.pointers[1]=null;//removing the pointer
		}
		}
		else
		{
			if(node instanceof NonLeafNode)//check if node is a NonLeafNode
			{
				node.keys[node.numberOfKeys]=parentKey;
				node.pointers[node.numberOfKeys+1]=siblingNode.pointers[0];
				node.numberOfKeys++;
				parentKey=siblingNode.keys[0];
				for(int i=degree-2;i>0;i--)//shifting the value and pointer of siblingNode
				{
					siblingNode.keys[i-1]=siblingNode.keys[i];
					siblingNode.pointers[i-1]=siblingNode.pointers[i];	
					siblingNode.pointers[i]=siblingNode.pointers[i+1];
			}
				siblingNode.keys[1]=null;//removing the key
				siblingNode.pointers[1]=null;//removing the pointer
				siblingNode.numberOfKeys--;//decrement number of keys
			}
			else
			{
				node.keys[node.numberOfKeys]=siblingNode.keys[0];
				node.pointers[node.numberOfKeys]=siblingNode.pointers[0];
				node.numberOfKeys++;
				
				for(int i=degree-2;i>0;i--)
				{
					siblingNode.keys[i-1]=siblingNode.keys[i];
					siblingNode.pointers[i-1]=siblingNode.pointers[i];	
				}
				siblingNode.keys[1]=null;//removing the key
				siblingNode.pointers[1]=null;//removing the pointer
				siblingNode.numberOfKeys--;//decrement number of keys
				parentKey=siblingNode.keys[0];
			}
		}
		}
	}

}
