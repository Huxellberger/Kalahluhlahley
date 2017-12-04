package MKAgent;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class Node<T> {

  public Node(T _data) 
  {
    data = _data;
    parent = null;
    children = new ArrayList<Node<T>>();
  }

  private T data;
  private Node<T> parent;
  private List<Node<T>> children;

  public List<Node<T>> getChildren()
  {
    return children;
  }

  public void addChild(Board board)
  {
    Node newChild = new Node(board);
    children.add(newChild);
  }

  public String toString()
  {
    //TODO: Make recursive - currently only works for root and its children

    StringBuilder boardString = new StringBuilder();

    boardString.append("Root\n" + data.toString() + "\n\n");

    for (int i = 0; i < children.size(); i++)
    {
      boardString.append("Child #" + (i+1) + "\n" + children.get(i).data.toString() + "\n");
    }

    return boardString.toString();
  } 
}